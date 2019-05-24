package de.kobair.videodebrief.ui.cameras.view;

import de.kobair.videodebrief.ui.cameras.model.Video;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class VideoCell extends ListCell<Video> {

	@FunctionalInterface
	public interface DragHandler {

		void handleOnDragDetected(ListCell<Video> cell);

	}

	private final ReadOnlyDoubleProperty listWidth;
	private final DragHandler dragHandler;

	public VideoCell(ReadOnlyDoubleProperty listWidth, DragHandler dragHandler) {
		this.listWidth = listWidth;
		this.dragHandler = dragHandler;
	}

	@Override
	protected void updateItem(final Video video, final boolean empty) {
		super.updateItem(video, empty);
		if(empty || video == null) {
			setText(null);
			setGraphic(null);
		} else {

			/* Title */
			HBox titleContainer = new HBox();

			// Name
			HBox nameContainer = new HBox();
			Label nameLabel = new Label();
			nameLabel.getStyleClass().add("title-label");
			nameLabel.textProperty().bind(video.getNameProperty());
			nameContainer.getChildren().add(nameLabel);
			nameContainer.setAlignment(Pos.TOP_LEFT);
			HBox.setHgrow(nameContainer, Priority.ALWAYS);

			// Size
			HBox sizeContainer = new HBox();
			Label sizeLabel = new Label();
			sizeLabel.getStyleClass().add("details-label");
			sizeLabel.textProperty().bind(video.getSizeProperty());
			sizeContainer.getChildren().add(sizeLabel);
			sizeContainer.setAlignment(Pos.TOP_RIGHT);

			titleContainer.getChildren().addAll(nameContainer, sizeContainer);

			/* Details */
			HBox detailsContainer = new HBox();

			// Timestamp
			HBox timestampContainer = new HBox();
			Label timestampLabel = new Label();
			timestampLabel.getStyleClass().add("details-label");
			timestampLabel.textProperty().bind(video.getTimestampProperty());
			timestampContainer.getChildren().add(timestampLabel);
			timestampContainer.setAlignment(Pos.CENTER_LEFT);
			HBox.setHgrow(timestampContainer, Priority.ALWAYS);

			// Format
			HBox formatContainer = new HBox();
			Label formatLabel = new Label();
			formatLabel.getStyleClass().add("details-label");
			formatLabel.textProperty().bind(video.getFormatNameProperty());
			formatContainer.getChildren().add(formatLabel);
			formatContainer.setAlignment(Pos.CENTER_RIGHT);

			detailsContainer.getChildren().addAll(timestampContainer, formatContainer);

			/* Content */
			VBox content = new VBox();
			content.getChildren().addAll(titleContainer, detailsContainer);
			this.setGraphic(content);
			this.setText(null);
			this.setOnDragDetected(event -> {
				this.dragHandler.handleOnDragDetected(this);
			});

			this.prefWidthProperty().bind(this.listWidth.subtract(20));
			this.setMaxWidth(Control.USE_PREF_SIZE);
		}
	}
}
