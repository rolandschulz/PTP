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

public class ASTOperatorNode extends InteriorNode
{
    ASTOperatorNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTOperatorNode(this);
    }

    public boolean hasPowerOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POWER_OP_125)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasPowerOp();
        else
            return false;
    }

    public boolean hasTimesOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OP_126)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasTimesOp();
        else
            return false;
    }

    public boolean hasDivideOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OP_127)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasDivideOp();
        else
            return false;
    }

    public boolean hasPlusOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ADD_OP_128)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasPlusOp();
        else
            return false;
    }

    public boolean hasMinusOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ADD_OP_129)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasMinusOp();
        else
            return false;
    }

    public boolean hasConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONCAT_OP_132)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasConcatOp();
        else
            return false;
    }

    public boolean hasEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_133)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasEqOp();
        else
            return false;
    }

    public boolean hasNeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_134)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasNeOp();
        else
            return false;
    }

    public boolean hasLtOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_135)
            return getChild(0) != null;
        else if (getProduction() == Production.REL_OP_136)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasLtOp();
        else
            return false;
    }

    public boolean hasLeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_137)
            return getChild(0) != null;
        else if (getProduction() == Production.REL_OP_138)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasLeOp();
        else
            return false;
    }

    public boolean hasGtOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_139)
            return getChild(0) != null;
        else if (getProduction() == Production.REL_OP_140)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasGtOp();
        else
            return false;
    }

    public boolean hasGeOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_141)
            return getChild(0) != null;
        else if (getProduction() == Production.REL_OP_142)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasGeOp();
        else
            return false;
    }

    public boolean hasEqEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_143)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasEqEqOp();
        else
            return false;
    }

    public boolean hasSlashEqOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.REL_OP_144)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasSlashEqOp();
        else
            return false;
    }

    public boolean hasNotOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.NOT_OP_145)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasNotOp();
        else
            return false;
    }

    public boolean hasAndOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AND_OP_146)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasAndOp();
        else
            return false;
    }

    public boolean hasOrOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OR_OP_147)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasOrOp();
        else
            return false;
    }

    public boolean hasEqvOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EQUIV_OP_148)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasEqvOp();
        else
            return false;
    }

    public boolean hasNeqvOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EQUIV_OP_149)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasNeqvOp();
        else
            return false;
    }

    public Token getCustomDefinedOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_150)
            return (Token)getChild(0);
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return (Token)((ASTOperatorNode)getChild(0)).getCustomDefinedOp();
        else
            return null;
    }

    public boolean hasCustomDefinedOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_150)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasCustomDefinedOp();
        else
            return false;
    }

    public Token getDefinedUnaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_UNARY_OP_160)
            return (Token)getChild(0);
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedUnaryOp();
        else
            return null;
    }

    public boolean hasDefinedUnaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_UNARY_OP_160)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasDefinedUnaryOp();
        else
            return false;
    }

    public Token getDefinedBinaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_BINARY_OP_161)
            return (Token)getChild(0);
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return (Token)((ASTOperatorNode)getChild(0)).getDefinedBinaryOp();
        else
            return null;
    }

    public boolean hasDefinedBinaryOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_BINARY_OP_161)
            return getChild(0) != null;
        else if (getProduction() == Production.DEFINED_OPERATOR_151)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_152)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_153)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_154)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_155)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_156)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_157)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_158)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else if (getProduction() == Production.DEFINED_OPERATOR_159)
            return ((ASTOperatorNode)getChild(0)).hasDefinedBinaryOp();
        else
            return false;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.DEFINED_OPERATOR_151 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_152 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_153 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_154 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_155 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_156 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_157 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_158 && index == 0)
            return true;
        else if (getProduction() == Production.DEFINED_OPERATOR_159 && index == 0)
            return true;
        else
            return false;
    }
}
