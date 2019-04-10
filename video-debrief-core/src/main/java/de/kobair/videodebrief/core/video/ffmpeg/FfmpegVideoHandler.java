package de.kobair.videodebrief.core.video.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.kobair.videodebrief.core.utils.EnvironmentUtils;
import de.kobair.videodebrief.core.utils.LocalUtils;
import de.kobair.videodebrief.core.utils.EnvironmentUtils.Variable;
import de.kobair.videodebrief.core.video.VideoHandler;
import de.kobair.videodebrief.core.video.VideoInformation;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

public class FfmpegVideoHandler implements VideoHandler {

	private final FFmpeg ffmpeg;
	private final FFprobe ffprobe;

	private FFmpegProbeResult probeFile(String mediaPath) throws IOException {
		return this.ffprobe.probe(mediaPath);
	}
	
	private FFmpegBuilder builderForSnapshot(String input, String output, long startOffsetMillis, String quality) {
		FFmpegBuilder builder = new FFmpegBuilder();

		return builder
				.addInput(input)
				.setStartOffset(startOffsetMillis, TimeUnit.MILLISECONDS)
				.addOutput(output)
				.setFrames(1)
				.addExtraArgs("-q:v", quality)
				.done();
	}
	
	private FFmpegBuilder builderForClip(String input, String output, long startOffsetMillis, long durationMillis) {
		FFmpegBuilder builder = new FFmpegBuilder();

		return builder
				.addInput(input)
				.setStartOffset(startOffsetMillis, TimeUnit.MILLISECONDS)
				.addOutput(output)
				.setDuration(durationMillis, TimeUnit.MILLISECONDS)
				.addExtraArgs("-c", "copy")
				.done();
	}

	public FfmpegVideoHandler() {
		if (!EnvironmentUtils.environmentVariableSetAndNotEmpty(Variable.FFMPEG_HOME)) {
			throw new RuntimeException();
		}
		
		File ffmpegHome = new File(EnvironmentUtils.getEnvironmentVariable(Variable.FFMPEG_HOME));
		try {
			this.ffmpeg = new FFmpeg(LocalUtils.extendDirectory(ffmpegHome, "ffmpeg").getAbsolutePath()); 
		} catch (IOException e) {
			String message = String.format("FFmpeg not found in '%s'", ffmpegHome);
			throw new RuntimeException(message, e);
		}
		
		try {
			this.ffprobe = new FFprobe(LocalUtils.extendDirectory(ffmpegHome, "ffprobe").getAbsolutePath()); 
		} catch (IOException e) {
			String message = String.format("FFprobe not found in '%s'", ffmpegHome);
			throw new RuntimeException(message, e);
		}
	}

	@Override
	public VideoInformation extractInformation(final File videoFile) throws IOException {
		FFmpegProbeResult probeResult = this.probeFile(videoFile.getAbsolutePath());
		return new FfmpegInformation(probeResult);
	}

	@Override
	public File exportSnapshot(final File videoFile, final long time, final File targetFile) throws IOException {
		FFmpegBuilder builder = this.builderForSnapshot(videoFile.getAbsolutePath(), targetFile.getAbsolutePath(), time, "1");

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
		executor.createJob(builder).run();
		
		return targetFile;
	}

	@Override
	public File exportClip(final File videoFile, final long startTime, final long duration, final File targetFile)
			throws IOException {
		FFmpegBuilder builder = this.builderForClip(videoFile.getAbsolutePath(), targetFile.getAbsolutePath(), startTime, duration);

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
		executor.createJob(builder).run();
		
		return targetFile;
	}
}
