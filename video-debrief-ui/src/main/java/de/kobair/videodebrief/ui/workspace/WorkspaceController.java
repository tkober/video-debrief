package de.kobair.videodebrief.ui.workspace;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.export.ExportException;
import de.kobair.videodebrief.core.export.ExportManager;
import de.kobair.videodebrief.core.export.LocalExportManager;
import de.kobair.videodebrief.core.export.UnknwonExportException;
import de.kobair.videodebrief.core.importing.ImportManager;
import de.kobair.videodebrief.core.importing.LocalImportManager;
import de.kobair.videodebrief.core.importing.error.ImportException;
import de.kobair.videodebrief.core.importing.error.UnknownImportException;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.AddPerspectiveException;
import de.kobair.videodebrief.core.workspace.error.ChangeInOutPointException;
import de.kobair.videodebrief.core.workspace.error.CreateEventException;
import de.kobair.videodebrief.core.workspace.error.RenameEventException;
import de.kobair.videodebrief.core.workspace.error.RenamePerspectiveException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;
import de.kobair.videodebrief.ui.App;
import de.kobair.videodebrief.ui.cameras.CamerasViewController;
import de.kobair.videodebrief.ui.dialogs.DialogFactory;
import de.kobair.videodebrief.ui.errors.ApplicationError;
import de.kobair.videodebrief.ui.errors.ApplicationWarning;
import de.kobair.videodebrief.ui.events.EventsViewController;
import de.kobair.videodebrief.ui.events.EventsViewController.EventsDelegate;
import de.kobair.videodebrief.ui.generics.Controller;
import de.kobair.videodebrief.ui.playback.PlaybackController;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;
import org.apache.tools.ant.taskdefs.Local;

public class WorkspaceController extends Controller implements EventsDelegate, PlaybackController.PlaybackDelegate {

	public static final String DEFAULT_SNAPSHOT_FORMAT = ".jpg";
	@FXML
	private AnchorPane camerasAnchorPane;
	@FXML
	private AnchorPane eventsAnchorPane;
	@FXML
	private AnchorPane playbackAnchorPane;

	private final ImportManager importManager = new LocalImportManager();
	private final ExportManager exportManager = new LocalExportManager();

	private Workspace workspace;
	private EventsViewController evnetEventsViewController;
	private PlaybackController playbackViewController;
	private App app;

	private void updateEventsView() {
		try {
			Map<Event, List<Perspective>> result = new HashMap<Event, List<Perspective>>();
			for (Event event : this.workspace.getEvents()) {
				result.put(event, workspace.getPerspectives(event));
			}
			this.evnetEventsViewController.reload(result);
		} catch (UnknownWorkspaceException e) {
			e.printStackTrace();
		}
	}

	private void updatePlayback() {
		Event playbackEvent = this.playbackViewController.getEvent();
		Perspective playbackPerspective = this.playbackViewController.getPerspective();

		if (playbackEvent == null || playbackPerspective == null) {
			return;
		}

		if (this.workspace.containsEventWithName(playbackEvent.getName())) {
			Event event = this.workspace.getEventByName(playbackEvent.getName());
			try {
				if (workspace.containsPerspectiveWithName(event, playbackPerspective.getName())) {
					Perspective perspective = this.workspace.getPerspectiveByName(event, playbackPerspective.getName());
					this.updatePlaybackMedia(event, perspective);
					return;
				}
			} catch (UnknownWorkspaceException e) {
				// Should never happen
				throw new RuntimeException(e);
			}
		}
		this.clearPlaybackMedia();
	}

	private void updatePlaybackMedia(Event event, Perspective perspective) {
		try {
			this.playbackViewController.updateSelectedMedia(this.workspace, event, perspective);
		} catch (UnknownWorkspaceException | IOException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	private void clearPlaybackMedia() {
		this.playbackViewController.clearSelectedMedia();
	}

	private void workspaceChanged() {
		updateEventsView();
		updatePlayback();
	}

	private CamerasViewController loadCamerasView(AnchorPane anchorPane) {
		return this.loadViewIntoAnchorPane(anchorPane, "Cameras.fxml", CamerasViewController.class);
	}

	private EventsViewController loadEventsView(AnchorPane anchorPane) {
		return this.loadViewIntoAnchorPane(anchorPane, "Events.fxml", EventsViewController.class);
	}
	
	private PlaybackController loadPlaybackView(AnchorPane anchorPane) {
		return this.loadViewIntoAnchorPane(anchorPane, "Playback.fxml", PlaybackController.class);
	}

	private void exportWorkspace(List<ExportManager.ExportDescriptor> exportDescriptors, String placeholder) {
		String workspaceName = placeholder !=  null ? placeholder  : this.workspace.getWorkspaceDirectory().getName();
		Optional<String> result = DialogFactory.namingDialog("Export content", workspaceName).showAndWait();
		if (result.isPresent()) {
			String exportName = result.get();

			File exportDirectory = LocalUtils.extendDirectory(this.workspace.getWorkspaceDirectory(), this.workspace.getExportDirectory());
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setInitialDirectory(exportDirectory);
			chooser.setTitle(String.format("Pick destination for '%s'", exportName));
			File chosen = chooser.showDialog(this.getApp().getPrimaryStage());

			if (chosen != null) {
				File targetDirectory = LocalUtils.extendDirectory(chosen, exportName);
				try {
					this.exportManager.exportWorkspace(exportDescriptors, targetDirectory);
				} catch (ExportException e) {
					new ApplicationWarning(e).throwOnMainThread();
				} catch (UnknwonExportException | UnknownWorkspaceException e) {
					new ApplicationError(e).throwOnMainThread();
				}
			}
		}
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		this.workspaceChanged();
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loadCamerasView(camerasAnchorPane);
		
		this.evnetEventsViewController = this.loadEventsView(eventsAnchorPane);
		this.evnetEventsViewController.setDelegate(this);
		
		this.playbackViewController = this.loadPlaybackView(playbackAnchorPane);
		this.playbackViewController.setDelegate(this);
	}

	@Override
	public void createEvent(final String name) {
		try {
			this.workspace.createEvent(name);
			this.workspaceChanged();
		} catch (CreateEventException e) {
			new ApplicationWarning(e).throwOnMainThread();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void renameEvent(final Event event, final String newName) {
		try {
			this.workspace.renameEvent(event, newName);
			this.workspaceChanged();
		} catch (RenameEventException e) {
			new ApplicationWarning(e).throwOnMainThread();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void deleteEvent(final Event event, final boolean keepFiles) {
		Workspace.DeletionStrategy strategy = keepFiles ? Workspace.DeletionStrategy.KEEP_FILES : Workspace.DeletionStrategy.DELETE_FILES;
		try {
			this.workspace.deleteEvent(event, strategy);
			this.workspaceChanged();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void importFile(final Event event, final File file, final String perspectivename) {
		try {
			this.importManager.importFile(this.workspace, file, event, perspectivename, null);
			this.workspaceChanged();
		} catch (ImportException | AddPerspectiveException e) {
			new ApplicationWarning(e).throwOnMainThread();
		} catch (UnknownWorkspaceException | UnknownImportException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void renamePerspective(Event event, Perspective perspective, String newName) {
		try {
			this.workspace.renamePerspective(event, perspective, newName);
			this.workspaceChanged();
		} catch (RenamePerspectiveException e) {
			new ApplicationWarning(e).throwOnMainThread();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void deletePerspective(Event event, Perspective perspective, boolean keepFiles) {
		Workspace.DeletionStrategy strategy = keepFiles ? Workspace.DeletionStrategy.KEEP_FILES : Workspace.DeletionStrategy.DELETE_FILES;
		try {
			this.workspace.deletePerspective(event, perspective, strategy);
			this.workspaceChanged();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void movePerspective(Event oldEvent, Event newEvent, Perspective perspective) {
		System.out.println("TODO: movePerspective()");
		System.out.println(String.format("(%s):\t(%s) -> (%s)", perspective.getName(), oldEvent.getName(), newEvent.getName()));
	}

	@Override
	public void showPerspective(Event event, Perspective perspective) {
		try {
			this.playbackViewController.setSelectedMedia(this.workspace, event, perspective);
		} catch (UnknownWorkspaceException | IOException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void exportWorkspaceParts(List<Pair<Event, Perspective>> parts, String placeholder) {
		List<ExportManager.ExportDescriptor> exportDescriptors = parts.stream()
				.map(part -> new ExportManager.ExportDescriptor(this.workspace, part.getKey(), part.getValue()))
				.collect(Collectors.toList());
		this.exportWorkspace(exportDescriptors, placeholder);
	}

	@Override
	public void playWithSystemPlayer(Event event, Perspective perspective) {
		File videoFile = LocalUtils.extendDirectory(this.workspace.getWorkspaceDirectory(), event.getSubPath(), perspective.getFileName());
		try {
			Desktop.getDesktop().open(videoFile);
		} catch (IOException | IllegalArgumentException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void setInPoint(final Event event, final Perspective perspective, final long inPoint) {
		try {
			this.workspace.changeInPointForPerspective(event, perspective, inPoint);
			this.workspaceChanged();
		} catch (ChangeInOutPointException e) {
			new ApplicationWarning(e).throwOnMainThread();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void setOutPoint(final Event event, final Perspective perspective, final long outPoint) {
		try {
			this.workspace.changeOutPointForPerspective(event, perspective, outPoint);
			this.workspaceChanged();
		} catch (ChangeInOutPointException e) {
			new ApplicationWarning(e).throwOnMainThread();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void exportSnapshot(final Event event, final Perspective perspective, final long timeMillis) {
		ExportManager.ExportDescriptor descriptor = new ExportManager.ExportDescriptor(this.workspace, event, perspective);

		String placeholder = event.getName() + "_" + perspective.getName();
		Optional<String> result = DialogFactory.namingDialog("Export Snapshot", placeholder).showAndWait();

		if (result.isPresent()) {
			String fileName = result.get().trim() + DEFAULT_SNAPSHOT_FORMAT;
			File exportFile = LocalUtils.extendDirectory(workspace.getWorkspaceDirectory(), workspace.getExportDirectory(), fileName);
			try {
				this.exportManager.exportSnapshot(descriptor, timeMillis, exportFile);
			} catch (ExportException e) {
				new ApplicationWarning(e).throwOnMainThread();
			} catch (UnknwonExportException | UnknownWorkspaceException e) {
				new ApplicationError(e).throwOnMainThread();
			}
		}
	}

	@Override
	public void exportClips(Event event, List<Clip> clips) {
		List<ExportManager.ClipDescriptor> clipDescriptors = clips.stream()
				.map(c -> new ExportManager.ClipDescriptor(this.workspace, event, c.getPerspective(), c.getBegin(), c.getEnd() - c.getBegin()))
				.collect(Collectors.toList());

		String placeholder = event.getName() + "_Clip";
		Optional<String> result = DialogFactory.namingDialog("Export Clip", placeholder).showAndWait();

		if (result.isPresent()) {
			String dirName = result.get().trim();
			File exportDirectory = LocalUtils.extendDirectory(workspace.getWorkspaceDirectory(), workspace.getExportDirectory(), dirName);
			try {
				this.exportManager.exportClip(clipDescriptors, exportDirectory);
			} catch (ExportException e) {
				new ApplicationWarning(e).throwOnMainThread();
			} catch (UnknwonExportException | UnknownWorkspaceException e) {
				new ApplicationError(e).throwOnMainThread();
			}
		}
	}

	@Override
	public void setAlignmentPoint(final Event event, final Perspective perspective, final long timeMillis) {
		try {
			this.workspace.changeAlignmentPointForPerspective(event, perspective, timeMillis);
			this.workspaceChanged();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}
}
