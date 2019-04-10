package de.kobair.videodebrief.core.workspace.error;

import de.kobair.videodebrief.core.workspace.checks.RenameEventCheckResult;

public class RenameEventException extends WorkspaceException {

	private static final long serialVersionUID = 1L;

	private RenameEventCheckResult checkResult;

	public RenameEventException(RenameEventCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
	}

	public RenameEventCheckResult getCheckResult() {
		return checkResult;
	}
}