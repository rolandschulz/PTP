/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.rm.core.utils.DebugUtil;

/**
 * 
 * @author Greg Watson
 * 
 */
public class MPICH2TraceParser {
	private String errorMessage = null;
	private final MPICH2HostMap map = new MPICH2HostMap();
	private static Pattern pattern = Pattern.compile("([^_]+)_([0-9])+ \\(([0-9.]+)\\)"); //$NON-NLS-1$

	/**
	 * Parse the output of the mpdtrace command.
	 * 
	 * @param reader
	 * @return an MPICH2HostMap containing the known hosts, or null if there was
	 *         an error
	 * @throws IOException
	 */
	public boolean parse(BufferedReader reader) {
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();

				if (line.length() == 0) {
					// Ignore empty line
					continue;
				}

				Matcher matcher = pattern.matcher(line);
				if (!matcher.matches()) {
					/*
					 * Probably an error from mpdtrace. Collect lines and mark
					 * error.
					 */
					errorMessage = line;

					while ((line = reader.readLine()) != null) {
						errorMessage += line + "\n"; //$NON-NLS-1$
					}

					return false;
				}

				if (matcher.matches() && matcher.groupCount() == 3) {
					String host = matcher.group(1);
					String port = matcher.group(2);
					String addr = matcher.group(3);
					map.addHost(host, addr, port);
					DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "found host " + host + " addr " + addr + " port " + port); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			return true;
		} catch (IOException e) {
			errorMessage = e.getLocalizedMessage();
			return false;
		}
	}

	/**
	 * @return error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	public MPICH2HostMap getHostMap() {
		return map;
	}
}
