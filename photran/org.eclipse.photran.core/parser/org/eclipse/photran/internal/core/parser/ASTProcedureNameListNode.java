package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTProcedureNameListNode extends InteriorNode
{
    protected int count = -1;

    ASTProcedureNameListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTProcedureNameListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTProcedureNameListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTProcedureNameListNode recurseToIndex(int listIndex)
    {
        ASTProcedureNameListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTProcedureNameListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTProcedureNameListNode(this);
    }

    public ASTProcedureNameNode getProcedureName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTProcedureNameListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.PROCEDURE_NAME_LIST_945)
            return (ASTProcedureNameNode)node.getChild(0);
        else if (node.getProduction() == Production.PROCEDURE_NAME_LIST_946)
            return (ASTProcedureNameNode)node.getChild(2);
        else
            return null;
    }

    private ASTProcedureNameListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PROCEDURE_NAME_LIST_946)
            return (ASTProcedureNameListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTProcedureNameListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.PROCEDURE_NAME_LIST_946)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
