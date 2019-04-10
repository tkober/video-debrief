package de.kobair.videodebrief.core.importing;

import java.io.File;

import de.kobair.videodebrief.core.perspective.Perspective;

public class ImportResult {

	private final Perspective perspective;
	private final File file;

	public ImportResult(final Perspective perspective, final File file) {
		this.perspective = perspective;
		this.file = file;
	}

	public Perspective getPerspective() {
		return perspective;
	}

	public File getFile() {
		return file;
	}
}
