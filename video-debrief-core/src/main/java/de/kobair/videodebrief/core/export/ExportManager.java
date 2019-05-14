package de.kobair.videodebrief.core.export;

import java.io.File;
import java.util.List;
import java.util.Optional;

import de.kobair.videodebrief.core.event.Event;
import de.kobair.videodebrief.core.operations.Operation;
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

	public class ClipDescriptor extends ExportDescriptor {

		private final long begin;
		private final long duration;

		public ClipDescriptor(Workspace workspace, Event event, Perspective perspective, long begin, long duration) {
			super(workspace, event, perspective);
			this.begin = begin;
			this.duration = duration;
		}

		public long getBegin() {
			return begin;
		}

		public long getDuration() {
			return duration;
		}
	}

	public void exportSnapshot(ExportDescriptor exportDescriptor, long time, File exportFile, Optional<Operation> operation)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException;

	public void exportClip(List<ClipDescriptor> clipDescriptors, File exportDirectory, Optional<Operation> operation)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException;

	public void exportWorkspace(List<ExportDescriptor> exportDescriptors, File exportDirectory, Optional<Operation> operation)
			throws UnknownWorkspaceException, UnknwonExportException, ExportException;

}
