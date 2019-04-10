package de.kobair.videodebrief.core.video;

import java.io.File;
import java.io.IOException;

public interface VideoHandler {

	public VideoInformation extractInformation(File videoFile) throws IOException;

	public File exportSnapshot(File videoFile, long time, File targetFile) throws IOException;

	public File exportClip(File videoFile, long startTime, long duration, File targetFile) throws IOException;

}
