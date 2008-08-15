/*******************************************************************************
 * Copyright (c) 2007-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.miners;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author crecoskie
 *
 */
public class ScopeManager {
	
	private Map<String, Set<String>> scopeNamesToFileSetMap = null;
	static private ScopeManager instance = null;
	
	private ScopeManager()
	{
		scopeNamesToFileSetMap = new TreeMap<String, Set<String>>();
	}
	
	static public synchronized ScopeManager getInstance() {
		if(instance == null)
			instance = new ScopeManager();
		
		return instance;
	}
	
	public void addScope(String name, Set<String> files)
	{
		scopeNamesToFileSetMap.put(name, files);
	}
	
	public void removeScope(String name) {
		scopeNamesToFileSetMap.remove(name);
	}
	
	public Set<String> getFilesForScope(String name) {
		return scopeNamesToFileSetMap.get(name);
	}
	
	public void addFileToScope(String scope, String filename) {
		Set<String> scopeFiles = getFilesForScope(scope);
		if(scopeFiles == null) {
			scopeFiles = new TreeSet<String>();
			scopeNamesToFileSetMap.put(scope, scopeFiles);
		}
		
		scopeFiles.add(filename);
	}
	
	public void removeFileFromScope(String scope, String filename) {
		Set<String> scopeFiles = getFilesForScope(scope);
		
		if(scopeFiles != null)
			scopeFiles.remove(filename);
	}
	
	public Set<String> getAllScopes() {
		return scopeNamesToFileSetMap.keySet();
	}

}
