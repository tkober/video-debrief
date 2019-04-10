package de.kobair.videodebrief.core.media.dcf;

import  java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.formats.FileFormat;

public class DcfDirectory {

	private final File directory;
	
	public DcfDirectory(File directory) {
		this.directory = directory;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	public List<DcfMediaFile> getFilesForFormats(FileFormat... formats) {
		List<DcfMediaFile> result = new ArrayList<DcfMediaFile>();
		
		for (FileFormat format : formats) {
			List<DcfMediaFile> matches = Arrays.asList(this.getDirectory()
					.listFiles(format.getFilenameFilter()))
					.stream()
					.map((File file) -> new DcfMediaFile(file, format))
					.collect(Collectors.toList());
			result.addAll(matches);
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return directory.toString();
	}
}
