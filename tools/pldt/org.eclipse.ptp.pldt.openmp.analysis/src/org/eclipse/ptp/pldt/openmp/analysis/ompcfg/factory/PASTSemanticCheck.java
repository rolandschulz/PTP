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

import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPErrorManager;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFG;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFGNode;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPDFS;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPPragmaNode;

/**
 * Look for common semantic errors
 * 
 * @author pazel
 * 
 */
public class PASTSemanticCheck
{
	protected int numErrors_ = 0;

	protected PASTSemanticCheck(OMPCFG cfg)
	{
		SemanticDFS dfs = new SemanticDFS(cfg.getRoot());
		dfs.startWalking();

	}

	public static boolean checkSemantics(OMPCFG cfg)
	{
		PASTSemanticCheck psc = new PASTSemanticCheck(cfg);
		return psc.getNumErrors() == 0 ? true : false;
	}

	public int getNumErrors()
	{
		return numErrors_;
	}

	private void checkPragmaSemantics(OMPPragmaNode pnode)
	{
		PASTOMPPragma pragma = pnode.getPragma();
		if (pragma == null) // implicit barrier
			return;

		switch (pragma.getOMPType()) {
		// followed by structured region
		case PASTOMPPragma.OmpParallel:
			parallelCheck(pnode);
			break;
		case PASTOMPPragma.OmpSections:
		case PASTOMPPragma.OmpSection:
		case PASTOMPPragma.OmpParallelSections:
		case PASTOMPPragma.OmpSingle:
			break;
		case PASTOMPPragma.OmpMaster:
			masterCheck(pnode);
			break;
		case PASTOMPPragma.OmpCritical:
			break;
		case PASTOMPPragma.OmpOrdered:
			orderedCheck(pnode);
			break;

		// Must be followed by FOR
		case PASTOMPPragma.OmpFor:
		case PASTOMPPragma.OmpParallelFor:
			forCheck(pnode);
			break;

		// Stands alone
		case PASTOMPPragma.OmpBarrier:
			barrierCheck(pnode);
			break;
		case PASTOMPPragma.OmpFlush:
		case PASTOMPPragma.OmpThreadPrivate:
			break;

		// Followed by expression
		case PASTOMPPragma.OmpAtomic:
			break;
		case PASTOMPPragma.OmpUnknown:
			return;
		}
		return;
	}

	/**
	 * parallelCheck - semantic check for Parallel statements
	 * 
	 * @param pnode
	 *            - OMPPragmaNode
	 */
	private void parallelCheck(OMPPragmaNode pnode)
	{
		// A parallel directive dynamically inside another parallel
		// logically etablishes a new team which is composed of only the current thread,
		// unless nested parallelism is enabled.
		OMPPragmaNode parent = pnode.getContextPredecessor();
		while (parent != null) {
			int pType = parent.getPragma().getOMPType();
			if (pType == PASTOMPPragma.OmpParallel ||
					pType == PASTOMPPragma.OmpParallelFor ||
					pType == PASTOMPPragma.OmpParallelSections) {
				handleProblem(pnode.getPragma(),
						"Parallel directive dynamically inside another parallel, establishes single thread context",
						OpenMPError.WARN);
				numErrors_++;
				break;
			}
			parent = parent.getContextPredecessor();
		}
		return;
	}

	/**
	 * forCheck - Semantic check for For/parallelFor semantic mistakes
	 * 
	 * @param pnode
	 *            - OMPPragmaNode (for for/parallelFor)
	 */
	private void forCheck(OMPPragmaNode pnode)
	{
		// A for directive is not allowed to nest with another for, sections,
		// and single directive bound to the same parallel
		// neither for ritical, ordered, and master regions
		PASTOMPPragma pragma = pnode.getPragma();
		boolean isParallel = (pragma.getOMPType() == PASTOMPPragma.OmpParallelFor);
		OMPPragmaNode parent = pnode.getContextPredecessor();
		boolean errorFound = false;
		while (parent != null) {
			int parentType = parent.getPragma().getOMPType();
			if (!errorFound) {
				if (parentType == PASTOMPPragma.OmpFor ||
						parentType == PASTOMPPragma.OmpSections ||
						parentType == PASTOMPPragma.OmpSingle) {
					errorFound = true;
				}
				else if (parentType == PASTOMPPragma.OmpCritical ||
						parentType == PASTOMPPragma.OmpOrdered ||
						parentType == PASTOMPPragma.OmpMaster) {
					handleProblem(pnode.getPragma(),
							"For directive embedded within critical, ordered, or master extents",
							OpenMPError.ERROR);
					numErrors_++;
					errorFound = true;
					break;
				}
				else if (parentType == PASTOMPPragma.OmpParallelFor ||
						parentType == PASTOMPPragma.OmpParallelSections) {
					handleProblem(pnode.getPragma(),
							"For directive embedded within another parallel for or parallel sections",
							OpenMPError.ERROR);
					numErrors_++;
					errorFound = true;
					break;
				}
				else if (isParallel && parentType == PASTOMPPragma.OmpParallel) {
					handleProblem(pnode.getPragma(),
							"Parallel directive dynamically inside another parallel, establishes single thread context",
							OpenMPError.WARN);
					numErrors_++;
					break;
				}
			}
			if (errorFound && (parentType == PASTOMPPragma.OmpParallel ||
					parentType == PASTOMPPragma.OmpParallelFor ||
					parentType == PASTOMPPragma.OmpParallelSections))
			{
				handleProblem(pnode.getPragma(),
						"For directive embedded within another for, sections, or single directive",
						OpenMPError.ERROR);
				numErrors_++;
				break;
			}
			parent = parent.getContextPredecessor();
		}
		return;
	}

	/**
	 * barrierCheck - semantic check on barrier statement
	 * 
	 * @param pnode
	 *            - OMPPragmaNode
	 */
	private void barrierCheck(OMPPragmaNode pnode)
	{
		// Barrier directives are not permitted in the dynamic extent of for, ordered, sections, single,
		// master, and critical regions
		OMPPragmaNode parent = pnode.getContextPredecessor();
		while (parent != null) {
			int pType = parent.getPragma().getOMPType();
			if (pType == PASTOMPPragma.OmpFor || pType == PASTOMPPragma.OmpParallelFor ||
					pType == PASTOMPPragma.OmpOrdered || pType == PASTOMPPragma.OmpSections ||
					pType == PASTOMPPragma.OmpSingle || pType == PASTOMPPragma.OmpMaster ||
					pType == PASTOMPPragma.OmpCritical) {
				handleProblem(pnode.getPragma(),
						"Barrier directive not permitted in region extent of for, ordered, sections, single, master, and critical",
						OpenMPError.ERROR);
				numErrors_++;
				break;
			}
			parent = parent.getContextPredecessor();
		}
		return;
	}

	/**
	 * masterCheck - semantic check for use of master directive
	 * 
	 * @param pnode
	 *            - OMPPragmaNode
	 */
	private void masterCheck(OMPPragmaNode pnode)
	{
		// Master directives are not permitted in the dynamic extent of
		// for, sections, and single
		OMPPragmaNode parent = pnode.getContextPredecessor();
		while (parent != null) {
			int pType = parent.getPragma().getOMPType();
			if (pType == PASTOMPPragma.OmpFlush || pType == PASTOMPPragma.OmpParallelFor ||
					pType == PASTOMPPragma.OmpSections || pType == PASTOMPPragma.OmpParallelSections ||
					pType == PASTOMPPragma.OmpSingle) {
				handleProblem(pnode.getPragma(),
						"Master directive not permitted in dynamic extent of for, sections, or single directives",
						OpenMPError.ERROR);
				numErrors_++;
				break;
			}
			parent = parent.getContextPredecessor();
		}
		return;
	}

	/**
	 * orderedCheck - semantic check on ordered directive
	 * 
	 * @param pnode
	 *            - OMPPragmaNode
	 */
	private void orderedCheck(OMPPragmaNode pnode)
	{
		// Ordered directives are not permitted in the dynamic extend of critical regions
		OMPPragmaNode parent = pnode.getContextPredecessor();
		while (parent != null) {
			int pType = parent.getPragma().getOMPType();
			if (pType == PASTOMPPragma.OmpCritical) {
				handleProblem(pnode.getPragma(),
						"Ordered directive not permitted in dynamic extent of critical region",
						OpenMPError.ERROR);
				numErrors_++;
				break;
			}
			parent = parent.getContextPredecessor();
		}
		return;
	}

	/**
	 * handleProblem
	 * 
	 * @param description
	 *            - String
	 * @param severity
	 *            - int
	 */
	private void handleProblem(PASTOMPPragma pragma, String description, int severity)
	{
		OpenMPError error = new OpenMPError(description,
				pragma.getContainingFilename(),
				pragma.getStartingLine(),
				severity);
		OpenMPErrorManager.getCurrentErrorManager().addError(error);
		pragma.addProblem(error); // we really don't need this, but may be useful later
	}

	private class SemanticDFS extends OMPDFS
	{

		public SemanticDFS(OMPCFGNode startNode)
		{
			super(startNode);
		}

		public int visit(OMPCFGNode node)
		{
			if (node instanceof OMPPragmaNode)
				checkPragmaSemantics((OMPPragmaNode) node);
			return 0;
		}

	}

}
