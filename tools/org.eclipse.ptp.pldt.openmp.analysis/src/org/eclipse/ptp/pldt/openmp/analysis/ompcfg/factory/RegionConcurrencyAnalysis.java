/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory;

import java.io.PrintStream;
import java.util.Set;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFG;

/**
 * Holds the analysis for a portion of a function defined by an omp pragma
 * 
 * @author pazel
 * 
 */
public class RegionConcurrencyAnalysis {
	protected FunctionConcurrencyAnalysis analysis_ = null; // owning analysis

	protected OMPCFG cfg_ = null;

	protected PhaseConcurrencyAnalysis[] phases_ = null;

	protected RegionConcurrencyMap cMap_ = null;

	/**
	 * RegionConcurrencyAnalysis - Constructor
	 * 
	 * @param cfg
	 *            -
	 *            OMPCFG
	 */
	public RegionConcurrencyAnalysis(OMPCFG cfg) {
		cfg_ = cfg;

		semanticCheck();
	}

	/**
	 * setParent - set the FunctionConcurrencyAnalysis for this cfg
	 * 
	 * @param analysis
	 */
	public void setParent(FunctionConcurrencyAnalysis analysis) {
		analysis_ = analysis;
	}

	/**
	 * getCFG - accessor the OMPCFG
	 * 
	 * @return OMPCFG
	 */
	public OMPCFG getCFG() {
		return cfg_;
	}

	/**
	 * getPhases - get all the phases of this component
	 * 
	 * @return PhaseConcurrencyAnalysis []
	 */
	public PhaseConcurrencyAnalysis[] getPhases() {
		return phases_;
	}

	/**
	 * doPhaseAnalysis - analyse for phases
	 * 
	 */
	public void doPhaseAnalysis() {
		PhaseAnalysisFactory paf = new PhaseAnalysisFactory(cfg_);
		paf.buildPhases();
		phases_ = paf.getPhases();

		cMap_ = new RegionConcurrencyMap(this);
		cMap_.buildMap();
	}

	/**
	 * getNodesConcurrentTo - get all nodes concurrent to given node
	 * 
	 * @param node
	 *            -
	 *            IASTNode
	 * @return Set
	 */
	public Set getNodesConcurrentTo(IASTNode node) {
		return cMap_.getNodesConcurrentTo(node);
	}

	/**
	 * printPhases - print out all the phases
	 * 
	 * @param ps
	 */
	public void printComponent(PrintStream ps) {
		for (int i = 0; i < phases_.length; i++) {
			ps.println("Component Phase " + i
					+ " ----------------------------------");
			phases_[i].printPhase(ps);
		}
	}

	/**
	 * semanticCheck - check for semantic mistakes using pragmas
	 * 
	 */
	private void semanticCheck() {
		PASTSemanticCheck.checkSemantics(cfg_);
	}
}
