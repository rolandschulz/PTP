package org.eclipse.photran.internal.errorparsers;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

import java.util.regex.*;

/**
* PGI Fortran Error Parser -- An error parser for the Portland Group compiler
*
* cf90-400 f90fe: ERROR $MAIN, File = f.f95, Line = 44, Column = 13
*    Oops!
*    
* PGFTN-S-0034-Syntax error at or near :: (life_f.f: 19)
*
* @author Craig Rasmussen
*/
final public class PGIErrorParser implements IErrorParser
{
    /*
     * Regex notes:
     *     \S matches any non-whitespace character
     *     \d matches [0-9]
     *     \w matches [A-Za-z_0-9]
     *     Parentheses define a capturing group
     */
    private Pattern lineError = Pattern.compile("PGF\\w\\w-S-\\d+-([a-zA-Z ]+):* [(](\\S+): (\\d+)[)]");
    private Pattern nolineError = Pattern.compile("PGF\\w\\w-S-\\d+-([a-zA-Z, ]+)[(](\\S+)[)]");

    public boolean processLine(String thisLine, ErrorParserManager eoParser)
    {
        //String test = "PGFTN-S-0034-Syntax error at or near :: (life_f.f: 19)"
        String test = "PGF90-S-0038-Symbol, junk, has not been explicitly declared (life_f.f90)";
        boolean match = Pattern.matches("PGF\\w\\w-S-\\d+-([a-zA-Z, ]+)[(](\\S+)[)]", test);
        
        String errorMessage = null, filename = null;
        boolean errorFound = false;
        int lineNum = 0;
        
        Matcher m = lineError.matcher(thisLine);
        if (m.matches()) {
        	errorMessage = m.group(1);
        	filename = m.group(2);
            lineNum = Integer.parseInt(m.group(3));
            errorFound = true;
        } else {
        	m = nolineError.matcher(thisLine);
        	if (m.matches()) {
            	errorMessage = m.group(1);
            	filename = m.group(2);
            	lineNum = 1;			//TODO - should be end of file?
            	errorFound = true;
        	}
        }
        
        if (errorFound) {
        	IFile file = eoParser.findFilePath(filename);
            eoParser.generateMarker(file, lineNum, errorMessage, IMarkerGenerator.SEVERITY_ERROR_RESOURCE, null);
        }

        return errorFound;
    }
}