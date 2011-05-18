/**********************************************************************
 * Copyright (c) 2007,2011  IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.actions;

import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierAnalysisResults;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierExpr;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierMatching;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICFGBuilder;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIDUChain;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIMVAnalysis;
import org.eclipse.ptp.pldt.mpi.analysis.internal.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.view.BarrierArtifacts;
import org.eclipse.ptp.pldt.mpi.analysis.view.MPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.mpi.analysis.view.MatchingSet;
import org.eclipse.ptp.pldt.mpi.analysis.view.ShowErrors;

/**
 * Main class for handling MPI Barrier Analysis
 */
public class MPIAnalysisManager {
	protected BarrierTable btable_;
	protected MPICallGraph cg_;
	private static final boolean traceOn = false;

	public MPIAnalysisManager(MPICallGraph cg) {
		cg_ = cg;
	}

	/**
	 * Runs analysis and returns true if any errors were found
	 * 
	 * @return
	 * @since 4.0
	 */
	public boolean run(boolean reportErrors) {
		if (traceOn)
			System.out.println("MPI AM: 1. build CG"); //$NON-NLS-1$
		cg_.buildCG();
		// cg_.print();
		/*
		 * if(cg_.hasError()){ String markerID = IDs.barrierMarkerID;
		 * MPIArtifactMarkingVisitor visitor = new
		 * MPIArtifactMarkingVisitor(markerID); BarrierArtifacts ba = new
		 * BarrierArtifacts(cg_, visitor); ba.run(); return; }
		 */

		btable_ = cg_.getBarrierTable();
		if (traceOn)
			System.out.println("2. Call Graph finished !"); //$NON-NLS-1$

		MPIBarrierExpr BE = new MPIBarrierExpr(btable_, cg_);
		BE.run();
		if (traceOn)
			System.out.println("3. Barrier Expression Construction finished !"); //$NON-NLS-1$

		MPICFGBuilder cfg = new MPICFGBuilder(cg_);
		cfg.run();
		if (traceOn)
			System.out.println("4. Control Flow Graph Construction finished !"); //$NON-NLS-1$

		MPIDUChain rd = new MPIDUChain(cg_);
		rd.run();
		if (traceOn)
			System.out.println("5. Reaching Definition and Phi Placement finished !"); //$NON-NLS-1$

		MPIMVAnalysis mva = new MPIMVAnalysis(cg_);
		mva.run();
		if (traceOn)
			System.out.println("6. Multi-valued Analysis finished !"); //$NON-NLS-1$

		MPIBarrierMatching bm = new MPIBarrierMatching(cg_, btable_);
		bm.run(reportErrors);
		if (traceOn)
			System.out.println("7. Barrier Matching finished! "); //$NON-NLS-1$

		MPIBarrierAnalysisResults results = new MPIBarrierAnalysisResults();
		results.setBarrierTable(btable_);
		if (traceOn)
			System.out.println("8. ...got barrier table "); //$NON-NLS-1$

		String markerID = IDs.barrierMarkerID;
		MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(markerID);
		BarrierArtifacts ba = new BarrierArtifacts(cg_, visitor);
		ba.run();
		if (traceOn)
			System.out.println("9. ... got barrier artifacts "); //$NON-NLS-1$

		MatchingSet msv = new MatchingSet(btable_);
		msv.run();
		if (traceOn)
			System.out.println("10. ...got matching set "); //$NON-NLS-1$

		ShowErrors se = new ShowErrors(bm.getErrors());
		boolean foundError = se.run();
		if (traceOn)
			System.out.println("11.  ... got errors (if any) "); //$NON-NLS-1$

		return foundError;

	}

}
