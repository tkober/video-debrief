package de.kobair.videodebrief.core.importing.error;

public class UnknownImportException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public UnknownImportException(String message, Exception cause) {
		super(message, cause);
	}

}
