package de.kobair.videodebrief.ui.workspace;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;
import de.kobair.videodebrief.ui.cameras.CamerasViewController;
import de.kobair.videodebrief.ui.events.EventsViewController;
import de.kobair.videodebrief.ui.events.EventsViewController.EventsDelegate;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class WorkspaceController implements Initializable, EventsDelegate {
	
	@FXML
	public AnchorPane camerasAnchorPane;
	@FXML
	public AnchorPane eventsAnchorPane;
	
	private Workspace workspace;
	
	private EventsViewController evnetEventsViewController;
	
	private void updateEventsView() {
		try {
			Map<Event, List<Perspective>> result = new HashMap<Event, List<Perspective>>();
			for (Event event : this.workspace.getEvents()) {
				result.put(event, workspace.getPerspectives(event));
			}
			this.evnetEventsViewController.reload(result);
		} catch(UnknownWorkspaceException e) {
			e.printStackTrace();
		}
	}
	
	private <T> T loadViewIntoAnchorPane(AnchorPane anchorPane, String fxml, Class<T> controllerClass) {
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
	
	private CamerasViewController loadCamerasView(AnchorPane anchorPane) {
		return this.loadViewIntoAnchorPane(anchorPane, "Cameras.fxml", CamerasViewController.class);
	}
	
	private EventsViewController loadEventsView(AnchorPane anchorPane) {
		return this.loadViewIntoAnchorPane(anchorPane, "Events.fxml", EventsViewController.class);
	}
	
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		this.updateEventsView();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loadCamerasView(camerasAnchorPane);
		this.evnetEventsViewController = this.loadEventsView(eventsAnchorPane);
	}
	
}
