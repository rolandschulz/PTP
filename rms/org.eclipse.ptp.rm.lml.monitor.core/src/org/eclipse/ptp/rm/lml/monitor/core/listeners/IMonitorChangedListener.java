/**
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.rm.lml.monitor.core.listeners;

import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;

public interface IMonitorChangedListener {

	/**
	 * Notify the listener that the event has occurred
	 * 
	 * @param event
	 *            event
	 */
	public void monitorAdded(IMonitorControl[] monitors);

	/**
	 * Notify the listener that the event has occurred
	 * 
	 * @param event
	 *            event
	 */
	public void monitorRemoved(IMonitorControl[] monitors);

	/**
	 * Notify the listener that the event has occurred
	 * 
	 * @param event
	 *            event
	 */
	public void monitorUpdated(IMonitorControl[] monitors);

}
