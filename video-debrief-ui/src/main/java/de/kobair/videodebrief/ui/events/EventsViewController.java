package de.kobair.videodebrief.ui.events;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.ui.events.model.WorkspaceItem;
import de.kobair.videodebrief.ui.events.model.WorkspaceItem.EventItem;
import de.kobair.videodebrief.ui.events.model.WorkspaceItem.PerspectiveItem;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;

public class EventsViewController implements Initializable {

	public interface EventsDelegate {

	}

	private ObservableList<TreeItem<WorkspaceItem>> events;
	private ReadOnlyObjectProperty<TreeItem<WorkspaceItem>> selectedWorkspaceItemProperty;
	
	@FXML
	private TreeView<WorkspaceItem> eventsTreeView;

	@FXML
	private void onCreateEventButtonPressed(ActionEvent event) {
		System.out.println(event);
		throw new RuntimeException("test");
	}
	
	private void onSelectWorkspaceItem(ObservableValue<? extends TreeItem<WorkspaceItem>> observable, TreeItem<WorkspaceItem> oldValue,
			TreeItem<WorkspaceItem> newValue) {
		WorkspaceItem item = newValue.getValue();
		System.out.println(item);
	}

	public void reload(Map<Event, List<Perspective>> content) {
		this.events.clear();
		List<TreeItem<WorkspaceItem>> items = new ArrayList<>();
		for (Event event : content.keySet()) {
			EventItem eventItem = new EventItem(event);
			TreeItem<WorkspaceItem> eventTreeItem = new TreeItem<WorkspaceItem>(eventItem);

			List<Perspective> perspectives = content.get(event);
			for (Perspective perspective : perspectives) {
				PerspectiveItem perspectiveItem = new PerspectiveItem(perspective);
				TreeItem<WorkspaceItem> perspectiveTreeItem = new TreeItem<WorkspaceItem>(perspectiveItem);

				eventTreeItem.getChildren().add(perspectiveTreeItem);
			}
			items.add(eventTreeItem);
		}
		items.sort(new Comparator<TreeItem<WorkspaceItem>>() {

			@Override
			public int compare(TreeItem<WorkspaceItem> a, TreeItem<WorkspaceItem> b) {
				return a.getValue().toString().compareTo(b.getValue().toString());
			}
		});
		this.events.addAll(items);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.selectedWorkspaceItemProperty = this.eventsTreeView.getSelectionModel().selectedItemProperty();
		this.selectedWorkspaceItemProperty.addListener(this::onSelectWorkspaceItem);
		this.eventsTreeView.setRoot(new TreeItem<WorkspaceItem>());
		this.eventsTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
		
		this.events = this.eventsTreeView.getRoot().getChildren();
	}
}
