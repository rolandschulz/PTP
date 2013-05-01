package org.eclipse.ptp.internal.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrameDescriptor;

public class StackFrameDescriptor implements IPDIStackFrameDescriptor {
	private int			level;
	private IPDILocator	loc;
	
	public StackFrameDescriptor(int level, IPDILocator loc) {
		this.level = level;
		this.loc = loc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIStackFrameDescriptor#getLevel()
	 */
	public int getLevel() {
		return this.level;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIStackFrameDescriptor#getLocator()
	 */
	public IPDILocator getLocator() {
		return loc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIStackFrameDescriptor#toString()
	 */
	public String toString() {
		return getLevel() + " " + loc.toString();	  //$NON-NLS-1$
	}
}
