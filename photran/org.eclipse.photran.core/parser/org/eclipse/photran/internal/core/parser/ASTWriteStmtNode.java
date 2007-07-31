package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTWriteStmtNode extends InteriorNode
{
    ASTWriteStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTWriteStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WRITE_STMT_782)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.WRITE_STMT_783)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTWrite()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WRITE_STMT_782)
            return (Token)getChild(1);
        else if (getProduction() == Production.WRITE_STMT_783)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WRITE_STMT_782)
            return (Token)getChild(2);
        else if (getProduction() == Production.WRITE_STMT_783)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTIoControlSpecListNode getIoControlSpecList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WRITE_STMT_782)
            return (ASTIoControlSpecListNode)getChild(3);
        else if (getProduction() == Production.WRITE_STMT_783)
            return (ASTIoControlSpecListNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WRITE_STMT_782)
            return (Token)getChild(4);
        else if (getProduction() == Production.WRITE_STMT_783)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTOutputItemListNode getOutputItemList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WRITE_STMT_782)
            return (ASTOutputItemListNode)getChild(5);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WRITE_STMT_782)
            return (Token)getChild(6);
        else if (getProduction() == Production.WRITE_STMT_783)
            return (Token)getChild(5);
        else
            return null;
    }
}
