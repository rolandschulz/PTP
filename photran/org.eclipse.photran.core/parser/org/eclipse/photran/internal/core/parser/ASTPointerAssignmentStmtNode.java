package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTPointerAssignmentStmtNode extends InteriorNode
{
    ASTPointerAssignmentStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPointerAssignmentStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTEqgreaterthan()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (Token)getChild(2);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (Token)getChild(4);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (Token)getChild(7);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (Token)getChild(7);
        else
            return null;
    }

    public ASTTargetNode getTarget()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (ASTTargetNode)getChild(3);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (ASTTargetNode)getChild(5);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTTargetNode)getChild(8);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTTargetNode)getChild(8);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_593)
            return (Token)getChild(4);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (Token)getChild(6);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (Token)getChild(9);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (Token)getChild(9);
        else
            return null;
    }

    public Token getTPercent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (Token)getChild(2);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (Token)getChild(5);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_594)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTDataRefNode)getChild(6);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (Token)getChild(2);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTSFExprListNode getSFExprList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (ASTSFExprListNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_595)
            return (Token)getChild(4);
        else if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTSFDummyArgNameListNode getSFDummyArgNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_ASSIGNMENT_STMT_596)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else
            return null;
    }
}
