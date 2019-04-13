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
	
	private void showWorkspaceManager() {
		Stage stage = getPrimaryStage();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(WorkspaceManagerController.class.getResource("view/WorkspaceManager.fxml"));
		try {
			Scene scene = loader.load();
			stage.setScene(scene);
			
			WorkspaceManagerController controller = loader.getController();
			controller.setMainApp(this);
			
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void openWorkspace(Workspace workspace) {
		Stage stage = getPrimaryStage();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(WorkspaceController.class.getResource("view/Workspace.fxml"));
		
		try {
			Scene scene = loader.load();
			stage.setScene(scene);
			stage.setMaximized(true);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
