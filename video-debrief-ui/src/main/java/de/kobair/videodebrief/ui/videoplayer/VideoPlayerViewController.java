package de.kobair.videodebrief.ui.videoplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.ui.playback.model.SelectedMedia;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoPlayerViewController implements Initializable {
	
	private static final int SEEK_DURATION_MILLIS = 5000;
	private static final int FRAME_STEPS_MILLIS = 17;

	public interface VideoPlayerDelegate {

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
	private Label timestampLabel;
	@FXML
	private Label durationLabel;

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
//		if (this.mediaPlayer.getStatus() != Status.PAUSED) {
//			this.mediaPlayer.pause();
//		}
//		this.mediaPlayer.seek(this.mediaPlayer.getCurrentTime().add(new Duration(FRAME_STEPS_MILLIS)));
		this.mediaPlayer.setRate(-0.05);
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
		System.out.println("onFullscreenButtonPressed()");
	}

	@FXML
	private void onSetInpointButtonPressed(ActionEvent actionEvent) {
		System.out.println("onSetInpointButtonPressed()");
	}

	@FXML
	private void onSetOutpointButtonPressed(ActionEvent actionEvent) {
		System.out.println("onSetOutpointPressed()");
	}

	@FXML
	private void onMediaViewError(MediaErrorEvent event) {
		System.out.println(event);
	}

	private List<Control> mediaPlayerControls;
	private Optional<VideoPlayerDelegate> delegate = Optional.empty();
	private SelectedMedia selectedMedia;
	private MediaPlayer mediaPlayer;
	private Media media;

	private void handleMediaPlayerStatusChange(ObservableValue<? extends MediaPlayer.Status> ov,
			MediaPlayer.Status oldStatus, MediaPlayer.Status newStatus) {

		switch (newStatus) {
		
		case READY:
			this.enableMediaPlayerControls();
			this.showPlayButton();
			break;
			
		case PAUSED:
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

	private Media fxMediaFromSelection(SelectedMedia selectedMedia) {
		File file = LocalUtils.extendDirectory(selectedMedia.getWorkspace().getWorkspaceDirectory(),
				selectedMedia.getEvent().getSubPath(), selectedMedia.getPerspective().getFileName());
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

	public void setDelegate(final VideoPlayerDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public void setSelectedMedia(SelectedMedia selectedMedia) {
		this.removeExistingMediaPlayer();

		this.selectedMedia = selectedMedia;
		this.media = fxMediaFromSelection(this.selectedMedia);

		this.mediaPlayer = new MediaPlayer(this.media);
		this.mediaPlayer.statusProperty().addListener(this::handleMediaPlayerStatusChange);

		this.mediaPlayer.setOnError(new Runnable() {
			public void run() {
				// Handle asynchronous error in Player object.
				System.out.println(VideoPlayerViewController.this.mediaPlayer.getError());
			}
		});

		this.mediaView.setMediaPlayer(this.mediaPlayer);
		this.mediaView.setSmooth(true); // TODO: ?
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.mediaPlayerControls = Arrays.asList(
				new Control[] { exportSnapshotButton, exportClipButton, playPauseButton, setInpointButton, skipButton,
						backButton, nextFrameButton, previousFrameButton, setOutpointButton, fullscreenButton });
		this.disableMediaPlayerControls();
		this.setupMediaPlayerControls();
	}
	
	private void setupMediaPlayerControls() {
		this.nextFrameButton.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {

	        @Override
	        public void handle(MouseEvent event) {
	            if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
	                timer.start();
	            } else {
	                timer.stop();
	            }

	        }
	    });
	}
	
	final AnimationTimer timer = new AnimationTimer() {

        private long lastUpdate = -1;
        private final long REPEAT_INTERVAL = 1000000 * 300;

        @Override
        public void handle(long time) {
        	if (this.lastUpdate < 0) {
        		this.lastUpdate = time;
        	}
            if (time - this.lastUpdate > (REPEAT_INTERVAL)) {
            	System.out.println(time - this.lastUpdate);
                VideoPlayerViewController.this.onNextFrameButtonPressed(null);
                this.lastUpdate = time;
            }
        }
    };

}
