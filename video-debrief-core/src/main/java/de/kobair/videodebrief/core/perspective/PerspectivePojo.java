package de.kobair.videodebrief.core.perspective;

import java.util.Objects;

public class PerspectivePojo implements Perspective {

	private long inPoint;
	private long outPoint;
	private long alignmentPoint;
	private String fileName;
	private String name;

	public PerspectivePojo(String name, String fileName) {
		this(name, fileName, 0, Long.MAX_VALUE, 0);
	}
	
	public PerspectivePojo(String name, String fileName, long inPoint, long outPoint, long alignmentPoint) {
		this.name = name;
		this.fileName = fileName;
		this.inPoint = inPoint;
		this.outPoint = outPoint;
		this.alignmentPoint = alignmentPoint;
	}

	@Override
	public long getInPoint() {
		return inPoint;
	}

	public void setInPoint(long inPoint) {
		this.inPoint = inPoint;
	}

	@Override
	public long getOutPoint() {
		return outPoint;
	}

	public void setOutPoint(long outPoint) {
		this.outPoint = outPoint;
	}

	@Override
	public long getAlignmentPoint() {
		return alignmentPoint;
	}

	public void setAlignmentPoint(long alignmentPoint) {
		this.alignmentPoint = alignmentPoint;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
	    	return true;
	    }
	       
	    if (obj == null) {
	    	return false;
	    }
	    
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    
	    PerspectivePojo pojo = (PerspectivePojo) obj;

	    // field comparison
	    return Objects.equals(name, pojo.name)
	            && Objects.equals(fileName, pojo.fileName);
	}
}
