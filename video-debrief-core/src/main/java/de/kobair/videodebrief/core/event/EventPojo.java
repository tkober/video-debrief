package de.kobair.videodebrief.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.perspective.PerspectivePojo;

public class EventPojo implements Event {
	
	private String name;
	private String subPath;
	private Map<String, PerspectivePojo> perspectives;
	
	public EventPojo(String name, String subPath, Map<String, PerspectivePojo> perspectives) {
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
	
	public Map<String, PerspectivePojo> getPerspectives() {
		return perspectives;
	}
	
	public void setPerspectives(Map<String, PerspectivePojo> perspectives) {
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
