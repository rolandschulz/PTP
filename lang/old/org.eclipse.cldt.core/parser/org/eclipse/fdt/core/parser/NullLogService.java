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
package org.eclipse.fdt.core.parser;

/**
 * @author jcamelon
 */
public class NullLogService implements IParserLogService {

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.IParserLogService#traceLog(java.lang.String)
	 */
	public void traceLog(String message) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.IParserLogService#errorLog(java.lang.String)
	 */
	public void errorLog(String message) {
	}

	public boolean isTracing(){
		return false;
	}
}
