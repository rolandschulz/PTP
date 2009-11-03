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

import java.util.StringTokenizer;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

/**
 * Lahey Fortran 7.1 for Windows Error Parser
 * 
 * This Lahey 7.1 error parser only picks up __####-S: errors. 
 * We'll need to see what warnings look like, and if this format is consistent.
 * 
 * @author Brian Foote
 */
public class LaheyFortranErrorParser implements IErrorParser
{
    private boolean laheyFlag;

    public LaheyFortranErrorParser()
    {
        //We've never seen a Lahey line...
        laheyFlag = false;
    }

    /**
     * Extracts file, line number, and error information from lines of the form
     * 1117-S: "dog.f90", line 4, column 7: Missing left parenthesis.
     */
    public boolean processLine(String line, ErrorParserManager epm)
    {
        String /*lahey,*/ severityString, fileString, lineString, message;

        //If we ever see a line that starts this way, we are
        //henceforth in business...
        if (line.startsWith("Lahey/Fujitsu")) laheyFlag = true;

        //If we've never seen a Lahey ad, are work here is done...
        if (!laheyFlag) return false;

        //We've work to do. See if this is an error line...
        if (line.length()<5) return false;
        String s4 = line.substring(4);
        if (s4.startsWith("-S:"))
        {
            StringTokenizer tokenizer = new StringTokenizer(line, ":");
            try
            {
                /*lahey =*/ tokenizer.nextToken(); // __####-S:
                //$TODO Fix this when we see what a warning looks like...
                severityString = "Error";
                fileString = tokenizer.nextToken(",").substring(2).trim();
                lineString = tokenizer.nextToken(",:").substring(1).trim();
                message = tokenizer.nextToken("\r\n").substring(2).trim();

                int severity = (severityString.equals("Error") ? IMarkerGenerator.SEVERITY_ERROR_RESOURCE
                                                              : IMarkerGenerator.SEVERITY_WARNING);
                String lineNumberString = lineString.substring(5);
                int lineNumber = Integer.parseInt(lineNumberString);
                fileString = fileString.substring(1, fileString.length() - 1);
                IFile file = epm.findFilePath(fileString);

                epm.generateMarker(file, lineNumber, message, severity, null);
            }
            catch (Throwable x)
            {
                //Eat any errors right here...
            }
        }
        return false;
    }
}