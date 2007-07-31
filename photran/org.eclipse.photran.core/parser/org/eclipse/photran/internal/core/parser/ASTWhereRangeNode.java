package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTWhereRangeNode extends InteriorNode
{
    ASTWhereRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTWhereRangeNode(this);
    }

    public ASTEndWhereStmtNode getEndWhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_600)
            return (ASTEndWhereStmtNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_601)
            return (ASTEndWhereStmtNode)getChild(1);
        else
            return null;
    }

    public ASTWhereBodyConstructBlockNode getWhereBodyConstructBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_601)
            return (ASTWhereBodyConstructBlockNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_603)
            return (ASTWhereBodyConstructBlockNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_605)
            return (ASTWhereBodyConstructBlockNode)getChild(0);
        else
            return null;
    }

    public ASTMaskedElsewhereConstructNode getMaskedElsewhereConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_602)
            return (ASTMaskedElsewhereConstructNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_603)
            return (ASTMaskedElsewhereConstructNode)getChild(1);
        else
            return null;
    }

    public ASTElsewhereConstructNode getElsewhereConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_604)
            return (ASTElsewhereConstructNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_605)
            return (ASTElsewhereConstructNode)getChild(1);
        else
            return null;
    }
}
