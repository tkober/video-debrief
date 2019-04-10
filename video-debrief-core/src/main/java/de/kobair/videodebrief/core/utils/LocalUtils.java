package de.kobair.videodebrief.core.utils;

import java.io.File;
import java.nio.file.Paths;

public class LocalUtils {

	public static File extendDirectory(File directory, String... newParts) {
		return Paths.get(directory.getAbsolutePath(), newParts).toFile();
	}
	
}
