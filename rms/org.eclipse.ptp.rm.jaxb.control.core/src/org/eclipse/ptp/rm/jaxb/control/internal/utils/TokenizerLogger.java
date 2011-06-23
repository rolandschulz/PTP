/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMPreferenceConstants;

/**
 * For debugging stream parsers.
 * 
 * @author arossi
 * 
 */
public class TokenizerLogger {

	private static TokenizerLogger instance;

	public synchronized static TokenizerLogger getLogger() {
		if (instance == null) {
			initialize();
		}
		return instance;
	}

	public synchronized static void initialize() {
		instance = new TokenizerLogger();
	}

	private PrintWriter out;

	private final boolean segment;
	private final boolean match;
	private final boolean action;
	private final boolean properties;

	private TokenizerLogger() {
		segment = Preferences.getBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN);
		match = Preferences.getBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.MATCH_STATUS);
		action = Preferences.getBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.ACTIONS);
		properties = Preferences.getBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES);
		String path = Preferences.getString(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.LOG_FILE);
		if (out != null) {
			out.flush();
			out.close();
		}
		if (path != null && !JAXBCoreConstants.ZEROSTR.equals(path)) {
			try {
				out = new PrintWriter(path);
			} catch (FileNotFoundException t) {
				JAXBCorePlugin.log(t);
				out = new PrintWriter(System.out);
			}
		} else {
			out = new PrintWriter(System.out);
		}
	}

	public void logActionInfo(String message) {
		if (action) {
			out.println(message);
			out.flush();
		}
	}

	public void logMatchInfo(String message) {
		if (match) {
			out.println(message);
			out.flush();
		}
	}

	public void logPropertyInfo(String message) {
		if (properties) {
			out.println(message);
			out.flush();
		}
	}

	public void logSegmentInfo(String message) {
		if (segment) {
			out.println(message);
			out.flush();
		}
	}
}
