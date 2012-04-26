/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.lml.monitor.core;

import org.eclipse.ptp.rm.jaxb.control.ILaunchController;
import org.eclipse.ui.IMemento;

/**
 * @since 6.0
 */
public interface IMonitorControl extends ILaunchController {
	/**
	 * @return
	 */
	public String getMonitorId();

	/**
	 * @return
	 */
	public String getSystemType();

	/**
	 * @return
	 */
	public boolean isActive();

	/**
	 * Load persisted monitor information. Returns the state of the monitor when it was saved.
	 * 
	 * @param memento
	 * @return true if the monitor was active when saved
	 */
	public boolean load(IMemento memento);

	/**
	 * 
	 */
	public void refresh();

	/**
	 * Save monitor data to persisted store.
	 * 
	 * @param memento
	 */
	public void save(IMemento memento);

	/**
	 * @param type
	 */
	public void setSystemType(String type);
}
