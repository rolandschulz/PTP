package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExternalNameListNode extends InteriorNode
{
    protected int count = -1;

    ASTExternalNameListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTExternalNameListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTExternalNameListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTExternalNameListNode recurseToIndex(int listIndex)
    {
        ASTExternalNameListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTExternalNameListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExternalNameListNode(this);
    }

    public ASTExternalNameNode getExternalName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTExternalNameListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.EXTERNAL_NAME_LIST_952)
            return (ASTExternalNameNode)node.getChild(0);
        else if (node.getProduction() == Production.EXTERNAL_NAME_LIST_953)
            return (ASTExternalNameNode)node.getChild(2);
        else
            return null;
    }

    private ASTExternalNameListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXTERNAL_NAME_LIST_953)
            return (ASTExternalNameListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTExternalNameListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.EXTERNAL_NAME_LIST_953)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
