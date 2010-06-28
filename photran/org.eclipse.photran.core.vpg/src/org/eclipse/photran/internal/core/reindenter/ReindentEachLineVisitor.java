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
    
    protected ReindentEachLineVisitor(IFortranAST ast, Token firstTokenInRegion, Token lastTokenInRegion)
    {
        super(ast, firstTokenInRegion, lastTokenInRegion);
        
        this.previousLine = StartOfLine.createForLastNonemptyLineAbove(getFirstLineToReindent(), this.ast);
    }

    @Override protected void updateIndentation(StartOfLine currentLine)
    {
        final String currentIndentation = currentLine.getIndentation();
        final String newIndentation = computeNewIndentation(currentLine);

        currentLine.reindent(currentIndentation, newIndentation);
        
        this.previousLine = currentLine;
    }

    private String computeNewIndentation(StartOfLine currentLine)
    {
        if (previousLine == null) // currentLine is the line in the file
            return ""; //$NON-NLS-1$
        else if (previousLine.startsIndentedRegion() && !currentLine.endsIndentedRegion())
            return previousLine.getIncreasedIndentation();
        else if (currentLine.endsIndentedRegion() && !previousLine.startsIndentedRegion())
            return previousLine.getDecreasedIndentation();
        else
            return previousLine.getIndentation();
    }
}