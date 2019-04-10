package de.kobair.videodebrief.core.workspace.error;

import de.kobair.videodebrief.core.workspace.checks.ChangeInOutPointCheckResult;

public class ChangeInOutPointException extends WorkspaceException {

	private static final long serialVersionUID = 1L;

	private ChangeInOutPointCheckResult checkResult;

	public ChangeInOutPointException(ChangeInOutPointCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
	}

	public ChangeInOutPointCheckResult getCheckResult() {
		return checkResult;
	}
}
