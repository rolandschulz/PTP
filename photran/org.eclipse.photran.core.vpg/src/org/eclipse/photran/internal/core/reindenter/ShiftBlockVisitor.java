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
 * AST visitor implementing the {@link Strategy#SHIFT_ENTIRE_BLOCK} reindentation strategy.
 * 
 * @author Jeff Overbey
 */
final class ShiftBlockVisitor extends ReindentingVisitor
{
    private String oldIndentation, newIndentation;

    protected ShiftBlockVisitor(IFortranAST ast, Token firstTokenInRegion, Token lastTokenInRegion)
    {
        super(ast, firstTokenInRegion, lastTokenInRegion);
        
        this.oldIndentation = getFirstLineToReindent().getIndentation();

        if (!setNewIndentationFromFollowingLine())
            if (!setNewIndentationFromPreviousLine(getFirstLineToReindent()))
                setNewIndentationToDefault();
    }

    private boolean setNewIndentationFromFollowingLine()
    {
        StartOfLine lineAfterReindentedRegion = StartOfLine.createForFirstNonemptyLineBelow(lastTokenInRegion.getLine(), this.ast);

        if (lineAfterReindentedRegion == null) return false; // Cannot determine indentation
        
        if (lineAfterReindentedRegion.endsIndentedRegion()) return false; // Use the previous line rather than "guessing"

        this.newIndentation = lineAfterReindentedRegion.getIndentation();
        return true;
    }

    private boolean setNewIndentationFromPreviousLine(StartOfLine firstLineToReindent)
    {
        StartOfLine lineAboveReindentedRegion = StartOfLine.createForLastNonemptyLineAbove(firstLineToReindent, this.ast);
        if (lineAboveReindentedRegion == null) return false; // Cannot determine indentation

        if (lineAboveReindentedRegion.startsIndentedRegion())
            this.newIndentation = lineAboveReindentedRegion.getIncreasedIndentation();
        else
            this.newIndentation = lineAboveReindentedRegion.getIndentation();
        return true;
    }

    private void setNewIndentationToDefault()
    {
        this.newIndentation = ""; //$NON-NLS-1$
    }

    @Override protected void updateIndentation(StartOfLine line)
    {
        line.reindent(oldIndentation, newIndentation);
    }
}