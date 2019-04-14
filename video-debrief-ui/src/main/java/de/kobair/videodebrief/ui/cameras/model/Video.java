package de.kobair.videodebrief.ui.cameras.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.kobair.videodebrief.core.media.dcf.DcfMediaFile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Video {
	
	private final DcfMediaFile mediaFile;
	
	private final StringProperty nameProperty;
	private final StringProperty timestampProperty;
	private final StringProperty formatNameProperty;
	
	
	public Video(DcfMediaFile mediaFile) {
		this.mediaFile = mediaFile;
		
		final File file = mediaFile.getFile();
		Date lastModifiedDate = new Date(file.lastModified());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timestamp = simpleDateFormat.format(lastModifiedDate);
		
		this.nameProperty = new SimpleStringProperty(file.getName());
		this.timestampProperty = new SimpleStringProperty(timestamp);
		this.formatNameProperty = new SimpleStringProperty(mediaFile.getFormat().getName());
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
	
	@Override
	public String toString() {
		return this.nameProperty.get() + " " + this.formatNameProperty.get() + " " + this.timestampProperty.get();
	}
}
