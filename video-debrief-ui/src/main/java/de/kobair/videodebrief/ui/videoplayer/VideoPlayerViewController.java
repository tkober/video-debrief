package de.kobair.videodebrief.ui.videoplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.ui.playback.model.SelectedMedia;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;

public class VideoPlayerViewController implements Initializable {

	public interface VideoPlayerDelegate {

	}

	@FXML
	private AnchorPane mediaViewAnchorPane;
	@FXML
	private Button playPauseButton;
	@FXML
	private Button skipButton;
	@FXML
	private Button backButton;
	@FXML
	private Button nextFrameButton;
	@FXML
	private Button previousFrameButton;
	@FXML
	private Button fullscreenButton;
	@FXML
	private Label timestampLabel;
	@FXML
	private Label durationLabel;


	private Optional<VideoPlayerDelegate> delegate = Optional.empty();
	private SelectedMedia selectedMedia;

	public void setDelegate(final VideoPlayerDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
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

		return new Media(mediaUrl.toExternalForm());
	}

	public void setSelectedMedia(SelectedMedia selectedMedia) {
		this.selectedMedia = selectedMedia;
		final Media media = fxMediaFromSelection(selectedMedia);

		final MediaPlayer player = new MediaPlayer(media);

		mediaView.setMediaPlayer(player);
		mediaView.setSmooth(true);

//		// Create the Buttons
//		Button playButton = new Button("Play");
//		Button stopButton = new Button("Stop");
//
//		// Create the Event Handlers for the Button
//		playButton.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent event) {
//				if (player.getStatus() == Status.PLAYING) {
//					player.stop();
//					player.play();
//				} else {
//					player.play();
//				}
//			}
//		});
//
//		stopButton.setOnAction(new EventHandler<ActionEvent>() {
//			public void handle(ActionEvent event) {
//				player.stop();
//			}
//		});
//
//		// Create Handlers for handling Errors
//		player.setOnError(new Runnable() {
//			public void run() {
//				// Handle asynchronous error in Player object.
//				printMessage(player.getError());
//			}
//		});
//
//		media.setOnError(new Runnable() {
//			public void run() {
//				// Handle asynchronous error in Media object.
//				printMessage(media.getError());
//			}
//		});
//
//		mediaView.setOnError(new EventHandler<MediaErrorEvent>() {
//			public void handle(MediaErrorEvent event) {
//				// Handle asynchronous error in MediaView.
//				printMessage(event.getMediaError());
//			}
//		});
//
//		// Add a ChangeListener to the player
//		player.statusProperty().addListener(new ChangeListener<MediaPlayer.Status>() {
//			// Log the Message
//			public void changed(ObservableValue<? extends MediaPlayer.Status> ov, final MediaPlayer.Status oldStatus,
//					final MediaPlayer.Status newStatus) {
//				System.out.println("\nStatus changed from " + oldStatus + " to " + newStatus);
//			}
//		});
//
//		// Add a Handler for PLAYING status
//		player.setOnPlaying(new Runnable() {
//			public void run() {
//				System.out.println("\nPlaying now");
//			}
//		});
//
//		// Add a Handler for STOPPED status
//		player.setOnStopped(new Runnable() {
//			public void run() {
//				System.out.println("\nStopped now");
//			}
//		});
	}

	private void printMessage(MediaException error) {
		String errorMessage = error.getMessage();
		System.out.println(errorMessage);
	}

	@FXML
	private MediaView mediaView;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
}
