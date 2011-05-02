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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTPragma;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.OMPCFG;

/**
 * Concurrency Analysis for a given function
 * 
 * @author pazel
 */
public class FunctionConcurrencyAnalysis extends ASTVisitor
{
	protected IASTFunctionDefinition fDef_ = null;
	protected Hashtable pragmaRegionMap_ = null; // maps region to omp pragma
	protected Hashtable pragmaLocationMap_ = null; // maps stmt to set of pragmas
	protected LinkedList components_ = new LinkedList(); // of RegionConcurrencyAnalysis's

	public FunctionConcurrencyAnalysis(IASTFunctionDefinition fDef, PASTOMPPragma[] pList)
	{
		fDef_ = fDef;
		pragmaRegionMap_ = buildRegionMap(pList);
		pragmaLocationMap_ = buildLocationMap(pList);

		shouldVisitStatements = true;
		fDef_.accept(this);
	}

	/**
	 * addComponent - add a component to this analysis
	 * 
	 * @param component
	 *            - RegionConcurrencyAnalysis
	 */
	private void addComponent(RegionConcurrencyAnalysis component)
	{
		if (!components_.contains(component)) {
			components_.add(component);
			component.setParent(this);
		}
	}

	/**
	 * getComponents - get the list of existing components
	 * 
	 * @return RegionConcurrencyAnalysis
	 */
	public RegionConcurrencyAnalysis[] getComponents()
	{
		RegionConcurrencyAnalysis[] list = new RegionConcurrencyAnalysis[components_.size()];
		for (int i = 0; i < list.length; i++)
			list[i] = (RegionConcurrencyAnalysis) components_.get(i);
		return list;
	}

	/**
	 * doPhaseAnalysis - for each component
	 * 
	 */
	public void doPhaseAnalysis()
	{
		for (Iterator i = components_.iterator(); i.hasNext();) {
			RegionConcurrencyAnalysis occ = (RegionConcurrencyAnalysis) i.next();
			occ.doPhaseAnalysis();
		}
	}

	/**
	 * getNodesConcurrentTo - get all nodes concurrent to given node
	 * 
	 * @param node
	 *            - IASTNode
	 * @return Set
	 */
	public Set getNodesConcurrentTo(IASTNode node)
	{
		// designed so we use the 1st non-null. Components are assumed disjoint now.
		for (Iterator i = components_.iterator(); i.hasNext();) {
			RegionConcurrencyAnalysis component = (RegionConcurrencyAnalysis) i.next();
			Set s = component.getNodesConcurrentTo(node);
			if (s != null)
				return s;
		}
		return null;
	}

	public void printAnalysis(PrintStream ps)
	{
		int count = 0;
		for (Iterator i = components_.iterator(); i.hasNext();) {
			RegionConcurrencyAnalysis occ = (RegionConcurrencyAnalysis) i.next();
			ps.println("Component " + count + " ----------------------------------");
			occ.printComponent(ps);
			count++;
		}
	}

	/**
	 * buildRegionMap - build the region-->pragma map for this function
	 * 
	 * @param pList
	 *            - PASTPragma [] pList [obtainable from OpenMPAnalysisManager]
	 * @return Hashtable (mapping IASTNode-->PASTOMPPragma for this function)
	 */
	protected Hashtable buildRegionMap(PASTPragma[] pList)
	{
		Hashtable map = new Hashtable();

		for (int i = 0; i < pList.length; i++) {
			if (pList[i] instanceof PASTOMPPragma) {
				PASTOMPPragma pragma = (PASTOMPPragma) pList[i];
				IASTNode region = pragma.getRegion();
				if (region != null && pragma.isCompiled() && isRegionDefinedInFctn(region))
					map.put(region, pragma); // add to the map
			}
		}

		return map;
	}

	/**
	 * buildLocationMap - build a map from location to set of pragmas "following" it
	 * 
	 * @param pList
	 * @return
	 */
	protected Hashtable buildLocationMap(PASTPragma[] pList)
	{
		Hashtable map = new Hashtable();

		for (int i = 0; i < pList.length; i++) {
			if (pList[i] instanceof PASTOMPPragma) {
				PASTOMPPragma pragma = (PASTOMPPragma) pList[i];
				IASTNode location = pragma.getLocation();
				if (location != null && pragma.isCompiled() && isRegionDefinedInFctn(location)) {
					LinkedList l = (LinkedList) map.get(location);
					if (l == null) {
						l = new LinkedList();
						map.put(location, l);
					}
					l.add(pragma);
				}
			}
		}

		return map;

	}

	/**
	 * isRegionDefinedInFctn - determines in node in scope of function
	 * 
	 * @param node
	 *            - IASTNode
	 * @return boolean
	 */
	public boolean isRegionDefinedInFctn(IASTNode node)
	{
		while (node != null) {
			if (node == fDef_)
				return true;
			node = node.getParent();
		}
		return false;
	}

	/**
	 * visit - visit statements
	 * NOTE: we keep looking for stmts associated with pragmas - to build a new RegionConcurrencyAnalysis
	 * 
	 * @param statement
	 *            - IASTStatement
	 */
	public int visit(IASTStatement statement)
	{
		PASTOMPPragma pragma = (PASTOMPPragma) pragmaRegionMap_.get(statement);
		if (pragma != null) {
			buildRegionAnalysis(pragma, statement);
			return PROCESS_SKIP; // Skip the part that will be handled in more detail
		}
		return PROCESS_CONTINUE;
	}

	/**
	 * buildRegionAnalysis - build an analysis of this "pragma" region of the program
	 * 
	 * @param pragma
	 *            - PASTOMPPragma
	 * @param statement
	 *            - IASTStatement;
	 */
	protected void buildRegionAnalysis(PASTOMPPragma pragma, IASTStatement statement)
	{
		// build the cfg
		OMPCFG cfg = OMPCFGMaker.constructCFG(pragma, statement, pragmaRegionMap_, pragmaLocationMap_);

		cfg.printCFG(System.out);

		addComponent(new RegionConcurrencyAnalysis(cfg));
	}

}
