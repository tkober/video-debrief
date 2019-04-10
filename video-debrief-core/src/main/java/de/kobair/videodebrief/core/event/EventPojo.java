package de.kobair.videodebrief.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.kobair.videodebrief.core.perspective.Perspective;

public class EventPojo implements Event {
	
	private String name;
	private String subPath;
	private Map<String, Perspective> perspectives;
	
	public EventPojo(String name, String subPath, Map<String, Perspective> perspectives) {
		this.name = name;
		this.subPath = subPath;
		this.perspectives = perspectives;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getSubPath() {
		return subPath;
	}
	
	public void setSubPath(String subPath) {
		this.subPath = subPath;
	}
	
	public Map<String, Perspective> getPerspectives() {
		return perspectives;
	}
	
	public void setPerspectives(Map<String, Perspective> perspectives) {
		this.perspectives = perspectives;
	}

	@Override
	public List<String> getPerspectivesIds() {
		return new ArrayList<>(this.perspectives.keySet());
	}

	@Override
	public boolean containsPerspectiveWithId(String id) {
		return this.perspectives.containsKey(id);
	}

	@Override
	public Perspective getPerspectiveById(String id) {
		return this.perspectives.get(id);
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
}
