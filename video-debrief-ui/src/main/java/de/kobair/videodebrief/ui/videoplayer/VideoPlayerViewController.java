package de.kobair.videodebrief.ui.videoplayer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.lang3.time.DurationFormatUtils;

import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.video.VideoInformation;
import de.kobair.videodebrief.ui.errors.ApplicationError;
import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoPlayerViewController implements Initializable {

	private static final KeyCombination PREVIOUS_FRAME_KEY_COMBINATION = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHIFT_DOWN);
	private static final KeyCombination NEXT_FRAME_KEY_COMBINATION = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHIFT_DOWN);
	private static final KeyCombination GO_TO_INPOINT_KEY_COMBINATION = new KeyCodeCombination(KeyCode.I, KeyCombination.SHIFT_DOWN);
	private static final KeyCombination GO_TO_OUTPOINT_KEY_COMBINATION = new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN);
	private static final KeyCombination GO_TO_ALIGNMENT_POINT_KEY_COMBINATION = new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN);

	private static final List<MediaPlayer.Status> INTERACTABLE_STATES = Arrays.asList(new MediaPlayer.Status[] {
			Status.READY, Status.PLAYING, Status.PAUSED
	});

	private static final int SEEK_DURATION_MILLIS = 5000;
	private static final int SLIDER_PADDING = 5;
	private static final long SEEK_MARGIN_RIGHT = 50;
	private static final String VIDEO_DURATION_FORMAT = "mm:ss.SSS";

	public abstract class VideoPlayerException extends Exception {


		public VideoPlayerException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	public class UnknownVideoPlayerException extends VideoPlayerException {

		public UnknownVideoPlayerException(Perspective perspective, Throwable cause) {
			super(String.format("An unkown error occured during the playback of '%s'.", perspective.getName()), cause);
		}
	}

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
	private BorderPane borderPane;
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
		Duration time = this.mediaPlayer.getCurrentTime().add(new Duration(frameStepMillis));
		this.seekAndUpdateTimeSlider(time);
	}

	@FXML
	private void onPreviousFrameButtonPressed(ActionEvent actionEvent) {
		if (this.mediaPlayer.getStatus() != Status.PAUSED) {
			this.mediaPlayer.pause();
		}
		Duration time = this.mediaPlayer.getCurrentTime().subtract(new Duration(frameStepMillis));
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
	private void onSetAlignmentPointButtonPressed(ActionEvent actionEvent) {
		long time = (long) this.mediaPlayer.getCurrentTime().toMillis();
		this.delegate.ifPresent(delegate -> delegate.setAlignmentPoint(time));
	}

	@FXML
	private void onMediaViewError(MediaErrorEvent event) {
		MediaException mediaException = event.getMediaError();
		this.handleUnexpectedError(mediaException);
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
	private int frameStepMillis = 17;

	private void mediaDurationAvailable(Duration duration) {
		this.timeSlider.setMax(duration.toMillis());
		this.durationLabel.setText(stringFromDuration(duration));
		this.currentTimeLabel.setText(stringFromDuration(new Duration(0)));
	}

	private String stringFromDuration(Duration duration) {
		return DurationFormatUtils.formatDuration((long) duration.toMillis(), VIDEO_DURATION_FORMAT);
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
			this.mediaViewContainerResized();
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
			this.handleUnexpectedError(e);
			return null;
		}

		Media media = new Media(mediaUrl.toExternalForm());
		media.setOnError(() -> {
			MediaException mediaException = this.media.getError();
			this.handleUnexpectedError(mediaException);
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
			double totalWidth = this.timelineAnchorPane.getWidth() - 2 * SLIDER_PADDING; // padding of slider
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
			double totalWidth = this.timelineAnchorPane.getWidth() - 2 * SLIDER_PADDING; // padding of slider
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
		long maxSeekPosition = mediaLength - SEEK_MARGIN_RIGHT;
		long timeMillis = (long) time.toMillis();
		timeMillis = Math.max(timeMillis, 0);
		timeMillis = Math.min(timeMillis, maxSeekPosition);

		this.mediaPlayer.seek(new Duration(timeMillis));
		this.updateTimeline(new Duration(timeMillis));
	}

	private int millisPerFrame(AttributedPerspective perspective) {
		VideoInformation.VideoTrack track = perspective.getVideoInformation().getVideoInformation().get(0);
		double fps = Math.ceil(track.getFramesPerSecond());
		double result = 1000.0 / fps;
		return (int) Math.ceil(result);
	}

	private void mediaViewContainerResized(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
		this.mediaViewContainerResized();
	}

	private void mediaViewContainerResized() {
		if (this.media != null) {
			Pane top = (Pane) this.borderPane.getTop();
			Pane bottom = (Pane) this.borderPane.getBottom();
			Pane left = (Pane) this.borderPane.getLeft();
			Pane right = (Pane) this.borderPane.getRight();

			double leftWidth = left == null ? 0 : left.getWidth();
			double rightWidth = right == null ? 0 : right.getWidth();
			double maxWidth = this.borderPane.getWidth() - leftWidth - rightWidth;

			double topHeight = top == null ? 0 : top.getHeight();
			double bottomHeight = bottom == null ? 0 : bottom.getHeight();
			double maxHeight = this.borderPane.getHeight() - topHeight - bottomHeight;

			double aspectRatio = (double) this.media.getWidth() / (double) this.media.getHeight();

			double height = maxHeight;
			double width = Math.floor(height * aspectRatio);

			if (width > maxWidth) {
				width = maxWidth;
				height = Math.floor(width / aspectRatio);
			}

			this.mediaView.setFitWidth(width);
			this.mediaView.setFitHeight(height);
		}
	}

	private boolean isKeyEventApplicable() {
		return this.mediaPlayer != null && INTERACTABLE_STATES.contains(this.mediaPlayer.getStatus());
	}

	private void handleKeyPressedEvent(KeyEvent event) {
		if (!this.isKeyEventApplicable()) {
			return;
		}

		if (PREVIOUS_FRAME_KEY_COMBINATION.match(event)) {
			this.onPreviousFrameButtonPressed(null);
		} else if (NEXT_FRAME_KEY_COMBINATION.match(event)) {
			this.onNextFrameButtonPressed(null);
		} else if (GO_TO_INPOINT_KEY_COMBINATION.match(event)) {
			this.onGoToInPointButtonPressed(null);
		} else if (GO_TO_OUTPOINT_KEY_COMBINATION.match(event)) {
			this.onGoToOutPointButtonPressed(null);
		} else if (GO_TO_ALIGNMENT_POINT_KEY_COMBINATION.match(event)) {
			this.onGoToAlignmentPointButtonPressed(null);
		} else {
			switch (event.getCode()) {

			case SPACE:
				this.onPlayPauseButtonPressed(null);
				break;

			case I:
				this.onSetInpointButtonPressed(null);
				break;

			case O:
				this.onSetOutpointButtonPressed(null);
				break;

			case A:
				this.onSetAlignmentPointButtonPressed(null);
				break;

			case LEFT:
				this.onBackButtonPressed(null);
				break;

			case RIGHT:
				this.onSkipButtonPressed(null);
				break;

			case F:
				this.onFullscreenButtonPressed(null);
				break;

			case S:
				this.onExportSnapshotButtonPressed(null);
				break;

			case C:
				this.onExportClipButtonPressed(null);
				break;

			default:
				return;

			}
		}

		event.consume();
	}

	private void handleUnexpectedError(Throwable cause) {
		UnknownVideoPlayerException e = new UnknownVideoPlayerException(this.attributedPerspective.getPerspective(), cause);
		new ApplicationError(e).throwOnMainThread();
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

		this.mediaPlayer.setOnError(() -> {
			MediaException mediaException = this.mediaPlayer.getError();
			this.handleUnexpectedError(mediaException);
		});

		this.mediaView.setMediaPlayer(this.mediaPlayer);

		this.mediaPlayer.currentTimeProperty().addListener(this::handleMediaPlayerTimeChange);

		this.updateInOutPointView();
		this.updateAlignmentPointView();

		this.perspectives.clear();
		this.perspectives.addAll(allPerspectives);
		this.perspectivesComboBox.valueProperty().set(attributedPerspective.getPerspective().getName());

		this.frameStepMillis = this.millisPerFrame(attributedPerspective);
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

	public void addKeyboardShortcuts(Scene scene) {
		scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressedEvent);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.mediaPlayerControls = Arrays.asList(
				new Control[] {exportSnapshotButton, exportClipButton, playPauseButton, setInpointButton, skipButton,
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

		this.mediaView.setSmooth(true);

		this.borderPane.widthProperty().addListener(this::mediaViewContainerResized);
		this.borderPane.heightProperty().addListener(this::mediaViewContainerResized);
	}
}
