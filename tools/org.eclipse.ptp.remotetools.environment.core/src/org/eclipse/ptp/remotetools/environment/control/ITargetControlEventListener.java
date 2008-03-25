/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.control;

/**
 * Interface for target control event listeners. 
 * 
 * @author Ricardo M. Matinata
 * @since 1.2
 */
public interface ITargetControlEventListener {

	/**
	 * Notifies the listener that the state change event has occurred.
	 * 
	 * @param event the event code as in {@link ITargetStatus}
	 * @param from the originating control
	 */
	public void handleStateChangeEvent(int event, ITargetControl from);
	
}
