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
package org.eclipse.ptp.rm.ui.preferences;

/**
 * Common features useful for widget event listeners on PreferencePages.
 * <p>
 * At the moment, only enable/disable feature is provided. The listener must be disabled before updating contents
 * of input widgets. When content of SWT widgets is changed by code, some of the widgets do dispatch events as if
 * they were user events.
 * 
 * @author Daniel Felix Ferber
 */
public class PreferenceWidgetListener {
	/** State of the listener (enabled/disabled). */
	private boolean listenerEnabled = true;

	/** Enable the listener to handle events. */
	public synchronized void enable() { listenerEnabled = true; }
	/** Disable listener, received events shall be ignored. */
	public synchronized void disable() { listenerEnabled = false; }

	/** Test if the listener is enabled. */
	public synchronized boolean isEnabled() {
		return listenerEnabled;
	}
	
}
