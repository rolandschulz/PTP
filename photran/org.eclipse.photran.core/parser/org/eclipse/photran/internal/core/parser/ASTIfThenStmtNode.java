package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTIfThenStmtNode extends InteriorNode
{
    ASTIfThenStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTIfThenStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_662)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.IF_THEN_STMT_663)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTIf()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_662)
            return (Token)getChild(1);
        else if (getProduction() == Production.IF_THEN_STMT_663)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_662)
            return (Token)getChild(2);
        else if (getProduction() == Production.IF_THEN_STMT_663)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_662)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.IF_THEN_STMT_663)
            return (ASTExprNode)getChild(5);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_662)
            return (Token)getChild(4);
        else if (getProduction() == Production.IF_THEN_STMT_663)
            return (Token)getChild(6);
        else
            return null;
    }

    public Token getTThen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_662)
            return (Token)getChild(5);
        else if (getProduction() == Production.IF_THEN_STMT_663)
            return (Token)getChild(7);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_662)
            return (Token)getChild(6);
        else if (getProduction() == Production.IF_THEN_STMT_663)
            return (Token)getChild(8);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_663)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_THEN_STMT_663)
            return (Token)getChild(2);
        else
            return null;
    }
}
