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
package org.eclipse.ptp.remotetools.environment.core;

/**
 * Interface definition for targets' event listeners.
 * Events are fired when a target state changes.
 * 
 * @author Ricardo M. Matinata, Richard Maciel
 * @since 1.1
 */
public interface ITargetEventListener {

	/**
	 * Notifies the listener that the event has occured.
	 * 
	 * @param event the event code as in org.eclipse.ptp.remotetools.environment.model.ITargetElementStatus
	 * @param from the originating target element
	 */
	void handleStateChangeEvent(int event, ITargetElement from);
	
}
