package de.kobair.videodebrief.core.workspace.error;

import de.kobair.videodebrief.core.workspace.checks.CreateEventCheckResult;

public class CreateEventException extends WorkspaceException {

	private static final long serialVersionUID = 1L;
	
	private CreateEventCheckResult checkResult;
	
	public CreateEventException(CreateEventCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
	}
	
	public CreateEventCheckResult getCheckResult() {
		return checkResult;
	}
}