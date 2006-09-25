package org.eclipse.photran.core;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.ILexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.parser.Productions;

/**
 * A collection of (static) factory methods for creating Fortran parsers.
 * 
 * @author joverbey
 */
public class ASTFactory
{
    private ASTFactory() {;}
    
    public static IFortranAST buildAST(ILexer lexer) throws Exception
    {
        return (IFortranAST)new Parser().parse(lexer, Productions.getInstance());
    }
    
    public static IFortranAST buildAST(InputStream in, String filename, int lexerOptions) throws Exception
    {
        return buildAST(LexerFactory.createLexer(in, filename, lexerOptions));
    }
    
    public static IFortranAST buildAST(File file, int lexerOptions) throws Exception
    {
        return buildAST(LexerFactory.createLexer(file, lexerOptions));
    }
    
    public static IFortranAST buildAST(IFile file, int lexerOptions) throws Exception
    {
        return buildAST(LexerFactory.createLexer(file, lexerOptions));
    }
}
