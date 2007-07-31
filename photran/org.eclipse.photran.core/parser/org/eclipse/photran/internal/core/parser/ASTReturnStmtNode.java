package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTReturnStmtNode extends InteriorNode
{
    ASTReturnStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTReturnStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RETURN_STMT_1011)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.RETURN_STMT_1012)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTReturn()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RETURN_STMT_1011)
            return (Token)getChild(1);
        else if (getProduction() == Production.RETURN_STMT_1012)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RETURN_STMT_1011)
            return (Token)getChild(2);
        else if (getProduction() == Production.RETURN_STMT_1012)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.RETURN_STMT_1012)
            return (ASTExprNode)getChild(2);
        else
            return null;
    }
}
