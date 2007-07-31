package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAllocatableStmtNode extends InteriorNode
{
    ASTAllocatableStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAllocatableStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATABLE_STMT_344)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ALLOCATABLE_STMT_345)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTAllocatable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATABLE_STMT_344)
            return (Token)getChild(1);
        else if (getProduction() == Production.ALLOCATABLE_STMT_345)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTColon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATABLE_STMT_344)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTColon2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATABLE_STMT_344)
            return (Token)getChild(3);
        else
            return null;
    }

    public ASTArrayAllocationListNode getArrayAllocationList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATABLE_STMT_344)
            return (ASTArrayAllocationListNode)getChild(4);
        else if (getProduction() == Production.ALLOCATABLE_STMT_345)
            return (ASTArrayAllocationListNode)getChild(2);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATABLE_STMT_344)
            return (Token)getChild(5);
        else if (getProduction() == Production.ALLOCATABLE_STMT_345)
            return (Token)getChild(3);
        else
            return null;
    }
}
