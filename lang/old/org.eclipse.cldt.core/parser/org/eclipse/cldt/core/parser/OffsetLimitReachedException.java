/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cldt.core.parser;

import org.eclipse.cldt.core.parser.ast.IASTCompletionNode;

/**
 * @author jcamelon
 */
public class OffsetLimitReachedException extends EndOfFileException {

	private final IASTCompletionNode node;
	private final IToken finalToken;
	
	public OffsetLimitReachedException( IASTCompletionNode node )
	{
		this.node = node;
		finalToken = null;
	}
	
	public OffsetLimitReachedException( IToken token )
	{
		finalToken = token;
		node = null;
	}
	
	/**
	 * @return Returns the finalToken.
	 */
	public IToken getFinalToken() {
		return finalToken;
	}
	
	/**
	 * @return returns the IASTCompletionNode
	 */
	public IASTCompletionNode getCompletionNode()
	{
		return node;
	}

    /**
	 * @author jcamelon
	 */

}
