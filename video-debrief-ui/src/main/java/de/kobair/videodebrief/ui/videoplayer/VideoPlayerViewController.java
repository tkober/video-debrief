package de.kobair.videodebrief.ui.videoplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.lang3.time.DurationFormatUtils;

import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VideoPlayerViewController implements Initializable {
	
	private static final int SEEK_DURATION_MILLIS = 5000;
	private static final int FRAME_STEPS_MILLIS = 17; // TODO:

	public interface VideoPlayerDelegate {

		public void setInPoint(long inPoint);

		public void setOutPoint(long outPoint);

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
	private Button goToInPointButton;
	@FXML
	private Button goToOutPointButton;
	@FXML
	private StackPane timelineStackPane;
	@FXML
	private MediaView mediaView;

	@FXML
	void onExportSnapshotButtonPressed(ActionEvent actionEvent) {
		System.out.println("onExportSnapshotButtonPressed()");
	}

	@FXML
	void onExportClipButtonPressed(ActionEvent actionEvent) {
		System.out.println("onExportClipButtonPressed()");
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
		this.mediaPlayer.seek(this.mediaPlayer.getCurrentTime().add(new Duration(SEEK_DURATION_MILLIS)));
	}

	@FXML
	private void onBackButtonPressed(ActionEvent actionEvent) {
		this.mediaPlayer.seek(this.mediaPlayer.getCurrentTime().subtract(new Duration(SEEK_DURATION_MILLIS)));
	}

	@FXML
	private void onNextFrameButtonPressed(ActionEvent actionEvent) {
		if (this.mediaPlayer.getStatus() != Status.PAUSED) {
			this.mediaPlayer.pause();
		}
		this.mediaPlayer.seek(this.mediaPlayer.getCurrentTime().add(new Duration(FRAME_STEPS_MILLIS)));
	}

	@FXML
	private void onPreviousFrameButtonPressed(ActionEvent actionEvent) {
		if (this.mediaPlayer.getStatus() != Status.PAUSED) {
			this.mediaPlayer.pause();
		}
		this.mediaPlayer.seek(this.mediaPlayer.getCurrentTime().subtract(new Duration(FRAME_STEPS_MILLIS)));
	}

	@FXML
	private void onFullscreenButtonPressed(ActionEvent actionEvent) {
		((Stage)mediaView.getScene().getWindow()).setFullScreen(true);
		System.out.println("onFullscreenButtonPressed()");
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
		Duration duration = new Duration(this.attributedPerspective.getPerspective().getInPoint());
		this.mediaPlayer.seek(duration);
	}

	@FXML
	private void onGoToOutPointButtonPressed(ActionEvent actionEvent) {
		Duration duration = new Duration(this.attributedPerspective.getPerspective().getOutPoint());
		this.mediaPlayer.seek(duration);
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
			this.showPlayButton();
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
		this.timeSlider.setValue(newValue.toMillis());
		this.currentTimeLabel.setText(stringFromDuration(newValue));
	}

	private void handleTimeSliderValueChanged(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
		if (this.mediaPlayer.getStatus() != Status.PLAYING) {
			Duration duration = new Duration((Double) newValue);
			this.mediaPlayer.seek(duration);
			this.currentTimeLabel.setText(stringFromDuration(duration));
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
			control.setDisable(true);
		}
	}

	private void enableMediaPlayerControls() {
		for (Control control : this.mediaPlayerControls) {
			control.setDisable(false);
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
			double sliderPadding = 5;
			double totalWidth = this.timelineStackPane.getWidth() - 2*sliderPadding; // padding of slider
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

	public void setDelegate(final VideoPlayerDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public void setSelectedMedia(AttributedPerspective attributedPerspective) {
		this.removeExistingMediaPlayer();

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
	}

	public void selectedMediaChanged(AttributedPerspective attributedPerspective) {
		this.attributedPerspective = attributedPerspective;
		this.updateInOutPointView();
	}
	public void clearMedia() {
		// TODO
		System.out.println("clearMedia(): Not yet implemented");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.mediaPlayerControls = Arrays.asList(
				new Control[] { exportSnapshotButton, exportClipButton, playPauseButton, setInpointButton, skipButton,
						backButton, nextFrameButton, previousFrameButton, setOutpointButton, fullscreenButton, timeSlider});
		this.disableMediaPlayerControls();
		this.timeSlider.valueProperty().addListener(this::handleTimeSliderValueChanged);

		this.currentTimeLabel.setText(stringFromDuration(new Duration(0)));
		this.durationLabel.setText(stringFromDuration(new Duration(0)));

		this.mediaView.setSmooth(true); // TODO: ?
	}
}
