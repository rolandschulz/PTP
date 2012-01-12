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
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.ui.synchronize.SaveableCompareEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class SyncMergeEditor {
	public static void open(IFile file) {
		CompareUI.openCompareEditor(new FileCompareInput(file));
	}

	private static class FileCompareInput extends SaveableCompareEditorInput {
		String Ancestor = "This is a stupid paragraph that I wrote to test the compare editor."; //$NON-NLS-1$
		String Child1 = "This is a dumb paragraph that I concocted to test the compare editor feature."; //$NON-NLS-1$
		String Child2 = "This is a stupid paregraph which I wrote to try out the compaare edit0or!"; //$NON-NLS-1$


		public FileCompareInput(IFile file) {
				super(createCompareConfiguration(), getPage());
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
			return new DiffNode(null, Differencer.CONFLICTING, new SyncMergeItem(Ancestor), new SyncMergeItem(Child1), new SyncMergeItem(Child2));
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
