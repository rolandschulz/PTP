package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTOptionalStmtNode extends InteriorNode
{
    ASTOptionalStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTOptionalStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OPTIONAL_STMT_319)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.OPTIONAL_STMT_320)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTOptional()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OPTIONAL_STMT_319)
            return (Token)getChild(1);
        else if (getProduction() == Production.OPTIONAL_STMT_320)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTOptionalParListNode getOptionalParList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OPTIONAL_STMT_319)
            return (ASTOptionalParListNode)getChild(2);
        else if (getProduction() == Production.OPTIONAL_STMT_320)
            return (ASTOptionalParListNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OPTIONAL_STMT_319)
            return (Token)getChild(3);
        else if (getProduction() == Production.OPTIONAL_STMT_320)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OPTIONAL_STMT_320)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OPTIONAL_STMT_320)
            return (Token)getChild(3);
        else
            return null;
    }
}
