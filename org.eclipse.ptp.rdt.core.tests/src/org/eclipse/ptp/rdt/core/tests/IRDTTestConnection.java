/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public interface IRDTTestConnection {

	// manadatory properties
	public static final String PROPERTY_SYSTEMTYPEID = "connection.systemtypeid";
	public static final String PROPERTY_HOSTNAME     = "connection.hostname";
	public static final String PROPERTY_PORT         = "connection.port";
	public static final String PROPERTY_USERNAME     = "connection.username";
	public static final String PROPERTY_PASSWORD     = "connection.password";
	
	
	public void connect(Properties properties) throws ConnectException;
	
	public URI getURI(String path) throws URISyntaxException;
	
	public void disconnect() throws ConnectException;

	public boolean isConnected();
	
	public String getHostName();
	
}
