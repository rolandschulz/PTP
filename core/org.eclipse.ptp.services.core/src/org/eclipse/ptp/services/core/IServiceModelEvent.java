package org.eclipse.ptp.services.core;


public interface IServiceModelEvent {
	public static final int SERVICE_CONFIGURATION_ADDED = 1;
	public static final int SERVICE_CONFIGURATION_REMOVED = 2;
	public static final int SERVICE_MODEL_SAVED = 4;
	public static final int SERVICE_MODEL_LOADED = 8;

	/**
	 * Returns an object identifying the source of this event.
	 * 
	 * @return an object identifying the source of this event 
	 * @see java.util.EventObject
	 */
	public Object getSource();
	
	/**
	 * Returns the type of event being reported.
	 *
	 * @return one of the event type constants
	 * @see #SERVICE_CONFIGURATION_ADDED
	 * @see #SERVICE_CONFIGURATION_REMOVED
	 * @see #SERVICE_MODEL_SAVED
	 * @see #SERVICE_MODEL_LOADED
	 */
	public int getType();
}
