/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.actions;

import org.eclipse.ptp.pldt.common.actions.AbstractRemoveMarkerAction;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;

/**
 * Action to remove MPI Artifact Markers in the workspace.
 * 
 */
public class RemoveMarkerAction extends AbstractRemoveMarkerAction
{
    public RemoveMarkerAction()
    {
    	super(MpiIDs.MARKER_ID,"MPI");
    }

  
}