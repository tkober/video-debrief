package de.kobair.videodebrief.core.workspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.workspace.checks.CreateWorkspaceCheckResult;
import de.kobair.videodebrief.core.workspace.checks.LoadWorkspaceCheckResult;
import de.kobair.videodebrief.core.workspace.error.CreateWorkspaceException;
import de.kobair.videodebrief.core.workspace.error.LoadWorkspaceException;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;

public class WorkspaceManager {

	private static final String DEFAULT_EXPORT_DIRECTORY = "exports";
	
	public LocalWorkspace createWorkspace(File directory, String name) throws CreateWorkspaceException, UnknownWorkspaceException {
		CreateWorkspaceCheckResult checkResult = canCreateWorkspace(directory, name);
		
		if (checkResult != CreateWorkspaceCheckResult.OKAY) {
			throw new CreateWorkspaceException(checkResult, directory.toString());
		} else {
			File workspaceDirectory = checkResult.getWorkspaceDirectory();
			if (!workspaceDirectory.mkdir()) {
				String message = String.format("An unexpected error occurred while creating workspace directory '%s'", 
						workspaceDirectory.toString());
				throw new UnknownWorkspaceException(message, null);
			}
			
			File workspaceFile = LocalUtils.extendDirectory(workspaceDirectory, FileFormat.WORKSPACE.getFileExtensions().get(0));
			LocalWorkspace result = new LocalWorkspace(workspaceFile, initialWorkspaceData());
			try {
				result.save();
			} catch (Exception e) {
				String message = String.format("An unexpected error occurred while saving workspace to '%s'", 
						workspaceFile.toString());
				throw new UnknownWorkspaceException(message, e);
			}
			return result;
		}
	}
	
	public LocalWorkspace loadWorkspace(File directory) throws LoadWorkspaceException, UnknownWorkspaceException {
		LoadWorkspaceCheckResult checkResult = isWorkspace(directory);
		if (checkResult != LoadWorkspaceCheckResult.OKAY) {
			throw new LoadWorkspaceException(checkResult, directory.toString());
		} else {
			File workspaceFile = checkResult.getWorkspaceFile();
			LocalWorkspace result = new LocalWorkspace(workspaceFile);
			try {
				result.reload();
			} catch (Exception e) {
				String message = String.format("An unexpected error occurred while loading workspace from '%s'", 
						workspaceFile.toString());
				throw new UnknownWorkspaceException(message, e);
			}
			return result;
		}
	}
	
	public LoadWorkspaceCheckResult isWorkspace(File directory) {
		if (!directory.exists()) {
			return LoadWorkspaceCheckResult.DOES_NOT_EXISTS;
		}
		
		if (!directory.isDirectory()) {
			return LoadWorkspaceCheckResult.NOT_A_DIRECTORY;
		}
		
		if (!directory.canRead()) {
			return LoadWorkspaceCheckResult.CAN_NOT_READ;
		}
		
		if (!directory.canWrite()) {
			return LoadWorkspaceCheckResult.CAN_NOT_WRITE;
		}

		File[] found = directory.listFiles(FileFormat.WORKSPACE.getFilenameFilter());
		if (found.length == 0) {
			return LoadWorkspaceCheckResult.NO_WORKSPACE_FILE;
		}
		File workspaceFile = found[0];
		
		return LoadWorkspaceCheckResult.OKAY.setWorkspaceFile(workspaceFile);
	}
	
	public CreateWorkspaceCheckResult canCreateWorkspace(File directory, String name) {
		if (!directory.exists()) {
			return CreateWorkspaceCheckResult.DOES_NOT_EXISTS;
		}
		
		if (!directory.isDirectory()) {
			return CreateWorkspaceCheckResult.NOT_A_DIRECTORY;
		}
		
		if (!directory.canWrite()) {
			return CreateWorkspaceCheckResult.CAN_NOT_WRITE;
		}
		
		if (!directory.canRead()) {
			return CreateWorkspaceCheckResult.CAN_NOT_READ;
		}
		
		File workspaceDirectory = LocalUtils.extendDirectory(directory, name);
		if (workspaceDirectory.exists()) {
			return CreateWorkspaceCheckResult.SUBDIR_ALREADY_EXISTS;
		}
		
		return CreateWorkspaceCheckResult.OKAY.setWorkspaceDirectory(workspaceDirectory);
	}
	
	private WorkspaceData initialWorkspaceData() {
		WorkspaceData result = new WorkspacePojo();
		result.setEvents(new ArrayList<Event>());
		result.setExportFolder(DEFAULT_EXPORT_DIRECTORY);
		return result;
	}

	public static void main(String[] args) throws Exception {
		
		System.out.println(Arrays.asList("24733042-7a7f-45a9-9aa7-70bb8991bc8c.mp4".split("[.]")));
		
		File dir = new File("C:\\Users\\kober\\Desktop");
		WorkspaceManager workManager = new WorkspaceManager();
		LocalWorkspace bla = workManager.createWorkspace(dir, "test_ws");
		bla.createEvent(" my event sdf ");
	}
}
