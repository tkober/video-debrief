package de.kobair.videodebrief.core.export;

public enum ExportCheckResult {// TODO
	OKAY(null),
	CONTAINING_DIRECTORY_DOES_NOT_EXIST(""),
	MISSING_WRITE_PERMISSION(""),
	FILE_ALREADY_EXISTS(""),
	DIRECTORY_ALREADY_EXISTS(""),
	WORKSPACE_NULL(""),
	NOT_SAME_WORKSPACE(""),
	NOT_SAME_EVENT("");

	private String message;

	private ExportCheckResult(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
