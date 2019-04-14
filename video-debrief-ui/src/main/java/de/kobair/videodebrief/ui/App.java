package de.kobair.videodebrief.ui;
	
import java.io.IOException;

import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.ui.workspace.WorkspaceController;
import de.kobair.videodebrief.ui.workspacemanager.WorkspaceManagerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {
	
	private Stage primaryStage;
	
	private <T> T loadScene(Stage stage, String fxml, Class<T> controllerClass) {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(controllerClass.getResource("view/" + fxml));
		try {
			Scene scene = loader.load();
			stage.setScene(scene);
			return loader.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void showWorkspaceManager() {
		Stage stage = getPrimaryStage();
		WorkspaceManagerController controller = this.loadScene(stage, "WorkspaceManager.fxml", WorkspaceManagerController.class);
		controller.setMainApp(this);
		stage.show();
	}
	
	public void openWorkspace(Workspace workspace) {
		Stage stage = getPrimaryStage();
		WorkspaceController controller = this.loadScene(stage, "Workspace.fxml", WorkspaceController.class);
		controller.setWorkspace(workspace);
//		stage.setMaximized(true);
		stage.show();
	}
	
	@Override
	public void start(Stage primaryStage) {
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
