/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IResource;

/**
 * An error parser for GNU Fortran 4.x
 */
public class GFortranErrorParser implements IErrorParser
{
    private static final int MAX_LINES_IN_ERROR_MESSAGE = 10;

    /*================================================================================
    cray-pointers.f90:56.21:
    
    subroutine example3()
                        1
    cray-pointers.f90:43.21:
    
    subroutine example3()
                        2
    Error: Global name 'example3' at (1) is already being used as a SUBROUTINE at (2)
    cray-pointers.f90:58.22:
    
        real    :: pointee
                         1
    Error: Array 'pointee' at (1) cannot have a deferred shape
    ================================================================================*/
    
    private static final Pattern startLine = Pattern.compile("^(.+):([0-9]+)\\.([0-9]+):$");
    private static final Pattern errorLine = Pattern.compile("^(Fatal )?Error: .*");
    private static final Pattern warningLine = Pattern.compile("^Warning: .*");
    
    private IErrorParser currentState = new WaitForStartLine();

    /*
     * N.B. We don't return "true" (meaning we successfully matched an error line)
     * until we match the final line and generate the marker: The startLine pattern
     * (string:int.int:) is very general, so we might make a mistake.  If this
     * happens and we accumulate more than MAX_LINES_IN_ERROR_MESSAGE lines,
     * we bail.  Returning "false" on every line until the final one guarantees that
     * another error parser will still have a chance to handle these lines if we
     * do make a mistake.
     */
    
	public boolean processLine(String line, ErrorParserManager eoParser)
	{
	    return currentState.processLine(line, eoParser);
	}

	private class WaitForStartLine implements IErrorParser
	{
        public boolean processLine(String line, ErrorParserManager eoParser)
        {
            Matcher startLineMatcher = startLine.matcher(line);
            if (startLineMatcher.matches())
            {
                String filename = startLineMatcher.group(1);
                int lineNumber = Integer.parseInt(startLineMatcher.group(2));
                currentState = new AccumulateErrorMessageLines(filename, lineNumber, line);
            }
            return false;
        }
	}
	
	private class AccumulateErrorMessageLines implements IErrorParser
	{
	    private String filename;
	    private int lineNumber;
	    private StringBuffer errorMessage = new StringBuffer();
	    
	    private int linesAccumulated = 1;
	    
	    public AccumulateErrorMessageLines(String filename, int lineNumber, String line)
	    {
	        this.filename = filename;
	        this.lineNumber = lineNumber;
	        errorMessage.append(line);
	    }
	    
        public boolean processLine(String line, ErrorParserManager eoParser)
        {
            errorMessage.append("\n");
            errorMessage.append(line);
            linesAccumulated++;
            
            Matcher errorMatcher = errorLine.matcher(line);
            Matcher warningMatcher = warningLine.matcher(line);
            
            if (errorMatcher.matches())
            {
                // Matched "Error: Description" or "Fatal Error: Description"
                addMarker(eoParser, true);
                return true;
            }
            else if (warningMatcher.matches())
            {
                // Matched "Warning: Description"
                addMarker(eoParser, false);
                return true;
            }
            else if (linesAccumulated > MAX_LINES_IN_ERROR_MESSAGE)
            {
                // We probably made a mistake matching the first line
                currentState = new WaitForStartLine();
                return false;
            }
            else
            {
                // Still accumulating an error message
                return false;
            }
        }

        private void addMarker(ErrorParserManager eoParser, boolean isError)
        {
            IResource file = eoParser.findFileName(filename);
            eoParser.generateMarker(file,
                lineNumber,
                errorMessage.toString(),
                isError ? IMarkerGenerator.SEVERITY_ERROR_RESOURCE : IMarkerGenerator.SEVERITY_WARNING,
                null);
            currentState = new WaitForStartLine();
        }
	}
}
