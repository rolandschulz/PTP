package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTStopStmtNode extends InteriorNode
{
    ASTStopStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTStopStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STOP_STMT_734)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.STOP_STMT_735)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.STOP_STMT_736)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTStop()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STOP_STMT_734)
            return (Token)getChild(1);
        else if (getProduction() == Production.STOP_STMT_735)
            return (Token)getChild(1);
        else if (getProduction() == Production.STOP_STMT_736)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STOP_STMT_734)
            return (Token)getChild(2);
        else if (getProduction() == Production.STOP_STMT_735)
            return (Token)getChild(3);
        else if (getProduction() == Production.STOP_STMT_736)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTIcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STOP_STMT_735)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTScon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STOP_STMT_736)
            return (Token)getChild(2);
        else
            return null;
    }
}
