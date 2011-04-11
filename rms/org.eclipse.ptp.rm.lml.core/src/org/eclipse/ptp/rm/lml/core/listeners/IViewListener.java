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
import org.eclipse.ptp.rm.lml.core.events.ILguiSelectedEvent;

/**
 * This interface manages the handling of different events.
 * @author Claudia Knobloch
 */
public interface IViewListener extends IListener{

	/**
	 * Handles an ILguiAddedEvent.
	 * @param e an ILguiAddedEvent
	 */
	public void handleEvent(ILguiAddedEvent e);
	
	/**
	 * Handles an IlguiRemovedEvent.
	 * @param e an ILguiRemovedEvent
	 */
	public void handleEvent(ILguiRemovedEvent e);
	

	/**
	 * Handles an ILguiSelectedEvent.
	 * @param e an ILguiSelectedEvent 
	 */
	public void handleEvent(ILguiSelectedEvent e);
}
