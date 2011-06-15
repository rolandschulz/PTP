/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests;

import java.io.File;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A test suite that will establish a connection before running the tests.
 */

public class ConnectionSuite extends TestSuite {

	private File propertyFile;
	private File serviceModelFile;

	public ConnectionSuite() {
		
	}
	
	public ConnectionSuite(File propertiesFile, File serviceModelFile2) {
		this.propertyFile = propertiesFile;
		this.serviceModelFile = serviceModelFile2;
	}
	
	public void connect() throws Exception {
		ConnectionManager cm = ConnectionManager.getInstance();
		cm.initialize(propertyFile, serviceModelFile);
		cm.connect();
	}
		
	public void disconnect() throws Exception {
		ConnectionManager.getInstance().disconnect();
	}
	
	
	@Override
	public void run(TestResult result) {
		// connect to server
		try {
			connect();
		} catch(Exception e) {
			result.addError(this, e);
			result.stop();
			return;
		}
		
		// run all the tests
		super.run(result);
		
		// disconnect
		try {
			disconnect();
		} catch(Exception e) {
			result.addError(this, e);
		}
	}
	
}
