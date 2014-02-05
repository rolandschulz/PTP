/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

/**
 * Utility class for inserting/extracting data from the
 * RemmoteSettings class.
 * 
 * @see RemoteSettings
 * @author Steven R. Brandt
 *
 */
public class HashSettingsStore implements ISettingsStore {

	Map<String, String> settings = new HashMap<String, String>();

	public String get(String key) {
		return settings.get(key);
	}

	public String get(String key, String defaultValue) {
		String val = settings.get(key);
		if (val == null)
			return defaultValue;
		else
			return val;
	}

	public void put(String key, String value) {
		settings.put(key, value);
	}

	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.print("HashSettings:"); //$NON-NLS-1$
		for (Map.Entry<String, String> e : settings.entrySet()) {
			pw.print(e.getKey() + "=" + e.getValue() + ";"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		pw.close();
		return sw.toString();
	}
}
