/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.IBlock;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.impl.CallGraphNode;

public class MPICallGraphNode extends CallGraphNode {

	/** Used (defined) global variables in this function */
	protected List<String> globalUse_;
	protected List<String> globalDef_;
	/** Defined parameters that can be propagated to its callers */
	protected List<String> paramDef_;

	protected boolean barrierRelated;
	/** One barrier expression for each communicator */
	protected Hashtable<String, BarrierExpression> barrierExpr_;
	/** The list of all barriers */
	protected List<BarrierInfo> barriers_;

	/**
	 * Is the parameter multi-valued in any call ?
	 * One entry for each parameter
	 */
	protected Hashtable<String, Boolean> paramMV_;
	/** Is the return value multi-valued? */
	protected boolean mv;

	/**
	 * For each parameter or used global variable "V", summarize
	 * a set of MV data (passable parameters, defined global vars,
	 * and returned value) if V is MV.
	 */
	protected Hashtable<String, List<String>> mvSummary_;

	/**
	 * A table storing each defined variable V in this function and
	 * the set of blocks that contain the definitions of V. This is
	 * used in \phi node placement.
	 */
	protected Hashtable<String, List<IBlock>> defTable_;

	/**
	 * hashtable contains number of assignments for each variable (local vars, global vars,
	 * and parameters)
	 */
	protected Hashtable<String, Integer> saVar_;
	protected Hashtable<String, Boolean> saVarPointer_;

	protected boolean hasSeed;

	boolean mpiInit = false;
	public boolean marked = false;

	public MPICallGraphNode(IResource resource, String filename,
			String funcname, IASTFunctionDefinition fdef) {
		super(resource, filename, funcname, fdef);
		init();
	}

	public MPICallGraphNode(IResource resource, String filename,
			IASTFunctionDefinition fdef) {
		super(resource, filename, fdef);
		init();
	}

	private void init() {
		globalUse_ = new ArrayList<String>();
		globalDef_ = new ArrayList<String>();
		paramDef_ = new ArrayList<String>();
		defTable_ = new Hashtable<String, List<IBlock>>();

		barrierRelated = false;
		barrierExpr_ = new Hashtable<String, BarrierExpression>();
		barriers_ = new ArrayList<BarrierInfo>();

		paramMV_ = new Hashtable<String, Boolean>();
		mvSummary_ = new Hashtable<String, List<String>>();

		saVar_ = new Hashtable<String, Integer>();
		saVarPointer_ = new Hashtable<String, Boolean>();

		mv = false;
		hasSeed = false;
	}

	public boolean barrierRelated() {
		return barrierRelated;
	}

	public void setBarrierRelated(boolean val) {
		barrierRelated = val;
	}

	public Hashtable<String, BarrierExpression> getBarrierExpr() {
		return barrierExpr_;
	}

	public List<BarrierInfo> getAllBarriers() {
		return barriers_;
	}

	public void setBarrierExpr(String comm, BarrierExpression be) {
		barrierExpr_.put(comm, be);
	}

	public void setGlobalUse(List<String> list) {
		globalUse_ = list;
	}

	public List<String> getGlobalUse() {
		return globalUse_;
	}

	public void setGlobalDef(List<String> list) {
		globalDef_ = list;
	}

	public List<String> getGlobalDef() {
		return globalDef_;
	}

	public void setParamDef(List<String> list) {
		paramDef_ = list;
	}

	public List<String> getParamDef() {
		return paramDef_;
	}

	public void setDefTable(Hashtable<String, List<IBlock>> defs) {
		defTable_ = defs;
	}

	public Hashtable<String, List<IBlock>> getDefTable() {
		return defTable_;
	}

	public Hashtable<String, Boolean> getParamMV() {
		return paramMV_;
	}

	public void setMV(boolean val) {
		mv = val;
	}

	public boolean getMV() {
		return mv;
	}

	public Hashtable<String, List<String>> getMVSummary() {
		return mvSummary_;
	}

	public Hashtable<String, Integer> getSAVar() {
		return saVar_;
	}

	public Hashtable<String, Boolean> getSAVarPointer() {
		return saVarPointer_;
	}

	public boolean hasSeed() {
		return hasSeed;
	}

	public void setSeed(boolean val) {
		hasSeed = val;
	}
}
