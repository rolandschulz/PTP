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
package org.eclipse.cldt.internal.core.parser;

import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.core.ICLogConstants;
import org.eclipse.cldt.core.parser.IParserLogService;
import org.eclipse.cldt.internal.core.model.Util;
import org.eclipse.cldt.internal.core.model.IDebugLogConstants.DebugLogConstant;

/**
 * @author jcamelon
 *
 */
public class ParserLogService implements IParserLogService
{

	final DebugLogConstant topic; 
	/**
	 * @param constant
	 */
	public ParserLogService(DebugLogConstant constant) {
		topic = constant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#traceLog(java.lang.String)
	 */
	public void traceLog(String message)
	{
		Util.debugLog( message, topic );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IParserLogService#errorLog(java.lang.String)
	 */
	public void errorLog(String message)
	{
		Util.log( message, ICLogConstants.CDT );
	}

	public boolean isTracing(){
		if( FortranCorePlugin.getDefault() == null )
			return false;
		
		return ( FortranCorePlugin.getDefault().isDebugging() && Util.isActive( topic ) ); 
	}
}
