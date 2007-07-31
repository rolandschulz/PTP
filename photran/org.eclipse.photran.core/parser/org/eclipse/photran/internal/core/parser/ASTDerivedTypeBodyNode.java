package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDerivedTypeBodyNode extends InteriorNode
{
    protected int count = -1;

    ASTDerivedTypeBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTDerivedTypeBodyNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTDerivedTypeBodyNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTDerivedTypeBodyNode recurseToIndex(int listIndex)
    {
        ASTDerivedTypeBodyNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTDerivedTypeBodyNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeBodyNode(this);
    }

    public ASTDerivedTypeBodyConstructNode getDerivedTypeBodyConstruct(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDerivedTypeBodyNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DERIVED_TYPE_BODY_180)
            return (ASTDerivedTypeBodyConstructNode)node.getChild(0);
        else if (node.getProduction() == Production.DERIVED_TYPE_BODY_181)
            return (ASTDerivedTypeBodyConstructNode)node.getChild(1);
        else
            return null;
    }

    private ASTDerivedTypeBodyNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_BODY_181)
            return (ASTDerivedTypeBodyNode)getChild(0);
        else
            return null;
    }
}
