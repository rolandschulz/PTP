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
package org.eclipse.photran.internal.core.lexer;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.core.resources.IFile;

/**
 * Fixed form wrapper for {@link FreeFormLexerPhase2}.
 * 
 * @author Dirk Rossow
 * 
 * @see FixedFormLexerPhase1
 * @see FreeFormLexerPhase2
 */
public class FixedFormLexerPhase2 implements ILexer
{
    private FreeFormLexerPhase2 freeLexer2;

    private IToken nextToken = null;

    public FixedFormLexerPhase2(Reader in, IFile file, String filename)
    {
        final Reader input = new LineAppendingReader(in);
        final FixedFormLexerPrepass prepass = new FixedFormLexerPrepass(input);
        Reader prepassReader = new SingleCharReader()
        {
            @Override
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
            
            @Override
            public void close() throws IOException
            {
                input.close();
            }
        };

        FixedFormLexerPhase1 fixedLexer1 = new FixedFormLexerPhase1(prepassReader, prepass, file, filename);
        freeLexer2 = new FreeFormLexerPhase2(fixedLexer1)
        {
            @Override
            protected void modifyPreprocessorDirective(IToken t)
            {
                IPreprocessorReplacement ppr = t.getPreprocessorDirective();
                if(ppr != null && ppr instanceof FixedFormReplacement)
                {
                    FixedFormReplacement ffr = (FixedFormReplacement)ppr;
                    String replStr = ffr.toString();
                    replStr = replStr.replaceAll("=", "");
                    ffr.setReplacementText(replStr);
                }
            }
        };
    }

    public IToken yylex() throws IOException, LexerException
    {
        if (nextToken == null) nextToken = freeLexer2.yylex();
        IToken t = nextToken;
        nextToken = freeLexer2.yylex();

        if (t.getTerminal() == Terminal.T_IDENT)
        {
            while ((nextToken.getTerminal() == Terminal.T_IDENT)
                || (nextToken.getTerminal() == Terminal.T_ICON)
                || (nextToken.getTerminal() == Terminal.T_UNDERSCORE))
            {
                //t.setEndLine(nextToken.getEndLine());
                //t.setEndCol(nextToken.getEndCol());
                t.setText(t.getText() + nextToken.getText());
                //t.setLength(t.getTokenText().length());
                nextToken = freeLexer2.yylex();
            }
        }
        return t;
    }

    public String getFilename()
    {
        return freeLexer2.getFilename();
    }
    
    public int getLastTokenLine()
    {
        return freeLexer2.getLastTokenLine();
    }

    public int getLastTokenCol()
    {
        return freeLexer2.getLastTokenCol();
    }
    
    public FileOrIFile getLastTokenFile()
    {
        return freeLexer2.getLastTokenFile();
    }
    
    public int getLastTokenFileOffset()
    {
        return freeLexer2.getLastTokenFileOffset();
    }
    
    public int getLastTokenStreamOffset()
    {
        return freeLexer2.getLastTokenStreamOffset();
    }
    
    public int getLastTokenLength()
    {
        return freeLexer2.getLastTokenLength();
    }

    public void setTokenAsCurrent(IToken token)
    {
        throw new UnsupportedOperationException();
    }
}
