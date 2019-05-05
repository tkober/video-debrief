package de.kobair.videodebrief.ui.playback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.video.VideoHandler;
import de.kobair.videodebrief.core.video.VideoInformation;
import de.kobair.videodebrief.core.video.ffmpeg.FfmpegVideoHandler;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;
import de.kobair.videodebrief.ui.dialogs.DialogFactory;
import de.kobair.videodebrief.ui.generics.Controller;
import de.kobair.videodebrief.ui.perspectives.PerspectivesViewController;
import de.kobair.videodebrief.ui.perspectives.model.TimelineItem;
import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import de.kobair.videodebrief.ui.videoplayer.VideoPlayerViewController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

public class PlaybackController extends Controller implements Initializable, VideoPlayerViewController.VideoPlayerDelegate, PerspectivesViewController.PerspectivesDelegate {

	public interface PlaybackDelegate {

		public class Clip {

			private final Perspective perspective;
			private final long begin;
			private final long end;

			public Clip(Perspective perspective, long begin, long end) {
				this.perspective = perspective;
				this.begin = begin;
				this.end = end;
			}

			public Perspective getPerspective() {
				return perspective;
			}

			public long getBegin() {
				return begin;
			}

			public long getEnd() {
				return end;
			}
		}

		public void setInPoint(Event event, Perspective perspective, long inPoint);

		public void setOutPoint(Event event, Perspective perspective, long outPoint);

		public void exportSnapshot(Event event, Perspective perspective, long timeMillis);

		public void exportClips(Event event, List<Clip> clips);

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
	private AttributedPerspective selectedPerspective;
	private List<AttributedPerspective> attributedPerspectives;
	private Map<AttributedPerspective, Long> relativeOffsets;

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

	private void updateModel(Workspace workspace, Event event, Perspective perspective) throws IOException, UnknownWorkspaceException {
		this.event = event;
		this.attributedPerspectives = this.attributedPerspectivesFromSelection(workspace, event, perspective);
		this.selectedPerspective = this.findAttributedPerspective(perspective, attributedPerspectives);
	}

	private AttributedPerspective findPerspectiveByName(String name) {
		for (AttributedPerspective attributedPerspective : this.attributedPerspectives) {
			if (attributedPerspective.getPerspective().getName().equals(name)) {
				return attributedPerspective;
			}
		}
		return null;
	}

	private int sortAttributedPerspectivesByAlignmentPointDesc(final AttributedPerspective a, final AttributedPerspective b) {
		return (int) Math.signum(b.getPerspective().getAlignmentPoint() - a.getPerspective().getAlignmentPoint());
	}

	private void calculateRelativeOffsets() {
		long maxAlignmentPoint = this.attributedPerspectives.stream()
										 .map(ap -> ap.getPerspective().getAlignmentPoint())
										 .max(Long::compare)
										 .get();

		this.relativeOffsets = new HashMap<>(this.attributedPerspectives.size());
		for (AttributedPerspective attributedPerspective : this.attributedPerspectives) {
			Long relativeOffset = Math.abs(maxAlignmentPoint - attributedPerspective.getPerspective().getAlignmentPoint());
			relativeOffsets.put(attributedPerspective, relativeOffset);
		}
	}

	private long perspectiveTimeToOverallTime(AttributedPerspective fromPerspective, long time) {
		long relativeOffset = this.relativeOffsets.get(fromPerspective);
		return time + relativeOffset;
	}

	private long overallTimeToPerspectiveTime(long time, AttributedPerspective toPerspective) {
		long relativeOffset = this.relativeOffsets.get(toPerspective);
		return time - relativeOffset;
	}

	private long convertTimeBetweenPerspectives(long time, AttributedPerspective fromPerspective, AttributedPerspective toPerspective) {
		long overallTime = this.perspectiveTimeToOverallTime(fromPerspective, time);
		return this.overallTimeToPerspectiveTime(overallTime, toPerspective);
	}

	private void changeToPerspective(AttributedPerspective perspective, long time, boolean play) {
		this.selectedPerspective = perspective;

		List<String> allPerspectives = this.attributedPerspectives.stream()
											   .map(ap -> ap.getPerspective().getName())
											   .collect(Collectors.toList());
		this.videoPlayerViewController.setSelectedMedia(perspective, allPerspectives, time, play);

		this.perspectivesViewController.changeSelectedPerspective(perspective);
	}

	private List<TimelineItem> getTimelines() {
		return this.attributedPerspectives.stream()
				.map(ap -> new TimelineItem(ap, this.relativeOffsets.get(ap)))
				.collect(Collectors.toList());
	}

	public void setSelectedMedia(Workspace workspace, Event event, Perspective perspective) throws UnknownWorkspaceException, IOException {
		this.updateModel(workspace, event, perspective);
		this.calculateRelativeOffsets();

		List<String> allPerspectives = this.attributedPerspectives.stream()
												 .map(ap -> ap.getPerspective().getName())
												 .collect(Collectors.toList());
		this.videoPlayerViewController.setSelectedMedia(this.selectedPerspective, allPerspectives);

		List<TimelineItem> timelineItems = this.getTimelines();
		this.perspectivesViewController.showTimeline(timelineItems, this.selectedPerspective);
	}

	public void setDelegate(PlaybackDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public Event getEvent() {
		return event;
	}

	public Perspective getPerspective() {
		return this.selectedPerspective != null ? this.selectedPerspective.getPerspective() : null;
	}

	public void updateSelectedMedia(Workspace workspace, Event event, Perspective perspective) throws UnknownWorkspaceException, IOException {
		this.updateModel(workspace, event, perspective);
		this.calculateRelativeOffsets();
		this.videoPlayerViewController.selectedMediaChanged(this.selectedPerspective);

		List<TimelineItem> timelineItems = this.getTimelines();
		this.perspectivesViewController.showTimeline(timelineItems, this.selectedPerspective);
	}

	public void clearSelectedMedia() {
		this.videoPlayerViewController.clearMedia();
		this.perspectivesViewController.clear();
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
		this.delegate.ifPresent(delegate -> delegate.setInPoint(this.event, this.selectedPerspective.getPerspective(), inPoint));
	}

	@Override
	public void setOutPoint(final long outPoint) {
		this.delegate.ifPresent(delegate -> delegate.setOutPoint(this.event, this.selectedPerspective.getPerspective(), outPoint));
	}

	@Override
	public void exportSnapshot(final long timeMillis) {
		this.delegate.ifPresent(delegate -> delegate.exportSnapshot(this.event, this.selectedPerspective.getPerspective(), timeMillis));
	}

	@Override
	public void exportClip(AttributedPerspective perspective) {
		List<AttributedPerspective> otherAvailablePerspectives = this.attributedPerspectives.stream()
				.filter(p -> !perspective.equals(p) && isAdditionalPerspectiveForClip(perspective, p))
				.collect(Collectors.toList());

		List<AttributedPerspective> perspectivesToExport = new ArrayList<>();
		perspectivesToExport.add(perspective);
		if (otherAvailablePerspectives.size() > 0) {
			Optional<Map<AttributedPerspective, Boolean>> selection = DialogFactory.chooseAdditionalPerspectivesDialog(otherAvailablePerspectives).showAndWait();
			if (!selection.isPresent()) {
				return;
			}
			for (AttributedPerspective ap : selection.get().keySet()) {
				if (selection.get().get(ap)) {
					perspectivesToExport.add(ap);
				}
			}
		}
		List<PlaybackDelegate.Clip> clips = perspectivesToExport.stream()
				.map(ap -> this.clipForExport(perspective, ap))
				.collect(Collectors.toList());
		this.delegate.ifPresent(delegate -> delegate.exportClips(this.event, clips));
	}

	private PlaybackDelegate.Clip clipForExport(AttributedPerspective perspective, AttributedPerspective additionalPerspective) {
		long relativeBegin = perspective.getPerspective().getInPoint();
		long relativeEnd = perspective.getPerspective().getOutPoint();
		long begin = this.convertTimeBetweenPerspectives(relativeBegin, perspective, additionalPerspective);
		long end = this.convertTimeBetweenPerspectives(relativeEnd, perspective, additionalPerspective);
		return new PlaybackDelegate.Clip(additionalPerspective.getPerspective(), begin, end);
	}

	private boolean isAdditionalPerspectiveForClip(AttributedPerspective clip, AttributedPerspective additionalPerspective) {
		long inPoint = clip.getPerspective().getInPoint();
		long outPoint = clip.getPerspective().getOutPoint();
		return this.isTimePointCoveredByOtherPerspective(inPoint, clip, additionalPerspective) && this.isTimePointCoveredByOtherPerspective(outPoint, clip, additionalPerspective);
	}

	private boolean isTimePointCoveredByOtherPerspective(long time, AttributedPerspective clip, AttributedPerspective perspective) {
		long perspectiveTime = this.convertTimeBetweenPerspectives(time, clip, perspective);
		return perspectiveTime >= 0 && perspectiveTime <= perspective.getVideoInformation().getDurationMillis();
	}

	@Override
	public void setAlignmentPoint(final long timeMillis) {
		this.delegate.ifPresent(delegate -> delegate.setAlignmentPoint(this.event, this.selectedPerspective.getPerspective(), timeMillis));
	}

	@Override
	public void changePerspective(final String name, final long timeMillis, boolean play) {
		AttributedPerspective attributedPerspective = findPerspectiveByName(name);
		if (attributedPerspective != null) {
			long timeInNewPerspective = this.convertTimeBetweenPerspectives(timeMillis, this.selectedPerspective, attributedPerspective);
			this.changeToPerspective(attributedPerspective, timeInNewPerspective, play);
		}
	}

	@Override
	public void timeChanged(final long timeMillis) {
		long overallTime = this.perspectiveTimeToOverallTime(this.selectedPerspective, timeMillis);
		this.perspectivesViewController.updateTimeline(overallTime);
	}

	@Override
	public void changeToPerspective(final AttributedPerspective perspective) {
		if (!this.selectedPerspective.equals(perspective)) {
			boolean play = this.videoPlayerViewController.shouldPlayOnPerspectiveChange();
			long time = this.videoPlayerViewController.getCurrentTime();
			long timeInNewPerspective = this.convertTimeBetweenPerspectives(time, this.selectedPerspective, perspective);
			this.changeToPerspective(perspective, timeInNewPerspective, play);
		}
	}
}
