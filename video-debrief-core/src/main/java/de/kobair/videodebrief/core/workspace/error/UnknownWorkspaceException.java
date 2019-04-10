package de.kobair.videodebrief.core.workspace.error;

public class UnknownWorkspaceException extends WorkspaceException {
	
	private static final long serialVersionUID = 1L;
	
	public UnknownWorkspaceException(String message, Exception cause) {
		super(message, cause);
	}
}
