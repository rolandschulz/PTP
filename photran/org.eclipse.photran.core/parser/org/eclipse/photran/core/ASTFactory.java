package org.eclipse.photran.core;

import java.io.File;
import java.io.InputStream;

import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;

/**
 * A collection of (static) factory methods for creating Fortran parsers.
 * 
 * @author joverbey
 */
public class ASTFactory
{
    private ASTFactory() {;}
    
    public static IFortranAST buildAST(IAccumulatingLexer lexer) throws Exception
    {
        return new FortranAST(FortranCorePlugin.getParser().parse(lexer), lexer.getTokenList());
    }
    
    public static IFortranAST buildAST(InputStream in, String filename, SourceForm sourceForm) throws Exception
    {
        return buildAST(LexerFactory.createLexer(in, filename, sourceForm));
    }
    
    public static IFortranAST buildAST(File file, SourceForm sourceForm) throws Exception
    {
        return buildAST(LexerFactory.createLexer(file, sourceForm));
    }
}
