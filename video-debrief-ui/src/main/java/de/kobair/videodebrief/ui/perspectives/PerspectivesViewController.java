package de.kobair.videodebrief.ui.perspectives;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import de.kobair.videodebrief.ui.perspectives.model.TimelineItem;
import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class PerspectivesViewController implements Initializable {


	public interface PerspectivesDelegate {

	}

	@FXML
	private VBox timelineVBox;
	@FXML
	private AnchorPane perspectivesContainer;

	private Optional<PerspectivesDelegate> delegate = Optional.empty();


	private Node nodeForPerspective(AttributedPerspective perspective, long timelineLength, long relativeOffset) {
		Button button = new Button();
		button.setText(perspective.getPerspective().getName());

		double lengthProportion = (double) perspective.getVideoInformation().getDurationMillis() / (double) timelineLength;
		DoubleBinding widthBinidng = this.perspectivesContainer.widthProperty().multiply(lengthProportion).subtract(1);
		button.minWidthProperty().bind(widthBinidng);

		double offsetProportion = (double) relativeOffset / (double) timelineLength;
		DoubleBinding offsetBinding = this.perspectivesContainer.widthProperty().multiply(offsetProportion).subtract(1);

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

	public void setDelegate(final PerspectivesDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public void showTimeline(List<TimelineItem> timelineItems, AttributedPerspective perspectivePlaying) {
		clearTimeline();
		long timelineLength = calculateTimelineLength(timelineItems);
		System.out.println(timelineLength);
		for (TimelineItem timelineItem : timelineItems) {
			this.timelineVBox.getChildren().add(nodeForPerspective(timelineItem.getAatributedPerspective(), timelineLength, timelineItem.getRelativeOffset()));
		}
	}

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {

	}
}
