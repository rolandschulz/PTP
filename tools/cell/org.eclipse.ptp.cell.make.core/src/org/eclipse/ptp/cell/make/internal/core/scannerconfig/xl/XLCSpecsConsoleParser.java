/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.make.internal.core.scannerconfig.xl;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.ptp.cell.make.core.debug.Debug;


/**
 * Parses output of ppuxlc -E -v specs.c or ppuxlc -E -v specs.cpp command
 * 
 * @author laggarcia
 * @since 1.0.0
 */
public class XLCSpecsConsoleParser implements IScannerInfoConsoleParser {

	// pattern for the output line of interest
	final Pattern linePattern = Pattern
			.compile("exec:\\s(?!export)(?:.*)\\((.*)\\)"); //$NON-NLS-1$

	// pattern for the symbols arguments
	final Pattern symbolPattern = Pattern.compile("-D(.*)"); //$NON-NLS-1$

	// pattern for the includes arguments
	final Pattern includePattern = Pattern
			.compile("-(?:qgcc_c_stdinc|qc_stdinc|qgcc_cpp_stdinc|qcpp_stdinc)=(.*)"); //$NON-NLS-1$

	private IProject fProject = null;

	private IScannerInfoCollector fCollector = null;

	private List symbols = new ArrayList();

	private List includes = new ArrayList();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#startup(org.eclipse.core.resources.IProject,
	 *      org.eclipse.core.runtime.IPath,
	 *      org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector,
	 *      org.eclipse.cdt.core.IMarkerGenerator)
	 * @since 1.0
	 */
	public void startup(IProject project, IPath workingDirectory,
			IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_XL_PROVIDER);
		this.fProject = project;
		this.fCollector = collector;
	}

	/*
	 * Process an output line from the compiler command line used to retrieve
	 * standard information about the compiler being used. <p> During the
	 * processing, builds two List objects, one with the standard symbols
	 * defined in the compiler and other with the standard include directories.
	 * 
	 * @param line the output line from the compiler command line used @return
	 * boolean
	 * 
	 * @see org.eclipse.cdt.make.intrenal.core.scannerconfig.gnu.GCCSpecsConsoleParser#processLine(java.lang.String)
	 * @since 1.0
	 */
	public boolean processLine(String line) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_XL_PROVIDER, line);
		boolean rc = false;
//		TraceUtil.outputTrace(
//				"XLCSpecsConsoleParser parsing line: [", line, "]"); //$NON-NLS-1$ //$NON-NLS-2$

		// testing the output line against the pattern of interest
		Matcher lineMatcher = linePattern.matcher(line);
		if (lineMatcher.matches()) {
			// getting the arguments from the line of interest from the
			// output
			// generated in command line
			String[] args = lineMatcher.group(1).split(","); //$NON-NLS-1$
			Debug.POLICY.trace(Debug.DEBUG_XL_PROVIDER, "Matches with {0} arguments", args.length); //$NON-NLS-1$
			for (int i = 0; i < args.length; i++) {
				// getting the arguments of interest
				Matcher symbolMatcher = symbolPattern.matcher(args[i]);
				if (symbolMatcher.matches()
						&& !symbols.contains(symbolMatcher.group(1))) {
					// if it is a symbol and it was not yet added
					symbols.add(symbolMatcher.group(1));
					Debug.POLICY.trace(Debug.DEBUG_XL_PROVIDER, "Added symbol {0}", symbolMatcher.group(1)); //$NON-NLS-1$
				} else {
					// if it is not a symbol, check to see if it is an
					// include
					Matcher includeMatcher = includePattern.matcher(args[i]);
					if (includeMatcher.matches()) {
						// if it is a set of include paths, split it
						String[] includePaths = includeMatcher.group(1).split(
								":"); //$NON-NLS-1$
						for (int j = 0; j < includePaths.length; j++) {
							if (!includes.contains(includePaths[j])) {
								// if the include path was not yet added
								includes.add(includePaths[j]);
								Debug.POLICY.trace(Debug.DEBUG_XL_PROVIDER, "Added include {0}", includePaths[j]); //$NON-NLS-1$
							}
						}
					}
				}
			}
		}
		Debug.POLICY.exit(Debug.DEBUG_XL_PROVIDER);
		return rc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
	 * @since 1.0
	 */
	public void shutdown() {
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_XL_PROVIDER);
		Map scannerInfo = new HashMap();
		scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, includes);
		scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
		fCollector.contributeToScannerConfig(fProject, scannerInfo);
		TraceUtil
				.outputTrace(
						"Scanner info from \'specs\' file", //$NON-NLS-1$
						"Include paths", includes, new ArrayList(), "Defined symbols", symbols); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
