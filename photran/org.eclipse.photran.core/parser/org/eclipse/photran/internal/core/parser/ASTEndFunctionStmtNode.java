package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTEndFunctionStmtNode extends InteriorNode
{
    ASTEndFunctionStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEndFunctionStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FUNCTION_STMT_990)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_FUNCTION_STMT_991)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_FUNCTION_STMT_992)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_FUNCTION_STMT_993)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.END_FUNCTION_STMT_994)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTEnd()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FUNCTION_STMT_990)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_FUNCTION_STMT_993)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_FUNCTION_STMT_994)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FUNCTION_STMT_990)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_FUNCTION_STMT_991)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_FUNCTION_STMT_992)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_FUNCTION_STMT_993)
            return (Token)getChild(3);
        else if (getProduction() == Production.END_FUNCTION_STMT_994)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTEndfunction()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FUNCTION_STMT_991)
            return (Token)getChild(1);
        else if (getProduction() == Production.END_FUNCTION_STMT_992)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FUNCTION_STMT_992)
            return (ASTEndNameNode)getChild(2);
        else if (getProduction() == Production.END_FUNCTION_STMT_994)
            return (ASTEndNameNode)getChild(3);
        else
            return null;
    }

    public Token getTFunction()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.END_FUNCTION_STMT_993)
            return (Token)getChild(2);
        else if (getProduction() == Production.END_FUNCTION_STMT_994)
            return (Token)getChild(2);
        else
            return null;
    }
}
