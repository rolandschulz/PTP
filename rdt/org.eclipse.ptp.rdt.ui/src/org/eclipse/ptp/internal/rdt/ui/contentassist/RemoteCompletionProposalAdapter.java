/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.contentassist;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;

/**
 * Adapts <code>Proposal</code>s from a standalone content assist invocation
 * to proposal instances required by Eclipse's content assist framework.
 */
public class RemoteCompletionProposalAdapter extends AbstractCompletionProposalAdapter {

	@Override
	protected IContentAssistService getService(IProject project) {
		ServiceModelManager manager = ServiceModelManager.getInstance();
		if(!manager.isConfigured(project))
			return null;
		IServiceConfiguration config = manager.getActiveConfiguration(project);
		if (config == null) {
			return null;
		}
		IService service = manager.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider provider = config.getServiceProvider(service);
		
		if (!(provider instanceof IIndexServiceProvider2)) {
			return null;
		}
		
		return ((IIndexServiceProvider2) provider).getContentAssistService();
	}

}
