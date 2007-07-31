package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEntityDeclListNode extends InteriorNode
{
    protected int count = -1;

    ASTEntityDeclListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTEntityDeclListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTEntityDeclListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTEntityDeclListNode recurseToIndex(int listIndex)
    {
        ASTEntityDeclListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTEntityDeclListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEntityDeclListNode(this);
    }

    public ASTEntityDeclNode getEntityDecl(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTEntityDeclListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ENTITY_DECL_LIST_258)
            return (ASTEntityDeclNode)node.getChild(0);
        else if (node.getProduction() == Production.ENTITY_DECL_LIST_259)
            return (ASTEntityDeclNode)node.getChild(2);
        else
            return null;
    }

    private ASTEntityDeclListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ENTITY_DECL_LIST_259)
            return (ASTEntityDeclListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTEntityDeclListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ENTITY_DECL_LIST_259)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
