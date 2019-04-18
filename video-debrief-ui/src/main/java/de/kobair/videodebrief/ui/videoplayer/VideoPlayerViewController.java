package de.kobair.videodebrief.ui.videoplayer;

import java.util.Optional;

public class VideoPlayerViewController {

	public interface VideoPlayerDelegate {

	}

	private Optional<VideoPlayerDelegate> delegate = Optional.empty();

	public void setDelegate(final VideoPlayerDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}

}
