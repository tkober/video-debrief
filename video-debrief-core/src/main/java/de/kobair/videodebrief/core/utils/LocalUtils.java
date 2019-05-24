package de.kobair.videodebrief.core.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalUtils {

	@FunctionalInterface
	public interface ProgressCallback {

		void update(double percentage);

	}

	private static class CopyJob {

		private volatile boolean keepObserving = true;

		public void copy(final File source, final File destination, final Optional<ProgressCallback> callback) throws IOException {
			final long targetSize = source.length();

			if (callback.isPresent()) {
				ProgressCallback percentageCallback = callback.get();
				copyObserverPool.execute(() -> {
					double previousPercentage = 0;
					percentageCallback.update(previousPercentage);

					while (keepObserving) {
						double actualPercentage = previousPercentage;
						if (destination.exists()) {
							long actualSize = destination.length();
							actualPercentage = Math.ceil(100.0 * (double) actualSize / (double) targetSize) / 100.0;
						} else {
							actualPercentage = 0;
						}
						if (previousPercentage != actualPercentage) {
							previousPercentage = actualPercentage;
							percentageCallback.update(actualPercentage);
						}
					}
				});
			}

			IOException ioException = null;
			try {
				FileUtils.copyFile(source, destination);
				keepObserving = false;
			} catch (IOException e) {
				ioException = e;
			} finally {
				keepObserving = false;
				if (ioException != null) {
					throw ioException;
				}
			}
		}
	}

	private static ExecutorService copyObserverPool = Executors.newCachedThreadPool();

	private static void startCopyJob(File source, File destination, Optional<ProgressCallback> progressCallback) throws IOException {
		CopyJob job = new CopyJob();
		job.copy(source, destination, progressCallback);
	}

	public static File extendDirectory(File directory, String... newParts) {
		return Paths.get(directory.getAbsolutePath(), newParts).toFile();
	}

	public static void copyFile(File source, File destination) throws IOException {
		startCopyJob(source, destination, Optional.empty());
	}

	public static void copyFile(File source, File destination, ProgressCallback progressCallback) throws IOException {
		startCopyJob(source, destination, Optional.ofNullable(progressCallback));
	}

	public enum FileSizeAccuracy {
		TWO_DECIMALS(2),
		NO_DECIMALS(0);

		private final int decimals;

		FileSizeAccuracy(final int decimals) {
			this.decimals = decimals;
		}

		public int getDecimals() {
			return decimals;
		}
	}

	@FunctionalInterface
	private interface FileSizeFormatter {
		String format(String value);
	}

	public enum FileSizeUnitFormat {
		DEFAULT(value -> value),
		UPPER_CASE(value -> value.toUpperCase()),
		LOWER_CASE(value -> value.toLowerCase());

		private final FileSizeFormatter formatter;

		FileSizeUnitFormat(final FileSizeFormatter formatter) {
			this.formatter = formatter;
		}

		protected FileSizeFormatter getFormatter() {
			return formatter;
		}
	}

	public static String smartFileSizeString(long sizeBytes) {
		return smartFileSizeString(sizeBytes, FileSizeAccuracy.TWO_DECIMALS, FileSizeUnitFormat.DEFAULT);
	}

	public static String smartFileSizeString(long sizeBytes, FileSizeAccuracy accuracy, FileSizeUnitFormat format) {
		final long KILOBYTE = 1<<10;
		final long MEGABYTE = KILOBYTE<<10;
		final long GIGABYTE = MEGABYTE<<10;

		if (sizeBytes > GIGABYTE) {
			double sizeGigabyte = sizeBytes / (double) GIGABYTE;
			return formatSize(sizeGigabyte, "GB", accuracy, format);
		}

		if (sizeBytes > MEGABYTE) {
			double sizeMegabyte = sizeBytes / (double) MEGABYTE;
			return formatSize(sizeMegabyte, "MB", accuracy, format);
		}

		if (sizeBytes > KILOBYTE) {
			double sizeKilobyte = sizeBytes / (double) KILOBYTE;
			return formatSize(sizeKilobyte, "KB", accuracy, format);
		}

		return formatSize(sizeBytes, "bytes", accuracy, format);
	}

	private static String formatSize(double size, String unit, FileSizeAccuracy accuracy, FileSizeUnitFormat format) {
		String sizeString = stringWithDecimals(size, accuracy.getDecimals());
		String result = String.format("%s %s", sizeString, unit);
		return format.getFormatter().format(result);
	}

	private static String stringWithDecimals(double value, int decimals) {
		String format = "%." + decimals + "f";
		return String.format(format, value);
	}


}
