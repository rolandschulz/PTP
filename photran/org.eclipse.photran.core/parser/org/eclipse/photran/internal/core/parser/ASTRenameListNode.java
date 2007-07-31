package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTRenameListNode extends InteriorNode
{
    protected int count = -1;

    ASTRenameListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTRenameListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTRenameListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTRenameListNode recurseToIndex(int listIndex)
    {
        ASTRenameListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTRenameListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTRenameListNode(this);
    }

    public ASTRenameNode getRename(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTRenameListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.RENAME_LIST_904)
            return (ASTRenameNode)node.getChild(0);
        else if (node.getProduction() == Production.RENAME_LIST_905)
            return (ASTRenameNode)node.getChild(2);
        else
            return null;
    }

    private ASTRenameListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RENAME_LIST_905)
            return (ASTRenameListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTRenameListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.RENAME_LIST_905)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
