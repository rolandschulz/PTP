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
package org.eclipse.ptp.pldt.lapi.actions;

import org.eclipse.ptp.pldt.common.actions.AbstractRemoveMarkerAction;
import org.eclipse.ptp.pldt.lapi.LapiIDs;

/**
 * Action to remove LAPI Artifact Markers in the workspace.
 * 
 * @author Beth Tibbitts
 * 
 */
public class RemoveMarkerAction extends AbstractRemoveMarkerAction
{

    /**
     * The constructor.  Indicates marker id and a string to use in dialog
     * messages about the markers to be removed.
     */
    public RemoveMarkerAction()
    {
    	super(LapiIDs.MARKER_ID, "LAPI");
    }

  
}