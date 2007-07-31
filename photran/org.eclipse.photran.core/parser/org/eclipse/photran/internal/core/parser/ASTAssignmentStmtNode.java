package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAssignmentStmtNode extends InteriorNode
{
    ASTAssignmentStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAssignmentStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTNameNode)getChild(1);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }

    public Token getTEquals()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(11);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (ASTExprNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (ASTExprNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTExprNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTExprNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTExprNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTExprNode)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTExprNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTExprNode)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTExprNode)getChild(12);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTExprNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTExprNode)getChild(11);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTExprNode)getChild(12);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_556)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(12);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(13);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(12);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(13);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTSFExprListNode getSFExprList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTSFExprListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTSFExprListNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_557)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(4);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(4);
        else
            return null;
    }

    public ASTSubstringRangeNode getSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_558)
            return (ASTSubstringRangeNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTSubstringRangeNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTSubstringRangeNode)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTSubstringRangeNode)getChild(10);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTSubstringRangeNode)getChild(10);
        else
            return null;
    }

    public ASTSFDummyArgNameListNode getSFDummyArgNameList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_559)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTSFDummyArgNameListNode)getChild(3);
        else
            return null;
    }

    public Token getTPercent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (Token)getChild(2);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(5);
        else
            return null;
    }

    public ASTDataRefNode getDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_560)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTDataRefNode)getChild(3);
        else if (getProduction() == Production.ASSIGNMENT_STMT_563)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_566)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTDataRefNode)getChild(6);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTDataRefNode)getChild(6);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_561)
            return (ASTSectionSubscriptListNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_562)
            return (ASTSectionSubscriptListNode)getChild(5);
        else if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (ASTSectionSubscriptListNode)getChild(8);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (ASTSectionSubscriptListNode)getChild(8);
        else
            return null;
    }

    public Token getTLparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(7);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(7);
        else
            return null;
    }

    public Token getTRparen2()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSIGNMENT_STMT_564)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_565)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_567)
            return (Token)getChild(9);
        else if (getProduction() == Production.ASSIGNMENT_STMT_568)
            return (Token)getChild(9);
        else
            return null;
    }
}
