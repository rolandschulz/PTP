/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.errorparsers;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
* gfortran Error Parser -- An error parser for gfortran (GNU GCC Fortran)
*
* @author ?
*/
public class GFortranErrorParser implements IErrorParser {
	
	String fileName = null;
	int lineNumber = -1;
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		return processLine(line, eoParser, IMarkerGenerator.SEVERITY_ERROR_RESOURCE);
	}

	public boolean processLine(String line, ErrorParserManager eoParser, int inheritedSeverity) {
		// Known patterns.
		// (a)
		//  In file filename:lineno
		//
		// use standard_types
		//                  1
		//
		// Fatal Error: Can't open module file 'standard_types.mod' for reading at (1): No such file or directory

		int firstColon = line.indexOf(':');

		/* Guard against drive in Windows platform.  */
		if (firstColon == 1) {
			try {
				String os = System.getProperty("os.name"); //$NON-NLS-1$
				if (os != null && os.startsWith("Win")) { //$NON-NLS-1$
					try {
						if (Character.isLetter(line.charAt(0))) {
							firstColon = line.indexOf(':', 2);
						}
					} catch (StringIndexOutOfBoundsException e) {
					}
				}
			} catch (SecurityException e) {
			}
		}

		if (firstColon != -1) {
			
			try {
			
				/*
				 *	In file filename:lineno
				 */
				if (line.startsWith("In file ")) {
					fileName = line.substring(8, firstColon);
					String lineno = line.substring(firstColon + 1);
					try {
						lineNumber = Integer.parseInt(lineno);
					} catch (NumberFormatException e) {
						lineNumber = -1;
						fileName = null;
						return false;
					}
				}

				/*
				 *	(a) Error: desc
				 *  (b) Fatal Error: desc
				 */
				else if (line.startsWith("Error:") || line.startsWith("Fatal Error:")) {
					String varName = null;
					String desc = line.substring(firstColon + 1).trim();
					int severity = extractSeverity("Error", inheritedSeverity);
					
					if (fileName == null || lineNumber == -1) {
						return false;
					}

					// The pattern is to generall we have to guard:
					// Before making this pattern a marker we do one more check
					// The fileName that we extract __must__ look like a valid file name.
					// We been having to much bad hits with patterns like
					//   /bin/sh ../libtool --mode=link gcc -version-info 0:1:0 foo.lo var.lo
					// Things like libtool that will fool the parser because of "0:1:0"
					if (!Path.EMPTY.isValidPath(fileName)) {
						return false;
					}
					IFile file = eoParser.findFileName(fileName);
					if (file != null) {
						if (eoParser.isConflictingName(fileName)) {
							desc = "[Conflicting names: " + fileName + " ] " + desc; //$NON-NLS-1$ //$NON-NLS-2$
							file = null;							
						}
					} else {
						file = eoParser.findFilePath(fileName);
						if (file == null) {
							// one last try before bailing out we may be in a wrong
							// directory.  This will happen, for example in the Makefile:
							// all: foo.c
							//    cd src3; gcc -c bar/foo.c
							// the user do a cd(1).
							IPath path = new Path(fileName);
							if (path.segmentCount() > 1) {
								String name = path.lastSegment();
								file = eoParser.findFileName(fileName);
								if (file != null) {
									if (eoParser.isConflictingName(fileName)) {
										desc = "[Conflicting names: " + name + " ] " + desc; //$NON-NLS-1$ //$NON-NLS-2$
										file = null;							
									}
								}
							}
						}
					}

					// Display the fileName.
					if (file == null) {
						desc = desc +"[" + fileName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					}

					// Look for variable name
					int p = desc.indexOf("Symbol \'");
				 	if (p != -1) {
				 		varName = desc.substring(p+8);
				 		p = varName.indexOf("'");
				 		if (p != -1) {
				 			varName = varName.substring(0, p);
				 		} else {
				 			varName = null;
				 		}
				 	}
				 	
				 	// Remove at (error number)
				 	p = desc.indexOf("at (");
				 	if (p != -1) {
				 		String tail = desc.substring(p+4);
				 		int q = tail.indexOf(")");
				 		if (q != -1) {
				 			desc = desc.substring(0, p) + tail.substring(q+1);
				 		}
				 	}

					eoParser.generateMarker(file, lineNumber, desc, severity, varName);
					lineNumber = -1;
					fileName = null;
				}
			} catch (StringIndexOutOfBoundsException e) {
			}
		}
		return false;
	}
	
	private int extractSeverity(String desc, int defaultSeverity) {
		int severity = defaultSeverity; 
		if (desc.startsWith("warning") || desc.startsWith("Warning")) { //$NON-NLS-1$ //$NON-NLS-2$
			severity = IMarkerGenerator.SEVERITY_WARNING;
		}
		return severity;
	}
}
