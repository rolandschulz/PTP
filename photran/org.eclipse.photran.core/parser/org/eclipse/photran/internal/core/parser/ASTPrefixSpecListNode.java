package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTPrefixSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTPrefixSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTPrefixSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTPrefixSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTPrefixSpecListNode recurseToIndex(int listIndex)
    {
        ASTPrefixSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTPrefixSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPrefixSpecListNode(this);
    }

    public ASTPrefixSpecNode getPrefixSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPrefixSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.PREFIX_SPEC_LIST_984)
            return (ASTPrefixSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.PREFIX_SPEC_LIST_985)
            return (ASTPrefixSpecNode)node.getChild(1);
        else
            return null;
    }

    private ASTPrefixSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PREFIX_SPEC_LIST_985)
            return (ASTPrefixSpecListNode)getChild(0);
        else
            return null;
    }
}
