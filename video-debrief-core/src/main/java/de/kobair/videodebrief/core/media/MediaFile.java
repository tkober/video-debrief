package de.kobair.videodebrief.core.media;

import java.io.File;

import de.kobair.videodebrief.core.formats.FileFormat;

public interface MediaFile {

	public File getFile();

	public FileFormat getFormat();

}
