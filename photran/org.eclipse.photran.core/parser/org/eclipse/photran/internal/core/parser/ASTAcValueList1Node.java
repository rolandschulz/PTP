package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAcValueList1Node extends InteriorNode
{
    protected int count = -1;

    ASTAcValueList1Node(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTAcValueList1Node nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTAcValueList1Node node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTAcValueList1Node recurseToIndex(int listIndex)
    {
        ASTAcValueList1Node node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTAcValueList1Node)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAcValueList1Node(this);
    }

    public ASTExprNode getExpr(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_220)
            return (ASTExprNode)node.getChild(0);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_221)
            return (ASTExprNode)node.getChild(0);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_223)
            return (ASTExprNode)node.getChild(2);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_220)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_221)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_223)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_224)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTExprNode getExpr2(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_220)
            return (ASTExprNode)node.getChild(2);
        else
            return null;
    }

    public ASTAcImpliedDoNode getAcImpliedDo(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_221)
            return (ASTAcImpliedDoNode)node.getChild(2);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_222)
            return (ASTAcImpliedDoNode)node.getChild(0);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_224)
            return (ASTAcImpliedDoNode)node.getChild(2);
        else
            return null;
    }

    private ASTAcValueList1Node getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_VALUE_LIST_1_223)
            return (ASTAcValueList1Node)getChild(0);
        else if (getProduction() == Production.AC_VALUE_LIST_1_224)
            return (ASTAcValueList1Node)getChild(0);
        else
            return null;
    }
}
