package de.kobair.videodebrief.core.importing;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.importing.error.ImportException;
import de.kobair.videodebrief.core.importing.error.UnknownImportException;
import de.kobair.videodebrief.core.media.MediaFile;
import de.kobair.videodebrief.core.media.dcf.DcfDevice;
import de.kobair.videodebrief.core.media.dcf.DcfMediaFile;
import de.kobair.videodebrief.core.operations.Operation;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.AddPerspectiveException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;

public interface ImportManager {

	public static final List<FileFormat> IMPORTABLE_FORMATS = Collections
																	  .unmodifiableList(Arrays.asList(FileFormat.VIDEO_FORMATS));

	public ImportResult importFile(Workspace workspace, File file, Event event, String perspectiveName, Optional<Operation> operation)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException;

	public ImportResult importMediaFile(Workspace workspace, MediaFile mediaFile, Event event, String perspecitveName,
			Optional<Operation> operation)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException;

	public ImportResult importMediaFile(Workspace workspace, DcfDevice device, DcfMediaFile mediaFile, Event event,
			Optional<Operation> operation)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException;

}
