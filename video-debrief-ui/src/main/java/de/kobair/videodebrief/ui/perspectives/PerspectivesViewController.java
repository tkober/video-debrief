package de.kobair.videodebrief.ui.perspectives;

import java.util.Optional;

import de.kobair.videodebrief.ui.playback.model.AttributedEvent;

public class PerspectivesViewController {

	public interface PerspectivesDelegate {

	}

	private Optional<PerspectivesDelegate> delegate = Optional.empty();
	private AttributedEvent selectedMedia;

	public void setDelegate(final PerspectivesDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}
	
	public void setSelectedMedia(AttributedEvent selectedMedia) {
		System.out.println(this.getClass().getName());
		this.selectedMedia = selectedMedia;
	}
}
