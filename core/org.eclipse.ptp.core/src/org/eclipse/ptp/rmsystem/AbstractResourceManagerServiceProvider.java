package org.eclipse.ptp.rmsystem;

import java.util.UUID;

import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ui.IMemento;

public abstract class AbstractResourceManagerServiceProvider extends ServiceProvider implements IResourceManagerConfiguration
{
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$
	
	public AbstractResourceManagerServiceProvider() {
	}
	
	public AbstractResourceManagerServiceProvider(AbstractResourceManagerServiceProvider provider) {
		super(provider);
		setConnectionName(provider.getConnectionName());
		setDescription(provider.getDescription());
		setRemoteServicesId(provider.getRemoteServicesId());
		setResourceManagerId(provider.getResourceManagerId());
		setResourceManagerId(provider.getResourceManagerId());
		setName(provider.getName());
		setUniqueName(provider.getUniqueName());
	}

	@Override
	public abstract Object clone();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getConnectionName()
	 */
	public String getConnectionName() {
		return getString(TAG_CONNECTION_NAME, ""); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getDescription()
	 */
	public String getDescription() {
		return getString(TAG_DESCRIPTION, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.ServiceProvider#getName()
	 */
	@Override
	public String getName() {
		return getString(TAG_NAME, ""); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getRemoteServicesId()
	 */
	public String getRemoteServicesId() {
		return getString(TAG_REMOTE_SERVICES_ID, ""); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return super.getId();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getType()
	 */
	public String getType() {
		return super.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getUniqueName()
	 */
	public String getUniqueName() {
		String name = getString(TAG_UNIQUE_NAME, null);
		if (name == null) {
			name = UUID.randomUUID().toString();
			putString(TAG_UNIQUE_NAME, name);
		}
		return name;
	}
	
	public boolean isConfigured() {
		return !getConnectionName().equals("") && !getRemoteServicesId().equals(""); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#needsDebuggerLaunchHelp()
	 */
	public boolean needsDebuggerLaunchHelp() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		/*
		 *  Not needed (needs to be @deprecated). Currently used to 
		 *  bridge between RM configurations and service configurations.
		 */
		memento.putString(TAG_UNIQUE_NAME, getUniqueName());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setConnectionName(java.lang.String)
	 */
	public void setConnectionName(String name) {
		putString(TAG_CONNECTION_NAME, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		putString(TAG_DESCRIPTION, description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setName(java.lang.String)
	 */
	public void setName(String name) {
		putString(TAG_NAME, name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setRemoteServicesId(java.lang.String)
	 */
	public void setRemoteServicesId(String id) {
		putString(TAG_REMOTE_SERVICES_ID, id);
	}
	
	/**
	 * @param id
	 */
	public void setResourceManagerId(String id) {
		// Do nothing (needs to be @deprecated)
	}
	
	/**
	 * Set the IResourceManagerConfiguration unique name. This is only used to transition
	 * to the new service model framework. It is set to the name of the service configuration
	 * that was created for this service provider.
	 * 
	 * @param id
	 */
	public void setUniqueName(String id) {
		putString(TAG_UNIQUE_NAME, id);
	}
}
