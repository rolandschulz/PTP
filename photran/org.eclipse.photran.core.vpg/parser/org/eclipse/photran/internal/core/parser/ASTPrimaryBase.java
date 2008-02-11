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

public class ASTPrimaryBase extends InteriorNode
{
    ASTPrimaryBase(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTPrimaryNode((ASTPrimaryNode)this);
    }

    protected ASTLogicalConstantNode getLogicalConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_476)
            return (ASTLogicalConstantNode)getChild(0);
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTLogicalConstantNode)((ASTPrimaryBase)getChild(0)).getLogicalConst();
        else
            return null;
    }

    protected boolean hasLogicalConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_476)
            return getChild(0) != null;
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasLogicalConst();
        else
            return false;
    }

    protected Token getStringConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_477)
            return (Token)getChild(0);
        else if (getProduction() == Production.COPERAND_498)
            return (Token)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_507)
            return (Token)getChild(0);
        else if (getProduction() == Production.PRIMARY_495)
            return (Token)((ASTSubstrConstNode)getChild(0)).getStringConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return (Token)((ASTPrimaryBase)getChild(0)).getStringConst();
        else
            return null;
    }

    protected boolean hasStringConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_477)
            return getChild(0) != null;
        else if (getProduction() == Production.COPERAND_498)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_507)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_495)
            return ((ASTSubstrConstNode)getChild(0)).hasStringConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasStringConst();
        else
            return false;
    }

    protected ASTArrayConstructorNode getArrayConstructor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_479)
            return (ASTArrayConstructorNode)getChild(0);
        else if (getProduction() == Production.SFPRIMARY_587)
            return (ASTArrayConstructorNode)getChild(0);
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTArrayConstructorNode)((ASTPrimaryBase)getChild(0)).getArrayConstructor();
        else
            return null;
    }

    protected boolean hasArrayConstructor()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_479)
            return getChild(0) != null;
        else if (getProduction() == Production.SFPRIMARY_587)
            return getChild(0) != null;
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasArrayConstructor();
        else
            return false;
    }

    protected ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_480)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_481)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_482)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_483)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_484)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_485)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_486)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_499)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_500)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_501)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_502)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_503)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_509)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_510)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_511)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_512)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_513)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_514)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_515)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_516)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_517)
            return (ASTNameNode)getChild(0);
        else if (getProduction() == Production.UFPRIMARY_508)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.COPERAND_505)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.PRIMARY_489)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.PRIMARY_491)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.PRIMARY_492)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.PRIMARY_493)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.SFPRIMARY_589)
            return (ASTNameNode)((ASTSFVarNameNode)getChild(0)).getName();
        else if (getProduction() == Production.SFPRIMARY_591)
            return (ASTNameNode)((ASTFunctionReferenceNode)getChild(0)).getName();
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTNameNode)((ASTPrimaryBase)getChild(0)).getName();
        else
            return null;
    }

    protected boolean hasName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_480)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_481)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_482)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_483)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_484)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_485)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_486)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_487)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_488)
            return getChild(0) != null;
        else if (getProduction() == Production.COPERAND_499)
            return getChild(0) != null;
        else if (getProduction() == Production.COPERAND_500)
            return getChild(0) != null;
        else if (getProduction() == Production.COPERAND_501)
            return getChild(0) != null;
        else if (getProduction() == Production.COPERAND_502)
            return getChild(0) != null;
        else if (getProduction() == Production.COPERAND_503)
            return getChild(0) != null;
        else if (getProduction() == Production.COPERAND_504)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_509)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_510)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_511)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_512)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_513)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_514)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_515)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_516)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_517)
            return getChild(0) != null;
        else if (getProduction() == Production.UFPRIMARY_508)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.COPERAND_505)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.PRIMARY_489)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.PRIMARY_490)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.PRIMARY_491)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.PRIMARY_492)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.PRIMARY_493)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.SFPRIMARY_589)
            return ((ASTSFVarNameNode)getChild(0)).hasName();
        else if (getProduction() == Production.SFPRIMARY_591)
            return ((ASTFunctionReferenceNode)getChild(0)).hasName();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasName();
        else
            return false;
    }

    protected ASTSectionSubscriptListNode getPrimarySectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_481)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_482)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_486)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.COPERAND_500)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.COPERAND_503)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_510)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_511)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_515)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_516)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_517)
            return (ASTSectionSubscriptListNode)getChild(2);
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTSectionSubscriptListNode)((ASTPrimaryBase)getChild(0)).getPrimarySectionSubscriptList();
        else
            return null;
    }

    protected boolean hasPrimarySectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_481)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_482)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_486)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_487)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_488)
            return getChild(2) != null;
        else if (getProduction() == Production.COPERAND_500)
            return getChild(2) != null;
        else if (getProduction() == Production.COPERAND_503)
            return getChild(2) != null;
        else if (getProduction() == Production.COPERAND_504)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_510)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_511)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_515)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_516)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_517)
            return getChild(2) != null;
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasPrimarySectionSubscriptList();
        else
            return false;
    }

    protected ASTSubstringRangeNode getSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_482)
            return (ASTSubstringRangeNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_485)
            return (ASTSubstringRangeNode)getChild(6);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTSubstringRangeNode)getChild(9);
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTSubstringRangeNode)getChild(1);
        else if (getProduction() == Production.PRIMARY_493)
            return (ASTSubstringRangeNode)getChild(6);
        else if (getProduction() == Production.UFPRIMARY_511)
            return (ASTSubstringRangeNode)getChild(4);
        else if (getProduction() == Production.UFPRIMARY_514)
            return (ASTSubstringRangeNode)getChild(6);
        else if (getProduction() == Production.UFPRIMARY_517)
            return (ASTSubstringRangeNode)getChild(9);
        else if (getProduction() == Production.PRIMARY_495)
            return (ASTSubstringRangeNode)((ASTSubstrConstNode)getChild(0)).getSubstringRange();
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTSubstringRangeNode)((ASTPrimaryBase)getChild(0)).getSubstringRange();
        else
            return null;
    }

    protected boolean hasSubstringRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_482)
            return getChild(4) != null;
        else if (getProduction() == Production.PRIMARY_485)
            return getChild(6) != null;
        else if (getProduction() == Production.PRIMARY_488)
            return getChild(9) != null;
        else if (getProduction() == Production.PRIMARY_490)
            return getChild(1) != null;
        else if (getProduction() == Production.PRIMARY_493)
            return getChild(6) != null;
        else if (getProduction() == Production.UFPRIMARY_511)
            return getChild(4) != null;
        else if (getProduction() == Production.UFPRIMARY_514)
            return getChild(6) != null;
        else if (getProduction() == Production.UFPRIMARY_517)
            return getChild(9) != null;
        else if (getProduction() == Production.PRIMARY_495)
            return ((ASTSubstrConstNode)getChild(0)).hasSubstringRange();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasSubstringRange();
        else
            return false;
    }

    protected ASTDataRefNode getDerivedTypeComponentRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_483)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_484)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_485)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_486)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.PRIMARY_491)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_492)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.PRIMARY_493)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.COPERAND_501)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.COPERAND_502)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.COPERAND_503)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.UFPRIMARY_512)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_513)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_514)
            return (ASTDataRefNode)getChild(2);
        else if (getProduction() == Production.UFPRIMARY_515)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.UFPRIMARY_516)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.UFPRIMARY_517)
            return (ASTDataRefNode)getChild(5);
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTDataRefNode)((ASTPrimaryBase)getChild(0)).getDerivedTypeComponentRef();
        else
            return null;
    }

    protected boolean hasDerivedTypeComponentRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_483)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_484)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_485)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_486)
            return getChild(5) != null;
        else if (getProduction() == Production.PRIMARY_487)
            return getChild(5) != null;
        else if (getProduction() == Production.PRIMARY_488)
            return getChild(5) != null;
        else if (getProduction() == Production.PRIMARY_491)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_492)
            return getChild(2) != null;
        else if (getProduction() == Production.PRIMARY_493)
            return getChild(2) != null;
        else if (getProduction() == Production.COPERAND_501)
            return getChild(2) != null;
        else if (getProduction() == Production.COPERAND_502)
            return getChild(2) != null;
        else if (getProduction() == Production.COPERAND_503)
            return getChild(5) != null;
        else if (getProduction() == Production.COPERAND_504)
            return getChild(5) != null;
        else if (getProduction() == Production.UFPRIMARY_512)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_513)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_514)
            return getChild(2) != null;
        else if (getProduction() == Production.UFPRIMARY_515)
            return getChild(5) != null;
        else if (getProduction() == Production.UFPRIMARY_516)
            return getChild(5) != null;
        else if (getProduction() == Production.UFPRIMARY_517)
            return getChild(5) != null;
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasDerivedTypeComponentRef();
        else
            return false;
    }

    protected ASTSectionSubscriptListNode getComponentSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_484)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_485)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_487)
            return (ASTSectionSubscriptListNode)getChild(7);
        else if (getProduction() == Production.PRIMARY_488)
            return (ASTSectionSubscriptListNode)getChild(7);
        else if (getProduction() == Production.PRIMARY_492)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.PRIMARY_493)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.COPERAND_502)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.COPERAND_504)
            return (ASTSectionSubscriptListNode)getChild(7);
        else if (getProduction() == Production.UFPRIMARY_513)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.UFPRIMARY_514)
            return (ASTSectionSubscriptListNode)getChild(4);
        else if (getProduction() == Production.UFPRIMARY_516)
            return (ASTSectionSubscriptListNode)getChild(7);
        else if (getProduction() == Production.UFPRIMARY_517)
            return (ASTSectionSubscriptListNode)getChild(7);
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTSectionSubscriptListNode)((ASTPrimaryBase)getChild(0)).getComponentSectionSubscriptList();
        else
            return null;
    }

    protected boolean hasComponentSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_484)
            return getChild(4) != null;
        else if (getProduction() == Production.PRIMARY_485)
            return getChild(4) != null;
        else if (getProduction() == Production.PRIMARY_487)
            return getChild(7) != null;
        else if (getProduction() == Production.PRIMARY_488)
            return getChild(7) != null;
        else if (getProduction() == Production.PRIMARY_492)
            return getChild(4) != null;
        else if (getProduction() == Production.PRIMARY_493)
            return getChild(4) != null;
        else if (getProduction() == Production.COPERAND_502)
            return getChild(4) != null;
        else if (getProduction() == Production.COPERAND_504)
            return getChild(7) != null;
        else if (getProduction() == Production.UFPRIMARY_513)
            return getChild(4) != null;
        else if (getProduction() == Production.UFPRIMARY_514)
            return getChild(4) != null;
        else if (getProduction() == Production.UFPRIMARY_516)
            return getChild(7) != null;
        else if (getProduction() == Production.UFPRIMARY_517)
            return getChild(7) != null;
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasComponentSectionSubscriptList();
        else
            return false;
    }

    protected ASTExpressionNode getNestedExpression()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_494)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.CPRIMARY_497)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.UFPRIMARY_518)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.SFPRIMARY_592)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTExpressionNode)((ASTPrimaryBase)getChild(0)).getNestedExpression();
        else
            return null;
    }

    protected boolean hasNestedExpression()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_494)
            return getChild(1) != null;
        else if (getProduction() == Production.CPRIMARY_497)
            return getChild(1) != null;
        else if (getProduction() == Production.UFPRIMARY_518)
            return getChild(1) != null;
        else if (getProduction() == Production.SFPRIMARY_592)
            return getChild(1) != null;
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasNestedExpression();
        else
            return false;
    }

    protected Token getIntConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFPRIMARY_506)
            return (Token)getChild(0);
        else if (getProduction() == Production.SFPRIMARY_588)
            return (Token)getChild(0);
        else if (getProduction() == Production.PRIMARY_478)
            return (Token)((ASTUnsignedArithmeticConstantNode)getChild(0)).getIntConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return (Token)((ASTPrimaryBase)getChild(0)).getIntConst();
        else
            return null;
    }

    protected boolean hasIntConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFPRIMARY_506)
            return getChild(0) != null;
        else if (getProduction() == Production.SFPRIMARY_588)
            return getChild(0) != null;
        else if (getProduction() == Production.PRIMARY_478)
            return ((ASTUnsignedArithmeticConstantNode)getChild(0)).hasIntConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasIntConst();
        else
            return false;
    }

    protected ASTDataRefNode getSFDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFPRIMARY_590)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTDataRefNode)((ASTPrimaryBase)getChild(0)).getSFDataRef();
        else
            return null;
    }

    protected boolean hasSFDataRef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFPRIMARY_590)
            return getChild(0) != null;
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasSFDataRef();
        else
            return false;
    }

    protected ASTFunctionArgListNode getFunctionArgList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFPRIMARY_508)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.COPERAND_505)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.PRIMARY_489)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.PRIMARY_490)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.PRIMARY_491)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.PRIMARY_492)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.PRIMARY_493)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.SFPRIMARY_591)
            return (ASTFunctionArgListNode)((ASTFunctionReferenceNode)getChild(0)).getFunctionArgList();
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTFunctionArgListNode)((ASTPrimaryBase)getChild(0)).getFunctionArgList();
        else
            return null;
    }

    protected boolean hasFunctionArgList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.UFPRIMARY_508)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.COPERAND_505)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.PRIMARY_489)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.PRIMARY_490)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.PRIMARY_491)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.PRIMARY_492)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.PRIMARY_493)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.SFPRIMARY_591)
            return ((ASTFunctionReferenceNode)getChild(0)).hasFunctionArgList();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasFunctionArgList();
        else
            return false;
    }

    protected Token getRealConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return (Token)((ASTUnsignedArithmeticConstantNode)getChild(0)).getRealConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return (Token)((ASTPrimaryBase)getChild(0)).getRealConst();
        else
            return null;
    }

    protected boolean hasRealConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return ((ASTUnsignedArithmeticConstantNode)getChild(0)).hasRealConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasRealConst();
        else
            return false;
    }

    protected Token getDblConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return (Token)((ASTUnsignedArithmeticConstantNode)getChild(0)).getDblConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return (Token)((ASTPrimaryBase)getChild(0)).getDblConst();
        else
            return null;
    }

    protected boolean hasDblConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return ((ASTUnsignedArithmeticConstantNode)getChild(0)).hasDblConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasDblConst();
        else
            return false;
    }

    protected ASTComplexConstNode getComplexConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return (ASTComplexConstNode)((ASTUnsignedArithmeticConstantNode)getChild(0)).getComplexConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTComplexConstNode)((ASTPrimaryBase)getChild(0)).getComplexConst();
        else
            return null;
    }

    protected boolean hasComplexConst()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return ((ASTUnsignedArithmeticConstantNode)getChild(0)).hasComplexConst();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasComplexConst();
        else
            return false;
    }

    protected Token getUnsignedArithConstIntKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return (Token)((ASTUnsignedArithmeticConstantNode)getChild(0)).getUnsignedArithConstIntKind();
        else if (getProduction() == Production.CPRIMARY_496)
            return (Token)((ASTPrimaryBase)getChild(0)).getUnsignedArithConstIntKind();
        else
            return null;
    }

    protected boolean hasUnsignedArithConstIntKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return ((ASTUnsignedArithmeticConstantNode)getChild(0)).hasUnsignedArithConstIntKind();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasUnsignedArithConstIntKind();
        else
            return false;
    }

    protected ASTNamedConstantUseNode getUnsignedArithConstNamedConstKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return (ASTNamedConstantUseNode)((ASTUnsignedArithmeticConstantNode)getChild(0)).getUnsignedArithConstNamedConstKind();
        else if (getProduction() == Production.CPRIMARY_496)
            return (ASTNamedConstantUseNode)((ASTPrimaryBase)getChild(0)).getUnsignedArithConstNamedConstKind();
        else
            return null;
    }

    protected boolean hasUnsignedArithConstNamedConstKind()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIMARY_478)
            return ((ASTUnsignedArithmeticConstantNode)getChild(0)).hasUnsignedArithConstNamedConstKind();
        else if (getProduction() == Production.CPRIMARY_496)
            return ((ASTPrimaryBase)getChild(0)).hasUnsignedArithConstNamedConstKind();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.CPRIMARY_497 && index == 0)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 2)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_483 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_491 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 0)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 2)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 0)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 2)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_501 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 5)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 6)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_512 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 0)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 2)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 0)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 2)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_483 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_491 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 0)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 2)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 0)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 2)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_501 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 5)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 6)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_512 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 0)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 2)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 0)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 2)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_483 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_491 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 0)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 2)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 0)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 2)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_501 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 5)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 6)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_512 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 0)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 2)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 0)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 2)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_483 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_491 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 0)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 2)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 0)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 2)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_501 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 5)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 6)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_512 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 0)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 2)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 0)
            return false;
        else if (getProduction() == Production.CPRIMARY_497 && index == 2)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_481 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_482 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_483 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_484 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_485 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_486 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_487 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 4)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 6)
            return false;
        else if (getProduction() == Production.PRIMARY_488 && index == 8)
            return false;
        else if (getProduction() == Production.PRIMARY_491 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_492 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 1)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 3)
            return false;
        else if (getProduction() == Production.PRIMARY_493 && index == 5)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 0)
            return false;
        else if (getProduction() == Production.PRIMARY_494 && index == 2)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 0)
            return false;
        else if (getProduction() == Production.SFPRIMARY_592 && index == 2)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_500 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_501 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_502 && index == 5)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_503 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 1)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 3)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 4)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 6)
            return false;
        else if (getProduction() == Production.COPERAND_504 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_510 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_511 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_512 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_513 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_514 && index == 5)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_515 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_516 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 1)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 3)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 4)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 6)
            return false;
        else if (getProduction() == Production.UFPRIMARY_517 && index == 8)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 0)
            return false;
        else if (getProduction() == Production.UFPRIMARY_518 && index == 2)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.CPRIMARY_496 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_478 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_489 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_490 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_491 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_492 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_493 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_495 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_589 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_591 && index == 0)
            return true;
        else if (getProduction() == Production.COPERAND_505 && index == 0)
            return true;
        else if (getProduction() == Production.UFPRIMARY_508 && index == 0)
            return true;
        else if (getProduction() == Production.CPRIMARY_496 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_478 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_489 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_490 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_491 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_492 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_493 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_495 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_589 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_591 && index == 0)
            return true;
        else if (getProduction() == Production.COPERAND_505 && index == 0)
            return true;
        else if (getProduction() == Production.UFPRIMARY_508 && index == 0)
            return true;
        else if (getProduction() == Production.CPRIMARY_496 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_478 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_489 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_490 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_491 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_492 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_493 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_495 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_589 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_591 && index == 0)
            return true;
        else if (getProduction() == Production.COPERAND_505 && index == 0)
            return true;
        else if (getProduction() == Production.UFPRIMARY_508 && index == 0)
            return true;
        else if (getProduction() == Production.CPRIMARY_496 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_478 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_489 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_490 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_491 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_492 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_493 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_495 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_589 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_591 && index == 0)
            return true;
        else if (getProduction() == Production.COPERAND_505 && index == 0)
            return true;
        else if (getProduction() == Production.UFPRIMARY_508 && index == 0)
            return true;
        else if (getProduction() == Production.CPRIMARY_496 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_478 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_489 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_490 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_491 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_492 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_493 && index == 0)
            return true;
        else if (getProduction() == Production.PRIMARY_495 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_589 && index == 0)
            return true;
        else if (getProduction() == Production.SFPRIMARY_591 && index == 0)
            return true;
        else if (getProduction() == Production.COPERAND_505 && index == 0)
            return true;
        else if (getProduction() == Production.UFPRIMARY_508 && index == 0)
            return true;
        else
            return false;
    }
}
