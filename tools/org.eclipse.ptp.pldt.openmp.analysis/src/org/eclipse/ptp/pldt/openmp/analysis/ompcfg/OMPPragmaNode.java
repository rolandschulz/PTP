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

import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;

/**
 * 
 * @author pazel
 * 
 */
public class OMPPragmaNode extends OMPCFGNode
{
	protected PASTOMPPragma pragma_ = null; // pragma we are holding
	protected OMPPragmaNode contextPred_ = null; // backward chain of context

	protected boolean implicitBarrier_ = false; // used to tie parallel/for/sections end

	/**
	 * OMPPragmaNode - Constructor
	 * 
	 * @param pragma
	 *            - PASTOMPPragma
	 * @param contextPredecessor
	 *            - OMPPragmaNode
	 */
	public OMPPragmaNode(PASTOMPPragma pragma, OMPPragmaNode contextPredecessor)
	{
		super();
		pragma_ = pragma;
		contextPred_ = contextPredecessor;
	}

	/**
	 * OMPPragmaNode - constructor
	 * 
	 * @param pragma
	 *            - PASTOMPPragma
	 */
	public OMPPragmaNode(PASTOMPPragma pragma)
	{
		super();
		pragma_ = pragma;
	}

	public OMPPragmaNode()
	{
		super();
		implicitBarrier_ = true;
	}

	/**
	 * getPragma - accessor to pragma
	 * 
	 * @return PASTOMPPragma
	 */
	public PASTOMPPragma getPragma()
	{
		return pragma_;
	}

	/**
	 * isImplicitBarrier - returns whether if is implicit barrier
	 * 
	 * @return boolean
	 */
	public boolean isImplicitBarrier()
	{
		return implicitBarrier_;
	}

	/**
	 * getContextPredecessor - get the pragma context predecessor
	 * 
	 * @return
	 */
	public OMPPragmaNode getContextPredecessor()
	{
		return contextPred_;
	}

	/**
	 * getContext - get the list of related pragma - most recent to oldest [0--N]
	 * 
	 * @return OMPPragmaNode []
	 */
	public OMPPragmaNode[] getContext()
	{
		int count = 0;
		OMPPragmaNode pred = this;
		while (pred != null) {
			count++;
			pred = pred.getContextPredecessor();
		}

		OMPPragmaNode[] list = new OMPPragmaNode[count];
		int i = 0;
		pred = this;
		while (pred != null) {
			list[i++] = pred;
			pred = pred.getContextPredecessor();
		}

		return list;
	}

	public String getType()
	{
		if (pragma_ != null)
			return pragma_.getType();
		else {
			if (implicitBarrier_)
				return "implicit barrier";
		}
		return "";
	}
}
