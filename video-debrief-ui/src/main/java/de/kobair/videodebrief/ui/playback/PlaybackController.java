package de.kobair.videodebrief.ui.playback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.w3c.dom.Attr;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.video.VideoHandler;
import de.kobair.videodebrief.core.video.VideoInformation;
import de.kobair.videodebrief.core.video.ffmpeg.FfmpegVideoHandler;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;
import de.kobair.videodebrief.ui.generics.Controller;
import de.kobair.videodebrief.ui.perspectives.PerspectivesViewController;
import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import de.kobair.videodebrief.ui.videoplayer.VideoPlayerViewController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class PlaybackController extends Controller implements Initializable, VideoPlayerViewController.VideoPlayerDelegate, PerspectivesViewController.PerspectivesDelegate {

	public interface PlaybackDelegate {

		public void setInPoint(Event event, Perspective perspective, long inPoint);

		public void setOutPoint(Event event, Perspective perspective, long outPoint);

		public void exportSnapshot(Event event, Perspective perspective, long timeMillis);

		public void setAlignmentPoint(Event event, Perspective perspective, long timeMillis);

	}

	private class VideoAttributer {
		
		private final File eventDirectory;
		private VideoHandler videoHandler = new FfmpegVideoHandler();
		
		VideoAttributer(Workspace workspace, Event event) {
			this.eventDirectory = LocalUtils.extendDirectory(workspace.getWorkspaceDirectory(), event.getSubPath());
		}
		
		AttributedPerspective addAttributesToPerspective(Perspective perspective) {
			File videoFile = LocalUtils.extendDirectory(eventDirectory, perspective.getFileName());
			VideoInformation videoInformation;
			try {
				videoInformation = this.videoHandler.extractInformation(videoFile);
				return new AttributedPerspective(perspective, videoInformation, videoFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	@FXML
	private AnchorPane videoPlayerAnchorPane;
	@FXML
	private AnchorPane perspectivesAnchorPane;

	private VideoPlayerViewController videoPlayerViewController;
	private PerspectivesViewController perspectivesViewController;

	private Optional<PlaybackDelegate> delegate = Optional.empty();
	private Event event;
	private Perspective perspective;

	private VideoPlayerViewController loadVideoPlayerView() {
		return this.loadViewIntoAnchorPane(videoPlayerAnchorPane, "VideoPlayer.fxml", VideoPlayerViewController.class);
	}

	private PerspectivesViewController loadPerspectivesView() {
		return this.loadViewIntoAnchorPane(perspectivesAnchorPane, "Perspectives.fxml", PerspectivesViewController.class);
	}
	
	private AttributedPerspective findAttributedPerspective(Perspective perspective, List<AttributedPerspective> attributedPerspectives) {
		List<AttributedPerspective> matches = attributedPerspectives.stream()
													  .filter(ap -> ap.getPerspective().equals(perspective))
													  .collect(Collectors.toList());
		return matches.size() == 1 ? matches.get(0) : null;
	}

	private List<AttributedPerspective> attributedPerspectivesFromSelection(Workspace workspace, Event event, Perspective perspective) throws UnknownWorkspaceException, IOException {
		VideoAttributer attributer = new VideoAttributer(workspace, event);

		List<AttributedPerspective> attributedPerspectives;
		try {
			List<Perspective> perspectives = workspace.getPerspectives(event);
			attributedPerspectives = perspectives.parallelStream()
											 .map(attributer::addAttributesToPerspective)
											 .collect(Collectors.toList());
		} catch (RuntimeException e) {
			if (e.getCause() instanceof IOException) {
				IOException ioException = (IOException) e.getCause();
				throw ioException;
			} else {
				throw e;
			}
		}
		return attributedPerspectives;
	}

	public void setSelectedMedia(Workspace workspace, Event event, Perspective perspective) throws UnknownWorkspaceException, IOException {
		this.event = event;
		this.perspective = perspective;
		List<AttributedPerspective> attributedPerspectives = this.attributedPerspectivesFromSelection(workspace, event, perspective);
		AttributedPerspective attributedPerspective = this.findAttributedPerspective(perspective, attributedPerspectives);
		List<String> otherPerspectives = attributedPerspectives.stream()
												 .map(ap -> ap.getPerspective().getName())
												 .collect(Collectors.toList());

		this.videoPlayerViewController.setSelectedMedia(attributedPerspective, otherPerspectives);
		// TODO: set for perspectivs view
	}

	public void setDelegate(PlaybackDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public Event getEvent() {
		return event;
	}

	public Perspective getPerspective() {
		return perspective;
	}

	public void updateSelectedMedia(Workspace workspace, Event event, Perspective perspective) throws UnknownWorkspaceException, IOException {
		this.event = event;
		this.perspective = perspective;
		List<AttributedPerspective> attributedPerspectives = this.attributedPerspectivesFromSelection(workspace, event, perspective);
		AttributedPerspective attributedPerspective = this.findAttributedPerspective(perspective, attributedPerspectives);

		this.videoPlayerViewController.selectedMediaChanged(attributedPerspective);
		// TODO: set for perspectivs view
	}

	public void clearSelectedMedia() {
		this.videoPlayerViewController.clearMedia();
		// TODO: set for perspectivs view
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.videoPlayerViewController = loadVideoPlayerView();
		this.videoPlayerViewController.setDelegate(this);

		this.perspectivesViewController = loadPerspectivesView();
		this.perspectivesViewController.setDelegate(this);
	}

	@Override
	public void setInPoint(final long inPoint) {
		this.delegate.ifPresent(delegate -> delegate.setInPoint(this.event, this.perspective, inPoint));
	}

	@Override
	public void setOutPoint(final long outPoint) {
		this.delegate.ifPresent(delegate -> delegate.setOutPoint(this.event, this.perspective, outPoint));
	}

	@Override
	public void exportSnapshot(final long timeMillis) {
		this.delegate.ifPresent(delegate -> delegate.exportSnapshot(this.event, this.perspective, timeMillis));
	}

	@Override
	public void setAlignmentPoint(final long timeMillis) {
		this.delegate.ifPresent(delegate -> delegate.setAlignmentPoint(this.event, this.perspective, timeMillis));
	}
}
