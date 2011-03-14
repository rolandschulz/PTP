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
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

/**
 * AST visitor implementing the {@link Strategy#REINDENT_EACH_LINE} reindentation strategy.
 * 
 * @author Jeff Overbey
 * @author Esfar Huq
 * @author Rui Wang
 */
final class ReindentEachLineVisitor extends ReindentingVisitor
{   
    private StartOfLine previousLine;
    private String previousIndentation;
    
    protected ReindentEachLineVisitor(IFortranAST ast, Token firstTokenInRegion, Token lastTokenInRegion)
    {
        super(ast, firstTokenInRegion, lastTokenInRegion);
        
        this.previousLine = StartOfLine.createForLastNonemptyLineAbove(getFirstLineToReindent(), this.ast);
        if (this.previousLine == null)
            this.previousIndentation = ""; //$NON-NLS-1$
        else
            this.previousIndentation = previousLine.getIndentation();
    }

    @Override protected void updateIndentation(StartOfLine currentLine)
    {
        final String currentIndentation = currentLine.getIndentation();
        final String newIndentation = computeNewIndentation(currentLine);

        currentLine.reindent(currentIndentation, newIndentation);
        
        this.previousLine = currentLine;
        // Lines with labels tend to have atypical indentation (heuristically),
        // so try to avoid using them to compute the next line's indentation
        // (unless we're the first line in the file)
        if (!currentLine.hasLabel() || previousLine == null)
            this.previousIndentation = currentLine.getIndentation();
    }

    private String computeNewIndentation(StartOfLine currentLine)
    {
        if (previousLine == null) // currentLine is the line in the file
            return ""; //$NON-NLS-1$
        else if (previousLine.startsIndentedRegion() && !currentLine.endsIndentedRegion())
            return StartOfLine.getIncreasedIndentation(previousIndentation);
        else if (currentLine.endsIndentedRegion() && !previousLine.startsIndentedRegion())
            return StartOfLine.getDecreasedIndentation(previousIndentation);
        else
            return previousIndentation;
    }
}