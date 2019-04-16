package de.kobair.videodebrief.ui.events;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import com.sun.java.accessibility.util.java.awt.CheckboxTranslator;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.ui.dialogs.DialogFactory;
import de.kobair.videodebrief.ui.events.model.WorkspaceItem;
import de.kobair.videodebrief.ui.events.model.WorkspaceItem.EventItem;
import de.kobair.videodebrief.ui.events.model.WorkspaceItem.PerspectiveItem;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

public class EventsViewController implements Initializable {

	public interface EventsDelegate {

		public void createEvent(String name);

		public void renameEvent(Event event, String newName);

		public void deleteEvent(Event event, boolean keepFiles);

	}

	private ObservableList<TreeItem<WorkspaceItem>> events;
	private ReadOnlyObjectProperty<TreeItem<WorkspaceItem>> selectedWorkspaceItemProperty;
	private Optional<EventsDelegate> delegate = Optional.empty();

	@FXML
	private TreeView<WorkspaceItem> eventsTreeView;

	@FXML
	private void onCreateEventButtonPressed(ActionEvent event) {
		Optional<String> result = DialogFactory.namingDialog("Create Event", "New Event").showAndWait();
		result.ifPresent(name -> this.getDelegate().ifPresent(delegate -> delegate.createEvent(name)));
	}

	@FXML
	private void onRenameSelectedWorkspaceItem(ActionEvent event) {
		TreeItem<WorkspaceItem> treeItem = this.selectedWorkspaceItemProperty.get();
		if (treeItem != null) {
			WorkspaceItem item = treeItem.getValue();
			if (item != null) {
				Object content = item.getContent();
				if (content instanceof Event) {
					renameEvent((Event) content);
				} else if (content instanceof Perspective) {
					renamePerspective((Perspective) content);
				}
			}
		}
	}

	@FXML
	private void onExportSelectedWorkspaceItem(ActionEvent event) {
		TreeItem<WorkspaceItem> treeItem = this.selectedWorkspaceItemProperty.get();
		if (treeItem != null) {
			WorkspaceItem item = treeItem.getValue();
			if (item != null) {
				Object content = item.getContent();
				if (content instanceof Event) {
					exportEvent((Event) content);
				} else if (content instanceof Perspective) {
					exportPerspective((Perspective) content);
				}
			}
		}
	}

	@FXML
	private void onDeleteSelectedWorkspaceItem(ActionEvent event) {
		TreeItem<WorkspaceItem> treeItem = this.selectedWorkspaceItemProperty.get();
		if (treeItem != null) {
			WorkspaceItem item = treeItem.getValue();
			if (item != null) {
				Object content = item.getContent();
				if (content instanceof Event) {
					deleteEvent((Event) content);
				} else if (content instanceof Perspective) {
					deletePerspective((Perspective) content);
				}
			}
		}
	}

	private void onSelectWorkspaceItem(ObservableValue<? extends TreeItem<WorkspaceItem>> observable, TreeItem<WorkspaceItem> oldValue,
			TreeItem<WorkspaceItem> newValue) {
		if (newValue != null) {
			WorkspaceItem item = newValue.getValue();
		}
	}

	private void renameEvent(Event event) {
		Optional<String> result = DialogFactory.namingDialog("Rename Event", event.getName()).showAndWait();
		result.ifPresent(name -> this.getDelegate().ifPresent(delegate -> delegate.renameEvent(event, name)));
	}

	private void renamePerspective(Perspective perspective) {

	}

	private void deleteEvent(Event event) {
		String question = String.format("Do you really want to delete the event '%s'", event.getName());
		Optional<ButtonType> result = DialogFactory.confirmDeleteDialog("Delete Event", question).showAndWait();

		if (result.isPresent()) {
			ButtonBar.ButtonData type = result.get().getButtonData();
			boolean keepFiles;
			switch (type) {

			case YES:
				keepFiles = false;
				break;

			case OTHER:
				keepFiles = true;
				break;

			default:
				return;

			}

			this.delegate.ifPresent(delegate -> delegate.deleteEvent(event, keepFiles));
		}

	}

	private void deletePerspective(Perspective perspective) {

	}

	private void exportEvent(Event event) {

	}

	private void exportPerspective(Perspective perspective) {

	}

	private ContextMenu workspaceItemContextMenu() {
		MenuItem rename = new MenuItem("Rename");
		rename.setOnAction(event -> this.onRenameSelectedWorkspaceItem(event));

		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(event -> this.onDeleteSelectedWorkspaceItem(event));

		MenuItem export = new MenuItem("Export");
		export.setOnAction(event -> this.onExportSelectedWorkspaceItem(event));

		return new ContextMenu(rename, delete, new SeparatorMenuItem(), export);
	}
	
	private boolean canDrop(DragEvent event) {
		if (!event.getDragboard().hasFiles()) {
			return false;
		}
		
		if (event.getDragboard().getFiles().size() != 1) {
			return false;
		}
		
		File file = event.getDragboard().getFiles().get(0);
		if (!file.exists() || !file.isFile() || !file.canRead()) {
			return false;
		}

		for (FileFormat fileFormat : FileFormat.VIDEO_FORMATS) {
			boolean accepted = fileFormat.getFilenameFilter().accept(file.getParentFile(), file.getName());
			if (accepted) {
				return true;
			}
		}
		
		return false;
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

	public void setDelegate(final EventsDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

	public Optional<EventsDelegate> getDelegate() {
		return delegate;
	}
	
	public void handleDragOver(DragEvent event) {
		if (this.canDrop(event)) {
			event.acceptTransferModes(TransferMode.COPY);
		}
	}
	
	public void handleDrop(DragEvent event) {
		if (canDrop(event)) {
			Dragboard dragboard = event.getDragboard();
			File file = dragboard.getFiles().get(0);
			String perspectiveName = null;
			if (dragboard.hasString()) {
				perspectiveName = dragboard.getString();
			}
			
			System.out.println(file + "(" + perspectiveName + ")");
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.selectedWorkspaceItemProperty = this.eventsTreeView.getSelectionModel().selectedItemProperty();
		this.selectedWorkspaceItemProperty.addListener(this::onSelectWorkspaceItem);
		this.eventsTreeView.setRoot(new TreeItem<WorkspaceItem>());
		this.eventsTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
		this.eventsTreeView.setContextMenu(this.workspaceItemContextMenu());
		this.eventsTreeView.setCellFactory(new Callback<TreeView<WorkspaceItem>, TreeCell<WorkspaceItem>>(){
            @Override
            public TreeCell<WorkspaceItem> call(TreeView<WorkspaceItem> p) {
            	CheckBoxTreeCell<WorkspaceItem> result = new CheckBoxTreeCell<WorkspaceItem>();
            	// TODO: only for Events
            	result.setOnDragOver(EventsViewController.this::handleDragOver);
            	// TODO: Propagate workspace-event to handleDrop
            	result.setOnDragDropped(EventsViewController.this::handleDrop);
                return result;
            }
        });

		this.events = this.eventsTreeView.getRoot().getChildren();
	}
}
