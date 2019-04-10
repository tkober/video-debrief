package de.kobair.videodebrief.core.export;

import java.io.File;
import java.util.List;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.perspective.Perspective;
import de.kobair.videodebrief.core.workspace.Workspace;
import de.kobair.videodebrief.core.workspace.error.UnknownWorkspaceException;

public interface ExportManager {

	public class ExportDescriptor {

		private final Workspace workspace;
		private final Event event;
		private final Perspective perspective;

		public ExportDescriptor(Workspace workspace, Event event, Perspective perspective) {
			assert workspace != null;
			assert event != null;
			assert perspective != null;

			this.workspace = workspace;
			this.event = event;
			this.perspective = perspective;
		}

		public Workspace getWorkspace() {
			return workspace;
		}

		public Event getEvent() {
			return event;
		}

		public Perspective getPerspective() {
			return perspective;
		}
	}

	public void exportSnapshot(ExportDescriptor exportDescriptor, long time, File exportFile)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException;

	public void exportClip(List<ExportDescriptor> exportDescriptors, long begin, long duration, File exportDirectory)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException;

	public void exportWorkspace(List<ExportDescriptor> exportDescriptors, File exportDirectory)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException;

}
