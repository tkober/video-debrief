package de.kobair.videodebrief.core.media.dcf;

import java.io.File;

import de.kobair.videodebrief.core.formats.FileFormat;
import de.kobair.videodebrief.core.media.MediaFile;

public class DcfMediaFile implements MediaFile {

	private final File file;
	private final FileFormat format;

	public DcfMediaFile(File file, FileFormat format) {
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
