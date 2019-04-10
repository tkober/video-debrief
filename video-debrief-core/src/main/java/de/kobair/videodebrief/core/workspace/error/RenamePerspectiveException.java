package de.kobair.videodebrief.core.workspace.error;

import de.kobair.videodebrief.core.workspace.checks.RenamePerspectiveCheckResult;

public class RenamePerspectiveException extends WorkspaceException {

	private static final long serialVersionUID = 1L;

	private RenamePerspectiveCheckResult checkResult;

	public RenamePerspectiveException(RenamePerspectiveCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
	}

	public RenamePerspectiveCheckResult getCheckResult() {
		return checkResult;
	}
}