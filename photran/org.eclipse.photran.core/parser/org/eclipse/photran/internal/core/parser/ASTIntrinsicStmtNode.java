package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTIntrinsicStmtNode extends InteriorNode
{
    ASTIntrinsicStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTIntrinsicStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTRINSIC_STMT_954)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.INTRINSIC_STMT_955)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTIntrinsic()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTRINSIC_STMT_954)
            return (Token)getChild(1);
        else if (getProduction() == Production.INTRINSIC_STMT_955)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTIntrinsicListNode getIntrinsicList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTRINSIC_STMT_954)
            return (ASTIntrinsicListNode)getChild(2);
        else if (getProduction() == Production.INTRINSIC_STMT_955)
            return (ASTIntrinsicListNode)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTRINSIC_STMT_954)
            return (Token)getChild(3);
        else if (getProduction() == Production.INTRINSIC_STMT_955)
            return (Token)getChild(5);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTRINSIC_STMT_955)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTRINSIC_STMT_955)
            return (Token)getChild(3);
        else
            return null;
    }
}
