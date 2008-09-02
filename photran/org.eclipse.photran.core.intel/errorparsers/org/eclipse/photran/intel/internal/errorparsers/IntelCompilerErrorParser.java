/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.intel.internal.errorparsers;

import java.util.StringTokenizer;
import java.io.File;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

/**
 * Intel Compilers error parser
 * 
 */

public class IntelCompilerErrorParser  implements IErrorParser {
    /**
     *  Extracts information from Intel compiler diagnostics.
     *  Diagnostics can be ifort-specific, icc-specific, or common to both.

     *** ifort-specific diagnostics ***
     * Format 1:
     * fortcom: severity: filename, line#: diagnostic text
     * (ex. fortcom: Error: test.f90, line 3: This global name is invalid in this context.)
     * "severity" can be "Info", "Warning", or "Error".
     * We extract file, line number, and error information from this.
     * 
     * Format 2:
     * fortcom: Severe: diagnostic text
     * (ex 1. fortcom: Severe: **Internal compiler error: ..
     *  ex 2. fortcom: Severe: No such file or directory)
     * We extract the error information from this.

     *** icc-specific diagnostics ***
     * Format 1:
     * filename(line#): severity #diagnostic_number: diagnostic text
     * "severity" can be "error", "warning", or "remark".
     * (ex. mig.c(1): warning #226: function declared implicitly.)
     *
     * Format 2:
     * filename(line#): severity: diagnostic text
     * "severity" can be "error", "internal error", or "catastrophic error".
     * (ex. mig.c(18): error: the size of an array must be greater than zero)
     *
     * Format 3:
     * non-standard driver/preprocessor message
     * ONLY HAS ONE COLON - this is an exception which will be detected just
     * before exiting
     * Catastrophic error: could not open source file <filename> 
     *
     *** diagnostics common to ifort(fortcom) and icc(mcpcom) ***
     * Format 1: (backend diagnostics)
     * filename(line#): (col.#) severity: diagnostic text
     * "severity" can be "error", "warning", or "remark".
     * (ex. mig.c(2): (col.1) remark: main has been targeted for automatic cpu dispatch.)
     *
     * Format 2: (driver diagnostics)
     * program: severity: diagnostic text
     * "program" is the name of the driver being run and can be
     * "icl", "icc", "icpc", "ifort", "xilink", "xild", "xiar", or "xilib".
     * "severity" can be "error", "warning", "internal error", "Command line
     * error", "Command line warning", or "Command line remark".
     * (ex. icc: Command line warning: overriding '-O1' with '-O2')
     *
     */
    public boolean processLine(String line, ErrorParserManager eoParser)
    {
        String severitystr, filestr, linestr, message;

        StringTokenizer tokenizer = new StringTokenizer(line, ":");
	/* First of all, see if this is a fortcom-specific error */
        if (line.startsWith("fortcom: ")) {
            try {
		tokenizer.nextToken(); /* fortcom */
                severitystr = tokenizer.nextToken().trim();
               	if (severitystr.equals("Severe")) {
		    message = tokenizer.nextToken("\r\n").substring(2).trim();
		    if (message.startsWith("No such file or directory")) {
			/* need to process this together with the line. Save
			   it in a buffer and return */
			eoParser.appendToScratchBuffer(message);
			return false;
		    }	
		    
		    eoParser.generateMarker(/*file=*/null, /*line#=*/-1,
					    message,
					    /*severity=*/IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
               	}	
               	else {
                
		    filestr = tokenizer.nextToken(",").substring(2).trim();
		    linestr = tokenizer.nextToken(":").substring(2).trim();
		    message = tokenizer.nextToken("\r\n").substring(2).trim();
		    IFile file = eoParser.findFileName(filestr);

		    int severity = -1;          
		    if (severitystr.equals("Info")) {
                	severity = IMarkerGenerator.SEVERITY_INFO;
		    }
		    else if (severitystr.equals("Warning")) {
                	severity = IMarkerGenerator.SEVERITY_WARNING;
		    }
		    else if (severitystr.equals("Error")) {
                	severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
		    }
                                                
		    int lineno = Integer.parseInt(linestr.substring(5));
               	
		    eoParser.generateMarker(file, lineno, message, severity, null);
               	}
            }
            catch (Throwable x) {
                ;
            }
	} /* fortcom */
    else if (eoParser.getScratchBuffer().startsWith("No such file or directory")) {
    		/* process the multi-line fortcom diagnostics */
    		String buffer = eoParser.getScratchBuffer();
    		eoParser.clearScratchBuffer();
    		message = buffer + ' ' + line;
    		eoParser.generateMarker(/*file=*/null, /*line#=*/-1, message,
    					/*severity=*/IMarkerGenerator.SEVERITY_ERROR_BUILD, null);
    }
	else {
	    /* the normal message recognition rules are
	     * the string must have two colons for the following parsing
	     * if there is a left paren in the first part of the message,
	     * assume a front end message format
	     * parse and return handled or not handled 
	     * else
	     * assume a driver message format
	     * parse and return handled or not handled 
	     */

	    /* 16-Jun-2004:
	     * Apparently CDT (thru and including CDT 2.0) expects all error parsers to return
	     * false always.  There is considerable confusion over this point, but this parser
	     * is now changed to conform until the issue can be revisited, hopefully for CDT 3.0.
	     * The structure of the code has not been changed, only the return values.
	     * Should it become desirable to revert to the previous behavior, just change
	     * the return values to true for the return statments with a comment indicating this
	     * parser has handled the message.
	     */
	    
	    int firstColon= line.indexOf(':');
	    String desc= line.substring(firstColon + 1).trim();
	    int secondColon= desc.indexOf(':');

	    if ((firstColon != -1) && (secondColon != -1)) {
		String firstPart= line.substring(0, firstColon);
		int leftParen = firstPart.indexOf("(");
		/* the string must have two colons to get this far. */
		/* do a paren check to distinguish the front end format from
		   the driver format. */
		if (leftParen != -1) {
		    /* have a left paren, proceed to parse as a mcpcom front end
		       message */
		    StringTokenizer tok= new StringTokenizer(firstPart, "()");
		    if (tok.hasMoreTokens()) {
		        String fileName= tok.nextToken();
		        if (tok.hasMoreTokens()) {
			    String lineNumber= tok.nextToken();
			    try {
				int num= Integer.parseInt(lineNumber);
				int i= fileName.lastIndexOf(File.separatorChar);
				if (i != -1) {
				    fileName= fileName.substring(i + 1);
				}
				IFile file= eoParser.findFileName(fileName);
				if (file != null || eoParser.isConflictingName(fileName)) {
				    String middle= desc.substring(0, secondColon);
				    int severity = -1;
				    if (middle.indexOf("warning")!= -1) {
					severity= IMarkerGenerator.SEVERITY_WARNING;
				    }
				    if (middle.indexOf("error")!= -1) {
					severity= IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
				    }
				    if (middle.indexOf("remark")!= -1) {
					severity= IMarkerGenerator.SEVERITY_INFO;
				    }
				    if (file == null) {
					desc= "*" + desc;
				    }
				    if (severity == IMarkerGenerator.SEVERITY_WARNING ||
					severity == IMarkerGenerator.SEVERITY_ERROR_RESOURCE ||
					severity == IMarkerGenerator.SEVERITY_INFO) {					            
					eoParser.generateMarker(file, num, desc, severity, null);
					return false; /* give handled return */
				    }
				    return false; /* give not handled return */
				}
			    } catch (NumberFormatException e) {
				return false;  /* error in number conversion, give not handled return */
			       }
		        } else
			    return false; /* not enough tokens, give not handled return */
		    }
		}
		else { /* no filename or line# - try to parse as a driver diagnostics */
		    /* qualify the message by checking the program name */
		    if((firstPart.indexOf("icl")!= -1)   ||(firstPart.indexOf("icc")!= -1) ||
		       (firstPart.indexOf("icpc")!= -1)  ||(firstPart.indexOf("ifort")!= -1) ||  
		       (firstPart.indexOf("xilink")!= -1)||(firstPart.indexOf("xild")!= -1) ||  
		       (firstPart.indexOf("xiar")!= -1)  ||(firstPart.indexOf("xilib")!= -1)) {  
			String  middle= desc.substring(0, secondColon);
			int severity = -1;
			if (middle.indexOf("warning")!= -1) {
			    severity= IMarkerGenerator.SEVERITY_WARNING;
			}
			if (middle.indexOf("error")!= -1) {
			    severity= IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			}
			if (middle.indexOf("remark")!= -1) {
			    severity= IMarkerGenerator.SEVERITY_INFO;
			}
			if (severity == IMarkerGenerator.SEVERITY_WARNING ||
			    severity == IMarkerGenerator.SEVERITY_ERROR_RESOURCE ||
			    severity == IMarkerGenerator.SEVERITY_INFO) {
			    eoParser.generateMarker(/*file=*/null, /*line#=*/-1, desc, severity, null);
			    return false; /* give handled return */
			}			      
			return false; /* message not qualified, give not handled return */
			} else {
				if (line.indexOf("Command-line error: invalid macro definition: -D") != -1) { 
					int severity= IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
					eoParser.generateMarker(null, 0, line, severity, null);
					return false; // give handled return 
				}
			    return false;  // message not qualified, give not handled return
			   }
		}
	    }
	    /* non-standard driver/preprocessor message */
	      if (line.indexOf("Catastrophic error: could not open source file") != -1 ) { 
		      int severity= IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
		      eoParser.generateMarker(/*file=*/null, /*line#=*/-1, line, severity, null);
		      return false; /* give handled return */
	    }
	    return false;
	} /* mcpcom */
        return false;
    }
}
