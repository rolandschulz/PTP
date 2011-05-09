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
package org.eclipse.ptp.pldt.openmp.analysis.ompcfg;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;

/**
 * Holds a consolidated (coarse) basic block for concurrency analysis
 * 
 * @author pazel
 * 
 */
public class OMPExpressionBlock extends OMPCFGNode
{
	/** Use this while building */
	protected LinkedList expressions_ = new LinkedList(); // of IASTExpressions (for now)

	public OMPExpressionBlock()
	{
		super();
	}

	/**
	 * addExpression - add a statement to the basic block
	 * 
	 * @param expression
	 *            - IASTExpression
	 */
	public void addExpression(IASTExpression expression)
	{
		expressions_.add(expression);
	}

	/**
	 * addExpression - but for an expression list
	 * 
	 * @param expression
	 *            - IASTExpressionList
	 */
	public void addExpression(IASTExpressionList expression)
	{
		IASTExpression[] list = expression.getExpressions();
		for (int i = 0; i < list.length; i++) {
			if (list[i] instanceof IASTExpressionList)
				addExpression((IASTExpressionList) list[i]);
			else
				addExpression(list[i]);
		}
	}

	/**
	 * getExpressions - accessor to array of statements
	 * 
	 * @return IASTExpression []
	 */
	public IASTExpression[] getExpressions()
	{
		IASTExpression[] list = new IASTExpression[expressions_.size()];
		int count = 0;
		for (Iterator i = expressions_.iterator(); i.hasNext();)
			list[count++] = (IASTExpression) i.next();
		return list;
	}

}
