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
 * 
 * Interface definition of event providers for Target Controls. ICellTargetControl
 * instances can optionally provide an implementation of this interface thru the
 * IAdaptable mechanism.
 * 
 * @author Ricardo M. Matinata
 * @since 1.2
 */
public interface ITargetControlEventProvider {

	/**
	 * Registers a listener to the event provider, keyed by a control instance.
	 * 
	 * @param control the control generating events
	 * @param listener the interested listener
	 */
	public void registerControlAndListener(ITargetControl control, ITargetControlEventListener listener);
	
	/**
	 * Unregisters a control,listener tuple from this event provider
	 * 
	 * @param control the control generating events
	 * @param listener the interested listener
	 */
	public void unregisterControlAndListener(ITargetControl control, ITargetControlEventListener listener);
	
}
