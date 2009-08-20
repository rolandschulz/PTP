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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * An error parser for GNU Fortran 4.x
 *
 * @author Jeff Overbey
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

    //                                                                     Filename
    //                                                                     |    Line        Column
    //                                                                     |    |           |
    //                                    Regex group number    1          2    3       4   5
    private static final Pattern startLine = Pattern.compile("^(In file )?(.+):([0-9]+)(\\.([0-9]+))?:$");
    private static final Pattern errorLine = Pattern.compile("^(Fatal )?Error: .*");
    private static final Pattern warningLine = Pattern.compile("^Warning: .*");

    /*
     * This error parser uses the GoF State pattern.  It starts in the first of two states:
     *
     * (1) WaitForStartLine
     * (2) AccumulateErrorMessageLines
     */

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

    /**
     * @see IErrorParser#processLine(String, ErrorParserManager)
     */
	public boolean processLine(String line, ErrorParserManager eoParser)
	{
	    return currentState.processLine(line, eoParser);
	}

	/**
	 * STATE 1: WAITING FOR A START LINE
	 * <p>
	 * This is the state of the error parser when it is waiting for a line like
	 * <pre>cray-pointers.f90:56.21:</pre>
	 * When it finds one, it switches to the next state (Accumulating an Error Message).
	 */
	private class WaitForStartLine implements IErrorParser
	{
        public boolean processLine(String line, ErrorParserManager eoParser)
        {
            Matcher startLineMatcher = startLine.matcher(line);
            if (startLineMatcher.matches())
            {
                String filename = startLineMatcher.group(2);
                int lineNumber = Integer.parseInt(startLineMatcher.group(3));
                currentState = new AccumulateErrorMessageLines(filename, lineNumber, line);
            }
            return false;
        }
	}

    /**
     * STATE 2: ACCUMULATING ERROR MESSAGE
     * <p>
     * This is the state of the error parser after it has seen a line like
     * <pre>cray-pointers.f90:56.21:</pre>.
     * The next several lines contain a source pointer and error message, like
     * <pre>
     *     subroutine example3()
     *                         2
     *     Error: Global name 'example3' at (1) is already being used as a SUBROUTINE at (2)
     * </pre>
     * In this state, we skip the first several lines (presumably the source pointer),
     * and when we see a line starting with "Fatal Error:", "Error:", or "Warning:",
     * we display that message in the Problems view, and the error parser returns to
     * its initial state, waiting for the next line.
     */
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
            //errorMessage.append("\n");
            //errorMessage.append(line);
            linesAccumulated++;

            Matcher errorMatcher = errorLine.matcher(line);
            Matcher warningMatcher = warningLine.matcher(line);

            if (errorMatcher.matches())
            {
                // Matched "Error: Description" or "Fatal Error: Description"
                errorMessage.append(" ");
                errorMessage.append(line);
                addMarker(eoParser, true);
                return true;
            }
            else if (warningMatcher.matches())
            {
                // Matched "Warning: Description"
                errorMessage.append(" ");
                errorMessage.append(line);
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
            IResource file = findFile(eoParser);
            eoParser.generateMarker(file,
                lineNumber,
                errorMessage.toString(),
                isError ? IMarkerGenerator.SEVERITY_ERROR_RESOURCE : IMarkerGenerator.SEVERITY_WARNING,
                null);
            currentState = new WaitForStartLine();
        }

        private IFile findFile(ErrorParserManager eoParser)
        {
            IFile result = eoParser.findFileName(filename);
            if (result != null) return result;

            // The managed build system prefixes ../ to filenames.
            // So (this is a hack) if the file can't be found and
            // it starts with ../ try removing that and hope that
            // maybe it will refer to a workspace location.

            if (filename.startsWith("../") || filename.startsWith("..\\"))
                return eoParser.findFileName(filename.substring(3));

            return null;
        }
	}
}
