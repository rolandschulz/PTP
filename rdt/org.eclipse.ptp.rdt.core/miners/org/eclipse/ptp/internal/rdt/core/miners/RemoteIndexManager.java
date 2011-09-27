/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.cdt.internal.core.indexer.StandaloneIndexer;
import org.eclipse.cdt.internal.core.pdom.PDOMWriter;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMIndexerTask;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.IRemoteIndexerInfoProvider;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

public class RemoteIndexManager {
	

	public static final String PDOM_EXTENSION = ".pdom"; //$NON-NLS-1$
	private static final String CLASS_NAME = "CDTMiner-RemoteIndexManager"; //$NON-NLS-1$
	
	private static RemoteIndexManager theInstance = null;
	
//	private static final IParserLogService LOG = new DefaultLogService();
	private static final PDOMCLinkageFactory cLinkageFactory = new PDOMCLinkageFactory();
	private static final PDOMCPPLinkageFactory cppLinkageFactory = new PDOMCPPLinkageFactory();
	private static final IIndexLocationConverter locationConverter = new RemoteLocationConverter();
	
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
	
	
	
	public IIndex getIndexForScope(String scope, DataStore dataStore) {
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			Set<IIndexFragment> fragments = new HashSet<IIndexFragment>();
			
			for(String currentScope : ScopeManager.getInstance().getAllScopes()) {
				IIndexFragment fragment = getIndexerForScope(currentScope, dataStore, null).getIndex().getWritableFragment();
				fragments.add(fragment);
			}
			
			if(fragments.isEmpty())
				if(dataStore !=null){
					UniversalServerUtilities.logWarning(CLASS_NAME, "Index contains 0 fragments", dataStore); //$NON-NLS-1$
				}else{
					StandaloneLogService.getInstance().errorLog(CLASS_NAME +":" + "Index contains 0 fragments"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			
			return new CIndex(fragments.toArray(new IIndexFragment[fragments.size()]), fragments.size()); 
		}
		else {
			StandaloneFastIndexer indexer = getIndexerForScope(scope, dataStore, null);
			//return indexer.getIndex();
			//create a new CIndex wrapper each time to prevent deadlock.
			return new CIndex(new IIndexFragment[]{indexer.getIndex().getWritableFragment()}, 1);
		}
	}
	
	/**
	 * Gets the indexer and also sets up indexer preferences.
	 * 
	 * @see PDOMIndexerTask constructor
	 */
	public StandaloneFastIndexer getIndexerForScope(String scope, IRemoteIndexerInfoProvider provider, DataStore dataStore, DataElement status) {
		StandaloneFastIndexer indexer = getIndexerForScope(scope, dataStore, status);
		
		// configure the indexer using the provider
		indexer.setScannerInfoProvider(provider);
		indexer.setLanguageMapper(new RemoteLanguageMapper(provider, dataStore));
		indexer.setFilesToParseUpFront(provider.getFilesToParseUpFront().toArray(new String[]{}));
		indexer.setFileEncodingRegistry(provider.getFileEncodingRegistry());
		
		if(provider.checkIndexerPreference(IRemoteIndexerInfoProvider.KEY_SKIP_ALL_REFERENCES)) {
			indexer.setSkipReferences(PDOMWriter.SKIP_ALL_REFERENCES);
		}
		else {
			int skipReferences = 0;
			if(provider.checkIndexerPreference(IRemoteIndexerInfoProvider.KEY_SKIP_TYPE_REFERENCES))
				skipReferences |= PDOMWriter.SKIP_TYPE_REFERENCES;
			if(provider.checkIndexerPreference(IRemoteIndexerInfoProvider.KEY_SKIP_MACRO_REFERENCES))
				skipReferences |= PDOMWriter.SKIP_MACRO_REFERENCES;
			//if(provider.checkIndexerPreference(IRemoteIndexerInfoProvider.KEY_SKIP_IMPLICIT_REFERENCES))
			//	skipReferences |= PDOMWriter.SKIP_IMPLICIT_REFERENCES;
			
			if(skipReferences == 0)
				indexer.setSkipReferences(PDOMWriter.SKIP_NO_REFERENCES);
			else
				indexer.setSkipReferences(skipReferences);
		}
		
		indexer.setIndexAllFiles(provider.checkIndexerPreference(IRemoteIndexerInfoProvider.KEY_INDEX_ALL_FILES));

		
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

	
	public StandaloneFastIndexer getIndexerForScope(String scope, DataStore dataStore, DataElement status) {
				
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			throw new IllegalArgumentException("Attempted to get indexer for root scope."); //$NON-NLS-1$
		}
		
		StandaloneFastIndexer indexer = (StandaloneFastIndexer) scopeToIndexerMap.get(scope);
		if (indexer != null) {
			IParserLogService LOG = null;
			if (dataStore!=null) {
				LOG = new RemoteLogService(dataStore, status);
				
			}else{
				LOG = StandaloneLogService.getInstance();
				
			}
			indexer.setParserLog(LOG);
			return indexer;
		}
		
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
			if(dataStore!=null){
				UniversalServerUtilities.logWarning(CLASS_NAME, "Can't create index file at " + path + " attempting to use " + indexFile.getParent() + " instead", dataStore); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}else{
				StandaloneLogService.getInstance().traceLog(CLASS_NAME + "Can't create index file at " + path + " attempting to use " + indexFile.getParent() + " instead");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			}
			scopeToIndexLocationMap.put(scope, indexFile.getParent());
		}
		if(dataStore!=null){
			UniversalServerUtilities.logInfo(CLASS_NAME, "Index at location:" + indexFile.getAbsolutePath(), dataStore);  //$NON-NLS-1$
		}else{
			StandaloneLogService.getInstance().traceLog(CLASS_NAME + ":" + "Index at location:" + indexFile.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		try {
			IParserLogService LOG = null;
			if (dataStore!=null) {
				LOG = new RemoteLogService(dataStore, status);
			}else{
				LOG = StandaloneLogService.getInstance();
			}
			indexer = new StandaloneFastIndexer(indexFile, locationConverter, linkageFactoryMap, null, null, LOG);

			scopeToIndexerMap.put(scope, indexer);
		} catch (CoreException e) {
			if(dataStore!=null){
				UniversalServerUtilities.logError(CLASS_NAME, "Core Exception while getting indexer for scope", e, dataStore);  //$NON-NLS-1$
			}else{
				StandaloneLogService.getInstance().errorLog(CLASS_NAME +":" + "Core Exception while getting indexer for scope", e);  //$NON-NLS-1$//$NON-NLS-2$
			}
		}

		return indexer;
	}
	

	
	/**
	 * Deletes the index file associated with the given scope name.
	 * @param scope
	 * @return true if and only if the file is successfully deleted; false otherwise
	 */
	public boolean removeIndexFile(String scope, DataStore dataStore) {
		
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
		if(dataStore!=null){
			UniversalServerUtilities.logInfo(CLASS_NAME, "Remove index at location: " + indexFile.getAbsolutePath(), dataStore);  //$NON-NLS-1$
		}else{
			StandaloneLogService.getInstance().traceLog(CLASS_NAME + ":"+ "Remove index at location: " + indexFile.getAbsolutePath());  //$NON-NLS-1$//$NON-NLS-2$
		}
		
		return indexFile.delete();
	}

	/**
	 * Return a list of indexes, in which each element is an index of a given project (in projects)
	 * @param projects the projects to get the index for
	 */
	public IWritableIndex[] getIndexListForProjects(ICProject[] projects, DataStore dataStore) {
		if(projects == null) {
			throw new IllegalArgumentException("Get index for projects - projects cannot be null."); //$NON-NLS-1$
		}
		IWritableIndex[] indexList = new IWritableIndex[projects.length];
		
		
		
		Set<String> allScopes = ScopeManager.getInstance().getAllScopes();
				
		int j = 0;
		for (int i = 0; i < projects.length; i++) {
			String currentScope = projects[i].getElementName();
			if (allScopes.contains(currentScope)) {
				IWritableIndex project_index = getIndexerForScope(currentScope, dataStore, null).getIndex();
				indexList[j++] = project_index;
				
			}
		}
				
		//CIndex scope_index = new CIndex(fragments.toArray(new IIndexFragment[fragments.size()]), fragments.size()); 
		//indexList[j]= scope_index;
		return indexList;
	}
	
	/**
	 * Return a list of indexes, in which each element is an index of a given project in the given scope
	 * @param scope, a scope to get indexes
	 */
	public IWritableIndex[] getIndexListForScope(String scope, DataStore dataStore) {
		IWritableIndex[] indexList;
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			indexList = new IWritableIndex[ScopeManager.getInstance().getAllScopes().size()];
			int j = 0;
			for(String currentScope : ScopeManager.getInstance().getAllScopes()) {
				IWritableIndex project_index = getIndexerForScope(currentScope, dataStore, null).getIndex();
				indexList[j++] = project_index;
			}
		}
		else {
			StandaloneFastIndexer indexer = getIndexerForScope(scope, dataStore, null);
			indexList = new IWritableIndex[]{indexer.getIndex()};
		}
		return indexList;
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
	public String moveIndexFile(String scope, String path, DataStore dataStore) {
		String oldLocation = scopeToIndexLocationMap.get(scope); 
		String newLocation = setIndexFileLocation(scope, path);
		if(!newLocation.equals(path)) {
			UniversalServerUtilities.logWarning(CLASS_NAME, "Can't move index file to " + path + " using " + newLocation + " instead", dataStore); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		}
		
		if(newLocation.equals(oldLocation)) {
			UniversalServerUtilities.logWarning(CLASS_NAME, "Index file not moved because source and destination are the same", dataStore); //$NON-NLS-1$ 
			return oldLocation;
		}
		UniversalServerUtilities.logWarning(CLASS_NAME, "Moving index file " + scope + PDOM_EXTENSION + " from " + oldLocation + " to " + newLocation, dataStore);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		String fileName = scope + PDOM_EXTENSION;
		File pdomFile = new File(oldLocation, fileName);
		
		if(!pdomFile.exists()) {
			UniversalServerUtilities.logWarning(CLASS_NAME, "Can't find index file at " + oldLocation, dataStore); //$NON-NLS-1$
			return oldLocation;
		}
			
		boolean success = pdomFile.renameTo(new File(newLocation, fileName));
		if(success) {
			UniversalServerUtilities.logInfo(CLASS_NAME, "Index file moved from " + oldLocation + " to " + newLocation, dataStore); //$NON-NLS-1$ //$NON-NLS-2$
			return newLocation;
		}
		else {
			UniversalServerUtilities.logWarning(CLASS_NAME, "Index file could not be moved", dataStore); //$NON-NLS-1$
			return oldLocation;
		}
	}
	
	//clear cached index
	public void clearIndex(String scope) throws InterruptedException, CoreException{
		scopeToIndexerMap.put(scope, null);
		
	}
	
	
	
	
	
}