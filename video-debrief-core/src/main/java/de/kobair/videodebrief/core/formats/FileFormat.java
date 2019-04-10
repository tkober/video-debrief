package de.kobair.videodebrief.core.formats;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

public enum FileFormat {

	WORKSPACE("Workspace", true, ".vdworkspace"), 
	
	MPEG4_CONTAINER("MPEG-4", false, ".mp4", ".m4a", ".m4p", ".m4b", ".m4r", ".m4v"),
	FLV_CONTAINER("FLV", false, "flv", ".f4v", ".f4p", ".f4a", ".f4b");
	
	private final String name;
	private final List<String> fileExtensions;
	private final boolean caseSensitive;

	private FileFormat(String name, boolean caseSensitive, String... fileExtensions) {
		this.name = name;
		this.caseSensitive = caseSensitive;
		assert fileExtensions.length > 0;
		this.fileExtensions = Arrays.asList(fileExtensions);
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getFileExtensions() {
		return fileExtensions;
	}
	
	public FilenameFilter getFilenameFilter() {
		return new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				for (String fileExtension : fileExtensions) {
					
					if (caseSensitive) {
						if (name.endsWith(fileExtension)) {
							return true;
						}
					} else {
						if (name.toLowerCase().endsWith(fileExtension.toLowerCase())) {
							return true;
						}
					}
				}
				
				return false;
			}
		};
	}
	
	public static final FileFormat[] VIDEO_FORMATS = { 
		MPEG4_CONTAINER,
		FLV_CONTAINER
	};

	public String getDefaultFileExtension() {
		return this.fileExtensions.get(0);
	}
}
