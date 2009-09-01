/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.services.core.ServiceProvider;

/**
 * A build service provider that does nothing.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author vkong
 *
 */
public class NullBuildServiceProvider extends ServiceProvider {
	
	public static final String ID = "org.eclipse.ptp.rdt.ui.NullBuildServiceProvider"; //$NON-NLS-1$
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
	public static final String NAME = Messages.getString("NullBuildServiceProvider.name"); //$NON-NLS-1$


	public String getConfigurationString() {
		return Messages.getString("NullServiceProvider.config"); //$NON-NLS-1$
	}


	public boolean isConfigured() {
		return true;
	}
}
