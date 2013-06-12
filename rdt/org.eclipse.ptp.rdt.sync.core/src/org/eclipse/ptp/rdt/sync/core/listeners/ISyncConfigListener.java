/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.listeners;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;

/**
 * Provides sync configuration callbacks. This allows clients to be notified of changes to sync configurations.
 * 
 * @since 3.0
 */
public interface ISyncConfigListener {

	/**
	 * Notify listeners that the sync configuration selection has been changed changed from oldConfig to newConfig
	 * 
	 * @param project
	 *            synchronized project
	 * @param newConfig
	 *            new configuration
	 * @param oldConfig
	 *            old configuration
	 */
	public void configSelected(IProject project, SyncConfig newConfig, SyncConfig oldConfig);

	/**
	 * Notify listeners that a new sync configuration has been added.
	 * 
	 * @param project
	 *            synchronized project
	 * @param config
	 *            config that was added
	 */
	public void configAdded(IProject project, SyncConfig config);

	/**
	 * Notify listeners that a sync configuration has been removed.
	 * 
	 * @param project
	 *            synchronized project
	 * @param config
	 *            config that was removed
	 */
	public void configRemoved(IProject project, SyncConfig config);

}
