/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rational Software - initial implementation
 *******************************************************************************/
package org.eclipse.fdt.core;

import org.eclipse.fdt.core.model.FortranCoreModel;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.model.CModelStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author hamer
 */
public class FortranConventions {

	private final static String ILLEGAL_FILE_CHARS = "/\\:<>?*|\""; //$NON-NLS-1$

	private static boolean isLegalFilename(String name) {
		if (name == null || name.length() == 0) {
			return false;
		}

		//TODO we need platform-independent validation, see bug#24152
		
		int len = name.length();
//		if (Character.isWhitespace(name.charAt(0)) || Character.isWhitespace(name.charAt(len - 1))) {
//			return false;
//		}
		for (int i = 0; i < len; i++) {
			char c = name.charAt(i);
			if (ILLEGAL_FILE_CHARS.indexOf(c) != -1) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * Validate the given file name.
	 * The name must be the short file name (including the extension).
	 * It should not contain any prefix or path delimiters.
	 *
	 * @param name the file name
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a C/C++ file name,
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged,
	 *      otherwise a status object indicating what is wrong with
	 *      the name
	 */
	public static IStatus validateFileName(String name) {
		//TODO could use a prefs option for file naming conventions
		if (name == null || name.length() == 0) {
			return new Status(IStatus.ERROR, FortranCorePlugin.PLUGIN_ID, -1, Util.bind("convention.filename.nullName"), null); //$NON-NLS-1$
		}
		if (!isLegalFilename(name)) {
			//TODO we need platform-independent validation, see bug#24152
			//return new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, Util.bind("convention.filename.invalid"), null); //$NON-NLS-1$
			return new Status(IStatus.WARNING, FortranCorePlugin.PLUGIN_ID, -1, Util.bind("convention.filename.possiblyInvalid"), null); //$NON-NLS-1$
		}

		String trimmed = name.trim();
		if ((!name.equals(trimmed)) || (name.indexOf(" ") != -1)) { //$NON-NLS-1$
			return new Status(IStatus.WARNING, FortranCorePlugin.PLUGIN_ID, -1, Util.bind("convention.filename.nameWithBlanks"), null); //$NON-NLS-1$
		}
		
		return CModelStatus.VERIFIED_OK;
	}
	
	/**
	 * Validate the given source file name.
	 * The name must be the short file name (including the extension).
	 * It should not contain any prefix or path delimiters.
	 *
	 * @param name the source file name
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a C/C++ source file name,
	 *      a status with code <code>IStatus.WARNING</code>
	 *		indicating why the given name is discouraged,
	 *      otherwise a status object indicating what is wrong with
	 *      the name
	 */
	public static IStatus validateSourceFileName(IProject project, String name) {
		//TODO could use a prefs option for source file naming conventions
	    IStatus val = validateFileName(name);
	    if (val.getSeverity() == IStatus.ERROR) {
	        return val;
	    }

	    if (!FortranCoreModel.isValidSourceUnitName(project, name)) {
			return new Status(IStatus.WARNING, FortranCorePlugin.PLUGIN_ID, -1, Util.bind("convention.sourceFilename.filetype"), null); //$NON-NLS-1$
	    }

		return val;
	}
}
