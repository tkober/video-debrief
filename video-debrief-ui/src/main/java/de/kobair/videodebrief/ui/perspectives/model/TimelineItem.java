package de.kobair.videodebrief.ui.perspectives.model;

import de.kobair.videodebrief.ui.playback.model.AttributedPerspective;

public class TimelineItem {

	private final AttributedPerspective aatributedPerspective;
	private final Long relativeOffset;

	public TimelineItem(final AttributedPerspective aatributedPerspective, final Long relativeOffset) {
		this.aatributedPerspective = aatributedPerspective;
		this.relativeOffset = relativeOffset;
	}

	public AttributedPerspective getAatributedPerspective() {
		return aatributedPerspective;
	}

	public Long getRelativeOffset() {
		return relativeOffset;
	}

	public long getOverallLength() {
		return this.relativeOffset + this.getAatributedPerspective().getVideoInformation().getDurationMillis();
	}
}
