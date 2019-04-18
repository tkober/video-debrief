package de.kobair.videodebrief.ui.generics;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public abstract class Controller implements Initializable {

	public <T> T loadViewIntoAnchorPane(AnchorPane anchorPane, String fxml, Class<T> controllerClass) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(controllerClass.getResource("view/" + fxml));
		try {
			Node view = loader.load();
			anchorPane.getChildren().add(view);
			AnchorPane.setTopAnchor(view, 0.0);
			AnchorPane.setLeftAnchor(view, 0.0);
			AnchorPane.setRightAnchor(view, 0.0);
			AnchorPane.setBottomAnchor(view, 0.0);

			return loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
