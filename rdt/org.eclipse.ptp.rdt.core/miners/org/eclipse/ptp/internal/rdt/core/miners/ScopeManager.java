/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
	
	// TODO this is a horrible hack
	private static final String DEFAULT_SCHEME = "rse"; //$NON-NLS-1$ 
	
	
	public class ScopeData {
		public String scheme;
		public String host;
		public String mappedPath;
		public String rootPath;
		public Set<String> files;
	}
	
	private Map<String, ScopeData> fScopeNamesToScopeDataMap = null;
	private Map<String, String> fFilePathToScopeNameMap = null;
	static private ScopeManager fInstance = null;
	
	private ScopeManager()
	{
		fScopeNamesToScopeDataMap = new TreeMap<String, ScopeData>();
		fFilePathToScopeNameMap = new TreeMap<String, String>();
		
	}
	
	static public synchronized ScopeManager getInstance() {
		if(fInstance == null)
			fInstance = new ScopeManager();
		
		return fInstance;
	}
	
	public synchronized void addScope(String scopeName, String scheme, String host, Set<String> files, String rootPath, String mappedPath)
	{
		ScopeData data = new ScopeData();
		data.scheme = scheme;
		data.host = host;
		data.rootPath = rootPath;
		data.mappedPath = mappedPath;
		data.files = files;
		fScopeNamesToScopeDataMap.put(scopeName, data);
		
		for(String filename : files) {
			fFilePathToScopeNameMap.put(filename, scopeName);
		}
		
	}
	
		public synchronized void removeScope(String name) {
		fScopeNamesToScopeDataMap.remove(name);
	}
	
	public synchronized Set<String> getFilesForScope(String name) {
		return fScopeNamesToScopeDataMap.get(name).files;
	}
	
	public void addFileToScope(String scope, String scheme, String host, String filename, String rootPath, String mappedPath) {
		ScopeData data = fScopeNamesToScopeDataMap.get(scope);
		
		if(data == null) {
			// empty scope... create it
			data = new ScopeData();
			data.files = new TreeSet<String>();
			data.files.add(filename);
			data.scheme = scheme;
			data.host = host;
			data.rootPath = rootPath;
			data.mappedPath = mappedPath;
			fScopeNamesToScopeDataMap.put(scope, data);
			
		}
		
		else {
			Set<String> scopeFiles = data.files;
			
			if(scopeFiles == null) {
				scopeFiles = new TreeSet<String>();
				data.files = scopeFiles;
			}
			
			scopeFiles.add(filename);
		}
		
		fFilePathToScopeNameMap.put(filename, scope);
	}
	
	public void removeFileFromScope(String scope, String filename) {
		Set<String> scopeFiles = getFilesForScope(scope);
		
		if(scopeFiles != null)
			scopeFiles.remove(filename);
		
		fFilePathToScopeNameMap.remove(filename);
	}
	
	public Set<String> getAllScopes() {
		return fScopeNamesToScopeDataMap.keySet();
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
	
	/**
	 * Returns the name of the scope the file belongs to, or null if there is no such scope.
	 * 
	 * @param filename
	 * @return
	 */
	public String getScopeForFile(String filename) {
		return fFilePathToScopeNameMap.get(filename);
	}
	
	/**
	 * Returns the EFS URI scheme which is used to host a given scope (project).
	 * 
	 * @param scopeName
	 * @return String
	 */
	public String getSchemeForScope(String scopeName) {
		ScopeData data = fScopeNamesToScopeDataMap.get(scopeName);
		if(data == null)
			return DEFAULT_SCHEME;
		else
			return data.scheme;
	}
	
	/**
	 * Returns an EFS URI scheme which can be used to access the given file.
	 * 
	 * @param filename
	 * @return
	 */
	public String getSchemeForFile(String filename) {
		String scope = getScopeForFile(filename);
		if(scope != null)
			return getSchemeForScope(scope);
		else
			return DEFAULT_SCHEME;
	}
	
	public String getMappedPathForFile(String filename) {
				
		String scope = getScopeForFile(filename);
		
		if(scope == null) {
			// it's an external file, so it will never be mapped... just return itself
			return filename;
		}
		
		ScopeData sd = fScopeNamesToScopeDataMap.get(scope);
		
		String scopeMappedPath = sd.mappedPath;
		String scopeRootPath = sd.rootPath;
		
		if(scopeMappedPath == null || scopeRootPath == null)
			return null;
		
		// figure out if the file resides as a child of the scope's root,
		// or if it's wholly external
		IPath scopePath = new Path(scopeRootPath);
		IPath mappedScopePath = new Path(scopeMappedPath);
		
		IPath filePath = new Path(filename);
		
		if(scopePath.isPrefixOf(filePath)) {
			int numSegments = filePath.matchingFirstSegments(scopePath);
			IPath fileMappedPath = mappedScopePath.append(filePath.removeFirstSegments(numSegments));
			
			return fileMappedPath.toString();
		}
		
		return null;
	}

	public String getHostForFile(String path, String defaultHost) {
		String scope = getScopeForFile(path);
		
		if(scope == null) {
			// external file...  use the default
			return defaultHost;
		}
		
		ScopeData sd = fScopeNamesToScopeDataMap.get(scope);
		return sd.host;
	}
	
	public String getRootPath(String scopeName){
		ScopeData sd = fScopeNamesToScopeDataMap.get(scopeName);
		return sd.rootPath;
	}
}
