/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTExpressionBase extends InteriorNode
{
    ASTExpressionBase(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
        
    @Override public InteriorNode getASTParent()
    {
        InteriorNode actualParent = super.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (actualParent != null && actualParent.childIsPulledUp(actualParent.findChild(this)))
            return actualParent.getParent();
        else 
            return actualParent;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExpressionNode((ASTExpressionNode)this);
    }

    protected ASTPrimaryNode getRhsPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_1_EXPR_519)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return (ASTPrimaryNode)getChild(1);
        else if (getProduction() == Production.UFFACTOR_523)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.UFTERM_529)
            return (ASTPrimaryNode)getChild(2);
        else if (getProduction() == Production.CEXPR_538)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.CEXPR_539)
            return (ASTPrimaryNode)getChild(2);
        else if (getProduction() == Production.SFFACTOR_585)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.MULT_OPERAND_521)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.SFTERM_583)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.UFTERM_527)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.UFEXPR_533)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.AND_OPERAND_542)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.OR_OPERAND_544)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.EXPR_550)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else if (getProduction() == Production.SFEXPR_580)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getRhsPrimary();
        else
            return null;
    }

    protected boolean hasRhsPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_1_EXPR_519)
            return getChild(0) != null;
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return getChild(1) != null;
        else if (getProduction() == Production.UFFACTOR_523)
            return getChild(0) != null;
        else if (getProduction() == Production.UFTERM_529)
            return getChild(2) != null;
        else if (getProduction() == Production.CEXPR_538)
            return getChild(0) != null;
        else if (getProduction() == Production.CEXPR_539)
            return getChild(2) != null;
        else if (getProduction() == Production.SFFACTOR_585)
            return getChild(0) != null;
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasRhsPrimary();
        else
            return false;
    }

    protected ASTExpressionNode getLhsExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OPERAND_522)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.ADD_OPERAND_526)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.UFTERM_528)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.UFTERM_529)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.UFEXPR_535)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.CEXPR_539)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.OR_OPERAND_545)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.EXPR_551)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.SFEXPR_582)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.SFTERM_584)
            return (ASTExpressionNode)getChild(0);
        else if (getProduction() == Production.MULT_OPERAND_521)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.SFTERM_583)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.UFTERM_527)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.UFEXPR_533)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.AND_OPERAND_542)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.OR_OPERAND_544)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.EXPR_550)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else if (getProduction() == Production.SFEXPR_580)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getLhsExpr();
        else
            return null;
    }

    protected boolean hasLhsExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OPERAND_522)
            return getChild(0) != null;
        else if (getProduction() == Production.ADD_OPERAND_526)
            return getChild(0) != null;
        else if (getProduction() == Production.UFTERM_528)
            return getChild(0) != null;
        else if (getProduction() == Production.UFTERM_529)
            return getChild(0) != null;
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return getChild(0) != null;
        else if (getProduction() == Production.UFEXPR_535)
            return getChild(0) != null;
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return getChild(0) != null;
        else if (getProduction() == Production.CEXPR_539)
            return getChild(0) != null;
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return getChild(0) != null;
        else if (getProduction() == Production.OR_OPERAND_545)
            return getChild(0) != null;
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return getChild(0) != null;
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return getChild(0) != null;
        else if (getProduction() == Production.EXPR_551)
            return getChild(0) != null;
        else if (getProduction() == Production.SFEXPR_582)
            return getChild(0) != null;
        else if (getProduction() == Production.SFTERM_584)
            return getChild(0) != null;
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasLhsExpr();
        else
            return false;
    }

    protected ASTExpressionNode getRhsExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OPERAND_522)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.UFFACTOR_524)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.ADD_OPERAND_526)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.UFTERM_528)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.LEVEL_2_EXPR_531)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.UFEXPR_534)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.UFEXPR_535)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.AND_OPERAND_543)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.OR_OPERAND_545)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.EXPR_551)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.SFEXPR_581)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.SFEXPR_582)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.SFTERM_584)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.SFFACTOR_586)
            return (ASTExpressionNode)getChild(2);
        else if (getProduction() == Production.MULT_OPERAND_521)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.SFTERM_583)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.UFTERM_527)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.UFEXPR_533)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.AND_OPERAND_542)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.OR_OPERAND_544)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.EXPR_550)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else if (getProduction() == Production.SFEXPR_580)
            return (ASTExpressionNode)((ASTExpressionBase)getChild(0)).getRhsExpr();
        else
            return null;
    }

    protected boolean hasRhsExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OPERAND_522)
            return getChild(2) != null;
        else if (getProduction() == Production.UFFACTOR_524)
            return getChild(2) != null;
        else if (getProduction() == Production.ADD_OPERAND_526)
            return getChild(2) != null;
        else if (getProduction() == Production.UFTERM_528)
            return getChild(2) != null;
        else if (getProduction() == Production.LEVEL_2_EXPR_531)
            return getChild(1) != null;
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return getChild(2) != null;
        else if (getProduction() == Production.UFEXPR_534)
            return getChild(1) != null;
        else if (getProduction() == Production.UFEXPR_535)
            return getChild(2) != null;
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return getChild(2) != null;
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return getChild(2) != null;
        else if (getProduction() == Production.AND_OPERAND_543)
            return getChild(1) != null;
        else if (getProduction() == Production.OR_OPERAND_545)
            return getChild(2) != null;
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return getChild(2) != null;
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return getChild(2) != null;
        else if (getProduction() == Production.EXPR_551)
            return getChild(2) != null;
        else if (getProduction() == Production.SFEXPR_581)
            return getChild(1) != null;
        else if (getProduction() == Production.SFEXPR_582)
            return getChild(2) != null;
        else if (getProduction() == Production.SFTERM_584)
            return getChild(2) != null;
        else if (getProduction() == Production.SFFACTOR_586)
            return getChild(2) != null;
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasRhsExpr();
        else
            return false;
    }

    protected ASTPrimaryNode getLhsPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFFACTOR_524)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.SFFACTOR_586)
            return (ASTPrimaryNode)getChild(0);
        else if (getProduction() == Production.MULT_OPERAND_521)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.SFTERM_583)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.UFTERM_527)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.UFEXPR_533)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.AND_OPERAND_542)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.OR_OPERAND_544)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.EXPR_550)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else if (getProduction() == Production.SFEXPR_580)
            return (ASTPrimaryNode)((ASTExpressionBase)getChild(0)).getLhsPrimary();
        else
            return null;
    }

    protected boolean hasLhsPrimary()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFFACTOR_524)
            return getChild(0) != null;
        else if (getProduction() == Production.SFFACTOR_586)
            return getChild(0) != null;
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasLhsPrimary();
        else
            return false;
    }

    protected boolean hasPowerOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasPowerOp();
        else
            return false;
    }

    protected boolean hasTimesOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasTimesOp();
        else
            return false;
    }

    protected boolean hasDivideOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasDivideOp();
        else
            return false;
    }

    protected boolean hasPlusOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasPlusOp();
        else
            return false;
    }

    protected boolean hasMinusOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasMinusOp();
        else
            return false;
    }

    protected boolean hasConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasConcatOp();
        else
            return false;
    }

    protected boolean hasEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasEqOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasEqOp();
        else
            return false;
    }

    protected boolean hasNeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasNeOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasNeOp();
        else
            return false;
    }

    protected boolean hasLtOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasLtOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasLtOp();
        else
            return false;
    }

    protected boolean hasLeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasLeOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasLeOp();
        else
            return false;
    }

    protected boolean hasGtOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasGtOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasGtOp();
        else
            return false;
    }

    protected boolean hasGeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasGeOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasGeOp();
        else
            return false;
    }

    protected boolean hasEqEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasEqEqOp();
        else
            return false;
    }

    protected boolean hasSlashEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasSlashEqOp();
        else
            return false;
    }

    protected boolean hasNotOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasNotOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasNotOp();
        else
            return false;
    }

    protected boolean hasAndOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasAndOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasAndOp();
        else
            return false;
    }

    protected boolean hasOrOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasOrOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasOrOp();
        else
            return false;
    }

    protected boolean hasEqvOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasEqvOp();
        else
            return false;
    }

    protected boolean hasNeqvOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasNeqvOp();
        else
            return false;
    }

    protected Token getCustomDefinedOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.SFTERM_583)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.SFTERM_584)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.UFTERM_527)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.UFTERM_528)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.UFTERM_529)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.UFEXPR_533)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.UFEXPR_535)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.EXPR_550)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.EXPR_551)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else if (getProduction() == Production.SFEXPR_580)
            return (Token)((ASTExpressionBase)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.SFEXPR_582)
            return (Token)((ASTOperatorNode)getChild(1)).getCustomDefinedOp();
        else
            return null;
    }

    protected boolean hasCustomDefinedOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasCustomDefinedOp();
        else
            return false;
    }

    protected Token getDefinedUnaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.SFTERM_583)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.SFTERM_584)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.UFTERM_527)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.UFTERM_528)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.UFTERM_529)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.UFEXPR_533)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.UFEXPR_535)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.EXPR_550)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.EXPR_551)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else if (getProduction() == Production.SFEXPR_580)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.SFEXPR_582)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedUnaryOp();
        else
            return null;
    }

    protected boolean hasDefinedUnaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasDefinedUnaryOp();
        else
            return false;
    }

    protected Token getDefinedBinaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.SFTERM_583)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.SFTERM_584)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.UFTERM_527)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.UFTERM_528)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.UFTERM_529)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.UFEXPR_533)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.UFEXPR_535)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.EXPR_550)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.EXPR_551)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else if (getProduction() == Production.SFEXPR_580)
            return (Token)((ASTExpressionBase)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.SFEXPR_582)
            return (Token)((ASTOperatorNode)getChild(1)).getDefinedBinaryOp();
        else
            return null;
    }

    protected boolean hasDefinedBinaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CEXPR_539)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.SFFACTOR_586)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.UFFACTOR_524)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_1_EXPR_520)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.MULT_OPERAND_521)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.MULT_OPERAND_522)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.SFTERM_583)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.SFTERM_584)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.UFTERM_527)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.UFTERM_528)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.UFTERM_529)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.ADD_OPERAND_525)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.ADD_OPERAND_526)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.UFEXPR_533)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.UFEXPR_535)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_2_EXPR_532)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_3_EXPR_537)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_4_EXPR_541)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.AND_OPERAND_543)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.OR_OPERAND_545)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.EXPR_551)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.SFEXPR_582)
            return ((ASTOperatorNode)getChild(1)).hasDefinedBinaryOp();
        else
            return false;
    }

    protected boolean Rhs2hasPlusSign()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFEXPR_534)
            return ((ASTSignNode)getChild(0)).hasPlusSign();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasPlusSign();
        else
            return false;
    }

    protected boolean Rhs2hasMinusSign()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFEXPR_534)
            return ((ASTSignNode)getChild(0)).hasMinusSign();
        else if (getProduction() == Production.LEVEL_2_EXPR_530)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).Rhs2hasMinusSign();
        else
            return false;
    }

    protected boolean RhshasPlusSign()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_2_EXPR_531)
            return ((ASTSignNode)getChild(0)).hasPlusSign();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).RhshasPlusSign();
        else if (getProduction() == Production.SFEXPR_581)
            return ((ASTSignNode)getChild(0)).hasPlusSign();
        else
            return false;
    }

    protected boolean RhshasMinusSign()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_2_EXPR_531)
            return ((ASTSignNode)getChild(0)).hasMinusSign();
        else if (getProduction() == Production.LEVEL_3_EXPR_536)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.LEVEL_4_EXPR_540)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.AND_OPERAND_542)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.OR_OPERAND_544)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.EQUIV_OPERAND_546)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.LEVEL_5_EXPR_548)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.EXPR_550)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.SFEXPR_580)
            return ((ASTExpressionBase)getChild(0)).RhshasMinusSign();
        else if (getProduction() == Production.SFEXPR_581)
            return ((ASTSignNode)getChild(0)).hasMinusSign();
        else
            return false;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_525 && index == 0)
            return true;
        else if (getProduction() == Production.ADD_OPERAND_526 && index == 1)
            return true;
        else if (getProduction() == Production.AND_OPERAND_542 && index == 0)
            return true;
        else if (getProduction() == Production.AND_OPERAND_543 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_536 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_3_EXPR_537 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_527 && index == 0)
            return true;
        else if (getProduction() == Production.UFTERM_528 && index == 1)
            return true;
        else if (getProduction() == Production.UFTERM_529 && index == 1)
            return true;
        else if (getProduction() == Production.EXPR_550 && index == 0)
            return true;
        else if (getProduction() == Production.EXPR_551 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_548 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_5_EXPR_549 && index == 1)
            return true;
        else if (getProduction() == Production.SFTERM_583 && index == 0)
            return true;
        else if (getProduction() == Production.SFTERM_584 && index == 1)
            return true;
        else if (getProduction() == Production.OR_OPERAND_544 && index == 0)
            return true;
        else if (getProduction() == Production.OR_OPERAND_545 && index == 1)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_521 && index == 0)
            return true;
        else if (getProduction() == Production.MULT_OPERAND_522 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_540 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_4_EXPR_541 && index == 1)
            return true;
        else if (getProduction() == Production.SFEXPR_580 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_581 && index == 0)
            return true;
        else if (getProduction() == Production.SFEXPR_582 && index == 1)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_546 && index == 0)
            return true;
        else if (getProduction() == Production.EQUIV_OPERAND_547 && index == 1)
            return true;
        else if (getProduction() == Production.LEVEL_1_EXPR_520 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_530 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_531 && index == 0)
            return true;
        else if (getProduction() == Production.LEVEL_2_EXPR_532 && index == 1)
            return true;
        else if (getProduction() == Production.UFEXPR_533 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_534 && index == 0)
            return true;
        else if (getProduction() == Production.UFEXPR_535 && index == 1)
            return true;
        else if (getProduction() == Production.UFFACTOR_524 && index == 1)
            return true;
        else if (getProduction() == Production.CEXPR_539 && index == 1)
            return true;
        else if (getProduction() == Production.SFFACTOR_586 && index == 1)
            return true;
        else
            return false;
    }
}
