package de.kobair.videodebrief.ui.generics;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public abstract class Controller implements Initializable {

	public <T> T loadViewIntoAnchorPane(AnchorPane anchorPane, String fxml, Class<T> controllerClass) {
		return loadViewIntoAnchorPane(anchorPane, fxml, controllerClass, Insets.EMPTY);
	}

	public <T> T loadViewIntoAnchorPane(AnchorPane anchorPane, String fxml, Class<T> controllerClass, Insets insets) {
		LoadedController<Node, T> loaded = this.loadView(fxml, controllerClass);
		if (loaded == null) {
			return null;
		}
		Node view = loaded.getUi();
		T controller = loaded.getController();
		anchorPane.getChildren().add(view);
		AnchorPane.setTopAnchor(view, insets.getTop());
		AnchorPane.setLeftAnchor(view, insets.getLeft());
		AnchorPane.setRightAnchor(view, insets.getRight());
		AnchorPane.setBottomAnchor(view, insets.getBottom());

		return controller;
	}

	public <T> LoadedController<Node, T> loadView(String fxml, Class<T> controllerClass) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(controllerClass.getResource("view/" + fxml));
		try {
			Node view = loader.load();
			T controller = loader.getController();

			return new LoadedController<>(view, controller);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
