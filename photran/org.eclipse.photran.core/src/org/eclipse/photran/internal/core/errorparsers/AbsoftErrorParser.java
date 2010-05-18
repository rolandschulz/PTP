/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

/**
* Absoft Error Parser -- An error parser for Absoft
*
* This error parser matches compiler errors of the following form:
* <pre>
* cf90-400 f90fe: ERROR $MAIN, File = f.f95, Line = 44, Column = 13
*    Oops!
* </pre>
*
* @author Jeff Overbey
*/
@SuppressWarnings("deprecation")
final public class AbsoftErrorParser implements IErrorParser
{
    /*
     * Regex notes:
     *     \S matches any non-whitespace character
     *     \d matches [0-9]
     *     \w matches [A-Za-z_0-9]
     *     Parentheses define a capturing group
     */
    private Pattern errorLineRegex = Pattern.compile("\\S+ f90fe: ERROR \\S+, File = (\\S+), Line = (\\d+), Column = \\d+"); //$NON-NLS-1$

    private boolean expectingErrorMessage = false;
    
    private Matcher errorLineMatcher = null;
    private String filename = null;
    private int lineNum = 0;

    public boolean processLine(String thisLine, ErrorParserManager eoParser)
    {
        if (isErrorLine(thisLine))
        {
            rememberInfoFromErrorLine(thisLine);
            expectingErrorMessage = true;
        }
        else if (expectingErrorMessage)
        {
            generateMarker(thisLine, eoParser);
            expectingErrorMessage = false;
        }
        
        return false;
    }

    private boolean isErrorLine(String thisLine)
    {
        errorLineMatcher = errorLineRegex.matcher(thisLine);
        return errorLineMatcher.matches();
    }

    private void rememberInfoFromErrorLine(String thisLine)
    {
        filename = errorLineMatcher.group(1);
        lineNum = Integer.parseInt(errorLineMatcher.group(2));
    }

    private void generateMarker(String lineContainingErrorMessage, ErrorParserManager eoParser)
    {
        String errorMessage = lineContainingErrorMessage.trim();
        IFile file = eoParser.findFilePath(filename);
        eoParser.generateMarker(file, lineNum, errorMessage, IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);
    }
}