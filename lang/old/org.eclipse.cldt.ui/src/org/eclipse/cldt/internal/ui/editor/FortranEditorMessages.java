/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.ui.editor;


import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class FortranEditorMessages  {
	private static final String RESOURCE_BUNDLE = "org.eclipse.cldt.internal.ui.editor.FortranEditorMessages"; //$NON-NLS-1$


	private static ResourceBundle fgResourceBundle;
	static {
		try {
			fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		} catch (MissingResourceException x) {
			fgResourceBundle = null;
		}
	}


	private FortranEditorMessages() {
	}


	public static ResourceBundle getResourceBundle() {
		return fgResourceBundle;
	}
	
	public static String getString( String key ) {
		try {
			return fgResourceBundle.getString( key );
		} catch(MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Gets a string from the resource bundle and formats it with arguments
	 */	
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}
	
	/**
	 * Gets a string from the resource bundle and formats it with arguments
	 */	
	public static String getFormattedString(String key, Object arg) {
		return MessageFormat.format(getString(key), new Object[] { arg } );
	}	

}


