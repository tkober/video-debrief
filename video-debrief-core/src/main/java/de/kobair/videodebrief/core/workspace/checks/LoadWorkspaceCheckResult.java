package de.kobair.videodebrief.core.workspace.checks;

import java.io.File;

public enum LoadWorkspaceCheckResult {
	OKAY(null, null), 
	DOES_NOT_EXISTS(null, "No such file or directory found: '%s'"), 
	NOT_A_DIRECTORY(null, "Not a directory: '%s'"), 
	CAN_NOT_READ(null, "Missing READ permission: '%s'"), 
	CAN_NOT_WRITE(null, "Missing WRITE permission: '%s'"), 
	NO_WORKSPACE_FILE(null, "Does not contain a workspace file: '%s'");
	
	private File workspaceFile = null;
	private String message;
	
	private LoadWorkspaceCheckResult(File workspaceFile, String message) {
		this.workspaceFile = workspaceFile;
		this.message = message;
	}
	
	public File getWorkspaceFile() {
		return workspaceFile;
	}
	
	public String getMessage() {
		return message;
	}
	
	public LoadWorkspaceCheckResult setWorkspaceFile(File workspaceFile) {
		this.workspaceFile = workspaceFile;
		return this;
	}
}
