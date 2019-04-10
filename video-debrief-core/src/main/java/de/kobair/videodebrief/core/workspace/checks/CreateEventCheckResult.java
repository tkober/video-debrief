package de.kobair.videodebrief.core.workspace.checks;

public enum CreateEventCheckResult {
	OKAY(null),
	NAME_EMPTY("Name must not be empty"),
	NAME_ALREADY_USED("An event with the name '%s' already exists");
	
	private final String message;
	
	private CreateEventCheckResult(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
