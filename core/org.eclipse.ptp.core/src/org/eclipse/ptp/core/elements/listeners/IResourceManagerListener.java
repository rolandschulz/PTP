package org.eclipse.ptp.core.elements.listeners;

import org.eclipse.ptp.core.elements.events.IResourceManagerChangeEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent;

public interface IResourceManagerListener {
	/**
	 * Handle a resource manager attribute change event
	 * 
	 * @param e event
	 */
	public void handleEvent(IResourceManagerChangeEvent e);
	
	/**
	 * Handle general resource manager error
	 * 
	 * @param e event
	 */
	public void handleEvent(IResourceManagerErrorEvent e);
	
	/**
	 * Handle job submission error
	 * 
	 * @param e event
	 */
	public void handleEvent(IResourceManagerSubmitJobErrorEvent e);

}
