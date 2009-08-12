package org.eclipse.ptp.services.core;


public interface IServiceModelEvent {
	/**
	 * Event type used to indicate that a new service configuration has
	 * been added to the model. For this event type, {@link #getSource()}
	 * will return the new service configuration.
	 */
	public static final int SERVICE_CONFIGURATION_ADDED = 		0x001;

	/**
	 * Event type used to indicate that a new service configuration has
	 * been removed from the model. For this event type, {@link #getSource()}
	 * will return the old service configuration.
	 */
	public static final int SERVICE_CONFIGURATION_REMOVED = 	0x002;

	/**
	 * Event type used to indicate that a service configuration has
	 * been modified. For this event type, {@link #getSource()}
	 * will return the modified service configuration.
	 */
	public static final int SERVICE_CONFIGURATION_CHANGED = 	0x004;
	
	/**
	 * Event type used to indicate that a service configuration has
	 * been selected as the default. For this event type, {@link #getSource()}
	 * will return the service configuration, or null if all service
	 * configurations are unselected.
	 */
	public static final int SERVICE_CONFIGURATION_SELECTED = 	0x008;
	
	/**
	 * Event type used to indicate that the service model has been successfully
	 * saved to persistent storage. For this event type, {@link #getSource()}
	 * will return an instance of IServiceModelManager.
	 */
	public static final int SERVICE_MODEL_SAVED = 				0x010;
	
	/**
	 * Event type used to indicate that the service model has been successfully 
	 * loaded from persistent storage. For this event type, {@link #getSource()}
	 * will return an instance of IServiceModelManager.
	 */
	public static final int SERVICE_MODEL_LOADED = 				0x020;

	/**
	 * Event type that can be used to indicate that notification of all
	 * types of events is requested.
	 */
	public static final int ALL_EVENTS = 						0x03f;

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
	 * @see #SERVICE_CONFIGURATION_CHANGED
	 * @see #SERVICE_CONFIGURATION_SELECTED
	 * @see #SERVICE_MODEL_SAVED
	 * @see #SERVICE_MODEL_LOADED
	 */
	public int getType();
}
