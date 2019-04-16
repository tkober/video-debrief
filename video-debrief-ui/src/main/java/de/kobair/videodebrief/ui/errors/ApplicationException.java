package de.kobair.videodebrief.ui.errors;

import javafx.application.Platform;

public abstract class ApplicationException extends RuntimeException {

	public ApplicationException(Exception cause) {
		super(cause);
	}

	public void throwOnMainThread() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				throw ApplicationException.this;
			}
		});
	}
}
