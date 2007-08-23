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
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;

/**
 * Methods to modify an AST.
 * 
 * @author Jeff Overbey
 */
public class SourceEditor
{
    public static void changeTokenText(Token t, String newText)
    {
        t.setText(newText);
    }
    
    public static CSTNode cut(CSTNode subtree)
    {
        return subtree.getParent().removeChild(subtree) == true ? subtree : null;
    }

    public static void paste(CSTNode subtreeToPaste, CSTNode pasteBefore, IFortranAST entireAST, boolean reindent)
    {
        InteriorNode parent = pasteBefore.getParent();
        parent.addChild(parent.findChild(pasteBefore), subtreeToPaste);
        if (reindent) Reindenter.reindent(subtreeToPaste, entireAST);
    }

    public static void pasteAsFirstChild(CSTNode subtreeToPaste, InteriorNode pasteUnder, IFortranAST entireAST, boolean reindent)
    {
        pasteUnder.addChild(0, subtreeToPaste);
        if (reindent) Reindenter.reindent(subtreeToPaste, entireAST);
    }

    public static void pasteAsLastChild(CSTNode subtreeToPaste, InteriorNode pasteUnder, IFortranAST entireAST, boolean reindent)
    {
        pasteUnder.addChild(subtreeToPaste);
        if (reindent) Reindenter.reindent(subtreeToPaste, entireAST);
    }
    
    public static void pasteAfterHeaderStmt(CSTNode subtreeToPaste, ScopingNode forScope, InteriorNode headerStmt, IFortranAST entireAST)
    {
    	if (headerStmt != null)
    		pasteAsLastChild(subtreeToPaste, headerStmt, entireAST, true);
    	else
    		pasteAsFirstChild(subtreeToPaste, forScope, entireAST, true);
    }

    public static boolean replace(CSTNode oldNode, CSTNode newNode)
    {
        InteriorNode parent = oldNode.getParent();
        
        int index = parent.findChild(oldNode);
        if (index < 0) return false;

        parent.removeChild(index);
        parent.addChild(index, newNode);
        return true;
    }
}
