package de.kobair.videodebrief.core.video;

import java.util.List;

public interface VideoInformation {

	public  interface Track {

		public String getCodecName();

	}

	public interface VideoTrack extends Track {

		public int getHeight();

		public int getWidth();

		public String getDisplayAspectRatio();

		public double getFramesPerSecond();

		public long getNumberOfFrames();

		public long getBitRate();
	}

	public interface AudioTrack extends Track {

		public long getSampleRate();

		public long getNumberOfChannels();

		public String getChannelLayout();
	}

	public String getFormatName();

	public long getDurationMillis();

	public long getBitRate();

	public List<VideoTrack> getVideoInformation();

	public List<AudioTrack> getAudioInformation();

	public Object getRawInformation();

	public  long getSizeInBytes();

}
