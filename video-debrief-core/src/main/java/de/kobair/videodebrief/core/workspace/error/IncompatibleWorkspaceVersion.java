package de.kobair.videodebrief.core.workspace.error;

import java.io.File;

public class IncompatibleWorkspaceVersion extends WorkspaceException {

	private static final long serialVersionUID = 1L;

	public IncompatibleWorkspaceVersion(File workspaceDirectory, String actualVersion, String requiredVersion) {
		super(String.format(
				"The workspace '%s' could not be loaded due to an incompatible version. The required version is '%s'. Found version is '%s'",
				workspaceDirectory.getAbsolutePath(), requiredVersion, actualVersion), null);
	}

}
