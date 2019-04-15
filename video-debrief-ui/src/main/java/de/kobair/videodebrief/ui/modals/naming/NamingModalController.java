package de.kobair.videodebrief.ui.modals.naming;

import java.net.URL;
import java.util.ResourceBundle;

import de.kobair.videodebrief.ui.modals.AbstractModalController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class NamingModalController extends AbstractModalController {

	@FXML
	protected Label captionLabel;
	@FXML
	protected TextField nameTextField;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}

	@Override
	protected void onConfirm(ActionEvent event) {

	}

}
