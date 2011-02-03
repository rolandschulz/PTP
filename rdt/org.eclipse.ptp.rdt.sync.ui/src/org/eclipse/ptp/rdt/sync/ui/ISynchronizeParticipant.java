/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * Must be implemented by extensions to the syncronizeParticipant extension
 * point.
 * 
 */
public interface ISynchronizeParticipant {
	/**
	 * Create a control to configure a synchronize provider
	 * 
	 * @param parent
	 *            parent composite that contains the configuration area
	 */
	public void createConfigurationArea(Composite parent, IRunnableContext context);

	/**
	 * Get the configured sync service provider for the supplied project. Only
	 * valid if {@link isConfigComplete()} is true.
	 * 
	 * @param project
	 *            project that will be synchronized by this provider
	 * @return configured sync service provider
	 */
	public ISyncServiceProvider getProvider(IProject project);

	/**
	 * Check if the configuration is complete
	 * 
	 * @return true if the configuration is complete
	 */
	public boolean isConfigComplete();
}
