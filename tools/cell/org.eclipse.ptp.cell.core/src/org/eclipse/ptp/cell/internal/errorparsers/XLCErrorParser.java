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

package org.eclipse.ptp.cell.internal.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.ptp.cell.debug.Debug;


/**
 * Error parser for XLC compiler messages.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0.0
 */
public class XLCErrorParser implements IErrorParser {
	Pattern p1 = Pattern
			.compile("\"([^\"]*)\",\\s*line\\s*(\\d+)\\s*\\.\\s*(\\d+)\\s*\\:\\s*(\\d+)\\s*\\-\\s*(\\d+)\\s*\\((\\w)\\)\\s*(.*)"); //$NON-NLS-1$

	Pattern p2 = Pattern.compile("([^:]*):\\s*(\\d+)\\s*\\-\\s*(\\d+)\\s*(.*)"); //$NON-NLS-1$

	public boolean processLine(String line, ErrorParserManager eoParser) {
		Debug.POLICY.enter(Debug.DEBUG_ERRORPARSERS, line, eoParser);
		/*
		 * Template 1: "wrong.c", line 1.10: 1506-296 (S) #include file
		 * <stdlibx.h> not found. "wrong.c", line 5.9: 1506-277 (S) Syntax
		 * error: possible missing ';' or ','? "wrong.c", line 4.23: 1506-275
		 * (S) Unexpected text x encountered. "wrong.h", line 3.13: 1506-166 (S)
		 * Definition of function asfd requires parentheses.
		 * 
		 * Template 2: /opt/ibmcmp/xlc/1.0/bin/ppuxlc: 1501-216 command option
		 * -3 is not recognized - passed to ld /opt/ibmcmp/xlc/1.0/bin/ppuxlc:
		 * 1501-210 command option Wall contains an incorrect
		 * 
		 * Values after message code: (I) = information, (W) = warning, (E) =
		 * error, (S) = severe error, (U) = unrecoverable error
		 * 
		 */

		// Test rule p1
		Matcher matcher = p1.matcher(line);
		if (matcher.matches()) {
			Debug.POLICY.trace(Debug.DEBUG_ERRORPARSERS, "Matched rule p1"); //$NON-NLS-1$
			String fileName = matcher.group(1);
			IFile file = eoParser.findFileName(fileName);
			int lineNumber = Integer.parseInt(matcher.group(2));
			// int columnNumber = Integer.parseInt(matcher.group(3));
			String errorMessage = matcher.group(7);

			int severity = IMarkerGenerator.SEVERITY_ERROR_BUILD;
			String severityChar = matcher.group(6);
			if (severityChar.equals("I")) //$NON-NLS-1$
				severity = IMarkerGenerator.SEVERITY_INFO;
			else if (severityChar.equals("W")) //$NON-NLS-1$
				severity = IMarkerGenerator.SEVERITY_WARNING;
			else if (severityChar.equals("E")) //$NON-NLS-1$
				severity = IMarkerGenerator.SEVERITY_ERROR_BUILD;
			else if (severityChar.equals("S")) //$NON-NLS-1$
				severity = IMarkerGenerator.SEVERITY_ERROR_BUILD;
			else if (severityChar.equals("U")) //$NON-NLS-1$
				severity = IMarkerGenerator.SEVERITY_ERROR_BUILD;

			String varName = null;
			
			Debug.POLICY.trace(Debug.DEBUG_ERRORPARSERS, "New error: file={0}, lineNumber={1}, errorMessage={2}, severity={3}, varName={4}", file, lineNumber, errorMessage, severity, varName); //$NON-NLS-1$
			eoParser.generateMarker(file, lineNumber, errorMessage, severity,
					varName);

			Debug.POLICY.exit(Debug.DEBUG_ERRORPARSERS, true);
			return true;
		}

		// Test rule p2
		matcher = p2.matcher(line);
		if (matcher.matches()) {
			Debug.POLICY.trace(Debug.DEBUG_ERRORPARSERS, "Matched rule p2"); //$NON-NLS-1$
			IFile file = null;
			int lineNumber = -1;
			String errorMessage = matcher.group(4);
			int severity = IMarkerGenerator.SEVERITY_ERROR_BUILD;

			String varName = null;
			Debug.POLICY.trace(Debug.DEBUG_ERRORPARSERS, "New error: file={0}, lineNumber={1}, errorMessage={2}, severity={3}, varName={4}", file, lineNumber, errorMessage, severity, varName); //$NON-NLS-1$
			eoParser.generateMarker(file, lineNumber, errorMessage, severity,
					varName);

			Debug.POLICY.exit(Debug.DEBUG_ERRORPARSERS, true);
			return true;
		}
		
		// No error
		Debug.POLICY.exit(Debug.DEBUG_ERRORPARSERS, false);
		return false;
	}

	public XLCErrorParser() {
		super();
	}
}
