package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFunctionArgListNode extends InteriorNode
{
    protected int count = -1;

    ASTFunctionArgListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTFunctionArgListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTFunctionArgListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTFunctionArgListNode recurseToIndex(int listIndex)
    {
        ASTFunctionArgListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTFunctionArgListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFunctionArgListNode(this);
    }

    public ASTFunctionArgNode getFunctionArg(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFunctionArgListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FUNCTION_ARG_LIST_965)
            return (ASTFunctionArgNode)node.getChild(0);
        else if (node.getProduction() == Production.FUNCTION_ARG_LIST_966)
            return (ASTFunctionArgNode)node.getChild(2);
        else if (node.getProduction() == Production.FUNCTION_ARG_LIST_967)
            return (ASTFunctionArgNode)node.getChild(2);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFunctionArgListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FUNCTION_ARG_LIST_966)
            return (ASTSectionSubscriptListNode)node.getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTFunctionArgListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.FUNCTION_ARG_LIST_966)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.FUNCTION_ARG_LIST_967)
            return (Token)node.getChild(1);
        else
            return null;
    }

    private ASTFunctionArgListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FUNCTION_ARG_LIST_967)
            return (ASTFunctionArgListNode)getChild(0);
        else
            return null;
    }
}
