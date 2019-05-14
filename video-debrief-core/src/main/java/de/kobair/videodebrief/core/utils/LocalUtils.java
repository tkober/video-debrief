package de.kobair.videodebrief.core.utils;

import de.kobair.videodebrief.core.operations.Operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public class LocalUtils {

	public static File extendDirectory(File directory, String... newParts) {
		return Paths.get(directory.getAbsolutePath(), newParts).toFile();
	}

	public static void copyFile(File source, File destination, Optional<Operation> operation) throws IOException {
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		long length = source.length();
		long counter = 0;
		int r = 0;
		byte[] b = new byte[8096];
		double progress = 0;

		try {
			inputStream = new FileInputStream(source);
			outputStream = new FileOutputStream(destination);
			while ((r = inputStream.read(b)) != -1) {
				counter += r;
				double newProgress = Math.ceil(100.0 * counter / length) / 100.0;
				if (progress != newProgress) {
					progress = newProgress;
					final double _progress = progress;
					operation.ifPresent(op -> op.updateProgress(_progress));
				}
				outputStream.write(b, 0, r);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			inputStream.close();
			outputStream.close();
		}
	}
}
