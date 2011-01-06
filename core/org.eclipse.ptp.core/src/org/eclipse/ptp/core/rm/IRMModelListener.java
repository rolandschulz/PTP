/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core.rm;

/**
 * @since 5.0
 */
public interface IRMModelListener {
	/**
	 * Notification when a resource manager is added to the model
	 * 
	 * @param rm
	 *            resource manager that was added to the model
	 */
	public void handleResourceManagerAdded(IResourceManager rm);

	/**
	 * Notification when a resource manager is changed
	 * 
	 * @param rm
	 *            resource manager that was changed
	 */
	public void handleResourceManagerChanged(IResourceManager rm);

	/**
	 * Notification when a resource manager is removed from the model
	 * 
	 * @param rm
	 *            resource manager that was removed from the model
	 */
	public void handleResourceManagerRemoved(IResourceManager rm);
}
