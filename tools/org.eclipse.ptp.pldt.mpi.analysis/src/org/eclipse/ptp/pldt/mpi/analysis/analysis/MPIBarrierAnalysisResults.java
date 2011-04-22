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

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

public class MPIBarrierAnalysisResults {
	protected BarrierTable barrierTable_ = null;

	protected static MPIBarrierAnalysisResults results_ = null;

	public MPIBarrierAnalysisResults() {
		results_ = this;
	}

	public static MPIBarrierAnalysisResults getAnalysisResults() {
		return results_;
	}

	public void setBarrierTable(BarrierTable table) {
		barrierTable_ = table;
	}

	public BarrierTable getBarrierTable() {
		return barrierTable_;
	}
}
