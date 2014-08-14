/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Static class to open a standalone merge editor for a given file.
 */
public class SyncMergeEditor {
	/**
	 * Open merge editor for the given file. Note that this is the only public method.
	 * 
	 * @param file
	 */
	public static void open(IFile file) {
		if (file == null) {
			return;
		}
		CompareUI.openCompareEditor(new FileCompareInput(file));
	}

	private static class FileCompareInput extends SaveableCompareEditorInput {
		private final IFile file;

		public FileCompareInput(IFile f) {
			super(createCompareConfiguration(), getPage());
			file = f;
		}

		private static CompareConfiguration createCompareConfiguration() {
			CompareConfiguration conf = new CompareConfiguration();
			conf.setLeftEditable(true);
			conf.setRightEditable(false);
			return conf;
		}

		private static IWorkbenchPage getPage() {
			try {
				return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			} catch (NullPointerException e) {
				throw new RuntimeException(Messages.SyncMergeEditor_0);
			}
		}

		@Override
		protected ICompareInput prepareCompareInput(IProgressMonitor monitor) throws InvocationTargetException,
				InterruptedException {
			IProject project = file.getProject();
			String currentSyncServiceId = SyncConfigManager.getActive(project).getSyncProviderId();
			ISynchronizeService syncService = SyncManager.getSyncService(currentSyncServiceId);
			SyncConfig syncConfig = SyncConfigManager.getActive(project);
			String[] mergeParts = null;

			try {
				mergeParts = syncService.getMergeConflictParts(project, file);
			} catch (CoreException e) {
				RDTSyncUIPlugin.log(e);
				return new DiffNode(null, Differencer.CONFLICTING, new SyncMergeItem(Messages.SyncMergeEditor_1),
						new SyncMergeItem(Messages.SyncMergeEditor_1), new SyncMergeItem(Messages.SyncMergeEditor_1));
			}

			if (mergeParts == null) {
				return new DiffNode(null, Differencer.CONFLICTING, new SyncMergeItem(Messages.SyncMergeEditor_2),
						new SyncMergeItem(Messages.SyncMergeEditor_2), new SyncMergeItem(Messages.SyncMergeEditor_2));
			} else {
				ITypedElement fileElement = SaveableCompareEditorInput.createFileElement(file);
				return new DiffNode(null, Differencer.CONFLICTING, new SyncMergeItem(mergeParts[2]), fileElement,
						new SyncMergeItem(mergeParts[1]));
			}
		}

		@Override
		protected void fireInputChange() {
			// Nothing to do
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			IPath path = file.getLocation();
			if (path == null) {
				return 0;
			} else {
				return path.toOSString().hashCode();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof FileCompareInput)) {
				return false;
			}
			FileCompareInput other = (FileCompareInput) obj;
			IPath path = file.getLocation();
			IPath otherPath = other.file.getLocation();
			if (path == null || otherPath == null) {
				return false;
			}
			return path.toOSString().equals(otherPath.toOSString());
		}
	}

	// IStreamContentAccessor interface needed for passing file contents.
	// Not sure, though, why IModificationDate was implemented, but we keep it just in case.
	private static class SyncMergeItem implements IStreamContentAccessor, ITypedElement, IModificationDate {
		private final String content;

		SyncMergeItem(String c) {
			content = c;
		}

		@Override
		public long getModificationDate() {
			return 0;
		}

		@Override
		public String getName() {
			return "Compare Editor"; //$NON-NLS-1$
		}

		@Override
		public Image getImage() {
			return null;
		}

		@Override
		public String getType() {
			return ITypedElement.TEXT_TYPE;
		}

		@Override
		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content.getBytes());
		}
	}
}
