/**
 * Based on the class IResourceManagerListener 
 * 		 in package org.eclipse.ptp.core.listeners
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.core.listeners;

import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableFilterEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;

/**
 * This interface manages the handling of different events.
 */
public interface ILMLListener {

	public void handleEvent(ILguiAddedEvent event);

	public void handleEvent(ILguiRemovedEvent event);

	public void handleEvent(IMarkObjectEvent event);

	public void handleEvent(ISelectObjectEvent event);

	public void handleEvent(ITableFilterEvent event);

	/**
	 * Handles an IJobListSortedEevnt.
	 * 
	 * @param event
	 *            an IJobListSortedEvent
	 */
	public void handleEvent(ITableSortedEvent event);

	public void handleEvent(IUnmarkObjectEvent event);

	public void handleEvent(IUnselectedObjectEvent event);

	public void handleEvent(IViewUpdateEvent event);
}
