package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTImplicitSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTImplicitSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTImplicitSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTImplicitSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTImplicitSpecListNode recurseToIndex(int listIndex)
    {
        ASTImplicitSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTImplicitSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTImplicitSpecListNode(this);
    }

    public ASTImplicitSpecNode getImplicitSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTImplicitSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.IMPLICIT_SPEC_LIST_393)
            return (ASTImplicitSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.IMPLICIT_SPEC_LIST_394)
            return (ASTImplicitSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTImplicitSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IMPLICIT_SPEC_LIST_394)
            return (ASTImplicitSpecListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTImplicitSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.IMPLICIT_SPEC_LIST_394)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
