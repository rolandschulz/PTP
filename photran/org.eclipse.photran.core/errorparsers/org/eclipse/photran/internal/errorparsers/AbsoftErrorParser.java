package org.eclipse.photran.internal.errorparsers;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

import java.util.regex.*;

/**
* Absoft Error Parser -- An error parser for Absoft
*
* cf90-400 f90fe: ERROR $MAIN, File = f.f95, Line = 44, Column = 13
*    Oops!
*
* @author Jeff Overbey
*/
final public class AbsoftErrorParser implements IErrorParser
{
    /*
     * Regex notes:
     *     \S matches any non-whitespace character
     *     \d matches [0-9]
     *     \w matches [A-Za-z_0-9]
     *     Parentheses define a capturing group
     */
    private Pattern errorLineRegex = Pattern.compile("\\S+ f90fe: ERROR \\S+, File = (\\S+), Line = (\\d+), Column = \\d+");

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