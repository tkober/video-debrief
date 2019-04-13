package de.kobair.videodebrief.ui.workspacemanager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.WorkspaceManager;
import de.kobair.videodebrief.core.workspace.checks.LoadWorkspaceCheckResult;
import de.kobair.videodebrief.core.workspace.error.CreateWorkspaceException;
import de.kobair.videodebrief.core.workspace.error.LoadWorkspaceException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;
import de.kobair.videodebrief.ui.App;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class WorkspaceManagerController {
	
	private final String KNOWN_WORKSPACES_PREFERENCES_KEY = "known_workspaces";
	
	@FXML
	public ComboBox<String> workspaceComboBox;
	@FXML
	public Button browseExistingButton;
	@FXML
	public Button openButton;

	@FXML
	public Button createButton;
	@FXML
	public Button browseNewButton;
	@FXML
	public TextField nameTextField;
	@FXML
	public TextField parentDirectoryTextField;
	
	private final ObservableList<String> knownWorkspaceLocations;
	private final Preferences preferences;
	private final WorkspaceManager workspaceManager;
	
	private File parentDirectory = null;
	private String name = null;
	
	private App app;

	public WorkspaceManagerController() {
		this.knownWorkspaceLocations = FXCollections.observableArrayList();
		this.preferences = Preferences.userRoot().node(this.getClass().getName());
		this.workspaceManager = new WorkspaceManager();
	}
	
	public void setMainApp(App mainApp) {
		this.app = mainApp;
	}
	
	public void onOpenWorkspace(ActionEvent actionEvent) {
		String location = this.workspaceComboBox.getEditor().getText();
		try {
			Workspace workspace = this.workspaceManager.loadWorkspace(new File(location));
			this.openWorkspace(workspace);
		} catch (LoadWorkspaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownWorkspaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onCreateWorkspace(ActionEvent actionEvent) {
		try {
			Workspace workspace = this.workspaceManager.createWorkspace(this.parentDirectory, this.name);
			this.openWorkspace(workspace);
		} catch (CreateWorkspaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownWorkspaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onBrowseExisting(ActionEvent actionEvent) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Open Workspace");
		File chosen = chooser.showDialog(app.getPrimaryStage());
		if (chosen != null) {
			this.workspaceComboBox.getEditor().textProperty().set(chosen.getAbsolutePath());
		}
	}

	public void onBrowseNew(ActionEvent actionEvent) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Open Parent Directory");
		File chosen = chooser.showDialog(app.getPrimaryStage());
		if (chosen != null) {
			this.parentDirectoryTextField.setText(chosen.getAbsolutePath());
		}
	}
	
	@FXML
	private void initialize() {
		this.openButton.setDisable(true);
		this.createButton.setDisable(true);
		
		this.workspaceComboBox.setItems(this.knownWorkspaceLocations);
		this.workspaceComboBox.getEditor().textProperty().addListener(this::evaluateExistingWorkspaceLocation);
		
		this.nameTextField.textProperty().addListener(this::evaluateWorkspaceName);
		this.parentDirectoryTextField.textProperty().addListener(this::evaluateWorkspaceDirectory);
		
		this.loadKnownWorkspaces();
	}

	private void loadKnownWorkspaces() {
		String value = this.preferences.get(KNOWN_WORKSPACES_PREFERENCES_KEY, "");
		List<String> paths = Arrays.asList(value.split(","));
		
		paths = groomWorkspaceHistory(paths);
		knownWorkspaceLocations.clear();
		knownWorkspaceLocations.addAll(paths);
		this.saveWorkspaceHistory();
		
		if (paths.size() > 0) {
			this.workspaceComboBox.getEditor().textProperty().set(paths.get(0));
		}
	}
	
	private List<String> groomWorkspaceHistory(List<String> paths) {
		return paths.stream().filter((String path) -> {
			return this.workspaceManager.isWorkspace(new File(path)) == LoadWorkspaceCheckResult.OKAY;
		}).collect(Collectors.toList());
	}
	
	private void saveWorkspaceHistory() {
		String workspaceHistory = "";
		for (String location : this.knownWorkspaceLocations) {
			workspaceHistory += location + ",";
		}
		this.preferences.put(KNOWN_WORKSPACES_PREFERENCES_KEY, workspaceHistory);
	}

	private void evaluateExistingWorkspaceLocation(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		this.openButton.setDisable(newValue.length() == 0);
	}
	
	private void evaluateWorkspaceName(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		this.name = newValue.length() == 0 ? null : newValue;
		evaluateNewWorkspace();
	}
	
	private void evaluateWorkspaceDirectory(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		this.parentDirectory = newValue.length() == 0 ? null : new File(newValue);
		evaluateNewWorkspace();
	}
	
	private void evaluateNewWorkspace() {
		this.createButton.setDisable(this.name == null ||  this.parentDirectory == null);
	}
	
	private void openWorkspace(Workspace workspace) {
		String path = workspace.getWorkspaceDirectory().getAbsolutePath();
		this.knownWorkspaceLocations.remove(path);
		this.knownWorkspaceLocations.add(0, path);
		this.saveWorkspaceHistory();
		this.loadKnownWorkspaces();
		this.app.openWorkspace(workspace);
	}
}
