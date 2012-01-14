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
package org.eclipse.ptp.rdt.sync.ui;

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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
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

		protected ICompareInput prepareCompareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			ISyncServiceProvider provider = SyncManager.getSyncProvider(file.getProject());
			String[] mergeParts = null;

			if (provider != null) {
				mergeParts = provider.getMergeConflictParts(file);
			}
			
			if (provider == null) {
				return new DiffNode(null, Differencer.CONFLICTING, new SyncMergeItem(Messages.SyncMergeEditor_1),
						new SyncMergeItem(Messages.SyncMergeEditor_1), new SyncMergeItem(Messages.SyncMergeEditor_1));
			} else if (mergeParts == null) {
				return new DiffNode(null, Differencer.CONFLICTING, new SyncMergeItem(Messages.SyncMergeEditor_2),
						new SyncMergeItem(Messages.SyncMergeEditor_2), new SyncMergeItem(Messages.SyncMergeEditor_2));
			} else {
				ITypedElement fileElement = SaveableCompareEditorInput.createFileElement(file);
				return new DiffNode(null, Differencer.CONFLICTING, new SyncMergeItem(mergeParts[2]),
						fileElement, new SyncMergeItem(mergeParts[1]));
			}
		}

		protected void fireInputChange() {
			// Nothing to do
		}
	}
	
	private static class SyncMergeItem implements IStreamContentAccessor, ITypedElement, IModificationDate {
		private final String content;
		
		SyncMergeItem(String c) {
			content = c;
		}

		public long getModificationDate() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getName() {
			return "Compare Editor Test"; //$NON-NLS-1$
		}

		public Image getImage() {
			return null;
		}

		public String getType() {
			return ITypedElement.TEXT_TYPE;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content.getBytes());
		}
	}
}
