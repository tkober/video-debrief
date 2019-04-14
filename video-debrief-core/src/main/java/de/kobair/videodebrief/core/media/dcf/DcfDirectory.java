package de.kobair.videodebrief.core.media.dcf;

import  java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.kobair.videodebrief.core.formats.FileFormat;

public class DcfDirectory {

	private final File directory;
	private final String cameraName;
	
	private String cameraNameFromDirectory(File directory) {
		Pattern pattern = Pattern.compile(Dcf.DCF_DIRECTORY_REGEX);
		Matcher matcher = pattern.matcher(directory.getName());
		matcher.find();
		return  matcher.group(3);
	}
	
	public DcfDirectory(File directory) {
		this.directory = directory;
		this.cameraName = cameraNameFromDirectory(directory);
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
	
	public String getCameraName() {
		return cameraName;
	}
	
	@Override
	public String toString() {
		return directory.toString();
	}
}
