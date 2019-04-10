package de.kobair.videodebrief.core.workspace;

import java.util.List;

import de.kobair.videodebrief.core.event.Event;

class WorkspacePojo implements WorkspaceData {
	
	private List<Event> events;
	private String exportFolder;
	
	@Override
	public List<Event> getEvents() {
		return events;
	}
	@Override
	public void setEvents(List<Event> events) {
		this.events = events;
	}
	@Override
	public String getExportFolder() {
		return exportFolder;
	}
	@Override
	public void setExportFolder(String exportFolder) {
		this.exportFolder = exportFolder;
	}
	
}
