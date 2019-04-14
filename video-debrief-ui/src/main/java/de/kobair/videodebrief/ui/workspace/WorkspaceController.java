package de.kobair.videodebrief.ui.workspace;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import de.kobair.videodebrief.ui.cameras.CamerasViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class WorkspaceController implements Initializable {
	
	@FXML
	public AnchorPane camerasAnchorPane;
	@FXML
	public AnchorPane eventsAnchorPane;
	
	private <T> T loadViewIntoAnchorPane(AnchorPane anchorPane, String fxml, Class<T> controllerClass) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(controllerClass.getResource("view/"+fxml));
		try {
			Node view = loader.load();
			this.camerasAnchorPane.getChildren().add(view);
			
			return loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private CamerasViewController loadCamerasView(AnchorPane anchorPane) {
		return this.loadViewIntoAnchorPane(anchorPane, "Cameras.fxml", CamerasViewController.class);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loadCamerasView(camerasAnchorPane);
	}
	
}
