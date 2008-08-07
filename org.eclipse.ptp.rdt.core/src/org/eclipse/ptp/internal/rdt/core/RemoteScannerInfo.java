/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * An implementation of IScannerInfo that is Serializable and immutable.
 *
 */
public class RemoteScannerInfo implements IScannerInfo, Serializable {
	private static final long serialVersionUID = 1L;
	
	private Map<String, String> symbols;
	private List<String> includePaths;

	
	/**
	 * Creates an empty RemoteScannerInfo object.
	 */
	public RemoteScannerInfo() {
		symbols = Collections.emptyMap();
		includePaths = Collections.emptyList();
	}
	
	/**
	 * Copy constructor.
	 */
	public RemoteScannerInfo(IScannerInfo scannerInfo) {
		symbols = new HashMap<String, String>(scannerInfo.getDefinedSymbols());
		includePaths = new ArrayList<String>(Arrays.asList(scannerInfo.getIncludePaths()));
	}
	
	
	public RemoteScannerInfo(Map<String, String> macroDefinitions, String[] includes) {
		this(macroDefinitions, includes == null ? null : Arrays.asList(includes));
	}
	
	public RemoteScannerInfo(Map<String, String> macroDefinitions, List<String> includes) {
		this();
		if(macroDefinitions != null)
			symbols = macroDefinitions;
		if(includes != null)
			includePaths = includes;
	}
	

	public Map<String, String> getDefinedSymbols() {
		return Collections.unmodifiableMap(symbols);
	}

	public String[] getIncludePaths() {
		return includePaths.toArray(new String[includePaths.size()]);
	}
	
	@Override
	public String toString() {
		return includePaths + " " + symbols; //$NON-NLS-1$
	}
}
