package de.kobair.videodebrief.ui.cameras.model;

import de.kobair.videodebrief.core.media.dcf.DcfDevice;
import de.kobair.videodebrief.core.media.dcf.DcfDirectory;

public class Camera {

	private final DcfDevice device;
	private final DcfDirectory directory;
	
	public Camera(DcfDevice device, DcfDirectory directory) {
		this.device = device;
		this.directory = directory;
	}
	
	@Override
	public String toString() {

		return directory.getCameraName() + " (" + device.getDisplayName() + ")";
	}
	
	public DcfDevice getDevice() {
		return device;
	}
	
	public DcfDirectory getDirectory() {
		return directory;
	}
}
