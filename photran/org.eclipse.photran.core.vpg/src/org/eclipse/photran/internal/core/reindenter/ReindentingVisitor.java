/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.reindenter;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.FortranAST;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;

/**
 * Base class for an AST visitor implementing a reindentation strategy.
 * 
 * @author Jeff Overbey
 */
abstract class ReindentingVisitor extends ASTVisitorWithLoops
{
    protected final IFortranAST ast;
    protected final Token firstTokenInRegion;
    protected final Token lastTokenInRegion;

    private boolean inFormatRegion = false;
    private Token previousToken = null;

    protected ReindentingVisitor(IFortranAST ast, Token firstTokenInRegion, Token lastTokenInRegion)
    {
        this.ast = recomputeLineColInfo(ast);
        this.lastTokenInRegion = lastTokenInRegion;
        this.firstTokenInRegion = firstTokenInRegion;
    }

    /**
     * Recomputes the AST's {@link TokenList}, as well as the line, column, and file offset
     * information for each token. This is needed for line number-based searches to be correct.
     * <p>
     * This does <i>not</i> update the stream offset information for each token, and it returns a
     * new {@link FortranAST} rather than modifying the existing one in-place. This allows
     * {@link PhotranTokenRef#findToken()} to continue to work correctly.
     */
    /*
     * A lengthier explanation:
     * PhotranTokenRef#findToken() uses stream offset information (but not file offset, line, or
     * column info) and looks it up by performing a binary search on the {@link TokenList}. So by
     * not modifying the stream offset information and by not modifying the existing {@link
     * TokenList}, it will continue to work correctly.)
     */
    private IFortranAST recomputeLineColInfo(IFortranAST ast)
    {
        ast.accept(new LineColComputer());
        return new FortranAST(ast.getFile(), ast.getRoot(), new TokenList(ast.getRoot()));
    }

    protected StartOfLine getFirstLineToReindent()
    {
        if (isFirstTokenOnLine(firstTokenInRegion))
            return StartOfLine.createForLineStartingWith(firstTokenInRegion);
        else
            return StartOfLine.createForFirstNonemptyLineBelow(firstTokenInRegion, ast);
    }

    private boolean isFirstTokenOnLine(Token token)
    {
        return token == ast.findFirstTokenOnLine(token.getLine());
    }

    @Override public void visitToken(Token token)
    {
    	if (token == firstTokenInRegion)
            inFormatRegion = true;
        else if (token == lastTokenInRegion)
            inFormatRegion = false;

        if (inFormatRegion && (previousToken == null || token.getLine() > previousToken.getLine()))
            updateIndentation(StartOfLine.createForLineStartingWith(token));

        previousToken = token;
    }

    /** Callback method that updates the indentation of the source line beginning with the given {@link StartOfLine}. */
    protected abstract void updateIndentation(StartOfLine linePrefix);
}