package de.kobair.videodebrief.ui.operations;

import java.net.URL;
import java.util.ResourceBundle;

import de.kobair.videodebrief.ui.operations.model.LongTermOperation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class OperationsViewController implements Initializable {

	private ObservableList<LongTermOperation> operations = FXCollections.observableArrayList();

	public ObservableList<LongTermOperation> getOperations() {
		return operations;
	}

	@FXML
	private ListView<LongTermOperation> operationsListView;

	@Override
	public void initialize(final URL location, final ResourceBundle resources) {
		this.operationsListView.setItems(operations);
	}

}
