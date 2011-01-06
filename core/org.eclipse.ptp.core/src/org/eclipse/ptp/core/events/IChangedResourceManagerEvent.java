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

import java.util.Collection;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.elements.IPResourceManager;

/**
 * This event is generated when the attributes or configuration of one or more
 * resource managers have changed. It is a bulk event that is sent to child
 * listeners on the model manager.
 * 
 * @see org.eclipse.ptp.core.elements.listeners.IModelManagerChildListener
 */
public interface IChangedResourceManagerEvent {
	/**
	 * Get the resource managers that have changed
	 * 
	 * @return resource managers that have changed
	 */
	public Collection<IPResourceManager> getResourceManagers();

	/**
	 * Get the source of the event
	 * 
	 * @return source of the event
	 */
	public IModelManager getSource();

}
