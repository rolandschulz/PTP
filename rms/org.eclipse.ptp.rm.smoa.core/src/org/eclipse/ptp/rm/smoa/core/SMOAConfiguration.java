/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core;

import java.util.List;

import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.smoa.core.rmsystem.SMOAResourceManager;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOAConnection;

import com.smoa.comp.stubs.factory.ApplicationsType.Application;

/**
 * Holds values needed by {@link SMOAResourceManager} to operate - that is RM
 * configuration and connection options.
 * 
 * Is also informed by {@link SMOAResourceManager} about the available
 * applications.
 * 
 * Implemented by {@link SMOAServiceProvider}
 */
public interface SMOAConfiguration extends IRemoteResourceManagerConfiguration {

	/** Authentication mechanisms used to connect to SMOA Computing */
	enum AuthType {
		Anonymous, UsernamePassword, GSI
	}

	/** Returns application for given name */
	Application getAppForName(String name);

	/** Returns the method of authentication */
	AuthType getAuthType();

	/** List of predefined applications on the host */
	List<String> getAvailableAppList();

	/** Path to CA certificate */
	String getCaCertPath();

	/* If the authentication type needs no <insert name here>, result undefined. */

	/** For convenience, returns the associated connection */
	SMOAConnection getConnection();

	/** Returns the password. */
	String getPassword();

	/** port number for the SMOA computing */
	Integer getPort();

	/** Service designed name */
	String getServiceDN();

	/* Managing available applications */

	/** must return SMOA computing URL - host name */
	String getUrl();

	/** Returns the user name. */
	String getUser();

	/** Sets predefined application list */
	void setAvailableAppList(List<Application> apps);
}
