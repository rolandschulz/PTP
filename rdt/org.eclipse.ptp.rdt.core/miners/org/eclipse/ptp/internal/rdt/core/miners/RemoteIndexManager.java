/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.cdt.internal.core.indexer.StandaloneIndexer;
import org.eclipse.cdt.internal.core.pdom.PDOMWriter;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.model.Scope;


public class RemoteIndexManager {
	
	public static final String PDOM_EXTENSION = ".pdom"; //$NON-NLS-1$
	
	private static RemoteIndexManager theInstance = null;
	
	private static final IParserLogService LOG = new DefaultLogService();
	private static final PDOMCLinkageFactory cLinkageFactory = new PDOMCLinkageFactory();
	private static final PDOMCPPLinkageFactory cppLinkageFactory = new PDOMCPPLinkageFactory();
	private static final IIndexLocationConverter locationConverter = new RemoteLocationConverter();;
	
	private static final Map<String, IPDOMLinkageFactory> linkageFactoryMap = new HashMap<String, IPDOMLinkageFactory>();
	static {
		linkageFactoryMap.put(ILinkage.C_LINKAGE_NAME, cLinkageFactory);
		linkageFactoryMap.put(ILinkage.CPP_LINKAGE_NAME, cppLinkageFactory);
	}
	
	private final Map<String,StandaloneIndexer> scopeToIndexerMap;
	private final Map<String,String> scopeToIndexLocationMap;
	
	
	private RemoteIndexManager() {
		scopeToIndexerMap = new HashMap<String,StandaloneIndexer>();
		scopeToIndexLocationMap = new HashMap<String,String>();
	}

	public static synchronized RemoteIndexManager getInstance() {
		if(theInstance == null)
			theInstance = new RemoteIndexManager();
		
		return theInstance;
	}
	
	
	
	public IIndex getIndexForScope(String scope) {
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			Set<IIndexFragment> fragments = new HashSet<IIndexFragment>();
			
			for(String currentScope : ScopeManager.getInstance().getAllScopes()) {
				IIndexFragment fragment = getIndexerForScope(currentScope).getIndex().getWritableFragment();
				fragments.add(fragment);
			}
			
			if(fragments.isEmpty())
				System.out.println("Warning: index contains 0 fragments"); //$NON-NLS-1$
			
			return new CIndex(fragments.toArray(new IIndexFragment[fragments.size()]), fragments.size()); 
		}
		else {
			StandaloneFastIndexer indexer = getIndexerForScope(scope);
			return indexer.getIndex();
		}
	}
	
	
	/**
	 * Gets the indexer and also sets up indexer preferences.
	 * 
	 * @see PDOMIndexerTask constructor
	 */
	public StandaloneFastIndexer getIndexerForScope(String scope, IRemoteIndexerInfoProvider provider) {
		StandaloneFastIndexer indexer = getIndexerForScope(scope);
		
		// configure the indexer using the provider
		indexer.setScannerInfoProvider(provider);
		indexer.setLanguageMapper(new RemoteLanguageMapper(provider));
		indexer.setFilesToParseUpFront(provider.getFilesToParseUpFront().toArray(new String[]{}));
		
		Map<String,Boolean> prefs = provider.getIndexerPreferences();
		
		if (!prefs.isEmpty()) {
			if(prefs.get(IRemoteIndexerInfoProvider.KEY_SKIP_ALL_REFERENCES)) {
				indexer.setSkipReferences(PDOMWriter.SKIP_ALL_REFERENCES);
			}
			else {
				int skipReferences = 0;
				if(prefs.get(IRemoteIndexerInfoProvider.KEY_SKIP_TYPE_REFERENCES))
					skipReferences |= PDOMWriter.SKIP_TYPE_REFERENCES;
				if(prefs.get(IRemoteIndexerInfoProvider.KEY_SKIP_MACRO_REFERENCES))
					skipReferences |= PDOMWriter.SKIP_MACRO_REFERENCES;
				//if(prefs.get(IRemoteIndexerInfoProvider.KEY_SKIP_IMPLICIT_REFERENCES))
				//	skipReferences |= PDOMWriter.SKIP_IMPLICIT_REFERENCES;
				
				if(skipReferences == 0)
					indexer.setSkipReferences(PDOMWriter.SKIP_NO_REFERENCES);
				else
					indexer.setSkipReferences(skipReferences);
			}
			
			indexer.setIndexAllFiles(prefs.get(IRemoteIndexerInfoProvider.KEY_INDEX_ALL_FILES));
		}
		
		return indexer;
	}
	
	
	/**
	 * Creates the given directory if it doesn't already exist.
	 */
	private boolean createIndexDirectory(String path) {
		if(path == null)
			return false;
		
		File dir = new File(path);
		if(dir.exists())
			return dir.canWrite();
		
		return dir.mkdirs();
	}

	
	public StandaloneFastIndexer getIndexerForScope(String scope) {
		
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			throw new IllegalArgumentException("Attempted to get indexer for root scope."); //$NON-NLS-1$
		}
		
		StandaloneFastIndexer indexer = (StandaloneFastIndexer) scopeToIndexerMap.get(scope);
		if (indexer != null)
			return indexer;

		String path = scopeToIndexLocationMap.get(scope);
		File indexFile = null;
		
		if(path != null) {
			indexFile = new File(path, scope + PDOM_EXTENSION);
			try {
				if(!indexFile.exists() && !indexFile.createNewFile()) {
					indexFile = null;
				}
			} catch (IOException e) {
				indexFile = null;
			}
		}
		
		if(indexFile == null) {
			indexFile = new File(scope + PDOM_EXTENSION); // creates a file object located in the server working directory
			System.err.printf("Can't create index file at %s, attempting to use %s instead\n", path, indexFile.getParent()); //$NON-NLS-1$
			scopeToIndexLocationMap.put(scope, indexFile.getParent());
		}
		
		System.out.println("Index at location: " + indexFile.getAbsolutePath()); //$NON-NLS-1$
		System.out.flush();

		try {
			indexer = new StandaloneFastIndexer(indexFile, locationConverter, linkageFactoryMap, null, LOG);

			scopeToIndexerMap.put(scope, indexer);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return indexer;
	}
	

	
	/**
	 * Deletes the index file associated with the given scope name.
	 * @param scope
	 * @return true if and only if the file is successfully deleted; false otherwise
	 */
	public boolean removeIndexFile(String scope) {
		
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			throw new IllegalArgumentException("Attempted to remove index file for root scope."); //$NON-NLS-1$
		}
		
		scopeToIndexerMap.remove(scope);
		String loc = scopeToIndexLocationMap.remove(scope);
		
		File indexFile;
		if(loc == null)
			indexFile = new File(scope + PDOM_EXTENSION);
		else
			indexFile = new File(loc, scope + PDOM_EXTENSION);
		
		System.out.println("Remove index at location: " + indexFile.getAbsolutePath()); //$NON-NLS-1$
		System.out.flush();
		
		return indexFile.delete();
	}

	/**
	 * Returns the index for the given projects.
	 * @param projects the projects to get the index for
	 * @return an index for the projects
	 */
	public IIndex getIndexForProjects(ICProject[] projects) {
		if(projects == null) {
			throw new IllegalArgumentException("Get index for projects - projects cannot be null."); //$NON-NLS-1$
		}
		
		Set<IIndexFragment> fragments = new HashSet<IIndexFragment>();
		
		Set<String> allScopes = ScopeManager.getInstance().getAllScopes();
				
		for (int i = 0; i < projects.length; i++) {
			String currentScope = projects[i].getElementName();
			if (allScopes.contains(currentScope)) {
				IIndexFragment fragment = getIndexerForScope(currentScope).getIndex().getWritableFragment();
				
				fragments.add(fragment);
			}
		}
				
		CIndex index = new CIndex(fragments.toArray(new IIndexFragment[fragments.size()]), fragments.size()); 
		return index;
	}

	
	public String setIndexFileLocation(String scope, String configLocation) {
		String oldLocation = scopeToIndexLocationMap.get(scope); 
		if(configLocation.equals(oldLocation))
			return configLocation;
		
		scopeToIndexerMap.remove(scope); // invalidates the indexer object that uses the old location
		
		if(createIndexDirectory(configLocation)) {
			scopeToIndexLocationMap.put(scope, configLocation);
			return configLocation;
		}
		else {
			String serverDir = System.getProperty("user.dir"); //$NON-NLS-1$
			scopeToIndexLocationMap.put(scope, serverDir);
			return serverDir;
			
		}
	}
	
	
	/**
	 * Attempts to move the pdom file to the given path. If that path
	 * is not writable it attempt to move the pdom to the server installation
	 * path instead.
	 * 
	 * @return The actual path to where the file was moved.
	 */
	public String moveIndexFile(String scope, String path) {
		String oldLocation = scopeToIndexLocationMap.get(scope); 
		String newLocation = setIndexFileLocation(scope, path);
		if(!newLocation.equals(path)) {
			System.out.printf("Can't move index file to %s, using %s instead\n", path, newLocation); //$NON-NLS-1$ 
		}
		
		if(newLocation.equals(oldLocation)) {
			System.out.println("Index file not moved because source and destination are the same"); //$NON-NLS-1$ 
			return oldLocation;
		}
		
		System.out.printf("Moving index file %s%s from %s to %s\n", scope, PDOM_EXTENSION, oldLocation, newLocation);//$NON-NLS-1$ 
		
		String fileName = scope + PDOM_EXTENSION;
		File pdomFile = new File(oldLocation, fileName);
		
		if(!pdomFile.exists()) {
			System.out.println("Can't find index file at " + oldLocation); //$NON-NLS-1$
			return oldLocation;
		}
			
		boolean success = pdomFile.renameTo(new File(newLocation, fileName));
		if(success) {
			System.out.printf("Index file moved from %s to %s\n", oldLocation, newLocation); //$NON-NLS-1$
			return newLocation;
		}
		else {
			System.out.println("Index file could not be moved"); //$NON-NLS-1$
			return oldLocation;
		}
	}
}