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
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.lexer.IPreprocessorReplacement;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;

/**
 * Traverses the tokens in an AST, using the text stored in each token to update its line, column,
 * and file offset information.
 * <p>
 * This does <i>not</i> update stream offset information (this allows
 * {@link PhotranTokenRef#findToken()} to continue to work correctly).
 * 
 * @author Jeff Overbey
 * 
 * @see IFortranAST#recomputeLineColInfo()
 */
final class LineColComputer extends ASTVisitorWithLoops
{
    private int fileOffset = 0;
    private int col = 1;
    private int line = 1;
    private IPreprocessorReplacement lastPreprocRepl = null;

    @Override public void visitToken(Token token)
    {
        IPreprocessorReplacement thisPreprocRepl = token.getPreprocessorDirective();

        // This method's structure is similar to Token#printOn
        if (thisPreprocRepl != lastPreprocRepl)
        {
            if (thisPreprocRepl != null)
            {
                updateLineColAndOffset(token.getWhiteBefore());
                token.setFileOffset(fileOffset);
                token.setLine(line);
                token.setCol(col);
                updateLineColAndOffset(thisPreprocRepl.toString());
            }
            lastPreprocRepl = thisPreprocRepl;
        }

        if (thisPreprocRepl == null)
        {
            updateLineColAndOffset(token.getWhiteBefore());
            token.setFileOffset(fileOffset);
            token.setLine(line);
            token.setCol(col);
            updateLineColAndOffset(token.getText());
            updateLineColAndOffset(token.getWhiteAfter());
        }
    }

    private void updateLineColAndOffset(String s)
    {
        for (int i = 0, len = s.length(); i < len; i++)
        {
            fileOffset++;
            
            if (s.charAt(i) == '\n')
            {
                line++;
                col = 1;
            }
            else col++;
        }
    }

//    /**
//     * @return the (1-based) line number of the last token in the file (assuming that this visitor
//     * has already traversed the AST)
//     */
//    public int lineNumberOfLastToken()
//    {
//        return line;
//    }
}