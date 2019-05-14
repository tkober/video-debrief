package de.kobair.videodebrief.ui.generics;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;

public abstract class Controller implements Initializable {

	public <T> T loadViewIntoAnchorPane(AnchorPane anchorPane, String fxml, Class<T> controllerClass) {
		Pair<Node, T> loaded = this.loadView(fxml, controllerClass);
		if (loaded == null) {
			return null;
		}
		Node view = loaded.getKey();
		T controller = loaded.getValue();
		anchorPane.getChildren().add(view);
		AnchorPane.setTopAnchor(view, 0.0);
		AnchorPane.setLeftAnchor(view, 0.0);
		AnchorPane.setRightAnchor(view, 0.0);
		AnchorPane.setBottomAnchor(view, 0.0);

		return controller;
	}

	public <T> Pair<Node, T> loadView(String fxml, Class<T> controllerClass) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(controllerClass.getResource("view/" + fxml));
		try {
			Node view = loader.load();
			T controller = loader.getController();

			return new Pair(view, controller);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
