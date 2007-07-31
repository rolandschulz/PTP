package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSelectCaseBodyNode extends InteriorNode
{
    protected int count = -1;

    ASTSelectCaseBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTSelectCaseBodyNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTSelectCaseBodyNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTSelectCaseBodyNode recurseToIndex(int listIndex)
    {
        ASTSelectCaseBodyNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTSelectCaseBodyNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSelectCaseBodyNode(this);
    }

    public ASTCaseStmtNode getCaseStmt(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSelectCaseBodyNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SELECT_CASE_BODY_678)
            return (ASTCaseStmtNode)node.getChild(0);
        else
            return null;
    }

    private ASTSelectCaseBodyNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_BODY_679)
            return (ASTSelectCaseBodyNode)getChild(0);
        else
            return null;
    }

    public ASTCaseBodyConstructNode getCaseBodyConstruct(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSelectCaseBodyNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SELECT_CASE_BODY_679)
            return (ASTCaseBodyConstructNode)node.getChild(1);
        else
            return null;
    }
}
