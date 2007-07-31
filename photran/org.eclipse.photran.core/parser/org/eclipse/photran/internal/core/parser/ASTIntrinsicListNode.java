package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTIntrinsicListNode extends InteriorNode
{
    protected int count = -1;

    ASTIntrinsicListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTIntrinsicListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTIntrinsicListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTIntrinsicListNode recurseToIndex(int listIndex)
    {
        ASTIntrinsicListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTIntrinsicListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTIntrinsicListNode(this);
    }

    public ASTIntrinsicProcedureNameNode getIntrinsicProcedureName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTIntrinsicListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.INTRINSIC_LIST_956)
            return (ASTIntrinsicProcedureNameNode)node.getChild(0);
        else if (node.getProduction() == Production.INTRINSIC_LIST_957)
            return (ASTIntrinsicProcedureNameNode)node.getChild(2);
        else
            return null;
    }

    private ASTIntrinsicListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTRINSIC_LIST_957)
            return (ASTIntrinsicListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTIntrinsicListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.INTRINSIC_LIST_957)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
