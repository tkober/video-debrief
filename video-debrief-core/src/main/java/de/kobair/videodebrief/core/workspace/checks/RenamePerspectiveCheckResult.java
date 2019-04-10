package de.kobair.videodebrief.core.workspace.checks;

public enum RenamePerspectiveCheckResult {
	OKAY(null),
	NAME_EMPTY("Name must not be empty"),
	NAME_ALREADY_USED("A perspective with the name '%s' already exists");

	private final String message;

	private RenamePerspectiveCheckResult(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}

