/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.utils.packagemanager;

import java.util.List;

/**
 * @author laggarcia
 * @since 1.3.1
 */
public interface PackageManager {

	public static final String WHITESPACE = " "; //$NON-NLS-1$

	/**
	 * Check if a package is installed.
	 * @param pack
	 * @return
	 */
	public boolean query(String pack);

	/**
	 * Check if all packages in the list are installed.
	 * @param packs
	 * @return
	 */
	public boolean queryAll(String[] packs);

	/**
	 * Check if all packages in the list are installed.
	 * @param packs
	 * @return
	 */
	public boolean queryAll(String packs, String delimiter);
	
	/**
	 * Returns a list with the path of all the files in the package
	 * 
	 * @param pack
	 * @return
	 */
	public List queryList(String pack);

	/**
	 * Returns the initial part of the path of the result of the search for a file path with pathEnd in the specified package
	 * 
	 * @param pack The package to be searched
	 * @param pathEnd The last segments of the path being searched
	 * @return The first segments (subtract lastSegments from full path) of the path or null if the file doesn't exist in the package
	 */
	public String searchFileInQueryListAndReturnInitialPathSegments(String pack, String pathEnd);
	
	/**
	 * Returns the full path of the result of the search for a file path with pathEnd in the specified pakcage
	 * 
	 * @param pack The package to be searched
	 * @param pathEnd The last segments of the path being searched
	 * @return the full path of the file or null if the file doesn't exist
	 */
	public String searchFileInQueryListAndReturnFullPath(String pack, String pathEnd);
	
	//TODO: Other methods to install, remove, etc. Pay attention because probably you can't run two installs at the same time. Same is probably valid for two removes or one install and one remove. 

}
