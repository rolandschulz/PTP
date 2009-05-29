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

import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.indexer.AbstractPDOMIndexer;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.IServiceModelManager;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;

/**
 * @author crecoskie
 *
 */
public class RemoteFastIndexer extends AbstractPDOMIndexer {
	
	public static final String ID = "org.eclipse.ptp.rdt.core.RemoteFastIndexer"; //$NON-NLS-1$
	
	public IPDOMIndexerTask createTask(ITranslationUnit[] added, ITranslationUnit[] changed, ITranslationUnit[] removed) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(getProject().getProject());
		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
		
		if(serviceProvider instanceof IIndexServiceProvider)
			return new RemoteIndexerTask(this, (IIndexServiceProvider) serviceProvider, added, changed, removed);
		
		return null;
	}

	
	public String getID() {
		return ID;
	}
	
}
