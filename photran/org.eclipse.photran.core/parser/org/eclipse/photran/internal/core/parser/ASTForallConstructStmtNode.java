package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTForallConstructStmtNode extends InteriorNode
{
    ASTForallConstructStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTForallConstructStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_630)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_631)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTForall()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_630)
            return (Token)getChild(1);
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_631)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTForallHeaderNode getForallHeader()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_630)
            return (ASTForallHeaderNode)getChild(2);
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_631)
            return (ASTForallHeaderNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_630)
            return (Token)getChild(3);
        else if (getProduction() == Production.FORALL_CONSTRUCT_STMT_631)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_631)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_CONSTRUCT_STMT_631)
            return (Token)getChild(2);
        else
            return null;
    }
}
