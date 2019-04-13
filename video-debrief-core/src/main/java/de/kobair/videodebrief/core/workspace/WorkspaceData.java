package de.kobair.videodebrief.core.workspace;

import java.util.List;

import de.kobair.videodebrief.core.event.EventPojo;

interface WorkspaceData {

	public List<EventPojo> getEvents();
	
	public void setEvents(List<EventPojo> events);
	
	public String getExportFolder();
	
	public void setExportFolder(String exportFolder);
	
}
