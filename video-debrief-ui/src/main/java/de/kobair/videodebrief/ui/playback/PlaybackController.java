package de.kobair.videodebrief.ui.playback;

import java.net.URL;
import java.util.ResourceBundle;

import de.kobair.videodebrief.ui.generics.Controller;
import de.kobair.videodebrief.ui.perspectives.PerspectivesViewController;
import de.kobair.videodebrief.ui.videoplayer.VideoPlayerViewController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class PlaybackController extends Controller implements Initializable, VideoPlayerViewController.VideoPlayerDelegate, PerspectivesViewController.PerspectivesDelegate {

	@FXML
	private AnchorPane videoPlayerAnchorPane;
	@FXML
	private AnchorPane perspectivesAnchorPane;

	private VideoPlayerViewController videoPlayerViewController;
	private PerspectivesViewController perspectivesViewController;

	private VideoPlayerViewController loadVideoPlayerView() {
		return this.loadViewIntoAnchorPane(videoPlayerAnchorPane, "VideoPlayer.fxml", VideoPlayerViewController.class);
	}

	private PerspectivesViewController loadPerspectivesView() {
		return this.loadViewIntoAnchorPane(videoPlayerAnchorPane, "Perspectives.fxml", PerspectivesViewController.class);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.videoPlayerViewController = loadVideoPlayerView();
		this.videoPlayerViewController.setDelegate(this);

		this.perspectivesViewController = loadPerspectivesView();
		this.perspectivesViewController.setDelegate(this);
	}

}
