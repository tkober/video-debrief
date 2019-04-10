package de.kobair.videodebrief.core.importing.error;

import de.kobair.videodebrief.core.importing.checks.ImportCheckResult;

public class ImportException extends Exception {

	private static final long serialVersionUID = 1L;

	private ImportCheckResult checkResult;

	public ImportException(ImportCheckResult checkResult, Object... messageParams) {
		super(String.format(checkResult.getMessage(), messageParams), null);
	}

	public ImportCheckResult getCheckResult() {
		return checkResult;
	}
}
