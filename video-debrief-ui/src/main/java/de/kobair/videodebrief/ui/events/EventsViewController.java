package de.kobair.videodebrief.ui.events;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.importing.ImportManager;
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

		public void importFile(Event event, File file, String perspectivename);
		
		public void renamePerspective(Event event, Perspective perspective, String newName);
		
		public void deletePerspective(Event event, Perspective perspective, boolean keepFiles);

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
					Event containingEvent = (Event) treeItem.getParent().getValue().getContent();
					renamePerspective(containingEvent, (Perspective) content);
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
					Event containingEvent = (Event) treeItem.getParent().getValue().getContent();
					exportPerspective(containingEvent, (Perspective) content);
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
					Event containingEvent = (Event) treeItem.getParent().getValue().getContent();
					deletePerspective(containingEvent, (Perspective) content);
				}
			}
		}
	}

	private void onSelectWorkspaceItem(ObservableValue<? extends TreeItem<WorkspaceItem>> observable,
			TreeItem<WorkspaceItem> oldValue, TreeItem<WorkspaceItem> newValue) {
		if (newValue != null) {
			WorkspaceItem item = newValue.getValue();
		}
	}

	private void renameEvent(Event event) {
		Optional<String> result = DialogFactory.namingDialog("Rename Event", event.getName()).showAndWait();
		result.ifPresent(name -> this.getDelegate().ifPresent(delegate -> delegate.renameEvent(event, name)));
	}

	private void renamePerspective(Event event, Perspective perspective) {
		Optional<String> result = DialogFactory.namingDialog("Rename Perspective", perspective.getName()).showAndWait();
		result.ifPresent(name -> this.getDelegate().ifPresent(delegate -> delegate.renamePerspective(event, perspective, name)));
	}

	private void deleteEvent(Event event) {
		String question = String.format("Do you really want to delete the event '%s'", event.getName());
		Optional<ButtonType> result = DialogFactory.confirmDeleteDialog("Delete Event", question).showAndWait();

		if (result.isPresent()) {
			boolean keepFiles = shouldFilesBeKept(result);
			this.delegate.ifPresent(delegate -> delegate.deleteEvent(event, keepFiles));
		}
	}

	private void deletePerspective(Event event, Perspective perspective) {
		String question = String.format("Do you really want to delete the perspective '%s'", perspective.getName());
		Optional<ButtonType> result = DialogFactory.confirmDeleteDialog("Delete Perspective", question).showAndWait();

		if (result.isPresent()) {
			boolean keepFiles = shouldFilesBeKept(result);
			this.delegate.ifPresent(delegate -> delegate.deletePerspective(event, perspective, keepFiles));
		}
	}

	private boolean shouldFilesBeKept(Optional<ButtonType> result) {
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
			return false;
	
		}
		return keepFiles;
	}

	private void exportEvent(Event event) {

	}

	private void exportPerspective(Event event, Perspective perspective) {

	}

	private void importVideoFile(Event event, File file, String perspectiveName) {
		Optional<String> result = DialogFactory.importMediaDialog(event, file, perspectiveName).showAndWait();
		result.ifPresent(name -> this.getDelegate().ifPresent(delegate -> delegate.importFile(event, file, name)));
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

		for (FileFormat fileFormat : ImportManager.IMPORTABLE_FORMATS) {
			boolean accepted = fileFormat.getFilenameFilter().accept(file.getParentFile(), file.getName());
			if (accepted) {
				return true;
			}
		}

		return false;
	}

	private void handleDragOver(DragEvent event) {
		if (this.canDrop(event)) {
			event.acceptTransferModes(TransferMode.COPY);
		}
	}

	private void handleDrop(DragEvent dragEvent, Event event) {
		if (canDrop(dragEvent)) {
			Dragboard dragboard = dragEvent.getDragboard();
			File file = dragboard.getFiles().get(0);
			String perspectiveName = null;
			if (dragboard.hasString()) {
				perspectiveName = dragboard.getString();
			}

			this.importVideoFile(event, file, perspectiveName);
		}
	}

	private void resetCell(TreeCell<WorkspaceItem> cell) {
		this.removeDragAndDropFromCell(cell);
		this.removeContextMenu(cell);
	}
	
	private void removeDragAndDropFromCell(TreeCell<WorkspaceItem> cell) {
		cell.setOnDragOver(null);
		cell.setOnDragDropped(null);
	}

	private void removeContextMenu(TreeCell<WorkspaceItem> cell) {
		cell.setContextMenu(null);
	}

	private void setUpDragAndDropForEventCell(TreeCell<WorkspaceItem> cell) {
		cell.setOnDragOver(EventsViewController.this::handleDragOver);
		Event event = (Event) cell.getItem().getContent();
		cell.setOnDragDropped(dropEvent -> this.handleDrop(dropEvent, event));
	}
	
	private ContextMenu eventCellContextMenu() {
		MenuItem rename = new MenuItem("Rename");
		rename.setOnAction(this::onRenameSelectedWorkspaceItem);
	
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(this::onDeleteSelectedWorkspaceItem);
	
		MenuItem export = new MenuItem("Export");
		export.setOnAction(this::onExportSelectedWorkspaceItem);
	
		return new ContextMenu(rename, delete, new SeparatorMenuItem(), export);
	}
	
	private ContextMenu perspectiveCellContextMenu() {
		MenuItem rename = new MenuItem("Rename");
		rename.setOnAction(this::onRenameSelectedWorkspaceItem);
	
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(this::onDeleteSelectedWorkspaceItem);
	
		MenuItem export = new MenuItem("Export");
		export.setOnAction(this::onExportSelectedWorkspaceItem);
	
		return new ContextMenu(rename, delete, new SeparatorMenuItem(), export);
	}

	private void setUpContextMenuForEventCell(TreeCell<WorkspaceItem> cell) {
		cell.setContextMenu(this.eventCellContextMenu());
	}

	private void setUpContextMenuForPerspectiveCell(TreeCell<WorkspaceItem> cell) {
		cell.setContextMenu(this.perspectiveCellContextMenu());
	}

	private void setUpEventCell(TreeCell<WorkspaceItem> cell) {
		this.setUpDragAndDropForEventCell(cell);
		this.setUpContextMenuForEventCell(cell);
	}
	
	private void setUpPerspectiveCell(TreeCell<WorkspaceItem> cell) {
		// TODO Drag and drop
		this.setUpContextMenuForPerspectiveCell(cell);
	}

	private void setUpCell(TreeCell<WorkspaceItem> cell) {
		if (cell.getItem() != null) {

			if (cell.getItem().getContent() instanceof Event) {
				this.setUpEventCell(cell);
			}

			else if (cell.getItem().getContent() instanceof Perspective) {
				this.setUpPerspectiveCell(cell);
			}
		}
	}

	private void setUpTreeCellFactory(TreeView<WorkspaceItem> treeView) {
		treeView.setCellFactory(new Callback<TreeView<WorkspaceItem>, TreeCell<WorkspaceItem>>() {
			@Override
			public TreeCell<WorkspaceItem> call(TreeView<WorkspaceItem> p) {
				CheckBoxTreeCell<WorkspaceItem> cell = new CheckBoxTreeCell<WorkspaceItem>();

				cell.itemProperty().addListener((obs, old, item) -> {
					EventsViewController.this.resetCell(cell);
					EventsViewController.this.setUpCell(cell);
				});
				return cell;
			}
		});
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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.selectedWorkspaceItemProperty = this.eventsTreeView.getSelectionModel().selectedItemProperty();
		this.selectedWorkspaceItemProperty.addListener(this::onSelectWorkspaceItem);
		this.eventsTreeView.setRoot(new TreeItem<WorkspaceItem>());
		this.eventsTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
		this.setUpTreeCellFactory(this.eventsTreeView);

		this.events = this.eventsTreeView.getRoot().getChildren();
	}
}
