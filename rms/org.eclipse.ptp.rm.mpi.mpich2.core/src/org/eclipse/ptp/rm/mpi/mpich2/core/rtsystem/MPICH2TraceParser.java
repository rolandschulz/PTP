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
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2TraceParser {
	private MPICH2TraceParser() {
		// Do not allow instances.
	}

	private MPICH2HostMap map = new MPICH2HostMap();
	private static Pattern pattern = Pattern.compile("([^_]+)_([0-9])+ \\(([0-9.]+)\\)"); //$NON-NLS-1$

	public static MPICH2HostMap parse(BufferedReader reader) throws IOException {
		MPICH2TraceParser parser = new MPICH2TraceParser();
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();

			if (line.length()==0) {
				// Ignore empty line
				continue;
			}

			Matcher matcher = pattern.matcher(line);
			if (! matcher.matches()) {
				// Ignore the line
				parser.map.hasErrors = true;
				DebugUtil.error(DebugUtil.RTS_DISCOVER_TRACING, "Ignoring invalid line: '{0}'", line); //$NON-NLS-1$
				continue;
			}

			if (matcher.matches() && matcher.groupCount() == 3) {
				String host = matcher.group(1);
				String port = matcher.group(2);
				String addr = matcher.group(3);
				parser.map.addHost(host, addr, port);
				System.out.println("found host " + host + " addr " + addr + " port " + port); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "found host " + host + " addr " + addr + " port " + port); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		
		return parser.map;
	}
}

