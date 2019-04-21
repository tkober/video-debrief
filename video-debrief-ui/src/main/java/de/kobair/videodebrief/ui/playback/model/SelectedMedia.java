package de.kobair.videodebrief.ui.playback.model;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.workspace.Workspace;

public class SelectedMedia {

	private final Workspace workspace;
	private final Event event;
	private final Perspective perspective;
	
	public SelectedMedia(Workspace workspace, Event event, Perspective perspective) {
		this.workspace = workspace;
		this.event = event;
		this.perspective = perspective;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public Perspective getPerspective() {
		return perspective;
	}
	
}
