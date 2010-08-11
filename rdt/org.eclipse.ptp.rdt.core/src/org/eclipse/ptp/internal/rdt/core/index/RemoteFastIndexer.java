/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.indexer.AbstractPDOMIndexer;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMRebuildTask;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * @author crecoskie
 *
 */
public class RemoteFastIndexer extends AbstractPDOMIndexer {
	public static final String ID = "org.eclipse.ptp.rdt.core.RemoteFastIndexer"; //$NON-NLS-1$	
	
	private static List<IRemoteFastIndexerListener> listeners = new LinkedList<IRemoteFastIndexerListener>();
	
	public IPDOMIndexerTask createTask(ITranslationUnit[] added, ITranslationUnit[] changed, ITranslationUnit[] removed) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(getProject().getProject());
		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
		
		if(serviceProvider instanceof IIndexServiceProvider) {
			boolean update = !isReindex();
			IndexBuildSequenceController indexBuildSequenceController = IndexBuildSequenceController.getIndexBuildSequenceController(getProject().getProject());
			if(indexBuildSequenceController.isIndexAfterBuildSet()){
				
				if(update){
					
					if(indexBuildSequenceController.shouldSkipIndexUpdate()){
						return null;
					}
					/* we don't turn index update to reindex.
					if(indexBuildSequenceController.shouldTurnIndexUpdateToReindex()){
						update = false;
					}
					*/
					
					
				}else{
					if(indexBuildSequenceController.shouldSkipReindex()){
						return null;
					}
				}
			}
			
			indexBuildSequenceController.setIndexRunning();	
			return new RemoteIndexerTask(this, (IIndexServiceProvider) serviceProvider, added, changed, removed, update);
			
		}
		
		return null;
	}

	
	protected static boolean isReindex() {
		// Uses reflection to determine if createTask() is being called from PDOMRebuildTask.
		String pdomRebuildTask = PDOMRebuildTask.class.getName();
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for(StackTraceElement frame : stackTrace) {
			if(frame.getClassName().equals(pdomRebuildTask)) {
				return true;
			}
		}	
		return false;
	}

	
	public String getID() {
		return ID;
	}
	
	/**
	 * Add an event listener to the list, duplicates will not be registered
	 * @param listener The listener to register
	 */
	public static void addRemoteFastIndexerListener(IRemoteFastIndexerListener listener) {
		if (!listeners.contains(listener)) 
				listeners.add(listener);
	}

	/**
	 * Get the collection of listeners wishing to receive notification events from the indexer
	 * @return A List containing all the listeners for this indexer
	 */
	static Collection<IRemoteFastIndexerListener> getRemoteFastIndexerListeners() {
		return Collections.unmodifiableCollection(listeners);
	}
}
