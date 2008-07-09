/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;

/**
 * @author crecoskie
 *
 */
public class RemoteIndexerTask implements IPDOMIndexerTask {

	protected RemoteFastIndexer fIndexer;
	
	protected IIndexServiceProvider fIndexServiceProvider;
	
	protected ITranslationUnit[] fAdded;
	protected ITranslationUnit[] fChanged;
	protected ITranslationUnit[] fRemoved;
	
	public RemoteIndexerTask(RemoteFastIndexer indexer,
			IIndexServiceProvider indexingServiceProvider, ITranslationUnit[] added, ITranslationUnit[] changed, ITranslationUnit[] removed) {
		fIndexer = indexer;
		
		fIndexServiceProvider = indexingServiceProvider;
		
		fAdded = added;
		fChanged = changed;
		fRemoved = removed;
		
	}
	
	private RemoteIndexerTask() {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.IPDOMIndexerTask#getIndexer()
	 */
	public IPDOMIndexer getIndexer() {
		return fIndexer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.IPDOMIndexerTask#getProgressInformation()
	 */
	public IndexerProgress getProgressInformation() {
		IndexerProgress progress = new IndexerProgress();
		// TODO:  real progress information
		return progress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.IPDOMIndexerTask#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InterruptedException {
		IIndexLifecycleService service = fIndexServiceProvider.getIndexLifeCycleService();
		IProject project = fIndexer.getProject().getProject();
		String name = project.getName();
		service.update(new Scope(name), Arrays.asList((ICElement[])fAdded), Arrays.asList((ICElement[])fChanged), Arrays.asList((ICElement[])fRemoved), monitor);

	}

}
