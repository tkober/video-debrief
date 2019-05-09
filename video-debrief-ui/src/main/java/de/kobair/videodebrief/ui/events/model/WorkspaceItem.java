package de.kobair.videodebrief.ui.events.model;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class WorkspaceItem {

	public BooleanProperty itemSelectedProperty = new SimpleBooleanProperty(false);
	
	public abstract Object getContent();

	public static class EventItem extends WorkspaceItem {
		
		private final Event event;
		
		public EventItem(Event event) {
			this.event = event;
		}
		
		@Override
		public Object getContent() {
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
		public Object getContent() {
			return this.perspective;
		}
		
		@Override
		public String toString() {
			return this.perspective.getName();
		}
	}

}
