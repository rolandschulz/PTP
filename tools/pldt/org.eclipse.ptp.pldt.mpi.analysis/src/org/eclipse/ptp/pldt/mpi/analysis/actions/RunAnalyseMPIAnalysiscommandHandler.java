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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.common.actions.AnalysisDropdownHandler;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandler;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIResourceCollector;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Do MPI barrier analysis from dropdown toolbar menu
 * @author tibbitts
 *
 */
public class RunAnalyseMPIAnalysiscommandHandler extends RunAnalyseHandler  /*implements IHandler*/ {
	protected MPICallGraph callGraph_;
	
	public RunAnalyseMPIAnalysiscommandHandler(){ 
		callGraph_ = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		getSelection(event);
		AnalysisDropdownHandler.setLastHandledAnalysis(this,selection);
		if ((selection == null) || selection.isEmpty()) {
			MessageDialog
					.openWarning(null, "No files selected for analysis.",
							"Please select a source file or container (folder or project) to analyze.");
			return null;
		} else {
			callGraph_ = new MPICallGraph();
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
			MPIAnalysisManager manager = new MPIAnalysisManager(callGraph_);
			manager.run();
		}
		return null;
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
				MPIResourceCollector rc = new MPIResourceCollector(callGraph_, file);
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
