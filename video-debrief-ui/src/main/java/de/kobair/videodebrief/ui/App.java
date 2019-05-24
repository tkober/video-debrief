package de.kobair.videodebrief.ui;

import java.io.IOException;

import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.ui.alerts.AlertFactory;
import de.kobair.videodebrief.ui.errors.ApplicationException;
import de.kobair.videodebrief.ui.errors.ApplicationFatal;
import de.kobair.videodebrief.ui.generics.LoadedController;
import de.kobair.videodebrief.ui.style.Styles;
import de.kobair.videodebrief.ui.workspace.WorkspaceController;
import de.kobair.videodebrief.ui.workspacemanager.WorkspaceManagerController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Pair;

public class App extends Application {
	
	private Stage primaryStage;
	
	private <T> LoadedController<Scene, T> loadScene(String fxml, Class<T> controllerClass) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(controllerClass.getResource("view/" + fxml));
		try {
			Scene scene = loader.load();
			return new LoadedController<>(scene, loader.getController());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void handleApplicationException(ApplicationException e) {
		Alert alert = AlertFactory.alertForApplicationException(e);
		alert.showAndWait();
	}

	private void onUncaughtException(Thread t, Throwable e) {
		if (e instanceof ApplicationException) {
			handleApplicationException((ApplicationException) e);
			if (e instanceof ApplicationFatal) {
				Platform.exit();
			}
		} else {
			e.printStackTrace();
		}
	}
	
	private void showWorkspaceManager() {
		Stage stage = getPrimaryStage();
		LoadedController<Scene, WorkspaceManagerController> loaded = this.loadScene("WorkspaceManager.fxml", WorkspaceManagerController.class);
		Scene scene = loaded.getUi();
		WorkspaceManagerController controller = loaded.getController();
		controller.setMainApp(this);
		stage.setScene(scene);
		stage.show();
	}
	
	public void openWorkspace(Workspace workspace) {
		Stage stage = getPrimaryStage();
		LoadedController<Scene, WorkspaceController> loaded = this.loadScene("Workspace.fxml", WorkspaceController.class);
		Scene scene = loaded.getUi();
		WorkspaceController controller = loaded.getController();
		Styles.applyStyle(scene, Styles.ColorTheme.DARK);
		controller.setWorkspace(workspace);
		stage.setScene(scene);
		stage.show();
		controller.setApp(this);
	}
	
	@Override
	public void start(Stage primaryStage) {
		Thread.setDefaultUncaughtExceptionHandler(this::onUncaughtException);
		this.primaryStage = primaryStage;
		showWorkspaceManager();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public Stage getPrimaryStage() {
		return primaryStage;
	}
}
