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
	 * Returns the current message for this participant.
	 * 
	 * @return the message, or <code>null</code> if none
	 */
	public String getMessage();

	/**
	 * Returns the current message type for this participant.
	 *
	 * @return message type
	 */
	public int getMessageType();

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
	
	/**
	 * Set project name so that the participant can use it in the UI
	 * 
	 * @param projectName
	 */
	public void setProjectName(String projectName);

	// Juno SR2 - allow participant to be read and written so that it can be used on property pages.
	// Eight additional functions added to get/set four common properties for all remotes.
	// Note: Currently we are assuming no tool-specific data exists that needs to be get or set.

	/**
	 * Get the remote sync location entered by the user
	 * @return remote location
	 */
	public String getLocation();

	/**
	 * Get the tool binary location entered by the user
	 * @return tool location
	 */
	public String getToolLocation();

	/**
	 * Get the connection selected by the user
	 * @return connection
	 */
	public IRemoteConnection getRemoteConnection();

	/**
	 * Get the remote provider
	 * @return remote provider
	 */
	public IRemoteServices getRemoteProvider();

	/**
	 * Set the remote sync location
	 * @param remote location
	 */
	public void setLocation(String location);

	/**
	 * Set the tool binary location
	 * @param tool location
	 */
	public void setToolLocation(String location);

	/**
	 * Set the remote connection
	 * @param connection
	 */
	public void setRemoteConnection(IRemoteConnection connection);

	/**
	 * Set the remote provider
	 * @param provider
	 */
	public void setRemoteProvider(IRemoteServices provider);
}