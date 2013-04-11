/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.util;

import java.net.URI;

import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
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
public class PathReplacer {
	/**
	 * Replaces the path portion of the given URI.
	 */
	public static URI replacePath(IProject project, URI u, String path) {
		// we may use a local service config with a non-standard EFS provider, so only do this
		// if we are using the default EFS provider
		if (isLocalServiceConfiguration(project) && u.getScheme().equals(EFS.SCHEME_FILE)) {
			return new EFSExtensionProvider(){}.createNewURIFromPath(u, path);
		}
		
		return EFSExtensionManager.getDefault().createNewURIFromPath(u, path);
	}
	
	private static boolean isLocalServiceConfiguration (IProject project) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		
		if(smm.isConfigured(project)) {
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
	
			if (serviceProvider instanceof IIndexServiceProvider) {
				return !((IIndexServiceProvider)serviceProvider).isRemote();
			}
		}
		return false;
	}
}
