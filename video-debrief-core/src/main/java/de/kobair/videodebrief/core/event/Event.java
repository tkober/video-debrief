package de.kobair.videodebrief.core.event;

import java.util.List;

import de.kobair.videodebrief.core.perspective.Perspective;

public interface Event {
	
	public String getName();
	
	public String getSubPath();
	
	public List<String> getPerspectivesIds();
	
	public boolean containsPerspectiveWithId(String id);
	
	public Perspective getPerspectiveById(String id);
	
}
