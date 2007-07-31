package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAccessIdListNode extends InteriorNode
{
    protected int count = -1;

    ASTAccessIdListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTAccessIdListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTAccessIdListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTAccessIdListNode recurseToIndex(int listIndex)
    {
        ASTAccessIdListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTAccessIdListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAccessIdListNode(this);
    }

    public ASTAccessIdNode getAccessId(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAccessIdListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ACCESS_ID_LIST_327)
            return (ASTAccessIdNode)node.getChild(0);
        else if (node.getProduction() == Production.ACCESS_ID_LIST_328)
            return (ASTAccessIdNode)node.getChild(2);
        else
            return null;
    }

    private ASTAccessIdListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ACCESS_ID_LIST_328)
            return (ASTAccessIdListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAccessIdListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ACCESS_ID_LIST_328)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
