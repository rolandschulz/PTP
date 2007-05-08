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

import org.eclipse.ui.IMemento;


public abstract class AbstractResourceManagerConfiguration implements IResourceManagerConfiguration {

	static public class CommonConfig {
		private final String name;
		private final String description;
		private final String uniqueName;

		public CommonConfig() {
			this("", "", generateUniqueName());
		}

		public CommonConfig(String name, String desc, String uniqueName) {
			this.name = name;
			this.description = desc;
			this.uniqueName = uniqueName;
		}
		
		public String getDescription() {
			return description;
		}

		public String getName() {
			return name;
		}

		public String getUniqueName() {
			return uniqueName;
		}		
	}
	
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$

	private static final String TAG_FACTORY_ID = "factoryId"; //$NON-NLS-1$
	
	/**
	 * @param factory
	 * @param memento
	 * @return
	 */
	public static CommonConfig loadCommon(IResourceManagerFactory factory,
			IMemento memento) {
		String factoryId = memento.getString(TAG_FACTORY_ID);
		if (!factoryId.equals(factory.getId())) {
			throw new IllegalStateException("Incompatable factory with factoryId"
					+ " stored id" + factoryId 
					+ ", factory id:" + factory.getId());
		}
		String name = memento.getString(TAG_NAME);
		String desc = memento.getString(TAG_DESCRIPTION);
		String uniqueName = memento.getString(TAG_UNIQUE_NAME);
		return new CommonConfig(name, desc, uniqueName);
	}

	protected static String generateUniqueName() {
		long time = System.currentTimeMillis();
		return "RMID:" + Long.toString(time);
	}
	private String description;
	private String name;
	private final String resourceManagerId;

	private final String uniqueName;

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
		this.uniqueName = commonConfig.getUniqueName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return resourceManagerId;
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
	public final void save(IMemento memento) {
		memento.putString(TAG_FACTORY_ID, getResourceManagerId());
		memento.putString(TAG_NAME, getName());
		memento.putString(TAG_DESCRIPTION, getDescription());
		memento.putString(TAG_UNIQUE_NAME, getUniqueName());
		doSave(memento);
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

	/**
	 * Save the rest of the config
	 * @param memento
	 */
	protected abstract void doSave(IMemento memento);
}