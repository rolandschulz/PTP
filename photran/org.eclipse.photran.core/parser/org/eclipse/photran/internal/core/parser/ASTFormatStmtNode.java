package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFormatStmtNode extends InteriorNodeWithErrorRecoverySymbols
{
    ASTFormatStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production, discardedSymbols);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFormatStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_STMT_865)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FORMAT_STMT_866)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.FORMAT_STMT_ERROR_0)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTFormat()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_STMT_865)
            return (Token)getChild(1);
        else if (getProduction() == Production.FORMAT_STMT_866)
            return (Token)getChild(1);
        else if (getProduction() == Production.FORMAT_STMT_ERROR_0)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_STMT_865)
            return (Token)getChild(2);
        else if (getProduction() == Production.FORMAT_STMT_866)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_STMT_865)
            return (Token)getChild(3);
        else if (getProduction() == Production.FORMAT_STMT_866)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_STMT_865)
            return (Token)getChild(4);
        else if (getProduction() == Production.FORMAT_STMT_866)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTFmtSpecNode getFmtSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMAT_STMT_866)
            return (ASTFmtSpecNode)getChild(3);
        else
            return null;
    }
}
