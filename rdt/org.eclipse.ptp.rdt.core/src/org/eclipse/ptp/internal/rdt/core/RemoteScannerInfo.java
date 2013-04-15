/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
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
		symbols = new HashMap<String, String>();
		includePaths = new ArrayList<String>();
	}
	
	/**
	 * Copy constructor.
	 */
	public RemoteScannerInfo(IScannerInfo scannerInfo) {
		symbols = new HashMap<String, String>(scannerInfo.getDefinedSymbols());
		String[] includePathsArray = scannerInfo.getIncludePaths();
		
		for(int k = 0; k < includePathsArray.length; k++) {
			// HACK: convert backslashes to slashes to compensate for CDT bug 315632
			includePathsArray[k] = includePathsArray[k].replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		includePaths = new ArrayList<String>(Arrays.asList(includePathsArray));
	}
	
	
	public RemoteScannerInfo(Map<String, String> macroDefinitions, String[] includes) {
		this(macroDefinitions, includes == null ? null : Arrays.asList(includes));
	}
	
	public RemoteScannerInfo(Map<String, String> macroDefinitions, List<String> includes) {
		this();
		if(macroDefinitions != null)
			symbols = macroDefinitions;
		if(includes != null) {
			//includePaths = includes;
			includePaths.clear();
			
			for(String includePath : includes) {
				// HACK: convert backslashes to slashes to compensate for CDT bug 315632
				includePaths.add(includePath.replaceAll("\\\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	

	public Map<String, String> getDefinedSymbols() {
		return Collections.unmodifiableMap(symbols);
	}

	public String[] getIncludePaths() {
		String[] paths = includePaths.toArray(new String[includePaths.size()]);
		
		//paths were changed by DescriptionScannerInfoProvider.getValues() (on Windows client at least), 
		//need to change it back for
		//CPreprocessor to pick up include paths correctly for remote include paths
		for (int i = 0; i < paths.length; i++) {
			paths[i] = paths[i].replace('\\', '/');
		}
		
		return paths;
	}
	
	@Override
	public String toString() {
		return includePaths + " " + symbols; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((includePaths == null) ? 0 : includePaths.hashCode());
		result = prime * result + ((symbols == null) ? 0 : symbols.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteScannerInfo other = (RemoteScannerInfo) obj;
		if (includePaths == null) {
			if (other.includePaths != null)
				return false;
		} else if (!includePaths.equals(other.includePaths))
			return false;
		if (symbols == null) {
			if (other.symbols != null)
				return false;
		} else if (!symbols.equals(other.symbols))
			return false;
		return true;
	}
	
	
}
