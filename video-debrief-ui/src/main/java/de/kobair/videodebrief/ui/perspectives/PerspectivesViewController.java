package de.kobair.videodebrief.ui.perspectives;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.kobair.videodebrief.core.video.VideoInformation;
import de.kobair.videodebrief.ui.badges.TrackBadgeFactory;
import de.kobair.videodebrief.ui.perspectives.model.TimelineItem;
import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

public class PerspectivesViewController implements Initializable {

	public interface PerspectivesDelegate {

		public void changeToPerspective(AttributedPerspective perspective);

	}

	@FXML
	private VBox timelineVBox;
	@FXML
	private AnchorPane perspectivesContainer;
	@FXML
	private Pane timeMarkerPane;

	private static final Insets BADGE_INSETS =  new Insets(3, 8, 3, 0);
	private static final Insets NAME_VIEW_INSETS = new Insets(0,20, 0,8 );

	private long timelineLength = 0;
	private Optional<PerspectivesDelegate> delegate = Optional.empty();

	private Node nodeForPerspective(AttributedPerspective perspective, long timelineLength, long relativeOffset) {
		Pane view = this.videoViewForPerspective(perspective);

		double lengthProportion = (double) perspective.getVideoInformation().getDurationMillis() / (double) timelineLength;
		DoubleBinding widthBinidng = this.perspectivesContainer.widthProperty().multiply(lengthProportion).subtract(2);
		view.minWidthProperty().bind(widthBinidng);

		double offsetProportion = (double) relativeOffset / (double) timelineLength;
		DoubleBinding offsetBinding = this.perspectivesContainer.widthProperty().multiply(offsetProportion).subtract(2);

		Pane offsetPane = new Pane();
		offsetPane.minWidthProperty().bind(offsetBinding);

		HBox container = new HBox();
		container.getChildren().add(offsetPane);
		container.getChildren().add(view);

		VBox.setMargin(container, new Insets(8, 0,0,0));

		return container;
	}

	private Pane videoViewForPerspective(AttributedPerspective perspective) {
		VideoInformation.VideoTrack videoTrack =  perspective.getVideoInformation().getVideoInformation().get(0);
		VideoInformation.AudioTrack audioTrack =  perspective.getVideoInformation().getAudioInformation().get(0);

		HBox view = new HBox();

		Node perspectiveNameView = this.nameLabelForPerspective(perspective);
		HBox.setMargin(perspectiveNameView, NAME_VIEW_INSETS);

		Node spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Node videoTrackBadge = TrackBadgeFactory.badgeForVideoTrack(videoTrack);
		HBox.setMargin(videoTrackBadge, BADGE_INSETS);

		Node audioTrackBadge = TrackBadgeFactory.badgeForAudioTrack(audioTrack);
		HBox.setMargin(audioTrackBadge, BADGE_INSETS);

		view.getChildren().addAll(perspectiveNameView, spacer, audioTrackBadge, videoTrackBadge);
		view.setStyle("-fx-background-color: rgb(202,202,202)"); // TODO: propper styling

		view.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				PerspectivesViewController.this.delegate.ifPresent(delegate -> delegate.changeToPerspective(perspective));
			}
		});

		return view;
	}

	private Node nameLabelForPerspective(AttributedPerspective perspective) {
		Label perspectiveNameLabel = new Label(perspective.getPerspective().getName());

		VBox result = new VBox();
		result.setAlignment(Pos.CENTER_LEFT);
		result.getChildren().add(perspectiveNameLabel);
		VBox.setVgrow(perspectiveNameLabel, Priority.ALWAYS);

		return result;
	}

	private void clearTimeline() {
		this.timelineVBox.getChildren().clear();
	}

	private long calculateTimelineLength(List<TimelineItem> timelineItems) {
		long result = 0;
		for (TimelineItem timelineItem : timelineItems) {
			result = Math.max(result, timelineItem.getOverallLength());
		}
		return result;
	}

	private void hideTimeMarker() {
		this.timeMarkerPane.setVisible(false);
	}

	private void moveTimeMarker(long timeMillis) {
		this.timeMarkerPane.setVisible(true);

		double proportion = (double) timeMillis / (double) this.timelineLength;
		double position = (this.perspectivesContainer.getWidth() * proportion) - (this.timeMarkerPane.getWidth() / 2.0);
		AnchorPane.setLeftAnchor(timeMarkerPane, position);
	}

	public void setDelegate(final PerspectivesDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public void showTimeline(List<TimelineItem> timelineItems, AttributedPerspective perspectivePlaying) {
		hideTimeMarker();
		clearTimeline();
		this.timelineLength = calculateTimelineLength(timelineItems);
		for (TimelineItem timelineItem : timelineItems) {
			this.timelineVBox.getChildren().add(nodeForPerspective(timelineItem.getAatributedPerspective(), this.timelineLength, timelineItem.getRelativeOffset()));
		}
		this.changeSelectedPerspective(perspectivePlaying);
	}

	public void clear() {
		this.hideTimeMarker();
		this.clearTimeline();
	}

	public void changeSelectedPerspective(AttributedPerspective perspective) {

	}

	public void updateTimeline(long time) {
		this.moveTimeMarker(time);
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		hideTimeMarker();
	}
}
