package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInterfaceStmtNode extends InteriorNode
{
    ASTInterfaceStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInterfaceStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_STMT_929)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.INTERFACE_STMT_930)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.INTERFACE_STMT_931)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTInterface()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_STMT_929)
            return (Token)getChild(1);
        else if (getProduction() == Production.INTERFACE_STMT_930)
            return (Token)getChild(1);
        else if (getProduction() == Production.INTERFACE_STMT_931)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTGenericNameNode getGenericName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_STMT_929)
            return (ASTGenericNameNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_STMT_929)
            return (Token)getChild(3);
        else if (getProduction() == Production.INTERFACE_STMT_930)
            return (Token)getChild(3);
        else if (getProduction() == Production.INTERFACE_STMT_931)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTGenericSpecNode getGenericSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_STMT_930)
            return (ASTGenericSpecNode)getChild(2);
        else
            return null;
    }
}
