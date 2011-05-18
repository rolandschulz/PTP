/**********************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.mpi.core.views;

import org.eclipse.ptp.pldt.common.views.SimpleTableMarkerView;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.core.messages.Messages;
import org.eclipse.ptp.pldt.mpi.internal.core.MpiIDs;

/**
 * An MPI artifact view based on SimpleTableView <br>
 * Note that the ID must be unique.
 * <p>
 * If you need to read icon images, etc. from the plug-in, be sure to pass in an actual Plugin class instance for first arg
 */
public class MPITableView extends SimpleTableMarkerView {
	public MPITableView() {

		super(MpiPlugin.getDefault(), Messages.MPITableView_ARTIFACT, Messages.MPITableView_ARTIFACTS,
				Messages.MPITableView_CONSTRUCT, MpiIDs.MARKER_ID);
	}
}
