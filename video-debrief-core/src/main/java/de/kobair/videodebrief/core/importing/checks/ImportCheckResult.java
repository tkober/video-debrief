package de.kobair.videodebrief.core.importing.checks;

import de.kobair.videodebrief.core.formats.FileFormat;

public enum ImportCheckResult {
	OKAY(null), // TODO
	SOURCE_DOES_NOT_EXIST(""),
	SOURCE_NOT_A_FILE(""),
	UNSUPPORTED_FILE_FORMAT(""),
	SOURCE_NOT_READABLE(""),
	DESTINATION_DOES_NOT_EXIST(""),
	DESTINATION_NOT_A_DIRECTORY(""),
	DESTINATION_NOT_WRITALBE("");

	private String message;
	private FileFormat fileFormat;

	private ImportCheckResult(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public ImportCheckResult setFileFormat(FileFormat fileFormat) {
		this.fileFormat = fileFormat;
		return this;
	}

	public FileFormat getFileFormat() {
		return fileFormat;
	}
}
