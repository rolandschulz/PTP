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
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.ptp.pldt.openmp.analysis.parser.OpenMPToken;

/**
 * 
 * @author pazel
 * 
 */
public class PASTOMPPragma extends PASTPragma
{
	protected int ompType_ = OmpUnknown;

	// OmpParallel, OmpParallelFor, OmpParallelSections
	protected OpenMPToken[] sharedList_ = null;
	// OmpParallel, OmpFor, OmpSections, OmpSingle, OmpParallelFor, OmpParallelSections
	protected OpenMPToken[] privateList_ = null;
	// OmpParallel, OmpFor, OmpSections, OmpSingle, OmpParallelFor, OmpParallelSections
	protected OpenMPToken[] firstPrivateList_ = null;
	// OmpFor, OmpSections, OmpParallelFor, OmpParallelSections
	protected OpenMPToken[] lastPrivateList_ = null;
	// OmpParallel, OmpParallelFor, OmpParallelSections
	protected int default_ = OmpShared; // or OmpNone
	// OmpParallel, OmpFor, OmpSections, OmpParallelFor, OmpParallelSections
	protected int reductionOperator_ = OmpOpUnknown;
	protected OpenMPToken[] reductionList_ = null;
	// OmpParallel, OmpParallelFor, OmpParallelSections
	protected OpenMPToken[] copyinList_ = null;
	// OmpParallel, OmpParallelFor, OmpParallelSections
	protected OpenMPToken[] ifExpression_ = null;
	// OmpFor, OmpParallelFor
	protected boolean ordered_ = false;
	// OmpFor, OmpParallelFor
	protected int scheduleKind_ = OmpSKUnknown;
	protected OpenMPToken[] chunkExpression_ = null;
	// OmpFor, OmpSections, OmpSingle
	protected boolean nowait_ = false;

	// OmpThreadPrivate
	protected OpenMPToken[] threadPrivateList_ = null;
	// OmpParallel
	protected OpenMPToken[] numThreadsExpr_ = null;
	// OmpSingle
	protected OpenMPToken[] copyPrivateList_ = null;
	// OmpFlush
	protected OpenMPToken[] flushList_ = null;
	// Region
	protected IASTNode region_ = null;
	protected String regionFilename_ = null;
	protected int regionOffset_ = 0; // local to file (not acctg for includes)
	protected int regionLength_ = 0;
	// Problem list for this omp pragma
	protected LinkedList problemList_ = new LinkedList(); // of OpenMPError's

	protected IASTStatement locationNode_ = null; // just preceding stmt - if cmpd stmt
	protected int locationProximity_ = NoProximity;

	public static final int NoProximity = -1;
	public static final int NeighborProximity = 0; // cst indicating pragma at same level and succeeding pragma
	public static final int ChildProximity = 1; // cst indicating pragma is child of ast node

	public static final int OmpUnknown = -1;
	public static final int OmpParallel = 0;
	public static final int OmpFor = 1;
	public static final int OmpSection = 2;
	public static final int OmpSections = 3;
	public static final int OmpSingle = 4;
	public static final int OmpParallelFor = 5;
	public static final int OmpParallelSections = 6;
	public static final int OmpMaster = 7;
	public static final int OmpCritical = 8;
	public static final int OmpBarrier = 9;
	public static final int OmpAtomic = 10;
	public static final int OmpFlush = 11;
	public static final int OmpOrdered = 12;
	public static final int OmpThreadPrivate = 13;

	public static final int OmpShared = 0;
	public static final int OmpNone = 1;

	public static final int OmpOpUnknown = -1;
	public static final int OmpOpPlus = 0;
	public static final int OmpOpMult = 1;
	public static final int OmpOpMinus = 2;
	public static final int OmpOpBAnd = 3;
	public static final int OmpOpBXor = 4;
	public static final int OmpOpBOr = 5;
	public static final int OmpOpLAnd = 6;
	public static final int OmpOpLOr = 7;

	public static final int OmpSKUnknown = -1;
	public static final int OmpSKStatic = 0;
	public static final int OmpSKDynamic = 1;
	public static final int OmpSKGuided = 2;
	public static final int OmpSKRuntime = 3;

	/**
	 * PASTOMPPragma - constructor using ASTPragma
	 * 
	 * @param pragma
	 *            - ASTPragma
	 */
	public PASTOMPPragma(IASTPreprocessorPragmaStatement pragma)
	{
		super(pragma);
		// pragma_ = pragma;
	}

	/**
	 * PASTOMPPragma - constructor using PASTPragma
	 * 
	 * @param pragma
	 *            - PASTPragma
	 */
	public PASTOMPPragma(PASTPragma pragma)
	{
		super(pragma);
	}

	public String getType()
	{
		String t = "";
		switch (ompType_) {
		case OmpUnknown:
			t = "--unknown==";
			break;
		case OmpParallel:
			t = "parallel";
			break;
		case OmpFor:
			t = "for";
			break;
		case OmpSections:
			t = "sections";
			break;
		case OmpSection:
			t = "section";
			break;
		case OmpSingle:
			t = "single";
			break;
		case OmpParallelFor:
			t = "parallel for";
			break;
		case OmpParallelSections:
			t = "parallel sections";
			break;
		case OmpMaster:
			t = "master";
			break;
		case OmpCritical:
			t = "critical";
			break;
		case OmpBarrier:
			t = "barrier";
			break;
		case OmpAtomic:
			t = "atomic";
			break;
		case OmpFlush:
			t = "flush";
			break;
		case OmpOrdered:
			t = "ordered";
			break;
		case OmpThreadPrivate:
			t = "thread private";
			break;
		}
		return "#pragma omp " + t;
	}

	/**
	 * getOMPType - get the type code for the OpenMP directive
	 * 
	 * @return
	 */
	public int getOMPType()
	{
		return ompType_;
	}

	public OpenMPToken[] getThreadPrivateList()
	{
		return threadPrivateList_;
	}

	public OpenMPToken[] getIfExpression()
	{
		return ifExpression_;
	}

	public OpenMPToken[] getPrivateList()
	{
		return privateList_;
	}

	public OpenMPToken[] getFirstPrivateList()
	{
		return firstPrivateList_;
	}

	public int getDefault()
	{
		return default_;
	}

	public OpenMPToken[] getSharedList()
	{
		return sharedList_;
	}

	public OpenMPToken[] getCopyinList()
	{
		return copyinList_;
	}

	public int getReductionOperator()
	{
		return reductionOperator_;
	}

	public OpenMPToken[] getReductionList()
	{
		return reductionList_;
	}

	public boolean getOrdered()
	{
		return ordered_;
	}

	public boolean getNoWait()
	{
		return nowait_;
	}

	public int getScheduleKind()
	{
		return scheduleKind_;
	}

	public OpenMPToken[] getChunkExpression()
	{
		return chunkExpression_;
	}

	public OpenMPToken[] getCopyPrivateList()
	{
		return copyPrivateList_;
	}

	public OpenMPToken[] getFlushList()
	{
		return flushList_;
	}

	public OpenMPToken[] getNumThreadsExpr()
	{
		return numThreadsExpr_;
	}

	public OpenMPToken[] getLastPrivateList()
	{
		return lastPrivateList_;
	}

	/**
	 * getRegion - accessor
	 * 
	 * @return IASTNode
	 */
	public IASTNode getRegion()
	{
		return region_;
	}

	public String getRegionFilename()
	{
		return regionFilename_;
	}

	public int getRegionOffset()
	{
		return regionOffset_;
	}

	public int getRegionLength()
	{
		return regionLength_;
	}

	public LinkedList getProblems()
	{
		return problemList_;
	}

	public IASTStatement getLocation()
	{
		return locationNode_;
	}

	public int getProximity()
	{
		return locationProximity_;
	}

	/**
	 * setOMPType - set the OMP type code
	 * 
	 * @param type
	 */
	protected void setOMPType(int type) {
		ompType_ = type;
	}

	/**
	 * setThreadPrivateList - set the threadprivate list of variables
	 * 
	 * @param l
	 */
	protected void setThreadPrivateList(OpenMPToken[] l) {
		threadPrivateList_ = l;
	}

	protected void setIfExpression(OpenMPToken[] l) {
		ifExpression_ = l;
	}

	protected void setPrivateList(OpenMPToken[] l) {
		privateList_ = l;
	}

	protected void setFirstPrivateList(OpenMPToken[] l) {
		firstPrivateList_ = l;
	}

	protected void setDefault(int sn) {
		default_ = sn;
	}

	protected void setSharedList(OpenMPToken[] l) {
		sharedList_ = l;
	}

	protected void setCopyinList(OpenMPToken[] l) {
		copyinList_ = l;
	}

	protected void setReductionOperator(int op) {
		reductionOperator_ = op;
	}

	protected void setReductionList(OpenMPToken[] l) {
		reductionList_ = l;
	}

	protected void setOrdered(boolean tf) {
		ordered_ = tf;
	}

	protected void setNoWait(boolean tf) {
		nowait_ = tf;
	}

	protected void setScheduleKind(int kind) {
		scheduleKind_ = kind;
	}

	protected void setChunkExpression(OpenMPToken[] expression) {
		chunkExpression_ = expression;
	}

	protected void setCopyPrivateList(OpenMPToken[] l) {
		copyPrivateList_ = l;
	}

	protected void setFlushList(OpenMPToken[] l) {
		flushList_ = l;
	}

	protected void setNumThreadsExpr(OpenMPToken[] l) {
		numThreadsExpr_ = l;
	}

	protected void setLastPrivateList(OpenMPToken[] l) {
		lastPrivateList_ = l;
	}

	protected void setRegion(IASTNode region) {
		region_ = region;
	}

	protected void setRegionFilename(String filename) {
		regionFilename_ = filename;
	}

	protected void setRegionOffset(int offset) {
		regionOffset_ = offset;
	}

	protected void setRegionLength(int length) {
		regionLength_ = length;
	}

	public void addProblem(OpenMPError error)
	{
		problemList_.add(error);
	}

	public void setLocation(IASTStatement location, int proximity)
	{
		locationNode_ = location;
		locationProximity_ = proximity;
	}

	public String toString() {
		return "PASTOMPPragma: " + getType() + " offset: " + regionOffset_ + "  length: " + regionLength_;
	}
}
