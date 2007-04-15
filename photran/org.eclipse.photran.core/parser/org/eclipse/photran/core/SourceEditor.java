package org.eclipse.photran.core;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTExprNode;
import org.eclipse.photran.internal.core.parser.AbstractParseTreeNode;
import org.eclipse.photran.internal.core.parser.ParseTreeNode;

public class SourceEditor
{
    public static void changeTokenText(Token t, String newText)
    {
        t.setText(newText);
    }
    
    public static AbstractParseTreeNode cut(AbstractParseTreeNode subtree)
    {
        return subtree.getParent().removeChild(subtree) == true ? subtree : null;
    }

    public static void paste(AbstractParseTreeNode subtreeToPaste, AbstractParseTreeNode pasteBefore, ASTExecutableProgramNode entireAST, boolean reindent)
    {
        ParseTreeNode parent = pasteBefore.getParent();
        parent.addChild(parent.findChild(pasteBefore), subtreeToPaste);
        if (reindent) Reindenter.reindent(subtreeToPaste, entireAST);
    }

    public static void pasteAsFirstChild(AbstractParseTreeNode subtreeToPaste, ParseTreeNode pasteUnder, ASTExecutableProgramNode entireAST, boolean reindent)
    {
        pasteUnder.addChild(0, subtreeToPaste);
        if (reindent) Reindenter.reindent(subtreeToPaste, entireAST);
    }

    public static void pasteAsLastChild(AbstractParseTreeNode subtreeToPaste, ParseTreeNode pasteUnder, ASTExecutableProgramNode entireAST, boolean reindent)
    {
        pasteUnder.addChild(subtreeToPaste);
        if (reindent) Reindenter.reindent(subtreeToPaste, entireAST);
    }

    public static boolean replace(AbstractParseTreeNode oldNode, AbstractParseTreeNode newNode)
    {
        ParseTreeNode parent = oldNode.getParent();
        
        int index = parent.findChild(oldNode);
        if (index < 0) return false;

        parent.removeChild(index);
        parent.addChild(index, newNode);
        return true;
    }
}
