package org.eclipse.cldt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cldt.core.ErrorParserManager;
import org.eclipse.cldt.core.IErrorParser;
import org.eclipse.cldt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

public class GASErrorParser implements IErrorParser {

	public boolean processLine(String line, ErrorParserManager eoParser) {
		// cc -c x.c
		// Only when the previous line sasys Assembler
		// /tmp/cc8EXnKk.s: Assembler messages:
		// /tmp/cc8EXnKk.s:46: Error: no such 386 instruction: `b'
		try {
			String previous = eoParser.getPreviousLine();
			String fileName = ""; //$NON-NLS-1$
			IFile file = null;
			int num = 0;
			int severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			String desc = line;
			if (previous != null && previous.startsWith("Assembler")) { //$NON-NLS-1$
				if (! line.startsWith("FATAL")) { //$NON-NLS-1$
					int firstColon= line.indexOf(':');
					if (firstColon != -1) {
						fileName = line.substring(0, firstColon);
						desc = line.substring(firstColon + 1);
						int secondColon= line.indexOf(':', firstColon + 1);
						if (secondColon != -1) {
							String lineNumber = line.substring(firstColon + 1, secondColon);
							try {
								num = Integer.parseInt(lineNumber);
							} catch (NumberFormatException e) {
							}
							if (num != 0) {
								desc = line.substring(secondColon + 2);
							}
						}
						file = eoParser.findFileName(fileName);
					}
				}
				boolean isConflicting = false;
				if (file != null) {
					isConflicting = eoParser.isConflictingName(fileName);
					file = null;
				} else {
					file = eoParser.findFileName(fileName);
				}
				if (file == null) {
					desc = fileName + ":" + desc; //$NON-NLS-1$
				}
				eoParser.generateMarker(file, num, desc, severity, null);
			}
		} catch (IndexOutOfBoundsException e) {
		}
		return false;
	}
}
