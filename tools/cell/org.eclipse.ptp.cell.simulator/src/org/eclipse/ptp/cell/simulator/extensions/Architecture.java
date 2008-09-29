/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator.extensions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.ptp.cell.simulator.tools.ContentRetrieverTool;


/**
 * Describes an architecture registered as a plugin.
 * It provided the TCL code configure the CPU in the simulator.
 * @author Daniel Felix Ferber
 */
public class Architecture {
	/** Unique ID to identify the architecture. */
	String id;
	/** Name displayed to the user. */
	String name;

	/** Name of the TCL on the host. */
	String tclScriptFileName;
	URL tclScriptURL;
//	String contributorName;
	
	public Architecture(String id, String name, String tclScriptFileName, URL tclScriptURL/*, String contributorName*/) {
		super();
		this.id = id;
		this.name = name;
		this.tclScriptFileName = tclScriptFileName;
		this.tclScriptURL = tclScriptURL;
//		this.contributorName= contributorName;
	}
	
	public String toString() {
		return id + "; " + name + "; " + tclScriptFileName + "; " + tclScriptURL + "; " /*+ contributorName*/; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getTclScriptFileName() {
		return tclScriptFileName;
	}
	
//	public String getContributorName() {
//		return contributorName;
//	}
	
	public String getTclScriptContent() throws IOException {
		InputStreamReader inputReader = new InputStreamReader(getTclScriptInputStream());
		return ContentRetrieverTool.readStreamContent(inputReader);
	}
	
	public InputStream getTclScriptInputStream() throws IOException {
		URLConnection connection = tclScriptURL.openConnection();
		return connection.getInputStream();
	}
	
	public void writeTclScriptTo(OutputStream outputStream) throws IOException {
		InputStream inputStream = getTclScriptInputStream();
		ContentRetrieverTool.copyStreamContent(outputStream, inputStream);
	}
}
