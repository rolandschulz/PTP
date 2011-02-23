/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.reindenter;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

/**
 * The Reindenter is used to correct indentation when a node is inserted or
 * moved in an AST so that the affected lines are correctly indented in their
 * new context.
 *  
 * @author Jeff Overbey
 */
/*
 * This code is very specific to the way Photran's parser uses the whiteBefore and
 * whiteAfter parts of a Token; it will not generalize to parsers that, say, include
 * newlines in the whitetext.
 */
public final class Reindenter
{
    public static enum Strategy
    {
        /**
         * Shifts the entire region left or right to match its new surroundings, keeping
         * the relative indentation of each line the same.
         * <p>
         * The algorithm is as follows:
         * <ul>
         *   <li> Usually, the reindented region is adjusted to match the indentation of
         *        the next non-empty line following the region.
         *   <li> However, if the next non-empty line starts with "END" (or something),
         *        the reindented region is adjusted to match the indentation of
         *        the last non-empty line above the region.
         *   <li> However, if the last non-empty line above starts with a token like
         *        "PROGRAM" or "IF," then a guess is made, and the indentation is set
         *        to match the following line, but four spaces are added.
         * </ul>
         */
        SHIFT_ENTIRE_BLOCK
        {
            @Override protected ReindentingVisitor createVisitor(IFortranAST ast, Token firstTokenInRegion, Token lastTokenInRegion)
            {
                return new ShiftBlockVisitor(ast, firstTokenInRegion, lastTokenInRegion);
            }
        },
        
        /**
         * Shifts each lines in the region left or right to match its new surroundings.
         * <p>
         * The algorithm is as follows:
         * <ul>
         *   <li> Initially, the line is indented the same as the previous line.
         *   <li> If the preceding line starts with PROGRAM, IF, DO, etc., the line is
         *        indented an additional 4 spaces.
         *   <li> If the line starts with END, it is unindented 4 spaces.
         * </ul>
         */
        REINDENT_EACH_LINE
        {
            @Override protected ReindentingVisitor createVisitor(IFortranAST ast, Token firstTokenInRegion, Token lastTokenInRegion)
            {
                return new ReindentEachLineVisitor(ast, firstTokenInRegion, lastTokenInRegion);
            }
        };
        
        protected abstract ReindentingVisitor createVisitor(IFortranAST ast, Token firstTokenInRegion, Token lastTokenInRegion);
    }
    
    public static void reindent(IASTNode node, IFortranAST ast)
    {
        reindent(node, ast, Strategy.SHIFT_ENTIRE_BLOCK);
    }

    public static void reindent(IASTNode node, IFortranAST ast, Strategy strategy)
    {
        reindent(node.findFirstToken(), node.findLastToken(), ast, strategy);
    }

    public static void reindent(Token firstTokenInAffectedNode, Token lastTokenInAffectedNode, IFortranAST ast)
    {
        reindent(firstTokenInAffectedNode, lastTokenInAffectedNode, ast, Strategy.SHIFT_ENTIRE_BLOCK);
    }

    public static void reindent(int fromLine, int thruLine, IFortranAST ast)
    {
        reindent(fromLine, thruLine, ast, Strategy.SHIFT_ENTIRE_BLOCK);
    }

    public static void reindent(int fromLine, int thruLine, IFortranAST ast, Strategy strategy)
    {
        reindent(ast.findFirstTokenOnOrAfterLine(fromLine), ast.findLastTokenOnOrBeforeLine(thruLine), ast, strategy);
    }
    
    private static void reindent(Token firstTokenInRegion, Token lastTokenInRegion, IFortranAST ast, Strategy strategy)
    {
        if (firstTokenInRegion != null && lastTokenInRegion != null)
            ast.accept(strategy.createVisitor(ast, firstTokenInRegion, lastTokenInRegion));
    }
    
    public static String defaultIndentation()
    {
        return FortranPreferences.TAB_WIDTH.getStringOfSpaces();
    }
    
    private Reindenter() {}
}