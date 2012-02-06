/*******************************************************************************
 * Copyright (c) 2011 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Jeff Overbey (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.compilers.internal.core.errorparsers;

import static org.eclipse.cdt.core.IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
import static org.eclipse.cdt.core.IMarkerGenerator.SEVERITY_WARNING;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.core.resources.IFile;

/**
 * CDT error parser for Cray C/C++ compilers (tested with Cray C/C++ version 7.3.4).
 * <p>
 * This error parser recognizes error messages such as the following from craycc (crayc++ is similar; the line begins with
 * &quot;CC-144 crayc++&quot;):
 * 
 * <pre>
 * CC-144 craycc: ERROR File = errors.c, Line = 4
 *   A value of type "double" cannot be used to initialize an entity of type
 *           "char *".
 * 
 *     char *ptr = 1.2345;
 *                 ^
 * 
 * CC-65 craycc: ERROR at end of source
 *   A semicolon is expected at this point.
 * 
 * CC-5 craycc: ERROR 
 *   The source file "file_does_not_exist.c" is unavailable.
 * </pre>
 * <p>
 * Note that Photran provides its own error parser for Cray Fortran.
 * 
 * @author Jeff Overbey
 */
public class CrayErrorParser implements IErrorParser {

	private static final Pattern C_ERROR_WARNING_LINE = Pattern.compile( //
			// Capture Group 1-----------2-------3--------4------------5
			"^CC-[0-9]+ crayc(c|\\+\\+): ([A-Z]+)( File = (.*), Line = ([0-9]+)|.*)?[ \t]*$"); //$NON-NLS-1$

	// Capture groups in the above regexes
	private static final int SEVERITY_GROUP = 2;
	private static final int FILENAME_GROUP = 4;
	private static final int LINE_NUMBER_GROUP = 5;

	@Override
	public boolean processLine(String currentLine, ErrorParserManager eoParser) {
		final Matcher matcher = matchErrorWarningLine(eoParser.getPreviousLine());
		if (matcher != null) {
			final int severity = matcher.group(SEVERITY_GROUP).equals("WARNING") ? SEVERITY_WARNING : SEVERITY_ERROR_RESOURCE; //$NON-NLS-1$
			final String filename = matcher.group(FILENAME_GROUP);
			final IFile file = filename == null ? null : eoParser.findFileName(filename);
			final int lineNumber = atoi(matcher.group(LINE_NUMBER_GROUP));
			final String description = currentLine.trim();

			eoParser.generateMarker(file, lineNumber, description, severity, null);

			return true;
		} else {
			return false;
		}
	}

	private Matcher matchErrorWarningLine(String previousLine) {
		if (previousLine != null) {
			final Matcher m = C_ERROR_WARNING_LINE.matcher(previousLine);
			if (m.matches()) {
				return m;
			}
		}

		return null;
	}

	private int atoi(String string) {
		if (string == null) {
			return 0;
		} else {
			try {
				return Integer.parseInt(string);
			} catch (final NumberFormatException e) {
				return 0;
			}
		}
	}
}
