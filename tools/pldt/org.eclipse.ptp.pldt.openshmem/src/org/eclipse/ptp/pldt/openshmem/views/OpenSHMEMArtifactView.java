/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem.views;

import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.openshmem.Activator;
import org.eclipse.ptp.pldt.openshmem.OpenSHMEMIDs;
import org.eclipse.ptp.pldt.openshmem.messages.Messages;




/**
 * An OpenSHMEM artifact view based on SimpleTableView Note that the ID must be unique.
 * 
 */
public class OpenSHMEMArtifactView extends SimpleTableMarkerView
{
    public OpenSHMEMArtifactView()
    {
        // if you need to read icon images, etc. from the plug-in, be sure to pass
        // in an actual Plugin class instance for first arg
        super(Activator.getDefault(), Messages.OpenSHMEMArtifactView_openshmem_artifact_column_title, Messages.OpenSHMEMArtifactView_openshmem_artifacts_plural, Messages.OpenSHMEMArtifactView_construct_column_title, OpenSHMEMIDs.MARKER_ID);
    }
}
