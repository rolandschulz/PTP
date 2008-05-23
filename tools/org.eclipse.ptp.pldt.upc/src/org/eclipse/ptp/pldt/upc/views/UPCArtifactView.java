/**********************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc.views;

import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.upc.UPCIDs;
import org.eclipse.ptp.pldt.upc.UPCPlugin;



/**
 * A LAPI artifact view based on SimpleTableView Note that the ID must be unique.
 * 
 */
public class UPCArtifactView extends SimpleTableMarkerView
{
    public UPCArtifactView()
    {
        // if you need to read icon images, etc. from the plug-in, be sure to pass
        // in an actual Plugin class instance for first arg
        super(UPCPlugin.getDefault(), "UPC Artifact", "UPC Artifacts", "Construct", UPCIDs.MARKER_ID);
    }
}
