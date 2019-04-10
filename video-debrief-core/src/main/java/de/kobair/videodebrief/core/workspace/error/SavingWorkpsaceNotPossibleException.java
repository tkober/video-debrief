package de.kobair.videodebrief.core.workspace.error;

public class SavingWorkpsaceNotPossibleException extends UnknownWorkspaceException {

	private static final long serialVersionUID = 1L;
	
	public SavingWorkpsaceNotPossibleException(String message, Exception cause) {
		super(message, cause);
	}
	
}
