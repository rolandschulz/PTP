package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDataRefNode extends InteriorNode
{
    protected int count = -1;

    ASTDataRefNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTDataRefNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTDataRefNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTDataRefNode recurseToIndex(int listIndex)
    {
        ASTDataRefNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTDataRefNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDataRefNode(this);
    }

    public ASTNameNode getVarName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_428)
            return (ASTNameNode)node.getChild(0);
        else
            return null;
    }

    private ASTDataRefNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_REF_429)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.DATA_REF_430)
            return (ASTDataRefNode)getChild(0);
        else
            return null;
    }

    public Token getTPercent(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_429)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.DATA_REF_430)
            return (Token)node.getChild(4);
        else
            return null;
    }

    public ASTNameNode getComponentName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_429)
            return (ASTNameNode)node.getChild(2);
        else if (node.getProduction() == Production.DATA_REF_430)
            return (ASTNameNode)node.getChild(5);
        else
            return null;
    }

    public Token getTLparen(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_430)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_430)
            return (ASTSectionSubscriptListNode)node.getChild(2);
        else
            return null;
    }

    public Token getTRparen(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_430)
            return (Token)node.getChild(3);
        else
            return null;
    }
}
