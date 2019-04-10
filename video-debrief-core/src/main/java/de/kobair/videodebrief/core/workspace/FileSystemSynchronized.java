package de.kobair.videodebrief.core.workspace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface FileSystemSynchronized {
	
	void save() throws IOException;
	
	void reload() throws FileNotFoundException;
	
	File getWorkingDirectory();
	
	File getAvailableSubDirectoryInWorkingDirectory(Object information);
	
}
