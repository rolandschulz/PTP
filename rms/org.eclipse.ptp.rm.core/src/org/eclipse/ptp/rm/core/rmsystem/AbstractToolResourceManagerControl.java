/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core.rmsystem;

import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;

/**
 * @since 3.0
 */
public abstract class AbstractToolResourceManagerControl extends AbstractRuntimeResourceManagerControl {

	/**
	 * @since 3.0
	 */
	public AbstractToolResourceManagerControl(IResourceManagerConfiguration config) {
		super(config);
	}
}
