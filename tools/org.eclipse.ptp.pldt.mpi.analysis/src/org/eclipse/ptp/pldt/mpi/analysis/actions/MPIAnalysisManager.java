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

package org.eclipse.ptp.pldt.mpi.analysis.actions;

import org.eclipse.ptp.pldt.mpi.analysis.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierAnalysisResults;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierExpr;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierMatching;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICFGBuilder;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIDUChain;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIMVAnalysis;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPISingleAssignAnalysis;
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
	
	public void run()
	{	
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
		if(traceOn)System.out.println("Call Graph finished !");
		
		MPIBarrierExpr BE = new MPIBarrierExpr(btable_, cg_); 
		BE.run();
		if(traceOn)System.out.println("Barrier Expression Construction finished !");
		
		MPICFGBuilder cfg = new MPICFGBuilder(cg_);
		cfg.run();
		if(traceOn)System.out.println("Control Flow Graph Construction finished !");
		
		MPIDUChain rd = new MPIDUChain(cg_);
		rd.run();
		if(traceOn)System.out.println("Reaching Definition and Phi Placement finished !");
		
		MPIMVAnalysis mva = new MPIMVAnalysis(cg_);
		mva.run();
		if(traceOn)System.out.println("Multi-valued Analysis finished !");
		
		MPIBarrierMatching bm = new MPIBarrierMatching(cg_, btable_);
		bm.run();
		if(traceOn)System.out.println("Barrier Matching finished! ");
				
		MPIBarrierAnalysisResults results = new MPIBarrierAnalysisResults();
		results.setBarrierTable(btable_);
		
		String markerID = IDs.barrierMarkerID;
		MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(markerID);
		BarrierArtifacts ba = new BarrierArtifacts(cg_, visitor);
		ba.run();
		
		MatchingSet msv = new MatchingSet(btable_);
		msv.run();
		
		ShowErrors se = new ShowErrors(bm.getErrors());
		se.run();	
	}

}
