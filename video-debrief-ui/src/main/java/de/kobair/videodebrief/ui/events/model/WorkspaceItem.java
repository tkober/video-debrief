package de.kobair.videodebrief.ui.events.model;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;

public abstract class WorkspaceItem {
	
	public abstract Object getData();

	public static class EventItem extends WorkspaceItem {
		
		private final Event event;
		
		public EventItem(Event event) {
			this.event = event;
		}
		
		@Override
		public Object getData() {
			return this.event;
		}
		
		@Override
		public String toString() {
			return this.event.getName();
		}
	}

	public static class PerspectiveItem extends WorkspaceItem {
		
		private final Perspective perspective;
		
		public PerspectiveItem(Perspective perspective) {
			this.perspective = perspective;
		}
		
		@Override
		public Object getData() {
			return this.perspective;
		}
		
		@Override
		public String toString() {
			return this.perspective.getName();
		}
	}

}
