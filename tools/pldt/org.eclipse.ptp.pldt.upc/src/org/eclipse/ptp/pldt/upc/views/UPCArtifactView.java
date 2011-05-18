/**********************************************************************
 * Copyright (c) 2008,2011 IBM Corporation.
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
import org.eclipse.ptp.pldt.upc.UPCPlugin;
import org.eclipse.ptp.pldt.upc.internal.UPCIDs;
import org.eclipse.ptp.pldt.upc.messages.Messages;

/**
 * A UPC artifact view based on SimpleTableView <br>
 * Note that the ID must be unique.
 * 
 */
public class UPCArtifactView extends SimpleTableMarkerView {
	/**
	 * If you need to read icon images, etc. from the plug-in, be sure to pass
	 * in an actual Plugin class instance for first arg
	 */
	public UPCArtifactView() {

		super(UPCPlugin.getDefault(), Messages.UPCArtifactView_upc_artifact, Messages.UPCArtifactView_upc_artifacts,
				Messages.UPCArtifactView_construct, UPCIDs.MARKER_ID);
	}
}
