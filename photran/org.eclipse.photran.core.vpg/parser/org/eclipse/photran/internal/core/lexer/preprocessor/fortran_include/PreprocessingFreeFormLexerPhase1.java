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
package org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.ASTTokenFactory;
import org.eclipse.photran.internal.core.lexer.FreeFormLexerPhase1;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.LineAppendingInputStream;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenFactory;

/**
 * Phase 1 lexer that handles Fortran INCLUDE directives.
 * <p>
 * This class feeds {@link FreeFormLexerPhase1} with a {@link FortranPreprocessor},
 * an {@link InputStream} that expands Fortran INCLUDE lines.
 * {@link FreeFormLexerPhase1} tokenizes the preprocessed stream.  However, this
 * class subclasses {@link FreeFormLexerPhase1} and overrides its tokenization
 * method ({@link #yylex()}), so when it produces a token, it can reset the
 * filename, line number, file offset, and guarding preprocessor directive
 * (INCLUDE line) correctly. 
 * 
 * @author Jeff Overbey
 */
public class PreprocessingFreeFormLexerPhase1 extends FreeFormLexerPhase1
{
    private FortranPreprocessor preprocessor;
    private String lastDirective;

    public PreprocessingFreeFormLexerPhase1(InputStream in, IFile file, String filename, IncludeLoaderCallback callback, boolean accumulateWhitetext) throws IOException
    {
        this(new FortranPreprocessor(new LineAppendingInputStream(in), file, filename, callback), file, filename, ASTTokenFactory.getInstance(), accumulateWhitetext);
    }
    
    // This would not be here if we could assign the preprocessor to a variable in the above ctor (grrr)
    private PreprocessingFreeFormLexerPhase1(FortranPreprocessor preprocessor, IFile file, String filename, TokenFactory tokenFactory, boolean accumulateWhitetext)
    {
        super(preprocessor, file, filename, tokenFactory, accumulateWhitetext);
        this.preprocessor = preprocessor;
        this.lastDirective = null;
    }

    public Token yylex() throws IOException, LexerException
    {
        Token token = (Token)super.yylex();
        
        String thisDirective = preprocessor.getDirectiveAtOffset(lastTokenStreamOffset);
        if (thisDirective != null && lastDirective == null)
            adjustWhiteBeforeAtIncludeStart(token);
        else if (thisDirective == null && lastDirective != null)
            adjustWhiteBeforeAtIncludeEnd(token);
        token.setPreprocessorDirective(thisDirective);
        lastDirective = thisDirective;
        
        this.lastTokenFileOffset = preprocessor.getFileOffsetFromStreamOffset(this.lastTokenStreamOffset);
        this.lastTokenLine = preprocessor.getFileLineFromStreamLine(this.lastTokenLine);
        
        //System.err.println(token.toString().replaceAll("\n","") + " - line " + this.lastTokenLine + " - " + token.getPreprocessorDirective());
        return token;
    }

    /**
     * In a case such as
     * <pre>
     * FILE.F90               INC.FH
     * ===================    ===================
     * program p              ! Comment B
     *   ! Comment A          integer :: i
     *   include "inc.fh"
     * end program p
     * </pre>
     * we need to adjust the <code>whiteBefore</code>
     * property of the first token in inc.fh to contain
     * only Comment A (from file.f90).  The source printer
     * will print the white text of the first token in
     * the included file along with the INCLUDE line,
     * so it should not contain Comment B.
     * @param token
     */
    private void adjustWhiteBeforeAtIncludeStart(Token token)
    {
        int fileStartOffset = preprocessor.getStartOffsetOfFileContainingStreamOffset(this.lastTokenStreamOffset);
        int tokenTextStartOffset = this.lastTokenStreamOffset;
        
        String whiteBefore = token.getWhiteBefore();
        int charsFromIncludeFile = tokenTextStartOffset - fileStartOffset;
        if (charsFromIncludeFile > 0)
        {
            int charsToKeep = whiteBefore.length() - charsFromIncludeFile;
            charsToKeep = Math.max(charsToKeep, 0); // TODO: Why is whiteBefore "" when it shouldn't be?
            token.setWhiteBefore(whiteBefore.substring(0, charsToKeep));
        }
    }

    /**
     * In a case such as
     * <pre>
     * FILE.F90               INC.FH
     * ===================    ===================
     * program p              integer :: i
     *   include "inc.fh"     ! Comment B
     *   ! Comment A
     * end program p
     * </pre>
     * we need to adjust the <code>whiteBefore</code>
     * property of the &quot;end&quot; token in file.f90 to contain
     * only Comment A (from file.f90).
     * @param token
     */
    private void adjustWhiteBeforeAtIncludeEnd(Token token)
    {
        int fileStartOffset = preprocessor.getStartOffsetOfFileContainingStreamOffset(this.lastTokenStreamOffset);
        int tokenTextStartOffset = this.lastTokenStreamOffset;
        
        String whiteBefore = token.getWhiteBefore();
        int charsFromRealFile = tokenTextStartOffset - fileStartOffset;
        int charsToTrim = whiteBefore.length() - charsFromRealFile;
        charsToTrim = Math.max(charsToTrim, 0); // TODO: Why does this go negative?
        token.setWhiteBefore(whiteBefore.substring(charsToTrim));
    }

    public void yypushback(int number)
    {
        throw new UnsupportedOperationException();
    }
}
