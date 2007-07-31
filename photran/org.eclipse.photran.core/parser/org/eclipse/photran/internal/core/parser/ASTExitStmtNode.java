package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExitStmtNode extends InteriorNode
{
    ASTExitStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExitStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXIT_STMT_721)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.EXIT_STMT_722)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTExit()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXIT_STMT_721)
            return (Token)getChild(1);
        else if (getProduction() == Production.EXIT_STMT_722)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXIT_STMT_721)
            return (Token)getChild(2);
        else if (getProduction() == Production.EXIT_STMT_722)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXIT_STMT_722)
            return (ASTNameNode)getChild(2);
        else
            return null;
    }
}
