package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSaveStmtNode extends InteriorNode
{
    ASTSaveStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSaveStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVE_STMT_331)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SAVE_STMT_332)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.SAVE_STMT_333)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTSave()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVE_STMT_331)
            return (Token)getChild(1);
        else if (getProduction() == Production.SAVE_STMT_332)
            return (Token)getChild(1);
        else if (getProduction() == Production.SAVE_STMT_333)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVE_STMT_331)
            return (Token)getChild(2);
        else if (getProduction() == Production.SAVE_STMT_332)
            return (Token)getChild(3);
        else if (getProduction() == Production.SAVE_STMT_333)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTSavedEntityListNode getSavedEntityList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVE_STMT_332)
            return (ASTSavedEntityListNode)getChild(2);
        else if (getProduction() == Production.SAVE_STMT_333)
            return (ASTSavedEntityListNode)getChild(4);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVE_STMT_333)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SAVE_STMT_333)
            return (Token)getChild(3);
        else
            return null;
    }
}
