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
public interface IRMModelChangeListener {
	/**
	 * Notification when a resource manager is added to the model
	 * 
	 * @param event
	 *            the event details
	 */
	public void added(IRMModelChangeEvent event);

	/**
	 * Notification when a resource manager is changed
	 * 
	 * @param event
	 *            the event details
	 */
	public void changed(IRMModelChangeEvent event);

	/**
	 * Notification when a resource manager is removed from the model
	 * 
	 * @param event
	 *            the event details
	 */
	public void removed(IRMModelChangeEvent event);
}
