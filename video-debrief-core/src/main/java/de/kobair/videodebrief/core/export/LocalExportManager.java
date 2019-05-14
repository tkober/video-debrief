package de.kobair.videodebrief.core.export;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.operations.Operation;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.video.VideoHandler;
import de.kobair.videodebrief.core.video.ffmpeg.FfmpegVideoHandler;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;

public class LocalExportManager implements ExportManager {

	private VideoHandler getVideoHandler() {
		return new FfmpegVideoHandler();
	}

	private boolean sameWorkspace(List<? extends ExportDescriptor> descriptors) {
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

	private boolean sameEvent(List<? extends ExportDescriptor> descriptors) {
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
		if (!containingDirectory.exists()) {
			return ExportCheckResult.CONTAINING_DIRECTORY_DOES_NOT_EXIST;
		}

		if (!containingDirectory.canWrite()) {
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

	private ExportCheckResult checkCanExportClip(File exportDirectory, List<? extends ExportDescriptor> exportDescriptors) {
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

	private File getFileForPerspective(ExportDescriptor exportDescriptor) throws UnknownWorkspaceException {
		return exportDescriptor.getWorkspace().getFileForPerspective(exportDescriptor.getEvent(),
				exportDescriptor.getPerspective());
	}

	@Override
	public void exportSnapshot(ExportDescriptor exportDescriptor, long time, File exportFile, Optional<Operation> operation)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException {
		ExportCheckResult checkResult = this.checkCanExportSnapshot(exportFile);
		if (checkResult != ExportCheckResult.OKAY) {
			throw new ExportException(checkResult);
		} else {
			File videoFile = getFileForPerspective(exportDescriptor);

			File exportDirectory = exportFile.getParentFile();
			if (!exportDirectory.mkdir() && !exportDirectory.exists()) {
				throw new UnknwonExportException("", null); // TODO
			}

			try {
				operation.ifPresent(op -> op.updateProgress(-1));
				operation.ifPresent(op -> op.updateDescription(String.format("Creating snapshot for '%s/%s'", exportDescriptor.getEvent().getName(), exportDescriptor.getPerspective().getName())));

				this.getVideoHandler().exportSnapshot(videoFile, time, exportFile);
			} catch (IOException e) {
				throw new UnknwonExportException("", e); // TODO
			}
		}
	}

	@Override
	public void exportClip(List<ClipDescriptor> clipDescriptors, File exportDirectory, Optional<Operation> operation)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException {
		ExportCheckResult checkResult = this.checkCanExportClip(exportDirectory, clipDescriptors);
		if (checkResult != ExportCheckResult.OKAY) {
			throw new ExportException(checkResult);
		} else {
			if (!exportDirectory.mkdir() && !exportDirectory.exists()) {
				throw new UnknwonExportException("", null); // TODO
			}
			operation.ifPresent(op -> op.updateProgress(-1));

			int i = 1;
			for (ClipDescriptor clip : clipDescriptors) {
				String currentStatus = String.format("Creating clip for '%s/%s'", clip.getEvent().getName(), clip.getPerspective().getName());
				operation.ifPresent(op -> op.updateDescription(currentStatus));
				final int step = i;
				operation.ifPresent(op -> op.updateStep(step));

				File videoFile = getFileForPerspective(clip);
				FileFormat exportFormat = FileFormat.MPEG4_CONTAINER;
				String name = clip.getPerspective().getName() + exportFormat.getDefaultFileExtension();
				File targetFile = LocalUtils.extendDirectory(exportDirectory, name);
				try {
					this.getVideoHandler().exportClip(videoFile, clip.getBegin(), clip.getDuration(), targetFile);
				} catch (IOException e) {
					throw new UnknwonExportException("", e); // TODO
				}
				i++;
			}
		}
	}

	@Override
	public void exportWorkspace(List<ExportDescriptor> exportDescriptors, File exportDirectory, Optional<Operation> operation)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException {
		ExportCheckResult checkResult = this.checkCanExportWorkspace(exportDirectory, exportDescriptors);
		if (checkResult != ExportCheckResult.OKAY) {
			throw new ExportException(checkResult);
		} else {
			if (!exportDirectory.mkdir() && !exportDirectory.exists()) {
				throw new UnknwonExportException("", null); // TODO
			}
			operation.ifPresent(op -> op.updateProgress(-1));

			int i = 1;
			for (ExportDescriptor descriptor : exportDescriptors) {
				String currentStatus = String.format("Copying '%s/%s'", descriptor.getEvent().getName(), descriptor.getPerspective().getName());
				operation.ifPresent(op -> op.updateDescription(currentStatus));
				final int step = i;
				operation.ifPresent(op -> op.updateStep(step));

				File videoFile = getFileForPerspective(descriptor);
				FileFormat exportFormat = FileFormat.MPEG4_CONTAINER;
				String eventName = descriptor.getEvent().getName();
				String persepctiveName = descriptor.getPerspective().getName();
				String name = String.format("%s_%s%s", eventName, persepctiveName,
						exportFormat.getDefaultFileExtension());
				File targetFile = LocalUtils.extendDirectory(exportDirectory, name);
				
				try {
					LocalUtils.copyFile(videoFile, targetFile, operation);
				} catch (IOException e) {
					throw new UnknwonExportException("", e); // TODO
				}
				i++;
			}
		}
	}
}
