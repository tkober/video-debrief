package de.kobair.videodebrief.core.video.ffmpeg;

import de.kobair.videodebrief.core.video.VideoInformation.AudioTrack;
import net.bramp.ffmpeg.probe.FFmpegStream;

public class FfmpegAudioTrack implements AudioTrack {

	private final FFmpegStream stream;

	public FfmpegAudioTrack(FFmpegStream stream) {
		this.stream = stream;
	}

	@Override
	public String getCodecName() {
		return this.stream.codec_name;
	}

	@Override
	public long getSampleRate() {
		return this.stream.sample_rate;
	}

	@Override
	public long getNumberOfChannels() {
		return this.stream.channels;
	}

	@Override
	public String getChannelLayout() {
		return this.stream.channel_layout;
	}
	
}
