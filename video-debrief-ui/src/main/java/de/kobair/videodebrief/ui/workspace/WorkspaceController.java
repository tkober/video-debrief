package de.kobair.videodebrief.ui.workspace;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.kobair.videodebrief.ui.cameras.CamerasViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;

public class WorkspaceController implements Initializable {
	
	@FXML
	public AnchorPane camerasAnchorPane;
	
	public WorkspaceController() {
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(CamerasViewController.class.getResource("view/Cameras.fxml"));
		try {
			SplitPane camerasView = loader.load();
			CamerasViewController camerasViewController = loader.getController();
			this.camerasAnchorPane.getChildren().add(camerasView);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
