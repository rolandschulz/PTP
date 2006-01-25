package org.eclipse.photran.internal.core.f95modelparser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.photran.internal.core.f95modelparser.Parser;
import org.eclipse.photran.internal.core.f95modelparser.Terminal;
import org.eclipse.photran.internal.core.f95modelparser.Token;

/**
 * This is the lexical analyzer that is used directly in the Fortran parser.
 * 
 * @author Dirk Rossow
 * 
 * @see FixedFormLexerPhase1
 * @see FreeFormLexerPhase2
 * @see Parser
 */
class FixedFormLexerPhase2 implements ILexer
{
    private FreeFormLexerPhase2 freeLexer2;

    private Token nextToken = null;

    public FixedFormLexerPhase2(InputStream in, String filename)
    {
        final FixedFormLexerPrepass prepass = new FixedFormLexerPrepass(in);
        InputStream prepassReader = new InputStream()
        {
            public int read() throws IOException
            {
                try
                {
                    return prepass.read();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return -1;
                }
            }
        };

        FixedFormLexerPhase1 phase1 = new FixedFormLexerPhase1(prepassReader, prepass);
        freeLexer2 = new FreeFormLexerPhase2(phase1, filename);
    }

    public Token yylex() throws Exception
    {
        if (nextToken == null) nextToken = freeLexer2.yylex();
        Token t = nextToken;
        nextToken = freeLexer2.yylex();

        if (t.getTerminal() == Terminal.T_IDENT)
        {
            while ((nextToken.getTerminal() == Terminal.T_IDENT)
                || (nextToken.getTerminal() == Terminal.T_ICON)
                || (nextToken.getTerminal() == Terminal.T_UNDERSCORE))
            {
                t.setEndLine(nextToken.getEndLine());
                t.setEndCol(nextToken.getEndCol());
                t.setText(t.getText() + nextToken.getText());
                t.setLength(t.getText().length());
                nextToken = freeLexer2.yylex();
            }
        }
        return t;
    }

    public void setFilename(String filename)
    {
        freeLexer2.setFilename(filename);
    }
    
    public List/*<NonTreeToken>*/ getNonTreeTokens()
    {
        return freeLexer2.getNonTreeTokens();
    }
}
