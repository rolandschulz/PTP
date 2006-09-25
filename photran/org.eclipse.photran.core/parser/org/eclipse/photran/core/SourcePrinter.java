package org.eclipse.photran.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

public class SourcePrinter
{
    private static final String EOL = System.getProperty("line.separator");
    
    private SourcePrinter() {;}
    
    public static String getSourceCodeFromAST(IFortranAST ast)
    {
        ASTExecutableProgramNode root = (ASTExecutableProgramNode)ast;
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        root.printOn(new PrintStream(out));
        String result = out.toString();
        // When we read in the AST, we use a LineAppendingInputStream so that the
        // user does not have to have a final carriage return in their file.  However,
        // we should chop that off here.
        result = result.substring(0, result.length() - EOL.length());
        return result;
    }
}
