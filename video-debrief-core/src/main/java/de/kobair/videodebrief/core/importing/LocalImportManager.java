package de.kobair.videodebrief.core.importing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.importing.checks.ImportCheckResult;
import de.kobair.videodebrief.core.importing.error.ImportException;
import de.kobair.videodebrief.core.importing.error.UnknownImportException;
import de.kobair.videodebrief.core.media.MediaFile;
import de.kobair.videodebrief.core.media.dcf.DcfDevice;
import de.kobair.videodebrief.core.media.dcf.DcfMediaFile;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.AddPerspectiveException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;
import de.kobair.videodebrief.core.workspace.error.WorkspaceException;

public class LocalImportManager implements ImportManager {

	private ImportCheckResult checkCanImport(File file, File destination) {
		if (!file.exists()) {
			return ImportCheckResult.SOURCE_DOES_NOT_EXIST;
		}

		if (!file.isFile()) {
			return ImportCheckResult.SOURCE_NOT_A_FILE;
		}

		boolean accepted = false;
		FileFormat detectedFileFormat = null;
		for (FileFormat fileFormat : IMPORTABLE_FORMATS) {
			accepted = fileFormat.getFilenameFilter().accept(file.getParentFile(), file.getName());
			if (accepted) {
				detectedFileFormat = fileFormat;
				break;
			}
		}
		if (!accepted) {
			return ImportCheckResult.UNSUPPORTED_FILE_FORMAT;
		}

		if (!file.canRead()) {
			return ImportCheckResult.SOURCE_NOT_READABLE;
		}

		if (!destination.exists()) {
			return ImportCheckResult.DESTINATION_DOES_NOT_EXIST;
		}

		if (!destination.isDirectory()) {
			return ImportCheckResult.DESTINATION_NOT_A_DIRECTORY;
		}

		if (!destination.canWrite()) {
			return ImportCheckResult.DESTINATION_NOT_WRITALBE;
		}

		return ImportCheckResult.OKAY.setFileFormat(detectedFileFormat);
	}

	private File generateFileForDirectory(File dir, String fileExtension) {
		File file = null;
		do {
			UUID uuid = UUID.randomUUID();
			file = LocalUtils.extendDirectory(dir, uuid.toString() + fileExtension);
		} while (file.exists());
		return file;
	}

	private void copyFile(File source, File destination, ImportStatusUpdate statusUpdate) throws IOException {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		long length = source.length();
		long counter = 0;
		int r = 0;
		byte[] b = new byte[8096];
		int progress = 0;
		
		final String message = String.format("Copying '%s' to '%s'", source, destination);
		try {
			inputStream = new FileInputStream(source);
			outputStream = new FileOutputStream(destination);
			while ((r = inputStream.read(b)) != -1) {
				counter += r;
				int newProgress = (int) Math.ceil(100.0 * counter / length);
				if (progress != newProgress) {
					progress = newProgress;
					statusUpdate.updateStatus(progress, message);
				}
				outputStream.write(b, 0, r);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			inputStream.close();
			outputStream.close();
		}
	}

	private void createPlaceholder(File file) {

	}

	/*
	 *	The import is a bit trick because we have two things that depend on each other.
	 *	1.)	Perspectives can only be created when the required file is there
	 * 	2.)	We should only copy the file if we are sure that we can create a Perspective
	 * 		with the given name.
	 */
	@Override
	public ImportResult importFile(Workspace workspace, File file, Event event, String perspectiveName, ImportStatusUpdate statusUpdate)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException {
		File destinationDirectory = LocalUtils.extendDirectory(workspace.getWorkspaceDirectory(), event.getSubPath());
		ImportCheckResult checkResult = checkCanImport(file, destinationDirectory);

		if (checkResult != ImportCheckResult.OKAY) {
			throw new ImportException(checkResult, file);
		} else {
			// To break the above mentionen cyclic dependencies we do the following:

			// 1.) Create an empty file with the same name as the desired file as a placeholder
			File destination = generateFileForDirectory(destinationDirectory,
					checkResult.getFileFormat().getDefaultFileExtension());
			createPlaceholder(destination);

			// 2.) Create the perspective with the placeholder
			Perspective perspective = null;
			try {
				perspective = workspace.addPerspectiveToEvent(event, destination, perspectiveName);
			} catch (WorkspaceException e) {
				// If this fails delete the placeholder...
				destination.delete();

				// ...and re-throw the exception
				throw e;
			}

			// 3.) Copy the actual file (placeholder will be overwritten)
			try {
				this.copyFile(file, destination, statusUpdate);
			} catch (IOException e) {
				String message = String.format("Failed copying '%s' to '%s'.", file, destination);
				throw new UnknownImportException(message, e);
			}

			return new ImportResult(perspective, destination);
		}
	}

	@Override
	public ImportResult importMediaFile(final Workspace workspace, final MediaFile mediaFile, final Event event,
			final String perspecitveName, ImportStatusUpdate statusUpdate)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException {
		return this.importFile(workspace, mediaFile.getFile(), event, perspecitveName, statusUpdate);
	}

	@Override
	public ImportResult importMediaFile(final Workspace workspace, final DcfDevice device, final DcfMediaFile mediaFile,
			final Event event, ImportStatusUpdate statusUpdate)
			throws ImportException, AddPerspectiveException, UnknownWorkspaceException, UnknownImportException {
		return this.importMediaFile(workspace, mediaFile, event, device.getName(), statusUpdate);
	}
}
