package org.eclipse.cldt.internal.errorparsers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cldt.core.ErrorParserManager;
import org.eclipse.cldt.core.IErrorParser;
import org.eclipse.cldt.core.IMarkerGenerator;
import org.eclipse.core.runtime.Path;

public class MakeErrorParser implements IErrorParser {
			
	public MakeErrorParser() {
	}
	
	static int getDirectoryLevel(String line) {
		int s = line.indexOf('[');
		int num = 0;
		if (s != -1) {
			int e = line.indexOf(']');
			String number = line.substring(s + 1, e).trim();		
			try {
				num = Integer.parseInt(number);
			} catch (NumberFormatException exc) {
			}
		}
		return num;
	}
	
	public boolean processLine(String line, ErrorParserManager eoParser) {
		// make\[[0-9]*\]:  error_desc
		int firstColon= line.indexOf(':');
		if (firstColon != -1 && line.startsWith("make")) { //$NON-NLS-1$
			boolean enter = false;
			String msg= line.substring(firstColon + 1).trim();		
			if ((enter = msg.startsWith("Entering directory")) || //$NON-NLS-1$
			    (msg.startsWith("Leaving directory"))) { //$NON-NLS-1$
			    int s = msg.indexOf('`');
			    int e = msg.indexOf('\'');
			    if (s != -1 && e != -1) {
			    	String dir = msg.substring(s+1, e);
			    	if (enter) {
			    		/* Sometimes make screws up the output, so
			    		 * "leave" events can't be seen.  Double-check level
			    		 * here.
			    		 */
			    		int level = getDirectoryLevel(line);
			    		int parseLevel = eoParser.getDirectoryLevel();
		    			for (; level < parseLevel; level++) {
		    				eoParser.popDirectory();
		    			}
			    		eoParser.pushDirectory(new Path(dir));
			    	} else {
			    		eoParser.popDirectory();
			    		/* Could check to see if they match */
			    	}
				}
			} else if (msg.startsWith("***")) { //$NON-NLS-1$
				boolean warning = false;
				if (msg.length() > 4) {
					String s = msg.substring(3).trim();
					warning = s.startsWith("Warning"); //$NON-NLS-1$
				}
				if (warning) {
					eoParser.generateMarker(null, -1, msg, IMarkerGenerator.SEVERITY_WARNING, null);
				} else {
					eoParser.generateMarker(null, -1, msg, IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
				}
			}
		}
		return false;
	}
}
