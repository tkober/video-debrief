package de.kobair.videodebrief.core.video.ffmpeg;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.kobair.videodebrief.core.video.VideoInformation;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.probe.FFmpegStream.CodecType;

public class FfmpegInformation implements VideoInformation {

	private final FFmpegProbeResult probeResult;
	private List<VideoTrack> videoTracks = new ArrayList<>(1);
	private List<AudioTrack> audioTracks = new ArrayList<>(1);

	public FfmpegInformation(FFmpegProbeResult probeResult) {
		this.probeResult = probeResult;
		for (FFmpegStream stream : this.probeResult.getStreams()) {
			if (stream.codec_type == CodecType.VIDEO) {
				this.videoTracks.add(new FfmpegVideoTrack(stream));
			} else if (stream.codec_type == CodecType.AUDIO) {
				this.audioTracks.add(new FfmpegAudioTrack(stream));
			}
		}
	}

	@Override
	public String getFormatName() {
		return this.probeResult.getFormat().format_long_name;
	}

	@Override
	public long getDurationMillis() {
		double seconds = this.probeResult.getFormat().duration;
		return (long) (seconds * 1000);
	}

	@Override
	public long getBitRate() {
		return this.probeResult.getFormat().bit_rate;
	}

	@Override
	public List<VideoTrack> getVideoInformation() {
		return this.videoTracks;
	}

	@Override
	public List<AudioTrack> getAudioInformation() {
		return this.audioTracks;
	}

	@Override
	public long getSizeInBytes() {
		return this.probeResult.getFormat().size;
	}

	@Override
	public Object getRawInformation() {
		return this.probeResult;
	}

	@Override
	public String getRawInformationAsString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this.probeResult);
	}
}
