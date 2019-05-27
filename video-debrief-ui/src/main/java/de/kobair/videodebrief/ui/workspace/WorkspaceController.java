package de.kobair.videodebrief.ui.workspace;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.export.ExportException;
import de.kobair.videodebrief.core.export.ExportManager;
import de.kobair.videodebrief.core.export.LocalExportManager;
import de.kobair.videodebrief.core.export.UnknwonExportException;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.importing.ImportManager;
import de.kobair.videodebrief.core.importing.LocalImportManager;
import de.kobair.videodebrief.core.importing.error.ImportException;
import de.kobair.videodebrief.core.importing.error.UnknownImportException;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.video.VideoHandler;
import de.kobair.videodebrief.core.video.VideoInformation;
import de.kobair.videodebrief.core.video.ffmpeg.FfmpegVideoHandler;
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
import de.kobair.videodebrief.ui.generics.LoadedController;
import de.kobair.videodebrief.ui.operations.OperationsViewController;
import de.kobair.videodebrief.ui.playback.PlaybackController;
import de.kobair.videodebrief.ui.operations.model.LongTermOperation;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class WorkspaceController extends Controller implements Workspace.WorkspaceDelegate, EventsDelegate, PlaybackController.PlaybackDelegate {

	public static final String DEFAULT_SNAPSHOT_FORMAT = ".jpg";
	public static final String LAST_SAVED_DATE_FORMAT = "dd. MMMM yyyy 'at' HH:mm";

	@FXML
	private AnchorPane camerasAnchorPane;
	@FXML
	private AnchorPane eventsAnchorPane;
	@FXML
	private AnchorPane playbackAnchorPane;
	@FXML
	private Label lastSavedLabel;
	@FXML
	private Label operationsStatusLabel;
	@FXML
	private ProgressBar operationsProgressBar;

	private final ImportManager importManager = new LocalImportManager();
	private final ExportManager exportManager = new LocalExportManager();
	private final VideoHandler videoHandler = new FfmpegVideoHandler();

	private Workspace workspace;
	private EventsViewController evnetEventsViewController;
	private PlaybackController playbackViewController;
	private App app;
	private ExecutorService longTermOperationsService = Executors.newCachedThreadPool();
	private ObservableList<LongTermOperation> runningLongTermOperations;
	private Stage operationsStage;
	private OperationsViewController operationsViewController;

	@FXML
	private void showHideOperationsList(MouseEvent event) {
		if (this.operationsStage.isShowing()) {
			this.operationsStage.hide();
		} else {
			this.operationsStage.show();
		}
	}

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
		return this.loadViewIntoAnchorPane(anchorPane, "Playback.fxml", PlaybackController.class, new Insets(-1, -1, -1, -1));
	}

	private void loadOperationsView() {
		LoadedController<Node, OperationsViewController> loaded = this.loadView("Operations.fxml", OperationsViewController.class);
		Parent view = (Parent) loaded.getUi();
		Scene scene = new Scene(view);
		this.operationsViewController = loaded.getController();
		this.operationsStage = new Stage();
		this.operationsStage.setScene(scene);
		this.operationsStage.setTitle("Running Operations");

		this.runningLongTermOperations = this.operationsViewController.getOperations();
	}

	private void exportWorkspace(List<ExportManager.ExportDescriptor> exportDescriptors, String placeholder) {
		String workspaceName = placeholder != null ? placeholder : this.workspace.getWorkspaceDirectory().getName();
		Optional<String> result = DialogFactory.namingDialog("Export content", workspaceName).showAndWait();
		if (result.isPresent()) {
			String exportName = result.get();

			File exportDirectory = LocalUtils.extendDirectory(this.workspace.getWorkspaceDirectory(), this.workspace.getExportDirectory());
			if (!exportDirectory.exists()) {
				exportDirectory = this.workspace.getWorkspaceDirectory();
			}
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setInitialDirectory(exportDirectory);
			chooser.setTitle(String.format("Pick destination for '%s'", exportName));
			File chosen = chooser.showDialog(this.getApp().getPrimaryStage());

			if (chosen != null) {
				this.longTermOperationsService.execute(() -> {
					LongTermOperation operation = new LongTermOperation("Export Workspace", "Preparing", 0, 1, exportDescriptors.size());
					this.pushLongTermOperation(operation);

					File targetDirectory = LocalUtils.extendDirectory(chosen, exportName);
					try {
						this.exportManager.exportWorkspace(exportDescriptors, targetDirectory, Optional.of(operation));
					} catch (ExportException e) {
						new ApplicationWarning(e).throwOnMainThread();
					} catch (UnknwonExportException | UnknownWorkspaceException e) {
						new ApplicationError(e).throwOnMainThread();
					} finally {
						this.popLongTermOperation(operation);
					}
				});
			}
		}
	}

	private void updateLastSavedLabel() {
		File workspaceDirectory = this.workspace.getWorkspaceDirectory();
		File workspaceFile = LocalUtils.extendDirectory(workspaceDirectory, FileFormat.WORKSPACE.getFileExtensions().get(0));
		long lastModifiedMillis = workspaceFile.lastModified();
		Date lastModifiedDate = new Date(lastModifiedMillis);

		String lastSavedText = DateFormatUtils.format(lastModifiedDate, LAST_SAVED_DATE_FORMAT);
		this.lastSavedLabel.setText(lastSavedText);
	}

	private void updateoOerationsStatus() {
		Platform.runLater(() -> {
			int numberOfOperations = this.runningLongTermOperations.size();

			if (numberOfOperations == 0) {
				operationsProgressBar.setProgress(0);
				operationsProgressBar.setDisable(true);
				this.operationsStatusLabel.setText("No jobs running");
			} else if (numberOfOperations == 1) {
				operationsProgressBar.setProgress(-1);
				operationsProgressBar.setDisable(false);
				this.operationsStatusLabel.setText("1 job running");
			} else {
				operationsProgressBar.setProgress(-1);
				operationsProgressBar.setDisable(false);
				this.operationsStatusLabel.setText(String.format("%d jobs running", numberOfOperations));
			}
		});
	}

	private synchronized void pushLongTermOperation(LongTermOperation operation) {
		this.runningLongTermOperations.add(operation);
	}

	private synchronized void removeLongTermOperation(LongTermOperation operation) {
		if (this.runningLongTermOperations.contains(operation)) {
			this.runningLongTermOperations.remove(operation);
		}
	}

	private void popLongTermOperation(LongTermOperation operation) {
		Platform.runLater(() -> {
			this.removeLongTermOperation(operation);
		});
	}

	private void updateStageTitle(Workspace workspace) {
		this.updateStageTitle(workspace, null);
	}

	private void updateStageTitle(Workspace workspace, Event event) {
		String workspaceName = workspace.getWorkspaceDirectory().getName();
		String workspacePath = workspace.getWorkspaceDirectory().getAbsolutePath();
		String title = String.format("%s [%s]", workspaceName, workspacePath);
		if (event != null) {
			title += String.format(" - %s", event.getName());
		}
		this.app.getPrimaryStage().setTitle(title);
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		this.workspace.setDelegate(this);
		this.updateLastSavedLabel();
		this.workspaceChanged();
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
		Scene scene = app.getPrimaryStage().getScene();
		this.playbackViewController.setScene(scene);
		this.updateStageTitle(workspace);
	}

	@Override
	public void workspaceSaved(final Workspace workspace) {
		Platform.runLater(() -> {
			this.updateLastSavedLabel();
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loadCamerasView(camerasAnchorPane);

		this.evnetEventsViewController = this.loadEventsView(eventsAnchorPane);
		this.evnetEventsViewController.setDelegate(this);

		this.playbackViewController = this.loadPlaybackView(playbackAnchorPane);
		this.playbackViewController.setDelegate(this);

		this.loadOperationsView();

		runningLongTermOperations.addListener(new ListChangeListener<Object>() {
			@Override
			public void onChanged(final Change<?> c) {
				WorkspaceController.this.updateoOerationsStatus();
			}
		});
		this.updateoOerationsStatus();
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
		this.longTermOperationsService.execute(() -> {
			LongTermOperation operation = new LongTermOperation("Import Perspective", "Preparing");
			this.pushLongTermOperation(operation);
			try {
				this.importManager.importFile(WorkspaceController.this.workspace, file, event, perspectivename, Optional.of(operation));
				Platform.runLater(() -> this.workspaceChanged());
			} catch (ImportException | AddPerspectiveException e) {
				new ApplicationWarning(e).throwOnMainThread();
			} catch (UnknownWorkspaceException | UnknownImportException e) {
				new ApplicationError(e).throwOnMainThread();
			} finally {
				this.popLongTermOperation(operation);
			}
		});
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
		try {
			File file = LocalUtils.extendDirectory(this.workspace.getWorkspaceDirectory(), newEvent.getSubPath(), perspective.getFileName());
			this.workspace.addPerspectiveToEvent(newEvent, file, perspective.getName());
			this.workspace.deletePerspective(oldEvent, perspective);
			this.workspaceChanged();
		} catch (AddPerspectiveException e) {
			new ApplicationWarning(e).throwOnMainThread();
		} catch (UnknownWorkspaceException e) {
			new ApplicationError(e).throwOnMainThread();
		}
	}

	@Override
	public void showPerspective(Event event, Perspective perspective) {
		try {
			this.playbackViewController.setSelectedMedia(this.workspace, event, perspective);
			this.updateStageTitle(workspace, event);
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
	public void showDetailInformation(Event event, Perspective perspective) {
		File videoFile = LocalUtils.extendDirectory(this.workspace.getWorkspaceDirectory(), event.getSubPath(), perspective.getFileName());
		try {
			VideoInformation videoInformation = this.videoHandler.extractInformation(videoFile);
			DialogFactory.videoInformationDialog(videoInformation, perspective).showAndWait();
		} catch (IOException e) {
			throw new RuntimeException(e);
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
			this.longTermOperationsService.execute(() -> {
				LongTermOperation operation = new LongTermOperation("Export Snapshot", "Creating Snapshot");
				this.pushLongTermOperation(operation);

				try {
					this.exportManager.exportSnapshot(descriptor, timeMillis, exportFile, Optional.of(operation));
				} catch (ExportException e) {
					new ApplicationWarning(e).throwOnMainThread();
				} catch (UnknwonExportException | UnknownWorkspaceException e) {
					new ApplicationError(e).throwOnMainThread();
				} finally {
					this.popLongTermOperation(operation);
				}
			});
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
			this.longTermOperationsService.execute(() -> {
				LongTermOperation operation = new LongTermOperation("Export Clip", "Gathering Perspectives");
				this.pushLongTermOperation(operation);

				try {
					this.exportManager.exportClip(clipDescriptors, exportDirectory, Optional.of(operation));
				} catch (ExportException e) {
					new ApplicationWarning(e).throwOnMainThread();
				} catch (UnknwonExportException | UnknownWorkspaceException e) {
					new ApplicationError(e).throwOnMainThread();
				} finally {
					this.popLongTermOperation(operation);
				}
			});
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
