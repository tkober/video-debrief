package de.kobair.videodebrief.core.workspace.checks;

public enum ChangeInOutPointCheckResult {

	OKAY(null),
	NEGATIVE_VALUE("Inpoint and outpoint must not be negative"),
	IN_POINT_GREATER_THAN_OR_EQUAL_OUT_POINT("The inpoint (%d) needs to be small less than the outpoint (%d)");

	private final String message;

	private ChangeInOutPointCheckResult(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
