/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBRMPreferenceConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * For debugging stream parsers.
 * 
 * @author arossi
 * 
 */
public class DebuggingLogger {

	private static DebuggingLogger instance;

	public synchronized static DebuggingLogger getLogger() {
		if (instance == null) {
			initialize();
		}
		return instance;
	}

	public synchronized static void initialize() {
		instance = new DebuggingLogger();
	}

	private PrintWriter out;

	private final boolean fSegment;
	private final boolean fMatch;
	private final boolean fAction;
	private final boolean fProperties;
	private final boolean fShowCommand;
	private final boolean fShowCommandOutput;

	private DebuggingLogger() {
		fSegment = Preferences.getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN);
		fMatch = Preferences.getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.MATCH_STATUS);
		fAction = Preferences.getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.ACTIONS);
		fProperties = Preferences.getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.CREATED_PROPERTIES);
		fShowCommand = Preferences.getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SHOW_COMMAND);
		fShowCommandOutput = Preferences.getBoolean(JAXBControlCorePlugin.getUniqueIdentifier(),
				JAXBRMPreferenceConstants.SHOW_COMMAND_OUTPUT);
		String path = Preferences.getString(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.LOG_FILE);
		if (out != null) {
			out.flush();
			out.close();
		}
		if (path != null && !JAXBCoreConstants.ZEROSTR.equals(path)) {
			try {
				out = new PrintWriter(path);
			} catch (FileNotFoundException t) {
				JAXBControlCorePlugin.log(t);
				out = new PrintWriter(System.out);
			}
		} else {
			out = new PrintWriter(System.out);
		}
	}

	public boolean getActionInfo() {
		return fAction;
	}

	public boolean getCommand() {
		return fShowCommand;
	}

	public boolean getCommandOutput() {
		return fShowCommandOutput;
	}

	public boolean getMatch() {
		return fMatch;
	}

	public boolean getProperties() {
		return fProperties;
	}

	public boolean getSegment() {
		return fSegment;
	}

	public void logActionInfo(String message) {
		if (fAction) {
			out.println(message);
			out.flush();
		}
	}

	public void logCommand(String message) {
		if (fShowCommand) {
			out.println(message);
			out.flush();
		}
	}

	public void logCommandOutput(String message) {
		if (fShowCommandOutput) {
			out.println(message);
			out.flush();
		}
	}

	public void logMatchInfo(String message) {
		if (fMatch) {
			out.println(message);
			out.flush();
		}
	}

	public void logPropertyInfo(String message) {
		if (fProperties) {
			out.println(message);
			out.flush();
		}
	}

	public void logSegmentInfo(String message) {
		if (fSegment) {
			out.println(message);
			out.flush();
		}
	}
}
