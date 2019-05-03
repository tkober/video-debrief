package de.kobair.videodebrief.ui.perspectives;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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

	private long timelineLength = 0;
	private Optional<PerspectivesDelegate> delegate = Optional.empty();

	private Node nodeForPerspective(AttributedPerspective perspective, long timelineLength, long relativeOffset) {
		Button button = new Button();
		button.setText(perspective.getPerspective().getName());
		button.setAlignment(Pos.BASELINE_LEFT);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				PerspectivesViewController.this.delegate.ifPresent(delegate -> delegate.changeToPerspective(perspective));
			}
		});

		double lengthProportion = (double) perspective.getVideoInformation().getDurationMillis() / (double) timelineLength;
		DoubleBinding widthBinidng = this.perspectivesContainer.widthProperty().multiply(lengthProportion).subtract(2);
		button.minWidthProperty().bind(widthBinidng);

		double offsetProportion = (double) relativeOffset / (double) timelineLength;
		DoubleBinding offsetBinding = this.perspectivesContainer.widthProperty().multiply(offsetProportion).subtract(2);

		Pane offsetPane = new Pane();
		offsetPane.minWidthProperty().bind(offsetBinding);

		HBox container = new HBox();
		container.getChildren().add(offsetPane);
		container.getChildren().add(button);

		VBox.setMargin(container, new Insets(8, 0,0,0));

		return container;
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
