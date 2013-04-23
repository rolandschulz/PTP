/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.swt.widgets.Composite;

/**
 * Implemented by clients wishing to extend the synchronize property page.
 * 
 */
public interface ISynchronizeProperties extends ISynchronizePropertiesDescriptor {
	/**
	 * @param parent
	 *            parent composite that contains the configuration area
	 * @param project
	 *            project properties being displayed
	 * @param context
	 *            runnable context (can be null)
	 */
	public void createConfigurationArea(Composite parent, IProject project, IRunnableContext context);

	/**
	 * @param config
	 *            select the sync configuration currently displayed
	 */
	public void selectConfiguration(SyncConfig config);
}
