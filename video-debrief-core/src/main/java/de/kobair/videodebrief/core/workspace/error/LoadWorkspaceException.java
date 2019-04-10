package de.kobair.videodebrief.core.workspace.error;

import de.kobair.videodebrief.core.workspace.checks.LoadWorkspaceCheckResult;

public class LoadWorkspaceException extends WorkspaceException {
	
	private static final long serialVersionUID = 1L;
	private LoadWorkspaceCheckResult checkResult;
	
	public LoadWorkspaceCheckResult getCheckResult() {
		return checkResult;
	}
	
	public LoadWorkspaceException(LoadWorkspaceCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
		this.checkResult = checkResult;
	}
}
