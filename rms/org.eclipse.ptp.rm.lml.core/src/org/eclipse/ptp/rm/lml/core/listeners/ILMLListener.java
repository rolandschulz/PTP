/**
 * Based on the class IResourceManagerListener 
 * 		 in package org.eclipse.ptp.core.listeners
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;

/**
 * This interface manages the handling of different events.
 * @author Claudia Knobloch
 */
public interface ILMLListener {
	
	/**
	 * Handles an IJobListSortedEevnt.
	 * @param e an IJobListSortedEvent
	 */
	public void handleEvent(IJobListSortedEvent e);
	

}
