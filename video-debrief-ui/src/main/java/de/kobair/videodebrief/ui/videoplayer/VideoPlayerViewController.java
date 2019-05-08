package de.kobair.videodebrief.ui.videoplayer;

import de.kobair.videodebrief.ui.dialogs.DialogFactory;
import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static de.kobair.videodebrief.ui.dialogs.DialogFactory.CHOOSE_PERSPECTIVES_CONFIG;

public class VideoPlayerViewController implements Initializable {

	private static final int SEEK_DURATION_MILLIS = 5000;
	private static final int FRAME_STEPS_MILLIS = 17; // TODO:
	public static final int SLIDER_PADDING = 5;

	public interface VideoPlayerDelegate {

		public void setInPoint(long inPoint);

		public void setOutPoint(long outPoint);

		public void exportSnapshot(long timeMillis);

		public void exportClip(AttributedPerspective perspective);

		public void setAlignmentPoint(long timeMillis);

		public void changePerspective(String name, long timeMillis, boolean play);

		public void timeChanged(long timeMillis);

		public void toggleFullscreen();
	}

	@FXML
	private AnchorPane mediaViewAnchorPane;
	@FXML
	private Button exportSnapshotButton;
	@FXML
	private Button exportClipButton;
	@FXML
	private Button playPauseButton;
	@FXML
	private Button setInpointButton;
	@FXML
	private Button skipButton;
	@FXML
	private Button backButton;
	@FXML
	private Button nextFrameButton;
	@FXML
	private Button previousFrameButton;
	@FXML
	private Button setOutpointButton;
	@FXML
	private Button fullscreenButton;
	@FXML
	private Label currentTimeLabel;
	@FXML
	private Label durationLabel;
	@FXML
	private Slider timeSlider;
	@FXML
	private Pane inPointPane;
	@FXML
	private Pane outPointPane;
	@FXML
	private Pane alignmentPointPane;
	@FXML
	private Button goToInPointButton;
	@FXML
	private Button goToOutPointButton;
	@FXML
	private Button goToAlignmentPointButton;
	@FXML
	private Button setAlignmentPointButton;
	@FXML
	private AnchorPane timelineAnchorPane;
	@FXML
	private MediaView mediaView;
	@FXML
	private ComboBox<String> perspectivesComboBox;

	@FXML
	void onExportSnapshotButtonPressed(ActionEvent actionEvent) {
		boolean isPlaying = this.mediaPlayer.getStatus() == Status.PLAYING;
		if (isPlaying) {
			this.mediaPlayer.pause();
		}

		long time = (long) this.mediaPlayer.getCurrentTime().toMillis();
		this.delegate.ifPresent(delegate -> delegate.exportSnapshot(time));

		if (isPlaying) {
			this.mediaPlayer.play();
		}
	}

	@FXML
	void onExportClipButtonPressed(ActionEvent actionEvent) {
		boolean isPlaying = this.mediaPlayer.getStatus() == Status.PLAYING;
		if (isPlaying) {
			this.mediaPlayer.pause();
		}

		this.delegate.ifPresent(delegate -> delegate.exportClip(this.attributedPerspective));

		if (isPlaying) {
			this.mediaPlayer.play();
		}
	}

	@FXML
	private void onPlayPauseButtonPressed(ActionEvent actionEvent) {
		if (this.mediaPlayer.getStatus() == Status.PLAYING) {
			this.mediaPlayer.pause();
		} else {
			this.mediaPlayer.play();
		}
	}

	@FXML
	private void onSkipButtonPressed(ActionEvent actionEvent) {
		Duration time = this.mediaPlayer.getCurrentTime().add(new Duration(SEEK_DURATION_MILLIS));
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onBackButtonPressed(ActionEvent actionEvent) {
		Duration time = this.mediaPlayer.getCurrentTime().subtract(new Duration(SEEK_DURATION_MILLIS));
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onNextFrameButtonPressed(ActionEvent actionEvent) {
		if (this.mediaPlayer.getStatus() != Status.PAUSED) {
			this.mediaPlayer.pause();
		}
		Duration time = this.mediaPlayer.getCurrentTime().add(new Duration(FRAME_STEPS_MILLIS));
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onPreviousFrameButtonPressed(ActionEvent actionEvent) {
		if (this.mediaPlayer.getStatus() != Status.PAUSED) {
			this.mediaPlayer.pause();
		}
		Duration time = this.mediaPlayer.getCurrentTime().subtract(new Duration(FRAME_STEPS_MILLIS));
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onFullscreenButtonPressed(ActionEvent actionEvent) {
		this.delegate.ifPresent(delegate -> delegate.toggleFullscreen());
	}

	@FXML
	private void onSetInpointButtonPressed(ActionEvent actionEvent) {
		long time = (long) this.mediaPlayer.getCurrentTime().toMillis();
		if (this.attributedPerspective.getPerspective().getOutPoint() > time) {
			this.delegate.ifPresent(delegate -> delegate.setInPoint(time));
		}
	}

	@FXML
	private void onSetOutpointButtonPressed(ActionEvent actionEvent) {
		long time = (long) this.mediaPlayer.getCurrentTime().toMillis();
		if (this.attributedPerspective.getPerspective().getInPoint() < time) {
			this.delegate.ifPresent(delegate -> delegate.setOutPoint(time));
		}
	}

	@FXML
	private void onGoToInPointButtonPressed(ActionEvent actionEvent) {
		Duration time = new Duration(this.attributedPerspective.getPerspective().getInPoint());
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onGoToOutPointButtonPressed(ActionEvent actionEvent) {
		Duration time = new Duration(this.attributedPerspective.getPerspective().getOutPoint());
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onGoToAlignmentPointButtonPressed(ActionEvent actionEvent) {
		Duration time = new Duration(this.attributedPerspective.getPerspective().getAlignmentPoint());
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onSetAlignmentPointButtonPressed() {
		long time = (long) this.mediaPlayer.getCurrentTime().toMillis();
		this.delegate.ifPresent(delegate -> delegate.setAlignmentPoint(time));
	}

	@FXML
	private void onMediaViewError(MediaErrorEvent event) {
		System.out.println(event);
	}

	@FXML
	private void onMediaViewClick(final MouseEvent event) {
		if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
			if (this.mediaPlayer != null) {
				this.onPlayPauseButtonPressed(null);
				if (event.getClickCount() == 2) {
					this.onFullscreenButtonPressed(null);
				}
			}
		}
	}

	@FXML
	private void onTimeSliderMousePressed(MouseEvent event) {
		this.sliderAndPlayerAsynchronous = true;
		this.continuePlayAfterScrubbing = this.mediaPlayer.getStatus() == Status.PLAYING;
		if (this.continuePlayAfterScrubbing) {
			this.mediaPlayer.pause();
		}
	}

	@FXML
	private void onTimeSliderMouseReleased(MouseEvent event) {
		if (this.continuePlayAfterScrubbing) {
			this.mediaPlayer.play();
		}
	}

	private List<Control> mediaPlayerControls;
	private Optional<VideoPlayerDelegate> delegate = Optional.empty();
	private AttributedPerspective attributedPerspective;
	private MediaPlayer mediaPlayer;
	private Media media;
	private boolean continuePlayAfterScrubbing;
	private boolean sliderAndPlayerAsynchronous;
	private boolean playOnReady;
	private ObservableList<String> perspectives;
	private Duration startTime;

	private void mediaDurationAvailable(Duration duration) {
		this.timeSlider.setMax(duration.toMillis());
		this.durationLabel.setText(stringFromDuration(duration));
		this.currentTimeLabel.setText(stringFromDuration(new Duration(0)));
	}

	private String stringFromDuration(Duration duration) {
		return DurationFormatUtils.formatDuration((long) duration.toMillis(), "mm:ss.SSS");
	}

	private void synchronizeTimeSliderAndMediaPlayer() {
		this.sliderAndPlayerAsynchronous = false;
		this.mediaPlayer.seek(new Duration((Double) this.timeSlider.getValue()));
	}

	private void handleMediaPlayerStatusChange(ObservableValue<? extends MediaPlayer.Status> ov,
			MediaPlayer.Status oldStatus, MediaPlayer.Status newStatus) {
		switch (newStatus) {
		
		case READY:
			this.enableMediaPlayerControls();
			this.mediaDurationAvailable(this.media.getDuration());
			this.showAlignmentPointIndicator();
			this.showPlayButton();
			this.seekAndUpdateTimeSlider(this.startTime);
			if (this.playOnReady) {
				mediaPlayer.play();
			}
			break;
			
		case PAUSED:
			if (sliderAndPlayerAsynchronous) {
				this.synchronizeTimeSliderAndMediaPlayer();
			}
			this.showPlayButton();
			break;
			
		case PLAYING:
			this.showPauseButton();
			break;
			
		case STOPPED:
			this.showPlayButton();
			break;

		default:
			this.disableMediaPlayerControls();
			break;
		}
	}

	private void handleMediaPlayerTimeChange(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		this.updateTimeline(newValue);
		if (this.mediaPlayer.getStatus() == Status.PLAYING || this.mediaPlayer.getStatus() == Status.PAUSED) {
			this.delegate.ifPresent(delegate -> delegate.timeChanged((long) newValue.toMillis()));
		}
	}

	private void updateTimeline(Duration time) {
		this.timeSlider.setValue(time.toMillis());
		this.currentTimeLabel.setText(stringFromDuration(time));
	}

	private void handleTimeSliderValueChanged(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
		if (this.mediaPlayer.getStatus() != Status.PLAYING) {
			Duration duration = new Duration((Double) newValue);
			this.mediaPlayer.seek(duration);
			this.currentTimeLabel.setText(stringFromDuration(duration));
		}
	}

	private void handlePerspectiveComboBoxChanged(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
		if (this.attributedPerspective == null) {
			return;
		}

		String currentPerspectiveName = this.attributedPerspective.getPerspective().getName();
		if (!currentPerspectiveName.equals(newValue)) {
			boolean play = this.shouldPlayOnPerspectiveChange();
			long time = this.getCurrentTime();
			this.delegate.ifPresent(delegate -> delegate.changePerspective(newValue, time, play));
		}
	}

	private Media fxMediaFromSelection(AttributedPerspective attributedPerspective) {
		File file = attributedPerspective.getVideoFile();
		URL mediaUrl;

		try {
			mediaUrl = file.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// TODO: error handling
			return null;
		}

		Media media = new Media(mediaUrl.toExternalForm());
		media.setOnError(new Runnable() {
			public void run() {
				// Handle asynchronous error in Media object.
				System.out.println(VideoPlayerViewController.this.media.getError());
			}
		});
		return media;
	}

	private void removeExistingMediaPlayer() {
		if (this.mediaPlayer == null) {
			return;
		}

		this.mediaPlayer.statusProperty().removeListener(this::handleMediaPlayerStatusChange);
		this.mediaPlayer.setOnError(null);
		this.mediaPlayer.stop();
		this.mediaPlayer = null;
	}

	private void disableMediaPlayerControls() {
		for (Control control : this.mediaPlayerControls) {
			if (control != null) {
				control.setDisable(true);
			}
		}
	}

	private void hideAlignmentPointIndicator() {
		this.alignmentPointPane.setVisible(false);
	}

	private void showAlignmentPointIndicator() {
		this.alignmentPointPane.setVisible(true);
	}

	private void enableMediaPlayerControls() {
		for (Control control : this.mediaPlayerControls) {
			if (control != null) {
				control.setDisable(false);
			}
		}
	}
	
	private void showPlayButton() {
		this.playPauseButton.setText(">");
	}
	
	private void showPauseButton() {
		this.playPauseButton.setText("||");
	}

	private void updateInOutPointView() {
		double inPointWidth = 0;
		double outPointWidth = 0;
		if (this.attributedPerspective != null) {
			double totalWidth = this.timelineAnchorPane.getWidth() - 2*SLIDER_PADDING; // padding of slider
			long duration = this.attributedPerspective.getVideoInformation().getDurationMillis();
			double pixelPerMillisecond = totalWidth / duration;

			if (this.attributedPerspective.getPerspective().getInPoint() > 0) {
				inPointWidth = this.attributedPerspective.getPerspective().getInPoint() * pixelPerMillisecond;
			}

			if (this.attributedPerspective.getPerspective().getOutPoint() < duration) {
				outPointWidth = totalWidth - (this.attributedPerspective.getPerspective().getOutPoint() * pixelPerMillisecond);
			}
		}

		this.inPointPane.setPrefWidth(inPointWidth);
		this.outPointPane.setPrefWidth(outPointWidth);
	}

	private void updateAlignmentPointView() {
		double alignmentPoint = SLIDER_PADDING;
		if (this.attributedPerspective != null) {
			double totalWidth = this.timelineAnchorPane.getWidth() - 2*SLIDER_PADDING; // padding of slider
			long duration = this.attributedPerspective.getVideoInformation().getDurationMillis();
			double pixelPerMillisecond = totalWidth / duration;

			alignmentPoint = SLIDER_PADDING;
			alignmentPoint += this.attributedPerspective.getPerspective().getAlignmentPoint() * pixelPerMillisecond;
			alignmentPoint -= this.alignmentPointPane.getWidth() / 2.0;
		}

		AnchorPane.setLeftAnchor(this.alignmentPointPane, alignmentPoint);
	}

	private void seekAndUpdateTimeSlider(Duration time) {
		long mediaLength = (long) mediaPlayer.getMedia().getDuration().toMillis();
		long timeMillis = (long) time.toMillis();
		timeMillis = Math.max(timeMillis, 0);
		timeMillis = Math.min(timeMillis, mediaLength);

		this.mediaPlayer.seek(new Duration(timeMillis));
		this.updateTimeline(new Duration(timeMillis));
	}

	public long getCurrentTime() {
		return (long) this.mediaPlayer.getCurrentTime().toMillis();
	}

	public boolean shouldPlayOnPerspectiveChange() {
		return this.mediaPlayer.getStatus() == Status.PLAYING;
	}

	public void setDelegate(final VideoPlayerDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public void setSelectedMedia(AttributedPerspective attributedPerspective, List<String> allPerspectives) {
		this.setSelectedMedia(attributedPerspective, allPerspectives, 0, true);
	}

	public void setSelectedMedia(AttributedPerspective attributedPerspective, List<String> allPerspectives, long startTimeMillis, boolean play) {
		this.removeExistingMediaPlayer();

		this.playOnReady = play;
		this.startTime = new Duration(startTimeMillis);
		this.attributedPerspective = attributedPerspective;
		this.media = fxMediaFromSelection(this.attributedPerspective);

		this.mediaPlayer = new MediaPlayer(this.media);
		this.mediaPlayer.statusProperty().addListener(this::handleMediaPlayerStatusChange);

		this.mediaPlayer.setOnError(new Runnable() {
			public void run() {
				// Handle asynchronous error in Player object.
				System.out.println(VideoPlayerViewController.this.mediaPlayer.getError());
			}
		});

		this.mediaView.setMediaPlayer(this.mediaPlayer);

		this.mediaPlayer.currentTimeProperty().addListener(this::handleMediaPlayerTimeChange);

		this.updateInOutPointView();
		this.updateAlignmentPointView();

		this.perspectives.clear();
		this.perspectives.addAll(allPerspectives);
		this.perspectivesComboBox.valueProperty().set(attributedPerspective.getPerspective().getName());
	}

	public void selectedMediaChanged(AttributedPerspective attributedPerspective) {
		this.attributedPerspective = attributedPerspective;
		this.updateInOutPointView();
		this.updateAlignmentPointView();
	}
	public void clearMedia() {
		// TODO
		System.out.println("clearMedia(): Not yet implemented");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.mediaPlayerControls = Arrays.asList(
				new Control[] { exportSnapshotButton, exportClipButton, playPauseButton, setInpointButton, skipButton,
						backButton, nextFrameButton, previousFrameButton, setOutpointButton, fullscreenButton, timeSlider,
						goToAlignmentPointButton, goToInPointButton, goToOutPointButton, setAlignmentPointButton, perspectivesComboBox});
		this.disableMediaPlayerControls();
		this.hideAlignmentPointIndicator();
		this.timeSlider.valueProperty().addListener(this::handleTimeSliderValueChanged);

		this.currentTimeLabel.setText(stringFromDuration(new Duration(0)));
		this.durationLabel.setText(stringFromDuration(new Duration(0)));

		this.perspectives = FXCollections.observableArrayList();
		this.perspectivesComboBox.setItems(this.perspectives);
		this.perspectivesComboBox.valueProperty().addListener(this::handlePerspectiveComboBoxChanged);

		this.mediaView.setSmooth(true); // TODO: ?
	}
}
