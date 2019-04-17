package de.kobair.videodebrief.core.workspace;

import java.util.List;

import de.kobair.videodebrief.core.event.EventPojo;

class WorkspacePojo implements WorkspaceData {
	
	private List<EventPojo> events;
	private String exportFolder;
	private String version = Workspace.WORKSPACE_VERSION;
	
	@Override
	public List<EventPojo> getEvents() {
		return events;
	}
	
	@Override
	public void setEvents(List<EventPojo> events) {
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
	
	@Override
	public String getVersion() {
		return version;
	}
}
