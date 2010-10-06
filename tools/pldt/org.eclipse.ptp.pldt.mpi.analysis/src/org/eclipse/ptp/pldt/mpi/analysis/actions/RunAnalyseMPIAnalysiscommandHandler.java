/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.pldt.common.actions.AnalysisDropdownHandler;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandler;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.mpi.analysis.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIResourceCollector;
import org.eclipse.ptp.pldt.mpi.analysis.messages.Messages;


/**
 * Do MPI barrier analysis from dropdown toolbar menu
 * 
 * @author Beth Tibbitts
 *
 */
public class RunAnalyseMPIAnalysiscommandHandler extends RunAnalyseHandler  {
	protected MPICallGraph callGraph_;
	boolean traceOn=false;
	
	public RunAnalyseMPIAnalysiscommandHandler(){ 
		callGraph_ = null;
	}

	/** 
	 * Execute the action for MPI barrier analysis
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean foundError=false;
		getSelection(event);
		AnalysisDropdownHandler.setLastHandledAnalysis(this,selection);
		if ((selection == null) || selection.isEmpty()) {
			MessageDialog.openWarning(null, Messages.RunAnalyseMPIAnalysiscommandHandler_noFilesSelectedForAnalysis,
							Messages.RunAnalyseMPIAnalysiscommandHandler_pleaseSelect);
			return null;
		} else {
			if(isRemote(selection)) {
				MessageDialog.openInformation(null, Messages.RunAnalyseMPIAnalysiscommandHandler_remoteProjectFound, Messages.RunAnalyseMPIAnalysiscommandHandler_remoteNotSupported);
				return null;
			}
			final boolean reportErrors=true;
			foundError = analyseBarriers(selection,reportErrors);
		}
		ViewActivator.activateView(IDs.matchingSetViewID);
		ViewActivator.activateView(IDs.barrierViewID);
		// if error found, assure its view has focus
		if(foundError) {
			ViewActivator.activateView(IDs.errorViewID);
		}
		return null;
	}

	/**
	 * Basics of MPI barrier analysis encapsulated here so can be used by
	 * testing <br>
	 * Results are saved in the markers that are generated.
	 * 
	 * @return true if error found
	 * @since 4.0
	 */
	public boolean analyseBarriers(IStructuredSelection selection, boolean reportErrors) {
		boolean foundError;
		callGraph_ = new MPICallGraph();
		//int numFiles=this.countFilesSelected();
		// BRT use numfiles for a progress monitor?

		for(@SuppressWarnings("rawtypes")
		Iterator iter = selection.iterator(); iter.hasNext();){
			Object obj =  iter.next();
			// It can be a Project, Folder, File, etc...
			if (obj instanceof IAdaptable) {
				final IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
				if (traceOn)
					System.out.println("resourceCollector on " + res.getName()); //$NON-NLS-1$
				// FIXME put this in a runnable to batch resource changes?
				if (res != null) {
					resourceCollector(res);
				}
			}
		} // end for
		MPIAnalysisManager manager = new MPIAnalysisManager(callGraph_);
		foundError=manager.run(reportErrors);
		return foundError;
	}
	
	/**
	 * Run analysis (collect resource info in the call graph) on a resource
	 * (e.g. File or Folder) <br>
	 * Will descend to members of folder
	 * 
	 * @param resource
	 *            the resource upon which barrier analysis was initiated: file,
	 *            folder, or project
	 * @return
	 */
	protected boolean resourceCollector(IResource resource) {

		boolean foundError = false;

		if (resource instanceof IFile) {
			try{
				resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			} catch(CoreException e){
				//System.out.println("Exception deleting markers.");
			}
			IFile file = (IFile) resource;
			String filename = file.getName();
			if(filename.endsWith(".c")){ //$NON-NLS-1$
				if (traceOn)
					System.out.println("resourceCollector on c file: " + file.getName()); //$NON-NLS-1$
				MPIResourceCollector rc = new MPIResourceCollector(callGraph_, file); // BRT why  'new'  each time?
				rc.run();
			}
			return true;
		} else if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			try {
				IResource[] mems = container.members();
				for (int i = 0; i < mems.length; i++) {
					if (traceOn)
						System.out.println("descend to " + mems[i].getName()); //$NON-NLS-1$
					boolean err = resourceCollector(mems[i]);
					foundError = foundError || err;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else {
			String name = ""; //$NON-NLS-1$
			if (resource instanceof IResource) {
				IResource res = resource;
				// name=res.getName(); // simple filename only, no path info
				IPath path = res.getProjectRelativePath();
				name = path.toString();
			}
			System.out.println("Cancelled by User, aborting analysis on subsequent files... " + name); //$NON-NLS-1$
		}

		return foundError;
	}
	
	/**
	 * Returns true if resource(s) represented by given selection are remote.
	 * Note that there may be some uncertainly that it's local if the returned value is false,
	 * but a returned value of 'true' means the resource is definitely determined to be remote.
	 * @param selection
	 * @return
	 */
	private boolean isRemote (IStructuredSelection selection) {	
		Object s1=selection.getFirstElement();
		if(s1 instanceof IAdaptable) {
			IAdaptable iAdaptable = (IAdaptable) s1;
			IResource resource=(IResource) iAdaptable.getAdapter(IResource.class);
			IProject proj = resource.getProject();
			if(isRemote(proj)) {
				return true;
			}			
		}
		return false; 

	}
	


	/**
	 * should go in a more publicly reusable/accessible place after 5.0 e.g. pldt.common.util.Utility
	 * @param project
	 * @return
	 */
	private static boolean isRemote(IProject project) {
		final String REMOTE_NATURE_ID = "org.eclipse.ptp.rdt.core.remoteNature"; //$NON-NLS-1$
		String[] natureIDs;
		try {
			natureIDs = project.getDescription().getNatureIds();
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		for (int i = 0; i < natureIDs.length; i++) {
			String id = natureIDs[i];
			if(id.equals(REMOTE_NATURE_ID)) {
				return true;
			}		
		}
		return false;
		
	}

}
