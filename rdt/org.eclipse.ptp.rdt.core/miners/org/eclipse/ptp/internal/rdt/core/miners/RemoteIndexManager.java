/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.cdt.internal.core.indexer.StandaloneIndexer;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.model.Scope;

/**
 * @author crecoskie
 *
 */
public class RemoteIndexManager {
	
	public static final String PDOM_EXTENSION = ".pdom"; //$NON-NLS-1$
	private static final IParserLogService LOG = new DefaultLogService();
	static private RemoteIndexManager theInstance = null;
	private Map<String,StandaloneIndexer> scopeToIndexerMap = null;
	private static final PDOMCLinkageFactory cLinkageFactory = new PDOMCLinkageFactory();
	private static final PDOMCPPLinkageFactory cppLinkageFactory = new PDOMCPPLinkageFactory();
	private static final IIndexLocationConverter locationConverter = new RemoteLocationConverter();;
	
	private static final Map<String, IPDOMLinkageFactory> linkageFactoryMap = new HashMap<String, IPDOMLinkageFactory>();
	
	static {
		linkageFactoryMap.put(ILinkage.C_LINKAGE_NAME, cLinkageFactory);
		linkageFactoryMap.put(ILinkage.CPP_LINKAGE_NAME, cppLinkageFactory);
	}
	
	private RemoteIndexManager() {
		scopeToIndexerMap = new HashMap<String,StandaloneIndexer>();
	}

	static public synchronized RemoteIndexManager getInstance() {
		if(theInstance == null)
			theInstance = new RemoteIndexManager();
		
		return theInstance;
	}
	
	public IIndex getIndexForScope(String scope) {
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			Set<IIndexFragment> fragments = new HashSet<IIndexFragment>();
			
			Set<String> allScopes = ScopeManager.getInstance().getAllScopes();
			
			Iterator<String> iterator = allScopes.iterator();
			
			while(iterator.hasNext()) {
				String currentScope = iterator.next();
				
				IIndexFragment fragment = getIndexerForScope(currentScope).getIndex().getWritableFragment();
				
				fragments.add(fragment);
			}
			
			CIndex index = new CIndex(fragments.toArray(new IIndexFragment[fragments.size()]), fragments.size()); 
			return index;
		}
		
		else {
			StandaloneFastIndexer indexer = getIndexerForScope(scope);
			return indexer.getIndex();
		}
	}
	
	public StandaloneFastIndexer getIndexerForScope(String scope) {
		
		if(scope.equals(Scope.WORKSPACE_ROOT_SCOPE_NAME)) {
			throw new IllegalArgumentException("Attempted to get indexer for root scope."); //$NON-NLS-1$
		}
		
		StandaloneFastIndexer indexer = (StandaloneFastIndexer) scopeToIndexerMap.get(scope);

		if (indexer != null)
			return indexer;

		File indexFile = new File(scope + PDOM_EXTENSION);
		
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
}