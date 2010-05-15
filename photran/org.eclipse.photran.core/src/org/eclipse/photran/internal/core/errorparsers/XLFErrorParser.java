/*******************************************************************************
 * Copyright (c) 2007 Los Alamos National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     LANL - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.errorparsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Error parser for IBM XLF
 * 
 * @author Craig Rasmussen
 */
public class XLFErrorParser implements IErrorParser
{
	Pattern p1 = Pattern.compile("^\"([^\"]*)\", line (\\d+)\\.(\\d+): (\\d+)-(\\d+) \\(([USEWLI])\\) (.*)$");
	Pattern p2 = Pattern.compile("^\"([^\"]*)\", (\\d+)-(\\d+) \\(([USEWLI])\\) (.*)$");

	public boolean processLine(String line, ErrorParserManager eoParser) {
		return processLine(line, eoParser, IMarkerGenerator.SEVERITY_ERROR_RESOURCE);
	}

	public boolean processLine(String line, ErrorParserManager eoParser, int inheritedSeverity) {
		// XLF error messsage format:
		//
		// "<filename>", line <lineno>.<column>: 15<CC>-<NNN> (<S>) <description>
		// "<filename>", line <lineno>.<column>: 15<CC>-<NNN> <description>
		//
		// <CC> is one of:
		//      00    : Indicates a code generation or optimization message.
		//      01    : Indicates an XL Fortran common message.
		//      11-20 : Indicates a Fortran-specific message.
		//      25    : Indicates a run-time message from an XL Fortran application program.
		//      85    : Indicates a loop-transformation message.
		//      86    : Indicates an interprocedural analysis (IPA) message.
		//
		// <NNN> is the message number
		//
		// <S> is one of:
		//      U     : An unrecoverable error.
		//      S     : A severe error.
		//      E     : An error that the compiler can correct.
		//      W     : Warning message. 
		//      L     : Language conformance warning message.
		//      I     : Informational message.
		
		String fileName;
        //String cc;
        String lineNum;
        //String colNum;
        //String msgNum;
        String desc;
        String varName = "";
        String level = "S";
        int num = -1;

        Matcher m = p1.matcher(line);
        if (m.matches())
        {
            fileName = m.group(1);
            lineNum = m.group(2);
            /*colNum =*/ m.group(3);
            /*cc =*/ m.group(4);
            /*msgNum =*/ m.group(5);
            level = m.group(6);
            desc = m.group(7);

            try
            {
                num = Integer.parseInt(lineNum);
            }
            catch (NumberFormatException e)
            {
                // Failed.
            }
        }
        else
        {
            m = p2.matcher(line);
            if (m.matches())
            {
                fileName = m.group(1);
                /*cc =*/ m.group(2);
                /*msgNum =*/ m.group(3);
                level = m.group(4);
                desc = m.group(5);
            }
            else
            {
                return false;
            }
        }

        if (!Path.EMPTY.isValidPath(fileName)) { return false; }

        IFile file = eoParser.findFileName(fileName);
        if (file != null)
        {
            if (eoParser.isConflictingName(fileName))
            {
                desc = "[Conflicting names: " + fileName + " ] " + desc; //$NON-NLS-1$ //$NON-NLS-2$
                file = null;
            }
        }
        else
        {
            file = eoParser.findFilePath(fileName);
            if (file == null)
            {
                // one last try before bailing out we may be in a wrong
                // directory. This will happen, for example in the Makefile:
                // all: foo.c
                // cd src3; gcc -c bar/foo.c
                // the user do a cd(1).
                IPath path = new Path(fileName);
                if (path.segmentCount() > 1)
                {
                    String name = path.lastSegment();
                    file = eoParser.findFileName(fileName);
                    if (file != null)
                    {
                        if (eoParser.isConflictingName(fileName))
                        {
                            desc = "[Conflicting names: " + name + " ] " + desc; //$NON-NLS-1$ //$NON-NLS-2$
                            file = null;
                        }
                    }
                }
            }
        }

        // Display the fileName.
        if (file == null)
        {
            desc = desc + " [" + fileName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        int severity = extractSeverity(level, inheritedSeverity);

        eoParser.generateMarker(file, num, desc, severity, varName);
        return true;
    }

    private int extractSeverity(String desc, int defaultSeverity)
    {
        int severity = defaultSeverity;

        if (desc == "U" || desc == "S")
        {
            severity = IMarkerGenerator.SEVERITY_ERROR_BUILD;
        }
        else if (desc == "E")
        {
            severity = IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
        }
        else if (desc == "W" || desc == "L")
        {
            severity = IMarkerGenerator.SEVERITY_WARNING;
        }
        else if (desc == "I")
        {
            severity = IMarkerGenerator.SEVERITY_INFO;
        }

        return severity;
    }
}
