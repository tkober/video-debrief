package de.kobair.videodebrief.core.workspace.error;

import de.kobair.videodebrief.core.workspace.checks.CreateWorkspaceCheckResult;

public class CreateWorkspaceException extends WorkspaceException {
	
	private static final long serialVersionUID = 1L;
	private CreateWorkspaceCheckResult checkResult;
	
	public CreateWorkspaceCheckResult getCheckResult() {
		return checkResult;
	}
	
	public CreateWorkspaceException(CreateWorkspaceCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
		this.checkResult = checkResult;
	}
}
