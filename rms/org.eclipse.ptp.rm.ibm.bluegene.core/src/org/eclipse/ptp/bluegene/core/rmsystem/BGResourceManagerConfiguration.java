/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
/**
 * 
 */
package org.eclipse.ptp.bluegene.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class BGResourceManagerConfiguration extends AbstractRemoteResourceManagerConfiguration {

	private static final String TAG_SERVICE_NODE = "bgServiceNode"; //$NON-NLS-1$
	private static final String TAG_DB_NAME = "bgDatabaseName"; //$NON-NLS-1$
	private static final String TAG_DB_USERNAME = "bgDatabaseUsername"; //$NON-NLS-1$
	private static final String TAG_DB_PASSWORD = "bgDatabasePassword"; //$NON-NLS-1$
	
	public static IResourceManagerConfiguration load(BGResourceManagerFactory factory,
			IMemento memento) {

		RemoteConfig remoteConfig = loadRemote(factory, memento);
		
		BGResourceManagerConfiguration config = 
			new BGResourceManagerConfiguration(factory, remoteConfig);

		config.setServiceNode(memento.getString(TAG_SERVICE_NODE));
		config.setDatabaseName(memento.getString(TAG_DB_NAME));
		config.setDatabasePassword(memento.getString(TAG_DB_USERNAME));
		config.setDatabasePassword(memento.getString(TAG_DB_PASSWORD));

		return config;
	}
	private String serviceNode = "";
	private String dbName = "";
	private String dbUsername = "";

	private String dbPassword = "";
	
	public BGResourceManagerConfiguration(BGResourceManagerFactory factory) {
		this(factory, new RemoteConfig());
		setDefaultNameAndDesc();
	}
	
	public BGResourceManagerConfiguration(BGResourceManagerFactory factory,
			RemoteConfig remoteConfig) {
		super(remoteConfig, factory);
	}
	

	public String getDatabaseName() {
		return dbName;
	}
	
	public String getDatabasePassword() {
		return dbPassword;
	}
	
	public String getDatabaseUsername() {
		return dbUsername;
	}
	
	public String getServiceNode() {
		return serviceNode;
	}
	
	public void setDatabaseName(String dbName) {
		this.dbName = dbName;
	}
	
	public void setDatabasePassword(String password) {
		this.dbPassword = password;
	}
	
	public void setDatabaseUsername(String username) {
		this.dbUsername = username;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = "Blue Gene";
		String conn = getConnectionName();
		if (conn != null && !conn.equals("")) {
			name += "@" + conn;
		}
		setName(name);
		setDescription("Blue Gene Resource Manager");
	}
	
	public void setServiceNode(String serviceNode) {
		this.serviceNode = serviceNode;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResourceManagerConfiguration#doSave(org.eclipse.ui.IMemento)
	 */
	protected void doSave(IMemento memento) {
		memento.putString(TAG_SERVICE_NODE, getServiceNode());
		memento.putString(TAG_DB_NAME, getDatabaseName());
		memento.putString(TAG_DB_USERNAME, getDatabaseUsername());
		memento.putString(TAG_DB_PASSWORD, getDatabasePassword());
		doSave(memento);
	}
}