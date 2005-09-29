package org.eclipse.photran.internal.errorparsers;

import java.util.StringTokenizer;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.core.resources.IFile;

/**
 * Intel Fortran 8.1 error parser
 * 
 * @author joverbey
 */
public class IntelFortranErrorParser implements IErrorParser
{
    /**
     * Extracts file, line number, and error information from lines of the form
     * fortcom: Error: test.f90, line 3: Message
     */
    public boolean processLine(String line, ErrorParserManager eoParser)
    {
        String fortcom, severitystr, filestr, linestr, message;

        StringTokenizer tokenizer = new StringTokenizer(line, ":");
        if (line.startsWith("fortcom: "))
        {
            try
            {
                fortcom = tokenizer.nextToken(); // fortcom
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