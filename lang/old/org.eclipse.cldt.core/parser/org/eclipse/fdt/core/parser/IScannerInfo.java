package org.eclipse.fdt.core.parser;

import java.util.Map;

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

public interface IScannerInfo {
	/**
	 * Answers a <code>Map</code> containing all the defined preprocessor 
	 * symbols and their values as string tuples, (symbol_name, symbol_value).
	 * Symbols defined without values have an empty string for a value. For 
	 * example,-Dsymbol=value would have a map entry (symbol,value). A symbol
	 * defined as -Dsymbol would have a map entry of (symbol,"").
	 * 
	 * If there are no defined symbols, the receiver will return 
	 * an empty Map, never <code>null</code>. 
	 *  
	 * @return
	 */
	public Map getDefinedSymbols();

	/**
	 * Answers a <code>String</code> array containing the union of all the 
	 * built-in include search paths followed by the user-defined include 
	 * search paths. 
	 * 
	 * If there are no paths defined, the receiver will return an empty 
	 * array, never <code>null</code>
	 * 
	 * @return
	 */
	public String[] getIncludePaths();
}
