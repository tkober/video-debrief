package de.kobair.videodebrief.core.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.video.VideoHandler;
import de.kobair.videodebrief.core.video.ffmpeg.FfmpegVideoHandler;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;

public class LocalExportManager implements ExportManager {

	private VideoHandler getVideoHandler() {
		return new FfmpegVideoHandler();
	}

	private boolean sameWorkspace(List<ExportDescriptor> descriptors) {
		Workspace workspace = null;
		for (ExportDescriptor descriptor : descriptors) {
			if (workspace != null) {
				if (workspace != descriptor.getWorkspace()) {
					return false;
				}
			}
			workspace = descriptor.getWorkspace();
		}
		return true;
	}

	private boolean sameEvent(List<ExportDescriptor> descriptors) {
		Event event = null;
		for (ExportDescriptor descriptor : descriptors) {
			if (event != null) {
				if (event != descriptor.getEvent()) {
					return false;
				}
			}
			event = descriptor.getEvent();
		}
		return true;
	}

	private ExportCheckResult checkContainingDirectory(File containingDirectory) {
		if (!containingDirectory.getParentFile().exists()) {
			return ExportCheckResult.CONTAINING_DIRECTORY_DOES_NOT_EXIST;
		}

		if (!containingDirectory.getParentFile().canWrite()) {
			return ExportCheckResult.MISSING_WRITE_PERMISSION;
		}

		return ExportCheckResult.OKAY;
	}

	private ExportCheckResult checkCanExportSnapshot(File exportFile) {
		ExportCheckResult result = checkContainingDirectory(exportFile.getParentFile());
		if (result != ExportCheckResult.OKAY) {
			return result;
		}

		if (exportFile.exists()) {
			return ExportCheckResult.FILE_ALREADY_EXISTS;
		}

		return ExportCheckResult.OKAY;
	}

	private ExportCheckResult checkCanExportClip(File exportDirectory, List<ExportDescriptor> exportDescriptors) {
		ExportCheckResult result = checkContainingDirectory(exportDirectory.getParentFile());
		if (result != ExportCheckResult.OKAY) {
			return result;
		}

		if (exportDirectory.exists()) {
			return ExportCheckResult.DIRECTORY_ALREADY_EXISTS;
		}

		if (!sameWorkspace(exportDescriptors)) {
			return ExportCheckResult.NOT_SAME_WORKSPACE;
		}

		if (!sameEvent(exportDescriptors)) {
			return ExportCheckResult.NOT_SAME_EVENT;
		}

		return ExportCheckResult.OKAY;
	}

	private ExportCheckResult checkCanExportWorkspace(File exportDirectory, List<ExportDescriptor> exportDescriptors) {
		ExportCheckResult result = checkContainingDirectory(exportDirectory.getParentFile());
		if (result != ExportCheckResult.OKAY) {
			return result;
		}

		if (exportDirectory.exists()) {
			return ExportCheckResult.DIRECTORY_ALREADY_EXISTS;
		}

		if (!sameWorkspace(exportDescriptors)) {
			return ExportCheckResult.NOT_SAME_WORKSPACE;
		}

		return ExportCheckResult.OKAY;
	}

	private File getFileForPerspectiv(ExportDescriptor exportDescriptor) throws UnknownWorkspaceException {
		return exportDescriptor.getWorkspace().getFileForPerspective(exportDescriptor.getEvent(),
				exportDescriptor.getPerspective());
	}

	@Override
	public void exportSnapshot(ExportDescriptor exportDescriptor, long time, File exportFile)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException {
		ExportCheckResult checkResult = this.checkCanExportSnapshot(exportFile);
		if (checkResult != ExportCheckResult.OKAY) {
			throw new ExportException(checkResult);
		} else {
			File videoFile = getFileForPerspectiv(exportDescriptor);
			try {
				this.getVideoHandler().exportSnapshot(videoFile, time, exportFile);
			} catch (IOException e) {
				throw new UnknwonExportException("", e); // TODO
			}
		}
	}

	@Override
	public void exportClip(List<ExportDescriptor> exportDescriptors, long begin, long duration, File exportDirectory)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException {
		ExportCheckResult checkResult = this.checkCanExportClip(exportDirectory, exportDescriptors);
		if (checkResult != ExportCheckResult.OKAY) {
			throw new ExportException(checkResult);
		} else {
			if (!exportDirectory.mkdir()) {
				throw new UnknwonExportException("", null); // TODO
			}

			for (ExportDescriptor descriptor : exportDescriptors) {
				File videoFile = getFileForPerspectiv(descriptor);
				FileFormat exportFormat = FileFormat.MPEG4_CONTAINER;
				String name = descriptor.getPerspective().getName() + exportFormat.getDefaultFileExtension();
				File targetFile = LocalUtils.extendDirectory(exportDirectory, name);
				try {
					this.getVideoHandler().exportClip(videoFile, begin, duration, targetFile);
				} catch (IOException e) {
					throw new UnknwonExportException("", e); // TODO
				}
			}
		}
	}

	@Override
	public void exportWorkspace(List<ExportDescriptor> exportDescriptors, File exportDirectory)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException {
		ExportCheckResult checkResult = this.checkCanExportWorkspace(exportDirectory, exportDescriptors);
		if (checkResult != ExportCheckResult.OKAY) {
			throw new ExportException(checkResult);
		} else {
			if (!exportDirectory.mkdir()) {
				throw new UnknwonExportException("", null); // TODO
			}

			for (ExportDescriptor descriptor : exportDescriptors) {
				File videoFile = getFileForPerspectiv(descriptor);
				FileFormat exportFormat = FileFormat.MPEG4_CONTAINER;
				String eventName = descriptor.getEvent().getName();
				String persepctiveName = descriptor.getPerspective().getName();
				String name = String.format("%s_%s%s", eventName, persepctiveName,
						exportFormat.getDefaultFileExtension());
				File targetFile = LocalUtils.extendDirectory(exportDirectory, name);
				
				try {
					FileUtils.copyFile(videoFile, targetFile);
				} catch (IOException e) {
					throw new UnknwonExportException("", e); // TODO
				}
			}
		}
	}
}
