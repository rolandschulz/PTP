/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIResourceCollector;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class MPIAnalysisMultipleAction implements IWorkbenchWindowActionDelegate {
	/**
	 * the current selection is cached here
	 */
	protected MPICallGraph CG_;
	protected IStructuredSelection selection;
	
	public MPIAnalysisMultipleAction(){
		CG_ = null;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}
	/**
	 * Remember what the selected object was. <br>
	 * If selection is empty, it's probably from another view, so don't change
	 * what we consider the current selection from this view.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.isEmpty()) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				this.selection = ss;
			}
		}

	}

	public void run(IAction action) {
		if ((selection == null) || selection.isEmpty()) {
			MessageDialog
					.openWarning(null, "No files selected for analysis.",
							"Please select a source file or container (folder or project) to analyze.");
			return;
		} else {
			CG_ = new MPICallGraph();
			for(Iterator iter = this.selection.iterator(); iter.hasNext();){
				Object obj = (Object) iter.next();
				// It can be a Project, Folder, File, etc...
				if (obj instanceof IAdaptable) {
					final IResource res = (IResource) ((IAdaptable) obj)
							.getAdapter(IResource.class);
					// FIXME put this in a runnable to batch resource changes?
					if (res != null) {
						resourceCollector(res);
					}
				}
			} // end for
			MPIAnalysisManager manager = new MPIAnalysisManager(CG_);
			manager.run();
		}
	}

	/**
	 * Run analysis on a resource (e.g. File or Folder) Will descend to members
	 * of folder
	 * 
	 * @param resource
	 *            the resource
	 * @param indent
	 *            number of levels of nesting/recursion for prettyprinting
	 * @param includes
	 *            contains header files include paths from the Preference page
	 * @return
	 */
	protected boolean resourceCollector(IResource resource) {

		boolean foundError = false;

		if (resource instanceof IFile) {
			try{
				resource.deleteMarkers(IMarker.PROBLEM, 
						true, IResource.DEPTH_INFINITE);
			} catch(CoreException e){
				//System.out.println("RM: exception deleting markers.");
				//e.printStackTrace();
			}
			IFile file = (IFile) resource;
			String filename = file.getName();
			if(filename.endsWith(".c")){
				MPIResourceCollector rc = new MPIResourceCollector(CG_, file);
				rc.run();
			}
			return true;
		}
		else if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			try {
				IResource[] mems = container.members();
				for (int i = 0; i < mems.length; i++) {
					boolean err = resourceCollector(mems[i]);
					foundError = foundError || err;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		else {
			String name = "";
			if (resource instanceof IResource) {
				IResource res = (IResource) resource;
				// name=res.getName(); // simple filename only, no path info
				IPath path = res.getProjectRelativePath();
				name = path.toString();
			}
			System.out.println("Cancelled by User, aborting analysis on subsequent files... " + name);
		}

		return foundError;
	}

}
