package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCaseStmtNode extends InteriorNode
{
    ASTCaseStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCaseStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_STMT_686)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.CASE_STMT_687)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTCase()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_STMT_686)
            return (Token)getChild(1);
        else if (getProduction() == Production.CASE_STMT_687)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTCaseSelectorNode getCaseSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_STMT_686)
            return (ASTCaseSelectorNode)getChild(2);
        else if (getProduction() == Production.CASE_STMT_687)
            return (ASTCaseSelectorNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_STMT_686)
            return (Token)getChild(3);
        else if (getProduction() == Production.CASE_STMT_687)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CASE_STMT_687)
            return (ASTNameNode)getChild(3);
        else
            return null;
    }
}
