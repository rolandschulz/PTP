package org.eclipse.ptp.rmsystem;


/**
 * Resource manager status
 * </p>
 */
public class ResourceManagerStatus {

	public static final ResourceManagerStatus EVENTS_DISABLED = new ResourceManagerStatus(Messages.getString("ResourceManagerStatus.EVENTS_DISABLED")); //$NON-NLS-1$

	public static final ResourceManagerStatus EVENTS_ENABLED = new ResourceManagerStatus(Messages.getString("ResourceManagerStatus.EVENTS_ENABLED")); //$NON-NLS-1$

	public static final ResourceManagerStatus ERROR = new ResourceManagerStatus(Messages.getString("ResourceManagerStatus.ERROR")); //$NON-NLS-1$

	public static final ResourceManagerStatus STARTED = new ResourceManagerStatus(Messages.getString("ResourceManagerStatus.STARTED")); //$NON-NLS-1$

	public static final ResourceManagerStatus STOPPED = new ResourceManagerStatus(Messages.getString("ResourceManagerStatus.STOPPED")); //$NON-NLS-1$
	
	public static final ResourceManagerStatus INIT = new ResourceManagerStatus(Messages.getString("ResourceManagerStatus.INIT")); //$NON-NLS-1$
	
	private final String state;

	private ResourceManagerStatus(String state) {
		this.state = state;
	}

	public String toString() {
		return state;
	}
	
}
