/**********************************************************************
 * Copyright (c) 2007,2008  IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.actions;

import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.mpi.analysis.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierAnalysisResults;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierExpr;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierMatching;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICFGBuilder;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIDUChain;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIMVAnalysis;
import org.eclipse.ptp.pldt.mpi.analysis.view.BarrierArtifacts;
import org.eclipse.ptp.pldt.mpi.analysis.view.MPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.mpi.analysis.view.MatchingSet;
import org.eclipse.ptp.pldt.mpi.analysis.view.ShowErrors;

public class MPIAnalysisManager{
	protected BarrierTable btable_;
	protected MPICallGraph cg_;
	private static final boolean traceOn=false;
	
	
	public MPIAnalysisManager(MPICallGraph cg)
	{
		cg_ = cg;
	}
	
	/**
	 * Runs analysis and returns true if any errors were found
	 * @return
	 */
	public boolean run()
	{	
	  if(traceOn)System.out.println("MPI AM: 1. build CG");
		cg_.buildCG();
		//cg_.print();
		/*
		if(cg_.hasError()){
			String markerID = IDs.barrierMarkerID;
			MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(markerID);
			BarrierArtifacts ba = new BarrierArtifacts(cg_, visitor);
			ba.run();
			return;
		}
		*/
    
		btable_ = cg_.getBarrierTable();
		if(traceOn)System.out.println("2. Call Graph finished !");
		
		MPIBarrierExpr BE = new MPIBarrierExpr(btable_, cg_); 
		BE.run();
		if(traceOn)System.out.println("3. Barrier Expression Construction finished !");
		
		MPICFGBuilder cfg = new MPICFGBuilder(cg_);
		cfg.run();
		if(traceOn)System.out.println("4. Control Flow Graph Construction finished !");
		
		MPIDUChain rd = new MPIDUChain(cg_);
		rd.run();
		if(traceOn)System.out.println("5. Reaching Definition and Phi Placement finished !");
		
		MPIMVAnalysis mva = new MPIMVAnalysis(cg_);
		mva.run();
		if(traceOn)System.out.println("6. Multi-valued Analysis finished !");
		
		MPIBarrierMatching bm = new MPIBarrierMatching(cg_, btable_);
		bm.run();
		if(traceOn)System.out.println("7. Barrier Matching finished! ");
				
		MPIBarrierAnalysisResults results = new MPIBarrierAnalysisResults();
		results.setBarrierTable(btable_);
		if(traceOn)System.out.println("8. ...got barrier table ");
		
		String markerID = IDs.barrierMarkerID;
		MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(markerID);
		BarrierArtifacts ba = new BarrierArtifacts(cg_, visitor);
		ba.run();
		if(traceOn)System.out.println("9. ... got barrier artifacts ");
		
		MatchingSet msv = new MatchingSet(btable_);
		msv.run();
		if(traceOn)System.out.println("10. ...got matching set ");
		
		ShowErrors se = new ShowErrors(bm.getErrors());
		boolean foundError = se.run();	
		if(traceOn)System.out.println("11.  ... got errors (if any) ");

		return foundError;
		
	}

}
