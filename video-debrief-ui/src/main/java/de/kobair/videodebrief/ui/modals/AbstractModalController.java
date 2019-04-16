package de.kobair.videodebrief.ui.modals;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public abstract class AbstractModalController implements Initializable {

	@FXML
	protected Button confirmButton; 
	
	@FXML
	protected abstract void onConfirm(ActionEvent event);
	
}
