/**********************************************************************
 * Copyright (c) 2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.fdt.internal.core.parser;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ParserMessages {
	private static final String BUNDLE_NAME = "org.eclipse.fdt.internal.core.parser.ParserMessages";//$NON-NLS-1$
	private static ResourceBundle resourceBundle;
	
	static {
		try {
			resourceBundle = ResourceBundle.getBundle( BUNDLE_NAME );
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}
	
	/**
	 * 
	 */
	private ParserMessages() {
	}
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		if( resourceBundle == null )
			return '#' + key +'#';
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object[] args) {
		String format = getString( key );
		return MessageFormat.format(format, args);
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument
	 * 
	 * @param key	the string used to get the bundle value, must not be null
	 */
	public static String getFormattedString(String key, Object arg) {
		String format = getString( key );
		
		if (arg == null)
			arg = ""; //$NON-NLS-1$
		
		return MessageFormat.format(format, new Object[] { arg });
	}
}