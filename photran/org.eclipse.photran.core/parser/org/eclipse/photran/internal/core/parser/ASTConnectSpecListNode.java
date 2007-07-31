package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTConnectSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTConnectSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTConnectSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTConnectSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTConnectSpecListNode recurseToIndex(int listIndex)
    {
        ASTConnectSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTConnectSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTConnectSpecListNode(this);
    }

    public ASTUnitIdentifierNode getUnitIdentifier(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTConnectSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.CONNECT_SPEC_LIST_740)
            return (ASTUnitIdentifierNode)node.getChild(0);
        else
            return null;
    }

    public ASTConnectSpecNode getConnectSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTConnectSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.CONNECT_SPEC_LIST_741)
            return (ASTConnectSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.CONNECT_SPEC_LIST_742)
            return (ASTConnectSpecNode)node.getChild(2);
        else
            return null;
    }

    private ASTConnectSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONNECT_SPEC_LIST_742)
            return (ASTConnectSpecListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTConnectSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.CONNECT_SPEC_LIST_742)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
