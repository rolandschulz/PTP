/**
 * Based on the class IResourceManagerListener 
 * 		 in package org.eclipse.ptp.core.listeners
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;

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
	
	public void handleEvent(ITableColumnChangeEvent e);

	public void handleEvent(ISelectedObjectChangeEvent event);

	public void handleEvent(IMarkObjectEvent event);

	public void handleEvent(IUnmarkObjectEvent event);

	public void handleEvent(IUnselectedObjectEvent event);
	

}
