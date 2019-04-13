package de.kobair.videodebrief.core.workspace;

import java.io.File;
import java.util.List;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.workspace.error.AddPerspectiveException;
import de.kobair.videodebrief.core.workspace.error.ChangeInOutPointException;
import de.kobair.videodebrief.core.workspace.error.CreateEventException;
import de.kobair.videodebrief.core.workspace.error.RenameEventException;
import de.kobair.videodebrief.core.workspace.error.RenamePerspectiveException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;

public interface Workspace {

	public enum NameMatching {
		CASE_INSENSITIVE, CASE_SENSITIVE;
	}

	public enum DeletionStrategy {
		KEEP_FILES, DELETE_FILES;
	}

	public static final NameMatching DEFAULT_NAME_MATCHING = NameMatching.CASE_INSENSITIVE;

	public static final DeletionStrategy DEFAULT_DELETION_STRATEGY = DeletionStrategy.KEEP_FILES;

	public File getWorkspaceDirectory();

	public List<? extends Event> getEvents();

	public Event getEventByName(String name);

	public Event getEventByName(String name, NameMatching nameMatching);

	public boolean containsEvent(Event event);

	public boolean containsEventWithName(String name);

	public boolean containsEventWithName(String name, NameMatching nameMatching);

	public Event createEvent(String name) throws CreateEventException, UnknownWorkspaceException;

	public void renameEvent(Event event, String newName) throws RenameEventException, UnknownWorkspaceException;

	public boolean deleteEvent(Event event) throws UnknownWorkspaceException;

	public boolean deleteEvent(Event event, DeletionStrategy deletionStrategy) throws UnknownWorkspaceException;

	public List<Perspective> getPerspectives(Event event) throws UnknownWorkspaceException;

	public Perspective getPerspectiveByName(Event event, String name) throws UnknownWorkspaceException;

	public Perspective getPerspectiveByName(Event event, String name, NameMatching nameMatching)
			throws UnknownWorkspaceException;

	public boolean containsPerspective(Event event, Perspective perspective) throws UnknownWorkspaceException;

	public boolean containsPerspectiveWithName(Event event, String name) throws UnknownWorkspaceException;

	public boolean containsPerspectiveWithName(Event event, String name, NameMatching nameMatching)
			throws UnknownWorkspaceException;

	public Perspective addPerspectiveToEvent(Event event, File file, String name)
			throws AddPerspectiveException, UnknownWorkspaceException;

	public void renamePerspective(Event event, Perspective perspective, String newName)
			throws RenamePerspectiveException, UnknownWorkspaceException;

	public void changeInPointForPerspective(Event event, Perspective perspective, long inPoint)
			throws ChangeInOutPointException, UnknownWorkspaceException;

	public void changeOutPointForPerspective(Event event, Perspective perspective, long outPoint)
			throws ChangeInOutPointException, UnknownWorkspaceException;

	public void changeAlignmentPointForPerspective(Event event, Perspective perspective, long alignmentPoint)
			throws UnknownWorkspaceException;

	public boolean deletePerspective(Event event, Perspective perspective) throws UnknownWorkspaceException;
	
	public boolean deletePerspective(Event event, Perspective perspective, DeletionStrategy deletionStrategy)
			throws UnknownWorkspaceException;
	
	public File getFileForPerspective(Event event, Perspective perspective) throws UnknownWorkspaceException;

}
