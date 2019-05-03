package de.kobair.videodebrief.ui.playback.model;

import java.io.File;

import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.video.VideoInformation;

public class AttributedPerspective {

	private final Perspective perspective;
	private final VideoInformation videoInformation;
	private final File videoFile;

	public AttributedPerspective(Perspective perspective, VideoInformation videoInformation, File videoFile) {
		this.perspective = perspective;
		this.videoInformation = videoInformation;
		this.videoFile = videoFile;
	}
	
	public Perspective getPerspective() {
		return perspective;
	}
	
	public VideoInformation getVideoInformation() {
		return videoInformation;
	}

	public File getVideoFile() {
		return videoFile;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		AttributedPerspective ap = (AttributedPerspective) obj;
		return this.getPerspective().equals(ap.getPerspective());
	}

	@Override
	public String toString() {
		return this.getPerspective().getName();
	}
}
