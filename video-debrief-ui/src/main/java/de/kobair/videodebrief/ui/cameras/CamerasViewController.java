package de.kobair.videodebrief.ui.cameras;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.media.dcf.DcfDetector;
import de.kobair.videodebrief.core.media.dcf.DcfDetector.DcfDetectorDelegate;
import de.kobair.videodebrief.core.media.dcf.DcfDevice;
import de.kobair.videodebrief.core.media.dcf.DcfDirectory;
import de.kobair.videodebrief.core.media.dcf.DcfMediaFile;
import de.kobair.videodebrief.ui.cameras.model.Camera;
import de.kobair.videodebrief.ui.cameras.model.Video;
import de.kobair.videodebrief.ui.cameras.view.VideoCell;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class CamerasViewController implements Initializable, DcfDetectorDelegate {

	private enum Order {
		ASCENDING("ASC"),
		DESCENDING("DSC");

		private final String displayText;

		Order(final String displayText) {
			this.displayText = displayText;
		}
	}

	private enum VideoSortCriteria {
		NAME("Name", (a, b) -> a.getName().compareTo(b.getName())),
		DATE("Date", (a, b) -> a.getTimestampMillis().compareTo(b.getTimestampMillis())),
		SIZE("Size", (a, b) -> a.getSize().compareTo(b.getSize()));

		private final String displayName;
		private final Comparator<Video> comparator;

		VideoSortCriteria(final String displayName, final Comparator<Video> comparator) {
			this.displayName = displayName;
			this.comparator = comparator;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	@FXML
	private ListView<Camera> camerasListView;
	@FXML
	private Button refreshButton;
	@FXML
	private Button toggleOrderButton;
	@FXML
	private ComboBox<VideoSortCriteria> sortByComboBox;
	@FXML
	private ListView<Video> videosListView;

	private final DcfDetector dcfDetector;

	private ObservableList<Camera> cameras;
	private ReadOnlyObjectProperty<Camera> selectedCameraProperty;
	private ObservableList<Video> videos;
	private Order videoOrder = Order.DESCENDING;

	@FXML
	private void onRefreshButtonPressed(ActionEvent event) {
		this.refresh();
	}

	@FXML
	private void onToggleOrderButtonClicked(ActionEvent event) {
		switch (this.videoOrder) {

		case ASCENDING:
			this.videoOrder = Order.DESCENDING;
			break;

		case DESCENDING:
			this.videoOrder = Order.ASCENDING;
			break;
		}

		this.updateOrderButton();
		this.sortVideoList();
	}

	private void refresh() {
		this.cameras.clear();

		for (DcfDevice device : this.dcfDetector.getConnectedDevices()) {
			List<DcfDirectory> directories = device.getDcfDirectories();
			for (DcfDirectory directory : directories) {
				Camera camera = new Camera(device, directory);
				this.cameras.addAll(camera);
			}
		}
	}

	private void refreshOnFxThread() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				CamerasViewController.this.refresh();
			}
		});
	}

	private void onSelectCamera(ObservableValue<? extends Camera> observable, Camera oldValue, Camera newValue) {
		Camera camera = newValue;
		this.showVideosFromCamera(camera);
		this.sortByComboBox.setDisable(false);
		this.toggleOrderButton.setDisable(false);
		this.sortVideoList();
	}

	private void showVideosFromCamera(Camera camera) {
		this.videos.clear();
		if (camera != null) {
			List<Video> videos = this.videosFromCamera(camera);
			this.videos.addAll(videos);
		}
	}

	private List<Video> videosFromCamera(Camera camera) {
		DcfDirectory directory = camera.getDirectory();
		List<DcfMediaFile> mediaFiles = directory.getFilesForFormats(FileFormat.VIDEO_FORMATS);
		return mediaFiles.stream()
					   .map(Video::new)
					   .collect(Collectors.toList());
	}

	public CamerasViewController() {
		this.dcfDetector = new DcfDetector(1000L);
		this.dcfDetector.setDelegate(this);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.selectedCameraProperty = this.camerasListView.getSelectionModel().selectedItemProperty();
		this.selectedCameraProperty.addListener(this::onSelectCamera);

		this.cameras = this.camerasListView.getItems();
		this.videos = this.videosListView.getItems();
		this.dcfDetector.start();

		this.videosListView.setItems(this.videos);
		this.videosListView.setCellFactory(listView -> new VideoCell(listView.widthProperty(), this::handleDragOfVideoCell));

		this.sortByComboBox.setDisable(true);
		this.toggleOrderButton.setDisable(true);
		this.sortByComboBox.getItems().addAll(VideoSortCriteria.values());
		this.sortByComboBox.valueProperty().addListener(observable -> this.sortVideoList());
		this.sortByComboBox.valueProperty().set(VideoSortCriteria.NAME);
		this.updateOrderButton();
	}

	private void handleDragOfVideoCell(ListCell<Video> cell) {
		Dragboard dragboard = cell.startDragAndDrop(TransferMode.ANY);
		dragboard.setDragView(cell.snapshot(null, null));
		ClipboardContent content = new ClipboardContent();
		List<File> files = Arrays.asList(new File[] {cell.getItem().getMediaFile().getFile()});
		content.putFiles(files);
		String deviceName = this.selectedCameraProperty.get().getDevice().getDisplayName();
		content.putString(deviceName);
		dragboard.setContent(content);
	}

	private void updateOrderButton() {
		this.toggleOrderButton.setText(this.videoOrder.displayText);
	}

	private void sortVideoList() {
		switch (this.videoOrder) {

		case ASCENDING:
			Collections.sort(this.videos, this.sortByComboBox.getValue().comparator);
			break;

		case DESCENDING:
			Collections.sort(this.videos, Collections.reverseOrder(this.sortByComboBox.getValue().comparator));
			break;
		}
	}

	@Override
	public void dcfDetectionStarted(DcfDetector dcimDetector) {
		this.refreshOnFxThread();
	}

	@Override
	public void dcfDeviceConnected(DcfDetector dcimDetector, DcfDevice device) {
		this.refreshOnFxThread();
	}

	@Override
	public void usbDeviceRemoved(DcfDetector dcimDetector) {
		this.refreshOnFxThread();
	}

	@Override
	public void dcfDetectionStopped(DcfDetector dcimDetector) {
		this.refreshOnFxThread();
	}
}
