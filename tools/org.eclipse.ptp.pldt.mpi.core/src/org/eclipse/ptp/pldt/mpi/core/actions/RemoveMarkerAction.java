/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.actions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Action to remove MPI Artifact Markers in the workspace.
 * 
 * Our sample action implements workbench action delegate. The action proxy will be created by the workbench and shown
 * in the UI. When the user tries to use the action, this delegate will be created and execution will be delegated to
 * it. Adapted from SampleAction in IBM Eclipse course.
 * 
 * @see IWorkbenchWindowActionDelegate
 * 
 */
public class RemoveMarkerAction implements IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow window;

    /**
     * The constructor.
     */
    public RemoveMarkerAction()
    {
    }

    /**
     * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
     * UI.
     * 
     * @see IWorkbenchWindowActionDelegate#run
     */
    public void run(IAction action)
    {
        // action.getDescription() returns the menu item's text.
        // System.out.println("Action: "+action.getDescription());

        removeMarkers();
    }

    /**
     * Remove all the MPI artifact markers and the objects from the (tree) view(s)
     * 
     */
    public void removeMarkers()
    {
        // PITree.getTree().clear(); // clear out PIs from tree view
        IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
        int numMarkers = 0;
        int numErrMarkers = 0;
        String mType = "";
        String meType = "";
        try {
            int depth = IResource.DEPTH_INFINITE;
            mType = MpiIDs.MARKER_ID;
            IMarker[] all = wsResource.findMarkers(mType, false, depth);
            numMarkers = all.length;
            wsResource.deleteMarkers(mType, false, depth);

            meType = MpiIDs.MARKER_ERROR_ID;
            IMarker[] allErrs = wsResource.findMarkers(meType, false, depth);
            numErrMarkers = allErrs.length;
            wsResource.deleteMarkers(meType, false, depth);

        } catch (CoreException e) {
            System.out.println("RM: exception deleting markers.");
            e.printStackTrace();
        }
        // clear the global cache of pis on the markers (for the tree view)
        // PITree.getTree().clear();

        MessageDialog.openInformation(window.getShell(), "MPI artifacts cleared", numMarkers + " MPI Artifact"
                + sIfMult(numMarkers) + " and " + numErrMarkers + " MPI Artifact Error" + sIfMult(numErrMarkers) + " "
                + "have been removed.\n"
                // + "Marker Type = "+ mType
                + "\n"
        // + "To replace markers on files for which analysis has been run:\n"
                // +"Rebuild project.\n"
                // + "(To build project, click on project name and select Project->rebuild project.)"
                );
    }

    String sIfMult(int num)
    {
        if (num != 1) return "s";
        return "";
    }

    /**
     * Selection in the workbench has been changed. We can change the state of the 'real' action here if we want, but
     * this can only happen after the delegate has been created.
     * 
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
    }

    /**
     * We can use this method to dispose of any system resources we previously allocated.
     * 
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose()
    {
    }

    /**
     * We will cache window object in order to be able to provide parent shell for the message dialog.
     * 
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window)
    {
        this.window = window;
    }
}