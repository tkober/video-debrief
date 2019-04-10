package de.kobair.videodebrief.core.media;

import java.io.File;

import de.kobair.videodebrief.core.formats.FileFormat;

public class UnverifiedMediaFile implements MediaFile {

	private final File file;
	private final FileFormat format;

	public UnverifiedMediaFile(File file, FileFormat format) {
		this.file = file;
		this.format = format;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public FileFormat getFormat() {
		return format;
	}

}
