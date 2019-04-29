package de.kobair.videodebrief.ui.playback.model;

import java.util.List;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.workspace.Workspace;

public class AttributedEvent {

	private final Workspace workspace;
	private final Event event;
	private final List<AttributedPerspective> perspectives;
	
	public AttributedEvent(Workspace workspace, Event event, List<AttributedPerspective> perspectives) {
		this.workspace = workspace;
		this.event = event;
		this.perspectives = perspectives;
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	public Event getEvent() {
		return event;
	}
	
	public List<AttributedPerspective> getPerspectives() {
		return perspectives;
	}

}
