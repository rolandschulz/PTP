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
 * Describes a launch profile registered as a plugin.
 * @author Daniel Felix Ferber
 */
public class LaunchProfile {
	/** Unique ID to identify the profile. */
	String id;
	/** Name displayed to the user. */
	String name;
	
	/** Name of the script when deployed to the working directory. */
	String tclScriptPath;
	/** Source where the content for the script can be read on the host. */
	URL tclScriptURL;
	
	/** Name of additional files when deployed to the working directory. */	
	String deployPaths[];
	/** Source where to read the content of additional files on the host. */
	URL deployURLs[];
	
	public LaunchProfile(String id, String name, String tclScriptPath, URL tclScriptURL, String[] deployPaths, URL[] deployURLs) {
		super();
		this.id = id;
		this.name = name;
		this.tclScriptPath = tclScriptPath;
		this.tclScriptURL = tclScriptURL;
		this.deployPaths = deployPaths;
		this.deployURLs = deployURLs;
	}
	
	public String toString() {
		return id + "; " + name + "; " + tclScriptPath + "; " + tclScriptURL + "; " + Integer.toString(deployPaths.length); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	public String[] getDeployPaths() {
		return deployPaths;
	}

	public void setDeployPaths(String[] deployPaths) {
		this.deployPaths = deployPaths;
	}

	public URL[] getDeployURLs() {
		return deployURLs;
	}

	public void setDeployURLs(URL[] deployURLs) {
		this.deployURLs = deployURLs;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTclScriptPath() {
		return tclScriptPath;
	}

	public void setTclScriptPath(String tclScriptPath) {
		this.tclScriptPath = tclScriptPath;
	}

	public URL getTclScriptURL() {
		return tclScriptURL;
	}

	public void setTclScriptURL(URL tclScriptURL) {
		this.tclScriptURL = tclScriptURL;
	}

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
	
	public String getDeployContent(int index) throws IOException {
		InputStreamReader inputReader = new InputStreamReader(getDeployInputStream(index));
		return ContentRetrieverTool.readStreamContent(inputReader);
	}
	
	public InputStream getDeployInputStream(int index) throws IOException {
		URLConnection connection = deployURLs[index].openConnection();
		return connection.getInputStream();
	}
	
	public void writeDeployContentTo(OutputStream outputStream, int index) throws IOException {
		InputStream inputStream = getDeployInputStream(index);
		ContentRetrieverTool.copyStreamContent(outputStream, inputStream);
	}
}
