package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDILocator;

public interface IPDIStackFrameDescriptor {

	/**
	 * @return
	 */
	public int getLevel();

	/**
	 * @return
	 */
	public IPDILocator getLocator();
}