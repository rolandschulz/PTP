/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.File;

/**
 * Represents the folder on the user's machine where RDT
 * will store its files, such as pdom and settings files.
 * 
 * @author Mike Kucera
 */
public class UserFile {

	private static final String RDT_SETTINGS_DIRECTORY = ".rdt"; //$NON-NLS-1$
	
	/**
	 * Instantiate using public factory methods.
	 */
	private UserFile() {
	}
	
	
	/**
	 * Returns a File object that represents the given filename under
	 * the directory where settings and pdom files should be stored.
	 * Returns null if no settings directory can be found.
	 */
	public static File get(String fileName) {
		File settingsDir = getSettingsDirectory();
		if(settingsDir == null)
			return null;
		
		return new File(settingsDir.toString() + File.separator + fileName);
	}
	
	
	/**
	 * Returns the directory where the user's settings and pdom files will be
	 * stored. Attempts to create a .rdt directory in the user's home directory
	 * if possible, uses server directory otherwise.
	 */
	public static File getSettingsDirectory() {
		File userDir = getSettingsDirectoryInUserHome();
		if(userDir.exists())
			return userDir;
		if(userDir.mkdir()) // creates the directory if possible
			return userDir;
		
		File currentDir = getCurrentDirectory();
		if(currentDir.exists())
			return currentDir;
		
		return null;
	}
	
	
	/**
	 * Returns the .rdt folder in the user's home folder.
	 */
	private static File getSettingsDirectoryInUserHome() {
		String userHome = System.getProperties().getProperty("user.home"); //$NON-NLS-1$
		return new File(userHome + File.separator + RDT_SETTINGS_DIRECTORY);
	}
	
	
	/**
	 * Returns the directory where the server is running. 
	 */
	private static File getCurrentDirectory() {
		return new File(System.getProperties().getProperty("user.dir")); //$NON-NLS-1$
	}
	
}
