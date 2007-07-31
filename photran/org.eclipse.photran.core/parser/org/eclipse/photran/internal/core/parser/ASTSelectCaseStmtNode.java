package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSelectCaseStmtNode extends InteriorNode
{
    ASTSelectCaseStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSelectCaseStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SELECT_CASE_STMT_683)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SELECT_CASE_STMT_685)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (Token)getChild(2);
        else if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTSelectcase()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (Token)getChild(3);
        else if (getProduction() == Production.SELECT_CASE_STMT_683)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (Token)getChild(4);
        else if (getProduction() == Production.SELECT_CASE_STMT_683)
            return (Token)getChild(2);
        else if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (Token)getChild(5);
        else if (getProduction() == Production.SELECT_CASE_STMT_685)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.SELECT_CASE_STMT_683)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (ASTExprNode)getChild(6);
        else if (getProduction() == Production.SELECT_CASE_STMT_685)
            return (ASTExprNode)getChild(4);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (Token)getChild(6);
        else if (getProduction() == Production.SELECT_CASE_STMT_683)
            return (Token)getChild(4);
        else if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (Token)getChild(7);
        else if (getProduction() == Production.SELECT_CASE_STMT_685)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_682)
            return (Token)getChild(7);
        else if (getProduction() == Production.SELECT_CASE_STMT_683)
            return (Token)getChild(5);
        else if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (Token)getChild(8);
        else if (getProduction() == Production.SELECT_CASE_STMT_685)
            return (Token)getChild(6);
        else
            return null;
    }

    public Token getTSelect()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (Token)getChild(3);
        else if (getProduction() == Production.SELECT_CASE_STMT_685)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTCase()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SELECT_CASE_STMT_684)
            return (Token)getChild(4);
        else if (getProduction() == Production.SELECT_CASE_STMT_685)
            return (Token)getChild(2);
        else
            return null;
    }
}
