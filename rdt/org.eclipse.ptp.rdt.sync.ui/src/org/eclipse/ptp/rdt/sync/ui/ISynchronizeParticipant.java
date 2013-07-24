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

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.swt.widgets.Composite;

/**
 * Must be implemented by extensions to the synchronizeParticipant extension
 * point.
 */
public interface ISynchronizeParticipant extends ISynchronizeParticipantDescriptor {
	/**
	 * Create a control to configure a remote location
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
	 * Get connection to a remote host
	 * @return connection
	 */
	public IRemoteConnection getConnection();

	/**
	 * Get directory on remote host
	 * @return directory
	 */
	public String getLocation();

	/**
	 * Get the name of the sync config to create for this participant. Only
	 * valid if {@link isConfigComplete()} is true.
	 *
	 * @return sync config name
	 */
	public String getSyncConfigName();

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
}
