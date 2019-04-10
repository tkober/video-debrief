package de.kobair.videodebrief.core.workspace.checks;

public enum AddPerspectiveCheckResult {
	OKAY(null),
	NAME_EMPTY("Name must not be empty"),
	NAME_ALREADY_USED("A perspective with the name '%s' already exists"),
	DOES_NOT_EXIST("No such file or directory found: '%s'"),
	IS_NOT_A_FILE("Not a directory: '%s'"),
	CAN_NOT_READ("Missing READ permission: '%s'"),
	NOT_IN_EVENT_DIRECTORY("The given file '%s' is not located in the correct event directory"),
	INVALID_FORMAT("Given file '%s' does not match the required naming format"),
	ALREADY_USED("Given file '%s' is already being used in another event");
	
	private final String message;
	
	private AddPerspectiveCheckResult(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
