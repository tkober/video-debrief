package de.kobair.videodebrief.ui.operations;

import java.net.URL;
import java.util.ResourceBundle;

import de.kobair.videodebrief.ui.operations.model.LongTermOperation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class OperationsViewController implements Initializable {

	public class OperationCell extends ListCell<LongTermOperation> {

		private final ReadOnlyDoubleProperty listWidth;

		public OperationCell(ReadOnlyDoubleProperty listWidth) {
			this.listWidth = listWidth;
		}

		@Override
		protected void updateItem(final LongTermOperation operation, final boolean empty) {
			super.updateItem(operation, empty);
			if(empty || operation == null) {
				setText(null);
				setGraphic(null);
			} else {
				/* Header */
				HBox header = new HBox();

				// Name
				Label nameLabel = new Label();
				nameLabel.textProperty().bind(operation.nameProperty);
				HBox nameContainer = new HBox();
				nameContainer.setAlignment(Pos.CENTER_LEFT);
				nameContainer.getChildren().add(nameLabel);
				header.getChildren().add(nameContainer);
				HBox.setHgrow(nameContainer, Priority.ALWAYS);

				// Steps
				if (operation.getTotalSteps() > 1) {
					Label stepLabel = new Label();
					String stepsFormat = "%d/" + operation.getTotalSteps();
					stepLabel.textProperty().bind(operation.stepProperty.asString(stepsFormat));
					HBox stepsContainer = new HBox();
					stepsContainer.setAlignment(Pos.CENTER_RIGHT);
					stepsContainer.getChildren().addAll(stepLabel);
					header.getChildren().add(stepsContainer);
				}

				/* Description */
				HBox description = new HBox();
				description.setAlignment(Pos.CENTER_LEFT);
				Label desciptionLabel = new Label();
				desciptionLabel.textProperty().bind(operation.descriptionProperty);
				description.getChildren().add(desciptionLabel);
				HBox.setHgrow(desciptionLabel, Priority.ALWAYS);

				/* Progress */
				HBox progress = new HBox();

				// Bar
				ProgressBar progressBar = new ProgressBar();
				progressBar.progressProperty().bind(operation.progressProperty);
				HBox progressBarContainer = new HBox();
				progressBarContainer.setAlignment(Pos.CENTER_LEFT);
				progressBarContainer.getChildren().add(progressBar);
				progress.getChildren().add(progressBarContainer);
				HBox.setHgrow(progressBarContainer, Priority.ALWAYS);

				// Label
				Label percentLabel = new Label();
				percentLabel.textProperty();
				operation.progressProperty.addListener((observable, oldValue, newValue) -> {
					int percent = (int) (newValue.doubleValue() * 100);
					if (percent >= 0) {
						percentLabel.setText(String.format("%d%%", percent));
					} else {
						percentLabel.setText(null);
					}
				});
				HBox percentContainer = new HBox();
				percentContainer.setAlignment(Pos.CENTER_RIGHT);
				percentContainer.getChildren().add(percentLabel);
				progress.getChildren().add(percentContainer);

				VBox content = new VBox();
				content.getChildren().addAll(header, description, progress);
				this.setGraphic(content);
				this.setText(null);

				this.prefWidthProperty().bind(this.listWidth.subtract(2));
				this.setMaxWidth(Control.USE_PREF_SIZE);
			}
		}
	}

	private ObservableList<LongTermOperation> operations = FXCollections.observableArrayList();

	public ObservableList<LongTermOperation> getOperations() {
		return operations;
	}

	@FXML
	private ListView<LongTermOperation> operationsListView;

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		this.operationsListView.setItems(operations);
		this.operationsListView.setCellFactory(listView -> new OperationCell(listView.widthProperty()));
	}

}
