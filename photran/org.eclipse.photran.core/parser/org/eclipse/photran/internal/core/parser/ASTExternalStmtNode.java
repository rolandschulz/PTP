package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExternalStmtNode extends InteriorNode
{
    ASTExternalStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExternalStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXTERNAL_STMT_950)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.EXTERNAL_STMT_951)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTExternal()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXTERNAL_STMT_950)
            return (Token)getChild(1);
        else if (getProduction() == Production.EXTERNAL_STMT_951)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTExternalNameListNode getExternalNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXTERNAL_STMT_950)
            return (ASTExternalNameListNode)getChild(2);
        else if (getProduction() == Production.EXTERNAL_STMT_951)
            return (ASTExternalNameListNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXTERNAL_STMT_950)
            return (Token)getChild(3);
        else if (getProduction() == Production.EXTERNAL_STMT_951)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXTERNAL_STMT_951)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXTERNAL_STMT_951)
            return (Token)getChild(3);
        else
            return null;
    }
}
