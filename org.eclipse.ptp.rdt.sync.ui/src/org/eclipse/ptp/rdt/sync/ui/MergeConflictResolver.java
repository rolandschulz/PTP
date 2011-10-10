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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ui.dialogs.CheckedTreeSelectionDialog;

/**
 * Tool for resolving merge conflicts between a project's local space and the current active configuration. Note that this
 * tool works independently and needs no intervention from the caller other than passing the project. So it can be launched at
 * any point.
 */
public class MergeConflictResolver {
	static private final String PLUGIN_ID = "org.eclipse.ptp.rdt.sync.ui"; //$NON-NLS-1$
	private final IProject fProject;
	private final CheckedTreeSelectionDialog fDialog;
	private ITreeContentProvider fContentProvider = null;
	private final ILabelProvider fLabelProvider;
	
	/**
	 * Returns a new instance that resolves conflicts for the given project.
	 * Normally, clients should not use this method but simply call launch, as this tool is designed to work independently of
	 * the caller and so a reference should not be needed.
	 *
	 * @param project
	 */
	public MergeConflictResolver(IProject project) {
		fProject = project;
		try {
			fContentProvider = new MergeConflictContextProvider(project);
		} catch (CoreException e) {
			RDTSyncUIPlugin.log(e.toString(), e);
		}
		fLabelProvider = null;
		fDialog = new CheckedTreeSelectionDialog(null, fLabelProvider, fContentProvider);
	}
	
	/**
	 * Launch an instance of the merge conflict resolution tool on the given project.
	 *
	 * @param project
	 */
	public static void launch(IProject project) {
		MergeConflictResolver resolver = new MergeConflictResolver(project);
		resolver.getDialog().open();
	}
	
	protected static class MergeConflictContextProvider implements ITreeContentProvider {
		private final IProject fProject;
		private final IConfiguration fConfig;

		public MergeConflictContextProvider(IProject project) throws CoreException {
			fProject = project;
            IManagedBuildInfo buildInfo;
            buildInfo = ManagedBuildManager.getBuildInfo(project);
            if (buildInfo == null) {
            	throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.MergeConflictResolver_0));
            }
            fConfig = buildInfo.getDefaultConfiguration();
            if (fConfig == null) {
            	throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, Messages.MergeConflictResolver_1));
            }
		}

		public void dispose() {
			// Nothing to do
		}

		/**
		 * This should only be called with fProject as input. Any other value is an error.
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if ((!(newInput instanceof IProject)) || (newInput != fProject)) {
				RDTSyncUIPlugin.getDefault().logErrorMessage(Messages.MergeConflictResolver_2);
			}
		}

		public Object[] getElements(Object inputElement) {
			return this.getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			try {
				return ((IContainer) parentElement).members();
			} catch (CoreException e) {
				RDTSyncUIPlugin.log(e.toString(), e);
				return null;
			}
		}

		public Object getParent(Object element) {
			return ((IContainer) element).getParent();
		}

		public boolean hasChildren(Object element) {
			return element instanceof IContainer;
		}
	}
	
	/**
	 * Return the resolver's dialog
	 * @return the dialog
	 */
	public Dialog getDialog() {
		return fDialog;
	}
}
