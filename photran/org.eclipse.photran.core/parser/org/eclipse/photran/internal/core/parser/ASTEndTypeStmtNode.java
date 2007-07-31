package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEndTypeStmtNode extends InteriorNode
{
    ASTEndTypeStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEndTypeStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_TYPE_STMT_210)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_TYPE_STMT_211)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_TYPE_STMT_212)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_TYPE_STMT_213)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEndtype()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_TYPE_STMT_210)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_TYPE_STMT_212)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTTypeNameNode getTypeName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_TYPE_STMT_210)
            return (ASTTypeNameNode)getChild(2);
        else if (getProduction() == Production.END_TYPE_STMT_211)
            return (ASTTypeNameNode)getChild(3);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_TYPE_STMT_210)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_TYPE_STMT_211)
            return (Token)getChild(4);
        else if (getProduction() == Production.END_TYPE_STMT_212)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_TYPE_STMT_213)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_TYPE_STMT_211)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_TYPE_STMT_213)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTType()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_TYPE_STMT_211)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_TYPE_STMT_213)
            return (Token)getChild(2);
        else
            return null;
    }
}
