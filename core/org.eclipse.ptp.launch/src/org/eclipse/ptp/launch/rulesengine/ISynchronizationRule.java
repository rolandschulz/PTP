/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.launch.rulesengine;

import org.eclipse.core.runtime.CoreException;

/**
 * Identifies a synchronization rule for the launch job.
 * 
 * @author Daniel Ferber
 * @since 5.0
 * 
 */

public interface ISynchronizationRule {
	/**
	 * Query if the rule is active and has all required information to be
	 * applied.
	 * 
	 * @return
	 */
	public boolean isActive();

	/**
	 * Validate the rule and raise a {@link CoreException} if some attribute is
	 * missing or not valid. The validation does not include check for existing
	 * files.
	 */
	public void validate() throws CoreException;

	/**
	 * Queries if the tule can be used by launch job after the application
	 * finishes execution on the remote host.
	 * 
	 * @author Daniel Ferber
	 * 
	 */
	public boolean isDownloadRule();

	/**
	 * Queries if the rule can be used by launch job before the application
	 * starts execution on the remote host.
	 * 
	 * @author Daniel Ferber
	 * 
	 */
	public boolean isUploadRule();
}
