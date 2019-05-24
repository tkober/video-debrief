package de.kobair.videodebrief.ui.cameras.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.kobair.videodebrief.core.media.dcf.DcfMediaFile;
import de.kobair.videodebrief.core.utils.LocalUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Video {
	
	private final DcfMediaFile mediaFile;
	
	private final StringProperty nameProperty;
	private final StringProperty timestampProperty;
	private final StringProperty formatNameProperty;
	private final StringProperty sizeProperty;
	
	public Video(DcfMediaFile mediaFile) {
		this.mediaFile = mediaFile;
		
		final File file = mediaFile.getFile();
		Date lastModifiedDate = new Date(file.lastModified());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String timestamp = simpleDateFormat.format(lastModifiedDate);
		String size = LocalUtils.smartFileSizeString(file.length(), LocalUtils.FileSizeAccuracy.NO_DECIMALS, LocalUtils.FileSizeUnitFormat.DEFAULT);
		
		this.nameProperty = new SimpleStringProperty(file.getName().split("[.]")[0]);
		this.timestampProperty = new SimpleStringProperty(timestamp);
		this.formatNameProperty = new SimpleStringProperty(mediaFile.getFormat().getName());
		this.sizeProperty = new SimpleStringProperty(size);
	}
	
	public DcfMediaFile getMediaFile() {
		return mediaFile;
	}
	
	public StringProperty getNameProperty() {
		return nameProperty;
	}
	
	public StringProperty getTimestampProperty() {
		return timestampProperty;
	}
	
	public StringProperty getFormatNameProperty() {
		return formatNameProperty;
	}

	public StringProperty getSizeProperty() {
		return sizeProperty;
	}

	public String getName() {
		return this.nameProperty.get();
	}

	public Long getSize() {
		return this.mediaFile.getFile().length();
	}

	public Long getTimestampMillis() {
		return this.mediaFile.getFile().lastModified();
	}

	@Override
	public String toString() {
		return this.nameProperty.get() + " " + this.formatNameProperty.get() + " " + this.timestampProperty.get();
	}
}
