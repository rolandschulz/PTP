package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTReadStmtNode extends InteriorNode
{
    ASTReadStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTReadStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_764)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.READ_STMT_765)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.READ_STMT_766)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.READ_STMT_767)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTRead()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_764)
            return (Token)getChild(1);
        else if (getProduction() == Production.READ_STMT_765)
            return (Token)getChild(1);
        else if (getProduction() == Production.READ_STMT_766)
            return (Token)getChild(1);
        else if (getProduction() == Production.READ_STMT_767)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTRdCtlSpecNode getRdCtlSpec()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_764)
            return (ASTRdCtlSpecNode)getChild(2);
        else if (getProduction() == Production.READ_STMT_765)
            return (ASTRdCtlSpecNode)getChild(2);
        else
            return null;
    }

    public ASTInputItemListNode getInputItemList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_764)
            return (ASTInputItemListNode)getChild(3);
        else if (getProduction() == Production.READ_STMT_766)
            return (ASTInputItemListNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_764)
            return (Token)getChild(4);
        else if (getProduction() == Production.READ_STMT_765)
            return (Token)getChild(3);
        else if (getProduction() == Production.READ_STMT_766)
            return (Token)getChild(5);
        else if (getProduction() == Production.READ_STMT_767)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTRdFmtIdNode getRdFmtId()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_766)
            return (ASTRdFmtIdNode)getChild(2);
        else if (getProduction() == Production.READ_STMT_767)
            return (ASTRdFmtIdNode)getChild(2);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.READ_STMT_766)
            return (Token)getChild(3);
        else
            return null;
    }
}
