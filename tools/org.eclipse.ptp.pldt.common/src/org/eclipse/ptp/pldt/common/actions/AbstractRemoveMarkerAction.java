/**********************************************************************
 * Copyright (c) 2006,2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Generic Action to remove Artifact Markers in the workspace.
 * 
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it. Adapted from SampleAction in IBM Eclipse course.
 * 
 * @see IWorkbenchWindowActionDelegate
 * 
 * @author Beth Tibbitts
 * 
 */
public abstract class AbstractRemoveMarkerAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	private String markerID;

	private String description;

	private boolean doProvideMessageConfirmation = true;

	/**
	 * The constructor. Uses default of providing a message dialog popup upon
	 * completion of marker removal.
	 */
	public AbstractRemoveMarkerAction(String markerID, String description) {
		this(markerID, description, false);
	}

	/**
	 * Construction that allows specification of whether or not to provide a
	 * message dialog popup upon completion of marker removal.
	 */
	public AbstractRemoveMarkerAction(String markerID, String description, boolean doProvideMessageConfirmation) {
		this.markerID = markerID;
		this.doProvideMessageConfirmation = doProvideMessageConfirmation;

	}

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		removeMarkers();
	}

	/**
	 * Remove all the artifact markers and thus the objects from the view(s)
	 * 
	 */
	public void removeMarkers() {
		IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
		int numMarkers = 0;
		try {
			int depth = IResource.DEPTH_INFINITE;
			IMarker[] all = wsResource.findMarkers(markerID, false, depth);
			numMarkers = all.length;
			wsResource.deleteMarkers(markerID, false, depth);
		} catch (CoreException e) {
			System.out.println("RM: exception deleting markers.");
			//e.printStackTrace();
		}
		if (doProvideMessageConfirmation) {
			MessageDialog.openInformation(window.getShell(), description + " Artifacts cleared", numMarkers + " "
					+ description + " Artifact" + sIfMult(numMarkers) 
					+ "have been removed.\n\n"
					);
		}
	}

	/**
	 * Return a letter "s" for plural if there is a value other than 1.<br>
	 * Admittedly, a rather poor pluralizer.
	 * 
	 * @param num
	 * @return
	 */
	private String sIfMult(int num) {
		if (num != 1)
			return "s";
		return "";
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}