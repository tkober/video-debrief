package de.kobair.videodebrief.core.export;

public class ExportException extends Exception {

	private static final long serialVersionUID = 1L;

	private final ExportCheckResult checkResult;
	
	public ExportException(ExportCheckResult checkResult, Object... params) {
		super(String.format(checkResult.getMessage(), params));
		this.checkResult = checkResult;
	}

	public ExportCheckResult getCheckResult() {
		return checkResult;
	}
	
}
