package de.kobair.videodebrief.core.importing;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.importing.error.ImportException;
import de.kobair.videodebrief.core.importing.error.UnknownImportException;
import de.kobair.videodebrief.core.media.MediaFile;
import de.kobair.videodebrief.core.media.dcf.DcfDevice;
import de.kobair.videodebrief.core.media.dcf.DcfMediaFile;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.AddPerspectiveException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;

public interface ImportManager {

	@FunctionalInterface
	public interface ImportStatusUpdate {

		void updateStatus(int progressInPercent, String status);

	}

	public ImportResult importMediaFile(Workspace workspace, MediaFile mediaFile, Event event, String perspecitveName,
			ImportStatusUpdate statusUpdate)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException;

	public ImportResult importMediaFile(Workspace workspace, DcfDevice device, DcfMediaFile mediaFile, Event event,
			ImportStatusUpdate statusUpdate)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException;

}
