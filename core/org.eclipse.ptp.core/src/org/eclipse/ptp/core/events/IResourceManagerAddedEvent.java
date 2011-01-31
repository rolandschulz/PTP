/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.core.events;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * @since 5.0
 */
public interface IResourceManagerAddedEvent {
	/**
	 * @return
	 */
	public IResourceManagerControl getResourceManager();

	/**
	 * @return
	 */
	public IModelManager getSource();
}
