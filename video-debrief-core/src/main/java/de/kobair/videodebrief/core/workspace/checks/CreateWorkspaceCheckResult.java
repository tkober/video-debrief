package de.kobair.videodebrief.core.workspace.checks;

import java.io.File;

public enum CreateWorkspaceCheckResult {
	OKAY(null, null), 
	DOES_NOT_EXISTS(null, "No such file or directory found: '%s'"), 
	NOT_A_DIRECTORY(null, "Not a directory: '%s'"), 
	CAN_NOT_READ(null, "Missing READ permission: '%s'"),
	CAN_NOT_WRITE(null, "Missing WRITE permission: '%s'"),
	SUBDIR_ALREADY_EXISTS(null, "Directory already exists: '%s'");
	
	private File workspaceDirectory;
	private String message;
	
	private CreateWorkspaceCheckResult(File workspaceDirectory, String message) {
		this.workspaceDirectory = workspaceDirectory;
		this.message = message;
	}
	
	public File getWorkspaceDirectory() {
		return workspaceDirectory;
	}
	
	public String getMessage() {
		return message;
	}
	
	public CreateWorkspaceCheckResult setWorkspaceDirectory(File workspaceDirectory) {
		this.workspaceDirectory = workspaceDirectory;
		return this;
	}
}
