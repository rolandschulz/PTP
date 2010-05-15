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
 * Intel Fortran 8.1 error parser
 * 
 * This error parser extracts file, line number, and error information from lines of the form
 * <pre>fortcom: Error: test.f90, line 3: Message</pre>
 *
 * @author Jeff Overbey
 */
public class IntelFortranErrorParser implements IErrorParser
{
    public boolean processLine(String line, ErrorParserManager eoParser)
    {
        String /*fortcom,*/ severitystr, filestr, linestr, message;

        StringTokenizer tokenizer = new StringTokenizer(line, ":");
        if (line.startsWith("fortcom: "))
        {
            try
            {
                /*fortcom =*/ tokenizer.nextToken(); // fortcom
                severitystr = tokenizer.nextToken().trim(); // Error
                filestr = tokenizer.nextToken(",").substring(2).trim(); // filename
                linestr = tokenizer.nextToken(":").substring(2).trim(); // line
                                                                        // n
                message = tokenizer.nextToken("\r\n").substring(2).trim(); // Message

                int severity = (severitystr.equals("Error") ? IMarkerGenerator.SEVERITY_ERROR_RESOURCE
                                                           : IMarkerGenerator.SEVERITY_WARNING);
                int lineno = Integer.parseInt(linestr.substring(5));
                IFile file = eoParser.findFilePath(filestr);

                eoParser.generateMarker(file, lineno, message, severity, null);
            }
            catch (Throwable x)
            {
                ;
            }
        }
        return false;
    }
}