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
package org.eclipse.ptp.services.core;

/**
 * A service model event listener is notified of events relating to the service model.
 */
public interface IServiceModelEventListener {

	/**
	 * Notifies this listener that some service model event has occurred.
	 * 
	 * @param event the service model event
	 */
	public void handleEvent(IServiceModelEvent event);
}
