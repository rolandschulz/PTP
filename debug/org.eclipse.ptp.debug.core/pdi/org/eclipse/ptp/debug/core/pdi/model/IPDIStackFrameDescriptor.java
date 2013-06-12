package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDILocator;

/**
 * Stack frame descriptor
 * 
 */
public interface IPDIStackFrameDescriptor {

	/**
	 * Get level
	 * 
	 * @return
	 */
	public int getLevel();

	/**
	 * Get locator
	 * 
	 * @return
	 */
	public IPDILocator getLocator();
}