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
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
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
	 * Returns the current error message for this participant.
	 * 
	 * @return the error message, or <code>null</code> if none
	 */
	public String getErrorMessage();

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
	
	// Setter methods to allow parent UI to set values programmatically and even disallow editing depending on the situation.
	// (For example, if the project already exists and is being converted to a sync project, this information is already known.)
	
	/**
	 * Set remote location
	 *
	 * @param location
	 */
	public void setRemoteLocation(String location);
	
	/**
	 * Disable editing of remote location
	 *
	 * @param isEditable
	 */
	public void setRemoteLocationEnabled(boolean isEditable);
	
	/**
	 * Set remote connection
	 *
	 * @param connection
	 */
	public void setRemoteConnection(IRemoteConnection connection);
	
	/**
	 * Disable editing of remote connection
	 *
	 * @param isEditable
	 */
	public void setRemoteConnectionEnabled(boolean isEditable);

	/**
	 * Set remote services 
	 *
	 * @param services
	 */
	public void setRemoteServices(IRemoteServices services);
	
	/**
	 * Disable editing of remote services
	 *
	 * @param isEditable
	 */
	public void setRemoteServicesEnabled(boolean isEditable);
}
