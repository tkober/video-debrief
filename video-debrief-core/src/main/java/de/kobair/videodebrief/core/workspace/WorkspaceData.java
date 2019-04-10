package de.kobair.videodebrief.core.workspace;

import java.util.List;

import de.kobair.videodebrief.core.event.Event;

interface WorkspaceData {

	public List<Event> getEvents();
	
	public void setEvents(List<Event> events);
	
	public String getExportFolder();
	
	public void setExportFolder(String exportFolder);
	
}
