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
package org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIHostMap.Host;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class OpenMPIHostMapParser {
	private OpenMPIHostMapParser() {
		// Do not allow instances.
	}

	OpenMPIHostMap map = new OpenMPIHostMap();
	// boolean hasErrors = false;

	static Pattern pattern = Pattern.compile("\\s*(\\S+)((?:\\s+\\S+=\\d+)*)\\s*"); //$NON-NLS-1$
	static Pattern paramPattern = Pattern.compile("\\s*(\\S+)=(\\d+)"); //$NON-NLS-1$

	// static Pattern hostnamePattern = Pattern.compile("\\s*(\\S+)");
	// static Pattern slotsPattern =
	// Pattern.compile("\\s+(?:slots|cpus|count)\\s*=\\s*(\\d+)",
	// Pattern.CASE_INSENSITIVE);
	// static Pattern maxSlotsPattern =
	// Pattern.compile("\\s+max-slots\\s*=\\s*(\\d+)",
	// Pattern.CASE_INSENSITIVE);
	/*
	 * Some very advanced regex. Maches a line that does NOT contain a valid
	 * parameter. In other words, matches a line with any invalid parameter. ^ -
	 * match beginning of line (! ) - match string that does not satisfy the
	 * enclosed regex (.*\\s(?:count|cpus|slots)\\s*= - matches the words
	 * count,cpus or slots starting anywhere, with a space before, and followed
	 * by equal with optional interleaving space.
	 */
	// static Pattern othersPattern =
	// Pattern.compile("^(?!.*\\s(?:count|cpus|slots|max-slots)\\s*=)",
	// Pattern.CASE_INSENSITIVE);

	public static OpenMPIHostMap parse(BufferedReader reader) throws IOException {
		OpenMPIHostMapParser parser = new OpenMPIHostMapParser();
		String line;
		while ((line = reader.readLine()) != null) {
			/*
			 * Remove comments from line.
			 */
			int index = line.indexOf('#');
			if (index != -1) {
				line = line.substring(0, index);
			}
			line = line.trim();

			if (line.length() == 0) {
				// Ignore empty line
				continue;
			}

			Matcher matcher = pattern.matcher(line);
			if (!matcher.matches()) {
				// Ignore the line
				parser.map.setHasParseErrors(true);
				DebugUtil.error(DebugUtil.RTS_DISCOVER_TRACING, "Ignoring invalid line: '{0}'", line); //$NON-NLS-1$
				continue;
			}

			/*
			 * Add host. According to MacOS X man page for mpirun: - If the
			 * number of slots and max-slots are omitted, then openmpi
			 * understands that the host accepts only one process and that
			 * oversubscribing is allowed. This is assumed as default. - If
			 * number of slots is omitted, but not max-slots, the it is assumed
			 * that slots=max-slots. - If both number of slots and max-slots are
			 * give, then their values are kept, as long as slots<=max-slots.
			 */
			OpenMPIHostMap.Host host = new OpenMPIHostMap.Host(matcher.group(1));
			parser.map.addHost(host);
			host.setNumProcessors(0);
			host.setMaxNumProcessors(0);

			String parameters = matcher.group(2);
			matcher = paramPattern.matcher(parameters);
			/*
			 * Try to get slots and max-slots information.
			 */
			while (matcher.find()) {
				// for (int i = 2; i <= matcher.groupCount(); i+=2) {
				String key = matcher.group(1);
				String value = matcher.group(2);

				if (key.equalsIgnoreCase("slots") || key.equalsIgnoreCase("cpus") || key.equalsIgnoreCase("count")) { //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
					/*
					 * On failure to parse, assume 0 processors (no info
					 * available).
					 */
					try {
						host.setNumProcessors(Integer.parseInt(value));
						if (host.getNumProcessors() <= 1) {
							host.setNumProcessors(0);
						}
					} catch (NumberFormatException e) {
						host.addErrors(OpenMPIHostMap.Host.ERR_NUM_SLOTS);
						host.setNumProcessors(1);
					}
				} else if (key.equalsIgnoreCase("max-slots")) { //$NON-NLS-1$
					try {
						/*
						 * On failure to parse, assume 0 (no information
						 * available).
						 */
						host.setMaxNumProcessors(Integer.parseInt(value));
						if (host.getMaxNumProcessors() <= 1) {
							host.setMaxNumProcessors(0);
						}
					} catch (NumberFormatException e) {
						host.addErrors(Host.ERR_MAX_NUM_SLOTS);
						host.setMaxNumProcessors(0);
					}
				} else {
					DebugUtil.error(DebugUtil.RTS_DISCOVER_TRACING, "Invalid attribute: '{0}'", matcher.group()); //$NON-NLS-1$
					host.addErrors(OpenMPIHostMap.Host.ERR_UNKNOWN_ATTR);
				}
			}
			/*
			 * Validate numbers.
			 */
			if (host.getNumProcessors() == 0) {
				if (host.getMaxNumProcessors() == 0) {
					host.setNumProcessors(1);
				} else {
					host.setNumProcessors(host.getMaxNumProcessors());
				}
			} else {
				if (host.getMaxNumProcessors() > 0) {
					if (host.getNumProcessors() > host.getMaxNumProcessors()) {
						host.addErrors(Host.ERR_MAX_NUM_SLOTS);
						host.setNumProcessors(host.getMaxNumProcessors());
					}
				}
			}
			DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING,
					"  {0} slots={1} max-slots={2}", host.getName(), host.getNumProcessors(), host.getMaxNumProcessors()); //$NON-NLS-1$
		}

		return parser.map;
	}

	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("\\s*(\\S+)(?:\\s+(\\S+)\\s*=\\s*(\\d+))*"); //$NON-NLS-1$
		Matcher m = pattern.matcher("e slots=3 max-slots=4 r=5"); //$NON-NLS-1$
		if (!m.matches()) {
			System.out.println("no"); //$NON-NLS-1$
			return;
		}
		for (int i = 1; i <= m.groupCount(); i++) {
			System.out.println(m.group(i));
		}
	}
}
