/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui;

/**
 * Exports tab update call.
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateHandler
 * @see org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab#fireContentsChanged()
 * @author arossi
 * @since 1.1
 * 
 */
public interface IUpdateModelEnabled {
	/**
	 * Used to delegate to the internal contents changed method of the tab.
	 */
	public void fireContentsChanged();

	/**
	 * Relinks hidden variables with link-to settings.
	 */
	public void relink();
}
