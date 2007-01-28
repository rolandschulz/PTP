package org.eclipse.photran.core;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.AbstractParseTreeNode;
import org.eclipse.photran.internal.core.parser.ParseTreeNode;

public class SourceEditor
{
    public static AbstractParseTreeNode cut(AbstractParseTreeNode subtree)
    {
        return subtree.getParent().removeChild(subtree) == true ? subtree : null;
    }

    public static void paste(AbstractParseTreeNode subtreeToPaste, AbstractParseTreeNode pasteBefore, ASTExecutableProgramNode entireAST, boolean reindent)
    {
        ParseTreeNode parent = pasteBefore.getParent();
        parent.addChild(parent.getChildren().indexOf(pasteBefore), subtreeToPaste);
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
}
