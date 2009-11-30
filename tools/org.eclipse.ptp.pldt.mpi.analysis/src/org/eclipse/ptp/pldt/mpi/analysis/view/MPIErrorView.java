/**********************************************************************
 * Copyright (c) 2007,2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.view;

import org.eclipse.ptp.pldt.mpi.analysis.Activator;
import org.eclipse.ptp.pldt.mpi.analysis.IDs;

/**
 * View to show list of barrier errors [new view architecture]
 * 
 */
public class MPIErrorView extends SimpleTreeTableMarkerView {
	public MPIErrorView() {

		super(Activator.getDefault(), "FunctionName", "Matching Set Artifacts", "IndexNum",
				IDs.errorMarkerID/* common needs:, IDs.parentIDAttr*/);
	}

}
