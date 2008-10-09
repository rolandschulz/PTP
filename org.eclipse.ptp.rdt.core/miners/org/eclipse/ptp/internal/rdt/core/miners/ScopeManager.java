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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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

	/**
	 * Returns a Set of all scopes that the specified file occurs in.
	 * 
	 * @param filename
	 * @return
	 */
	public Set<String> getScopesForFile(String filename) {
		Set<String> resultSet = new TreeSet<String>();
		
		// canonicalize the filename
		IPath path = Path.fromOSString(filename);
		String canonicalPath = path.toOSString();
		
		
		// HACK
		// FIXME IPath.fromOSString() won't strip leading double slashes due to support for UNC paths.
		// We need to strip them.  This means that using UNC paths on a UNIX backend won't work.  Oh well.
		if(canonicalPath.startsWith("//")) { //$NON-NLS-1$
			canonicalPath = canonicalPath.substring(1);
		}
		
		
		// check each scope for the file
		Set<String> allScopes = getAllScopes();
		Set<String> scopeFiles = null;
		
		for(String scopeName : allScopes) {
			scopeFiles = getFilesForScope(scopeName);
			if(scopeFiles.contains(canonicalPath)) {
				resultSet.add(scopeName);
			}
		}
		
		return resultSet;
	}
	
}
