package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTModuleProcedureStmtNode extends InteriorNode
{
    ASTModuleProcedureStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTModuleProcedureStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_PROCEDURE_STMT_944)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTModule()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_PROCEDURE_STMT_944)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTProcedure()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_PROCEDURE_STMT_944)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTProcedureNameListNode getProcedureNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_PROCEDURE_STMT_944)
            return (ASTProcedureNameListNode)getChild(3);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_PROCEDURE_STMT_944)
            return (Token)getChild(4);
        else
            return null;
    }
}
