/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jun 13, 2003
 */
package org.eclipse.fdt.core.search;

import org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface ICSearchPattern extends ICSearchConstants{

	public static final int IMPOSSIBLE_MATCH = 0;
	public static final int POSSIBLE_MATCH   = 1;
	public static final int ACCURATE_MATCH   = 2;
	public static final int INACCURATE_MATCH = 3;
	
	/**
	 * @param node
	 * @return
	 */
	int matchLevel( ISourceElementCallbackDelegate node, LimitTo limit );
	
	LimitTo getLimitTo();
	boolean	canAccept( LimitTo limit );
}
