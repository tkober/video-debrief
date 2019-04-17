package de.kobair.videodebrief.core.workspace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.event.EventPojo;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.perspective.PerspectivePojo;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.workspace.checks.AddPerspectiveCheckResult;
import de.kobair.videodebrief.core.workspace.checks.ChangeInOutPointCheckResult;
import de.kobair.videodebrief.core.workspace.checks.CreateEventCheckResult;
import de.kobair.videodebrief.core.workspace.checks.RenameEventCheckResult;
import de.kobair.videodebrief.core.workspace.checks.RenamePerspectiveCheckResult;
import de.kobair.videodebrief.core.workspace.error.AddPerspectiveException;
import de.kobair.videodebrief.core.workspace.error.ChangeInOutPointException;
import de.kobair.videodebrief.core.workspace.error.CreateEventException;
import de.kobair.videodebrief.core.workspace.error.RenameEventException;
import de.kobair.videodebrief.core.workspace.error.RenamePerspectiveException;
import de.kobair.videodebrief.core.workspace.error.SavingWorkpsaceNotPossibleException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;
import de.kobair.videodebrief.core.workspace.error.UnrecoverableWorkspaceException;
import de.kobair.videodebrief.core.workspace.error.WorkspaceException;

public class LocalWorkspace implements Workspace, FileSystemSynchronized {
	
	private enum ContentLocation {
		SHARED_CONTENT_DIRECTORY,
		ONE_DIRECTORY_PER_EVENT
	}

	private static final String UUID_STRING_REGEX = "([0-9,a-f]{8}(-[0-9,a-f]{4}){4}[0-9,a-f]{8})";
	private static final String VALID_PERSPECTIVE_FILE_NAME_REGEX = "(^" + UUID_STRING_REGEX + "([.].{3}))";
	private static final String SHARED_CONTENT_DIRECTORY = "content";
	
	private final ContentLocation contentLocation = ContentLocation.SHARED_CONTENT_DIRECTORY;

	private final File workspaceFile;
	private WorkspaceData workspaceData;

	protected LocalWorkspace(File workspaceFile) {
		this(workspaceFile, null);
	}

	protected LocalWorkspace(File workspaceFile, WorkspaceData workspaceData) {
		this.workspaceFile = workspaceFile;
		this.workspaceData = workspaceData;
	}

	private void rollback(WorkspaceException cause) {
		try {
			this.reload();
		} catch (FileNotFoundException e) {
			UnrecoverableWorkspaceException fatal = new UnrecoverableWorkspaceException(e, cause);
			throw fatal;
		}
	}

	private void saveWorkspaceAndRollbackOnError(String causeMessage) throws SavingWorkpsaceNotPossibleException {
		try {
			this.save();
		} catch (IOException e) {
			SavingWorkpsaceNotPossibleException cause = new SavingWorkpsaceNotPossibleException(causeMessage, e);
			rollback(cause);
			throw cause;
		}
	}

	@SuppressWarnings("rawtypes")
	private <T> T loadJsonPojo(File source, Class type) throws FileNotFoundException {
		JsonReader reader = new JsonReader(new FileReader(source));
		return new Gson().fromJson(reader, type);
	}

	private void saveJsonPojo(File destination, Object object) throws IOException {
		Writer writer = new FileWriter(destination);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		gson.toJson(object, writer);
		writer.close();
	}

	private boolean isNonEmptyName(String name) {
		return name != null && !name.trim().isEmpty();
	}

	private CreateEventCheckResult canCreateEvent(String name) {
		if (!isNonEmptyName(name)) {
			return CreateEventCheckResult.NAME_EMPTY;
		}

		if (containsEventWithName(name)) {
			return CreateEventCheckResult.NAME_ALREADY_USED;
		}

		return CreateEventCheckResult.OKAY;
	}

	private RenameEventCheckResult canRenameEvent(String name) {
		if (!isNonEmptyName(name)) {
			return RenameEventCheckResult.NAME_EMPTY;
		}

		if (containsEventWithName(name)) {
			return RenameEventCheckResult.NAME_ALREADY_USED;
		}

		return RenameEventCheckResult.OKAY;
	}

	private EventPojo assertCanManipulateEvent(Event event) throws UnknownWorkspaceException {
		if (!this.containsEvent(event)) {
			String message = String.format("The event '%s' located at '%s' is not included in this workspace (%s)",
					event.getName(), event.getSubPath(), workspaceFile);
			throw new UnknownWorkspaceException(message, null);
		}
		return (EventPojo) this.getEventByName(event.getName(), NameMatching.CASE_SENSITIVE);
	}

	private PerspectivePojo assertCanManipulatePerspective(Event event, Perspective perspective)
			throws UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		if (!this.containsPerspective(eventPojo, perspective)) {
			String message = String.format(
					"The perspective '%s' located at '%s' is not included in the event %s (at %s)",
					perspective.getName(), perspective.getFileName(), event.getName(), event.getSubPath());
			throw new UnknownWorkspaceException(message, null);
		}
		return (PerspectivePojo) this.getPerspectiveByName(eventPojo, perspective.getName(),
				NameMatching.CASE_SENSITIVE);
	}

	private boolean isFileInEventDirectory(File file, EventPojo event) {
		File eventDirectory = LocalUtils.extendDirectory(getWorkingDirectory(), event.getSubPath());
		File directory = file.getParentFile();

		if (directory == null) {
			return false;
		}

		return directory.getAbsolutePath().equals(eventDirectory.getAbsolutePath());
	}

	private boolean hasFileValidFormatForPerspective(File file) {
		String fileName = file.getName();

		if (!fileName.matches(VALID_PERSPECTIVE_FILE_NAME_REGEX)) {
			return false;
		}

		for (FileFormat format : FileFormat.VIDEO_FORMATS) {
			if (format.getFilenameFilter().accept(file.getParentFile(), fileName)) {
				return true;
			}
		}

		return false;
	}

	private String perspectiveIdFromFile(File file) {
		return file.getName().split("[.]")[0];
	}

	private boolean isFileAlreadyUsedAsPerspective(File file, Event event) {
		String id = perspectiveIdFromFile(file);
		return event.containsPerspectiveWithId(id);
	}

	private AddPerspectiveCheckResult canAddPerspective(String name, File file, EventPojo event) throws UnknownWorkspaceException {
		if (!isNonEmptyName(name)) {
			return AddPerspectiveCheckResult.NAME_EMPTY;
		}

		if (containsPerspectiveWithName(event, name)) {
			return AddPerspectiveCheckResult.NAME_ALREADY_USED;
		}

		if (!file.exists()) {
			return AddPerspectiveCheckResult.DOES_NOT_EXIST;
		}

		if (!file.isFile()) {
			return AddPerspectiveCheckResult.IS_NOT_A_FILE;
		}

		if (!file.canRead()) {
			return AddPerspectiveCheckResult.CAN_NOT_READ;
		}

		if (!isFileInEventDirectory(file, event)) {
			return AddPerspectiveCheckResult.NOT_IN_EVENT_DIRECTORY;
		}

		if (!hasFileValidFormatForPerspective(file)) {
			return AddPerspectiveCheckResult.INVALID_FORMAT;
		}

		if (isFileAlreadyUsedAsPerspective(file, event)) {
			return AddPerspectiveCheckResult.ALREADY_USED;
		}

		return AddPerspectiveCheckResult.OKAY;
	}

	private RenamePerspectiveCheckResult canRenamePerspective(String name, EventPojo event) throws UnknownWorkspaceException {
		if (!isNonEmptyName(name)) {
			return RenamePerspectiveCheckResult.NAME_EMPTY;
		}

		if (containsPerspectiveWithName(event, name)) {
			return RenamePerspectiveCheckResult.NAME_ALREADY_USED;
		}

		return RenamePerspectiveCheckResult.OKAY;
	}

	private File fileFromPerspective(EventPojo event, PerspectivePojo perspective) {
		return LocalUtils.extendDirectory(getWorkingDirectory(), event.getSubPath(), perspective.getFileName());
	}

	private ChangeInOutPointCheckResult canChangeInOutPoint(long inPoint, long outPoint) {
		if (inPoint < 0 || outPoint < 0) {
			return ChangeInOutPointCheckResult.NEGATIVE_VALUE;
		}

		if (inPoint >= outPoint) {
			return ChangeInOutPointCheckResult.IN_POINT_GREATER_THAN_OR_EQUAL_OUT_POINT;
		}

		return ChangeInOutPointCheckResult.OKAY;
	}
	
	private File sharedContentDirectory() {
		return LocalUtils.extendDirectory(getWorkingDirectory(), SHARED_CONTENT_DIRECTORY);
	}
	
	private File newUniqueSubdirectory() {
		File directory = null;
		do {
			UUID uuid = UUID.randomUUID();
			directory = LocalUtils.extendDirectory(getWorkingDirectory(), uuid.toString());
		} while (directory.exists());
		return directory;
	}

	@Override
	public File getWorkspaceDirectory() {
		return this.workspaceFile.getParentFile();
	}

	@Override
	public List<? extends Event> getEvents() {
		return this.workspaceData.getEvents();
	}

	@Override
	public Event getEventByName(String name) {
		return this.getEventByName(name, DEFAULT_NAME_MATCHING);
	}

	@Override
	public Event getEventByName(String name, NameMatching nameMatching) {
		if (isNonEmptyName(name)) {
			String trimmedName = name.trim();
			List<Event> result = this.getEvents().stream().filter((e) -> {
				if (nameMatching == NameMatching.CASE_SENSITIVE) {
					return trimmedName.equals(e.getName());
				} else {
					return trimmedName.equalsIgnoreCase(e.getName());
				}
			}).collect(Collectors.toList());
			if (!result.isEmpty()) {
				return result.get(0);
			}
		}
		return null;
	}

	@Override
	public boolean containsEvent(Event event) {
		if (event != null) {
			List<Event> result = this.getEvents().stream().filter((e) -> event.equals(e)).collect(Collectors.toList());
			return !result.isEmpty();
		}

		return false;
	}

	@Override
	public boolean containsEventWithName(String name) {
		return this.containsEventWithName(name, DEFAULT_NAME_MATCHING);
	}

	@Override
	public boolean containsEventWithName(String name, NameMatching nameMatching) {
		Event found = this.getEventByName(name, nameMatching);
		return found != null;
	}

	@Override
	public Event createEvent(String name) throws CreateEventException, UnknownWorkspaceException {
		CreateEventCheckResult checkResult = canCreateEvent(name);
		if (checkResult != CreateEventCheckResult.OKAY) {
			throw new CreateEventException(checkResult, name);
		} else {
			File directory = this.getAvailableSubDirectoryInWorkingDirectory(null);
			if (!directory.mkdir()) {
				if (this.contentLocation != ContentLocation.SHARED_CONTENT_DIRECTORY) {
					String message = String.format("Directory '%s' could not be created", directory);
					throw new UnknownWorkspaceException(message, null);
				}
			}
			String trimmedName = name.trim();
			String subDirectory = directory.getName();
			EventPojo event = new EventPojo(trimmedName, subDirectory, new HashMap<>());
			this.workspaceData.getEvents().add(event);

			String causeMessage = String.format(
					"An error occurred while saving the new event '%s' in directory '%s'. The change could not be saved.",
					name, subDirectory);
			this.saveWorkspaceAndRollbackOnError(causeMessage);

			return event;
		}
	}

	@Override
	public void renameEvent(Event event, String newName) throws RenameEventException, UnknownWorkspaceException {
		EventPojo pojo = assertCanManipulateEvent(event);
		RenameEventCheckResult checkResult = this.canRenameEvent(newName);
		if (checkResult != RenameEventCheckResult.OKAY) {
			throw new RenameEventException(checkResult, newName);
		}

		String trimmedName = newName.trim();
		pojo.setName(trimmedName);
		String causeMessage = String.format(
				"An error occurred while saving the name change of event '%s' to '%s'. The change could not be saved.",
				event.getName(), newName);
		this.saveWorkspaceAndRollbackOnError(causeMessage);
	}

	@Override
	public boolean deleteEvent(Event event) throws UnknownWorkspaceException {
		return this.deleteEvent(event, DEFAULT_DELETION_STRATEGY);
	}

	@Override
	public boolean deleteEvent(Event event, DeletionStrategy deletionStrategy) throws UnknownWorkspaceException {
		EventPojo pojo = assertCanManipulateEvent(event);
		if (!this.workspaceData.getEvents().remove(pojo)) {
			String message = String.format("Event '%s' could not be removed from the workspace at '%s'", pojo.getName(),
					this.workspaceFile);
			throw new UnknownWorkspaceException(message, null);
		}
		String causeMessage = String.format(
				"An error occurred while deleting event '%s' at '%s'. The change could not be saved.", pojo.getName(),
				this.workspaceFile);
		this.saveWorkspaceAndRollbackOnError(causeMessage);
		if (deletionStrategy == DeletionStrategy.DELETE_FILES) {
			File eventDirectory = LocalUtils.extendDirectory(getWorkspaceDirectory(), event.getSubPath());
			try {
				FileUtils.deleteDirectory(eventDirectory);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		return false;
	}

	@Override
	public File getAvailableSubDirectoryInWorkingDirectory(Object infomration) {
		switch (this.contentLocation) {
		case ONE_DIRECTORY_PER_EVENT:
			return this.newUniqueSubdirectory();

		case SHARED_CONTENT_DIRECTORY:
			return this.sharedContentDirectory();
		}
		return null;
	}

	@Override
	public List<Perspective> getPerspectives(Event event) throws UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		return new ArrayList<>(eventPojo.getPerspectives().values());
	}

	@Override
	public Perspective getPerspectiveByName(Event event, String name) throws UnknownWorkspaceException {
		return this.getPerspectiveByName(event, name, DEFAULT_NAME_MATCHING);
	}

	@Override
	public Perspective getPerspectiveByName(Event event, String name, NameMatching nameMatching)
			throws UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		if (isNonEmptyName(name)) {
			String trimmedName = name.trim();
			List<Perspective> result = eventPojo.getPerspectives().values().stream().filter((p) -> {
				if (nameMatching == NameMatching.CASE_SENSITIVE) {
					return trimmedName.equals(p.getName());
				} else {
					return trimmedName.equalsIgnoreCase(p.getName());
				}
			}).collect(Collectors.toList());
			if (!result.isEmpty()) {
				return result.get(0);
			}
		}
		return null;
	}

	@Override
	public boolean containsPerspective(Event event, Perspective perspective) throws UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		if (perspective != null) {
			List<Perspective> result = eventPojo.getPerspectives().values().stream()
											   .filter((p) -> perspective.equals(p)).collect(Collectors.toList());
			return !result.isEmpty();
		}

		return false;
	}

	@Override
	public boolean containsPerspectiveWithName(Event event, String name) throws UnknownWorkspaceException {
		return this.containsPerspectiveWithName(event, name, DEFAULT_NAME_MATCHING);
	}

	@Override
	public boolean containsPerspectiveWithName(Event event, String name, NameMatching nameMatching)
			throws UnknownWorkspaceException {
		Perspective found = this.getPerspectiveByName(event, name, nameMatching);
		return found != null;
	}

	@Override
	public Perspective addPerspectiveToEvent(Event event, File file, String name)
			throws AddPerspectiveException, UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		AddPerspectiveCheckResult checkResult = canAddPerspective(name, file, eventPojo);

		if (checkResult != AddPerspectiveCheckResult.OKAY) {
			String message = String.format(checkResult.getMessage(), file);
			throw new UnknownWorkspaceException(message, null);
		} else {
			PerspectivePojo pojo = new PerspectivePojo(name, file.getName());
			String id = this.perspectiveIdFromFile(file);
			eventPojo.getPerspectives().put(id, pojo);

			String message = String.format(
					"An error occurred while adding perspective %s (at '%s') to event '%s' (at '%s'). The change could not be saved.",
					pojo.getName(), file, eventPojo.getName(),
					LocalUtils.extendDirectory(getWorkingDirectory(), eventPojo.getSubPath()));
			this.saveWorkspaceAndRollbackOnError(message);

			return pojo;
		}
	}

	@Override
	public void renamePerspective(Event event, Perspective perspective, String newName)
			throws RenamePerspectiveException, UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		PerspectivePojo pojo = assertCanManipulatePerspective(event, perspective);
		RenamePerspectiveCheckResult checkResult = canRenamePerspective(newName, eventPojo);
		if (checkResult != RenamePerspectiveCheckResult.OKAY) {
			throw new RenamePerspectiveException(checkResult, newName);
		}

		String trimmedName = newName.trim();
		pojo.setName(trimmedName);
		String causeMessage = String.format(
				"An error occurred while saving the name change of perspective '%s' in '%s' to '%s'. The change could not be saved.",
				pojo.getName(), event.getName(), newName);
		this.saveWorkspaceAndRollbackOnError(causeMessage);
	}

	@Override
	public void changeInPointForPerspective(Event event, Perspective perspective, long inPoint)
			throws ChangeInOutPointException, UnknownWorkspaceException {
		PerspectivePojo pojo = assertCanManipulatePerspective(event, perspective);
		ChangeInOutPointCheckResult checkResult = this.canChangeInOutPoint(inPoint, pojo.getOutPoint());
		if (checkResult != ChangeInOutPointCheckResult.OKAY) {
			throw new ChangeInOutPointException(checkResult, inPoint, pojo.getOutPoint());
		}

		pojo.setInPoint(inPoint);
		String causeMessage = String.format(
				"An error occurred while saving the in point change of perspective '%s' in '%s'. The change could not be saved.",
				pojo.getName(), event.getName());
		this.saveWorkspaceAndRollbackOnError(causeMessage);
	}

	@Override
	public void changeOutPointForPerspective(Event event, Perspective perspective, long outPoint)
			throws ChangeInOutPointException, UnknownWorkspaceException {
		PerspectivePojo pojo = assertCanManipulatePerspective(event, perspective);
		ChangeInOutPointCheckResult checkResult = this.canChangeInOutPoint(pojo.getInPoint(), outPoint);
		if (checkResult != ChangeInOutPointCheckResult.OKAY) {
			throw new ChangeInOutPointException(checkResult, pojo.getInPoint(), outPoint);
		}

		pojo.setOutPoint(outPoint);
		String causeMessage = String.format(
				"An error occurred while saving the out point change of perspective '%s' in '%s'. The change could not be saved.",
				pojo.getName(), event.getName());
		this.saveWorkspaceAndRollbackOnError(causeMessage);
	}

	@Override
	public void changeAlignmentPointForPerspective(Event event, Perspective perspective, long alignmentPoint)
			throws UnknownWorkspaceException {
		PerspectivePojo pojo = assertCanManipulatePerspective(event, perspective);
		pojo.setAlignmentPoint(alignmentPoint);
		String causeMessage = String.format(
				"An error occurred while saving the alignment point change of perspective '%s' in '%s'. The change could not be saved.",
				pojo.getName(), event.getName());
		this.saveWorkspaceAndRollbackOnError(causeMessage);
	}

	@Override
	public boolean deletePerspective(Event event, Perspective perspective) throws UnknownWorkspaceException {
		return this.deletePerspective(event, perspective, DEFAULT_DELETION_STRATEGY);
	}

	@Override
	public boolean deletePerspective(Event event, Perspective perspective, DeletionStrategy deletionStrategy)
			throws UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		PerspectivePojo perspectivePojo = assertCanManipulatePerspective(event, perspective);
		File perspectiveFile = this.fileFromPerspective(eventPojo, perspectivePojo);
		String id = perspectiveIdFromFile(perspectiveFile);
		eventPojo.getPerspectives().remove(id);
		String causeMessage = String.format(
				"An error occurred while deleting perspective '%s' at '%s'. The change could not be saved.",
				perspectivePojo.getName(), perspectiveFile);
		this.saveWorkspaceAndRollbackOnError(causeMessage);

		if (deletionStrategy == DeletionStrategy.DELETE_FILES) {
			try {
				FileUtils.forceDelete(perspectiveFile);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		return false;
	}

	@Override
	public File getFileForPerspective(Event event, Perspective perspective) throws UnknownWorkspaceException {
		EventPojo eventPojo = assertCanManipulateEvent(event);
		PerspectivePojo perspectivePojo = assertCanManipulatePerspective(event, perspective);
		return this.getFileForPerspective(eventPojo, perspectivePojo);
	}

	@Override
	public void save() throws IOException {
		this.saveJsonPojo(workspaceFile, this.workspaceData);
	}

	@Override
	public void reload() throws FileNotFoundException {
		this.workspaceData = this.loadJsonPojo(workspaceFile, WorkspacePojo.class);
	}

	@Override
	public String toString() {
		return String.format("%s { exportFolder: '%s', events: %d}", super.toString(),
				this.workspaceData.getExportFolder(), this.workspaceData.getEvents().size());
	}

	@Override
	public File getWorkingDirectory() {
		return this.getWorkspaceDirectory();
	}

	@Override
	public String getVersion() {
		return this.workspaceData.getVersion();
	}
}
