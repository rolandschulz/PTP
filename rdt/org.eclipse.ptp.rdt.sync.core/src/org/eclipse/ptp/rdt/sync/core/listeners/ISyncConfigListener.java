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
 * Provides sync configuration callbacks.
 */
public interface ISyncConfigListener {

	public void configSelected(IProject project, SyncConfig newConfig, SyncConfig oldConfig);

	public void configAdded(IProject project, SyncConfig config);

	public void configRemoved(IProject project, SyncConfig config);

}
