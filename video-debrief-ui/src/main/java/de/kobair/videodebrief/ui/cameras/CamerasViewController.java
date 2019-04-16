package de.kobair.videodebrief.ui.cameras;

import java.net.URL;
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
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CamerasViewController implements Initializable, DcfDetectorDelegate {

	@FXML
	private ListView<Camera> camerasListView;
	@FXML
	private Button refreshButton;
	@FXML
	private TableView<Video> videosTableView;
	@FXML
	private TableColumn<Video, String> nameColumn;
	@FXML
	private TableColumn<Video, String> formatColumn;
	@FXML
	private TableColumn<Video, String> dateColumn;
	
	private final DcfDetector dcfDetector;

	private ObservableList<Camera> cameras;
	private ReadOnlyObjectProperty<Camera> selectedCameraProperty;
	private ObservableList<Video> videos;

	@FXML
	private void onRefreshButtonPressed(ActionEvent event) {
		this.refresh();
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
		
		this.nameColumn.setCellValueFactory(data -> data.getValue().getNameProperty());
		this.formatColumn.setCellValueFactory(data -> data.getValue().getFormatNameProperty());
		this.dateColumn.setCellValueFactory(data -> data.getValue().getTimestampProperty());
		
		this.cameras = this.camerasListView.getItems();
		this.videos = this.videosTableView.getItems();
		this.dcfDetector.start();
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