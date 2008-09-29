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

package org.eclipse.ptp.cell.managedbuilder.core;

import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator;
import org.eclipse.cdt.managedbuilder.core.IManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedCommandLineInfo;
import org.eclipse.cdt.managedbuilder.internal.core.Tool;

/**
 * @author laggarcia
 * @since 1.1.0
 */
public class EmbedSPUCommandLineGenerator implements
		IManagedCommandLineGenerator {

	public final String WHITESPACE = " "; //$NON-NLS-1$

	public final String CMD_LINE_PRM_NAME = "\\$\\{COMMAND\\}"; //$NON-NLS-1$

	public final String FLAGS_PRM_NAME = "\\$\\{FLAGS\\}"; //$NON-NLS-1$

	public final String OUTPUT_FLAG_PRM_NAME = "\\$\\{OUTPUT_FLAG\\}"; //$NON-NLS-1$

	public final String OUTPUT_PREFIX_PRM_NAME = "\\$\\{OUTPUT_PREFIX\\}"; //$NON-NLS-1$

	public final String OUTPUT_PRM_NAME = "\\$\\{OUTPUT\\}"; //$NON-NLS-1$

	public final String INPUTS_PRM_NAME = "\\$\\{INPUTS\\}"; //$NON-NLS-1$

	public final char BACKSLASH = '\\';

	public final char DOLLAR = '$';

	public final char SCAPE_CHAR = '\\';

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedCommandLineGenerator#generateCommandLineInfo(org.eclipse.cdt.managedbuilder.core.ITool,
	 *      java.lang.String, java.lang.String[], java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String[],
	 *      java.lang.String)
	 */
	public IManagedCommandLineInfo generateCommandLineInfo(ITool tool,
			String commandName, String[] flags, String outputFlag,
			String outputPrefix, String outputName, String[] inputResources,
			String commandLinePattern) {

		String commandLine;

		if (commandLinePattern == null || commandLinePattern.length() <= 0)
			commandLinePattern = Tool.DEFAULT_PATTERN;

		commandLine = replace(commandLinePattern, CMD_LINE_PRM_NAME,
				commandName.trim());
		commandLine = replace(commandLine, FLAGS_PRM_NAME,
				stringArrayToString(flags));
		commandLine = replace(commandLine, OUTPUT_FLAG_PRM_NAME, outputFlag
				.trim());
		commandLine = replace(commandLine, OUTPUT_PREFIX_PRM_NAME, outputPrefix
				.trim());
		commandLine = replace(commandLine, OUTPUT_PRM_NAME, outputName.trim());
		commandLine = replace(commandLine, INPUTS_PRM_NAME,
				stringArrayToString(inputResources));

		return new ManagedCommandLineInfo(commandLine.trim(),
				commandLinePattern, commandName, stringArrayToString(flags),
				outputFlag, outputPrefix, outputName,
				stringArrayToString(inputResources));
	}

	private String stringArrayToString(String[] array) {
		if (array == null || array.length <= 0)
			return new String();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; i++)
			sb.append(array[i] + WHITESPACE);
		return sb.toString().trim();
	}

	private String replace(String string, String regex, String replacement) {

		// Matcher.replaceAll has a limitation with backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in the
		// replacement String.
		// Then, lets substitute these characters with a scape sequence.
		if ((replacement.indexOf(BACKSLASH) != -1)
				|| (replacement.indexOf(DOLLAR) != -1)) {
			StringBuffer sb = new StringBuffer();
			char c;
			for (int i = 0; i < replacement.length(); i++) {
				c = replacement.charAt(i);
				if ((c == BACKSLASH) || (c == DOLLAR)) {
					sb.append(SCAPE_CHAR);
				}
				sb.append(c);
			}
			replacement = sb.toString();
		}

		return Pattern.compile(regex).matcher(string).replaceAll(replacement);
	}

}
