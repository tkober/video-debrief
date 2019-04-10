package de.kobair.videodebrief.core.workspace.error;

import de.kobair.videodebrief.core.workspace.checks.AddPerspectiveCheckResult;

public class AddPerspectiveException extends WorkspaceException {

	private static final long serialVersionUID = 1L;

	private AddPerspectiveCheckResult checkResult;

	public AddPerspectiveException(AddPerspectiveCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
	}

	public AddPerspectiveCheckResult getCheckResult() {
		return checkResult;
	}
}
