package de.kobair.videodebrief.ui.perspectives;

import java.util.Optional;

public class PerspectivesViewController {

	public interface PerspectivesDelegate {

	}

	private Optional<PerspectivesDelegate> delegate = Optional.empty();

	public void setDelegate(final PerspectivesDelegate delegate) {
		this.delegate = Optional.ofNullable(delegate);
	}
}
