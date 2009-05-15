/*******************************************************************************
 * Copyright (c) 2007 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ui.IMemento;


public abstract class AbstractResourceManagerConfiguration implements IResourceManagerConfiguration {

	static public class CommonConfig {
		private static final String EMPTY_STRING = ""; //$NON-NLS-1$
		
		private final String name;
		private final String description;
		private final String uniqueName;
		private final String connectionName;
		private final String remoteServicesID;

		public CommonConfig() {
			this(EMPTY_STRING, EMPTY_STRING, generateUniqueName(), EMPTY_STRING, EMPTY_STRING);
		}

		public CommonConfig(String name, String desc, String uniqueName, String remoteServicesID, String connectionName) {
			this.name = name;
			this.description = desc;
			this.uniqueName = uniqueName;
			this.remoteServicesID = remoteServicesID;
			this.connectionName = connectionName;
		}
		
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the uniqueName
		 */
		public String getUniqueName() {
			return uniqueName;
		}

		/**
		 * @return the connectionName
		 */
		private String getConnectionName() {
			return connectionName;
		}

		/**
		 * @return the remoteServicesID
		 */
		private String getRemoteServicesID() {
			return remoteServicesID;
		}		
	}

	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$
	private static final String TAG_FACTORY_ID = "factoryId"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$
	
	/**
	 * @param factory
	 * @param memento
	 * @return
	 */
	public static CommonConfig loadCommon(IResourceManagerFactory factory,
			IMemento memento) {
		String factoryId = memento.getString(TAG_FACTORY_ID);
		if (!factoryId.equals(factory.getId())) {
			throw new IllegalStateException(Messages.AbstractResourceManagerConfiguration_0
					+ Messages.AbstractResourceManagerConfiguration_1 + factoryId 
					+ Messages.AbstractResourceManagerConfiguration_2 + factory.getId());
		}
		String name = memento.getString(TAG_NAME);
		String desc = memento.getString(TAG_DESCRIPTION);
		String uniqueName = memento.getString(TAG_UNIQUE_NAME);
		String remoteServicesID = memento.getString(TAG_REMOTE_SERVICES_ID);
		String connectionName = memento.getString(TAG_CONNECTION_NAME);
		return new CommonConfig(name, desc, uniqueName, remoteServicesID, connectionName);
	}
	
	protected static String generateUniqueName() {
		long time = System.currentTimeMillis();
		return "RMID:" + Long.toString(time); //$NON-NLS-1$
	}
	
	private String description;
	private String name;
	private String connectionName;
	private String remoteServicesID;
	
	private final String resourceManagerId;
	private final String resourceManagerType;
	private final String uniqueName;
	
	private final IResourceManagerFactory factory;
	
	/**
	 * @param name
	 * @param description
	 * @param factory
	 */
	public AbstractResourceManagerConfiguration(CommonConfig commonConfig,
			IResourceManagerFactory factory) {
		this.name = commonConfig.getName();
		this.description = commonConfig.getDescription();
		this.resourceManagerId = factory.getId();
		this.resourceManagerType = factory.getName();
		this.uniqueName = commonConfig.getUniqueName();
		this.connectionName = commonConfig.getConnectionName();
		this.remoteServicesID = commonConfig.getRemoteServicesID();
		this.factory = factory;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getConnectionName()
	 */
	public String getConnectionName() {
		return connectionName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Get the factory for this configuration
	 * 
	 * @return factory
	 */
	public IResourceManagerFactory getFactory() {
		return factory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getRemoteServicesId()
	 */
	public String getRemoteServicesId() {
		return remoteServicesID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return resourceManagerId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getType()
	 */
	public String getType() {
		return resourceManagerType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getUniqueName()
	 */
	public String getUniqueName() {
		return uniqueName;
	}
	
	/**
	 * Save the common parts of this config then save the rest
	 * @param memento
	 */
	public void save(IMemento memento) {
		memento.putString(TAG_FACTORY_ID, getResourceManagerId());
		memento.putString(TAG_NAME, getName());
		memento.putString(TAG_DESCRIPTION, getDescription());
		memento.putString(TAG_UNIQUE_NAME, getUniqueName());
		memento.putString(TAG_REMOTE_SERVICES_ID, getRemoteServicesId());
		memento.putString(TAG_CONNECTION_NAME, getConnectionName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setConnectionName(java.lang.String)
	 */
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setRemoteServicesId(java.lang.String)
	 */
	public void setRemoteServicesId(String id) {
		this.remoteServicesID = id;
	}
}