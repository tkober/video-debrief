package de.kobair.videodebrief.core.media.dcf;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.utils.LocalUtils;
import net.samuelcampos.usbdrivedetector.USBStorageDevice;

public class DcfDevice {

	protected static final String DCIM_DIRECTORY_NAME = "DCIM";
	protected static final String DCF_DIRECTORY_REGEX = "(^[0-9]{3}[0-9,a-z,A-Z]{5}$)";
	
	private final USBStorageDevice device;
	private final File dcimDirectory;
	
	protected DcfDevice(USBStorageDevice verifiedDcimDevice) {
		this.device = verifiedDcimDevice;
		this.dcimDirectory = LocalUtils.extendDirectory(this.getRootDirectory(), DCIM_DIRECTORY_NAME);
	}
	
	public File getRootDirectory() {
		return device.getRootDirectory();
	}
	
	public File getDcimDirectory() {
		return dcimDirectory;
	}
	
	public String getName() {
		return device.getDeviceName();
	}
	
	public String getDisplayName() {
		return device.getDeviceName();
	}
	
	public List<DcfDirectory> getDcfDirectories() {
		List<File> files = Arrays.asList(this.getDcimDirectory().listFiles());
		List<DcfDirectory> result = files.stream()
				.filter((File file) -> isDcfDirectory(file))
				.map(DcfDirectory::new)
				.collect(Collectors.toList());
		
		return result;
	}
	
	@Override
	public String toString() {
		return String.format("(%s { name: '%s(%s)', root: '%s', dcim: '%s' })", 
				super.toString(), this.getName(), this.getDisplayName(), this.getRootDirectory(), 
				this.dcimDirectory);
	}
	
	private boolean isDcfDirectory(File file) {
		if (!file.exists() || !file.isDirectory()) {
			return false;
		}
		
		boolean matchesDcfNameFormat = file.getName().matches(DCF_DIRECTORY_REGEX);
		
		return matchesDcfNameFormat;
	}
}
