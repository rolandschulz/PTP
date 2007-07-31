package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEndModuleStmtNode extends InteriorNode
{
    ASTEndModuleStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEndModuleStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_MODULE_STMT_895)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_MODULE_STMT_896)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_MODULE_STMT_897)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_MODULE_STMT_898)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_MODULE_STMT_899)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_MODULE_STMT_895)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_MODULE_STMT_898)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_MODULE_STMT_899)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_MODULE_STMT_895)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_MODULE_STMT_896)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_MODULE_STMT_897)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_MODULE_STMT_898)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_MODULE_STMT_899)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTEndmodule()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_MODULE_STMT_896)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_MODULE_STMT_897)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_MODULE_STMT_897)
            return (ASTEndNameNode)getChild(2);
        else if (getProduction() == Production.END_MODULE_STMT_899)
            return (ASTEndNameNode)getChild(3);
        else
            return null;
    }

    public Token getTModule()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_MODULE_STMT_898)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_MODULE_STMT_899)
            return (Token)getChild(2);
        else
            return null;
    }
}
