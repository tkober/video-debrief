package de.kobair.videodebrief.core.media.dcf;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.utils.LocalUtils;
import net.samuelcampos.usbdrivedetector.USBDeviceDetectorManager;
import net.samuelcampos.usbdrivedetector.USBStorageDevice;
import net.samuelcampos.usbdrivedetector.events.IUSBDriveListener;
import net.samuelcampos.usbdrivedetector.events.USBStorageEvent;

public class DcfDetector implements IUSBDriveListener {

	public interface DcfDetectorDelegate {

		public void dcfDetectionStarted(DcfDetector dcimDetector);

		public void dcfDeviceConnected(DcfDetector dcimDetector, DcfDevice device);

		public void usbDeviceRemoved(DcfDetector dcimDetector);

		public void dcfDetectionStopped(DcfDetector dcimDetector);

	}

	private final USBDeviceDetectorManager detectorManager;
	private DcfDetectorDelegate delegate;

	public DcfDetector(long intervalMillis) {
		this.detectorManager = new USBDeviceDetectorManager(intervalMillis);

	}

	public void start() {
		if (!detectorManager.addDriveListener(this)) {
			throw new RuntimeException("TODO"); // TODO
		} else {
			if (delegate != null) {
				delegate.dcfDetectionStarted(this);
			}
		}
	}

	public void stop() {
		if (!detectorManager.removeDriveListener(this)) {
			throw new RuntimeException("TODO"); // TODO
		} else {
			if (delegate != null) {
				delegate.dcfDetectionStopped(this);
			}
		}
	}

	public List<DcfDevice> getConnectedDevices() {
		List<DcfDevice> result = this.detectorManager.getRemovableDevices().stream()
				.filter((USBStorageDevice device) -> isDcfDevice(device))
				.map(DcfDevice::new)
				.collect(Collectors.toList());
		return result;
	}

	public DcfDetectorDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(DcfDetectorDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	public void usbDriveEvent(USBStorageEvent event) {
		switch (event.getEventType()) {
		case CONNECTED:
			if (isDcfDevice(event.getStorageDevice())) {
				DcfDevice device = new DcfDevice(event.getStorageDevice());
				if (delegate != null) {
					delegate.dcfDeviceConnected(this, device);
				}
			}
			break;

		case REMOVED:
			if (delegate != null) {
				delegate.usbDeviceRemoved(this);
			}
			break;
		}
	}

	private boolean isDcfDevice(USBStorageDevice device) {
		if (device == null) {
			return false;
		}
		File dcimDirectory = LocalUtils.extendDirectory(device.getRootDirectory(), Dcf.DCIM_DIRECTORY_NAME);
		return dcimDirectory.exists() && dcimDirectory.isDirectory();
	}
}
