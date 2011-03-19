/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core.rmsystem;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerMonitor;

/**
 * @author Daniel Felix Ferber
 */
public abstract class AbstractToolResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * @since 3.0
	 */
	public AbstractToolResourceManager(AbstractResourceManagerConfiguration config, AbstractRuntimeResourceManagerControl control,
			AbstractRuntimeResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}
}
