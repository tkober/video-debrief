package de.kobair.videodebrief.core.video.ffmpeg;

import org.apache.commons.lang3.math.Fraction;

import de.kobair.videodebrief.core.video.VideoInformation.VideoTrack;
import net.bramp.ffmpeg.probe.FFmpegStream;

public class FfmpegVideoTrack implements VideoTrack {
	
	private final FFmpegStream stream;

	public FfmpegVideoTrack(FFmpegStream stream) {
		this.stream = stream;
	}

	@Override
	public String getCodecName() {
		return this.stream.codec_name;
	}

	@Override
	public int getHeight() {
		return this.stream.height;
	}

	@Override
	public int getWidth() {
		return this.stream.width;
	}

	@Override
	public String getDisplayAspectRatio() {
		return this.stream.display_aspect_ratio;
	}

	@Override
	public double getFramesPerSecond() {
		Fraction fraction = this.stream.r_frame_rate;
		double numerator = (double) fraction.getNumerator();
		double denominator = (double) fraction.getDenominator();
		return numerator / denominator;
	}

	@Override
	public long getNumberOfFrames() {
		return this.stream.nb_frames;
	}

	@Override
	public long getBitRate() {
		return this.stream.bit_rate;
	}
	
}
