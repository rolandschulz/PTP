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
package org.eclipse.photran.internal.core.analysis.binding;

import java.util.List;

import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.parser.ASTAccessIdListNode;
import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAllocatableStmtNode;
import org.eclipse.photran.internal.core.parser.ASTArrayAllocationListNode;
import org.eclipse.photran.internal.core.parser.ASTArrayDeclaratorListNode;
import org.eclipse.photran.internal.core.parser.ASTDimensionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTIntentParListNode;
import org.eclipse.photran.internal.core.parser.ASTIntentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTNamedConstantDefListNode;
import org.eclipse.photran.internal.core.parser.ASTOptionalParListNode;
import org.eclipse.photran.internal.core.parser.ASTOptionalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTParameterStmtNode;
import org.eclipse.photran.internal.core.parser.ASTPointerStmtNode;
import org.eclipse.photran.internal.core.parser.ASTPointerStmtObjectListNode;
import org.eclipse.photran.internal.core.parser.ASTSaveStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSavedEntityListNode;
import org.eclipse.photran.internal.core.parser.ASTSavedEntityNode;
import org.eclipse.photran.internal.core.parser.ASTTargetObjectListNode;
import org.eclipse.photran.internal.core.parser.ASTTargetStmtNode;

/**
 * Visits specification statements in an AST, updating the corresponding
 * Definitions in the VPG.
 * 
 * @author Jeff Overbey
 */
class SpecificationCollector extends BindingCollector
{
    // # R520
    // <IntentStmt> ::=
    // <LblDef> T_INTENT T_LPAREN <IntentSpec> T_RPAREN ( T_COLON T_COLON )? <IntentParList> T_EOS
    //
    // <IntentParList> ::=
    // <IntentPar>
    // | @:<IntentParList> T_COMMA <IntentPar>
    //
    // <IntentPar> ::=
    // <DummyArgName>

    @Override public void visitASTIntentStmtNode(ASTIntentStmtNode node)
    {
        ASTIntentParListNode list = node.getIntentParList();
        for (int i = 0; i < list.size(); i++)
            bind(list.getIntentPar(i).getDummyArgName().getTIdent());
    }

    // # R521
    // <OptionalStmt> ::=
    // <LblDef> T_OPTIONAL ( T_COLON T_COLON )? <OptionalParList> T_EOS
    //
    // <OptionalParList> ::=
    // <OptionalPar>
    // | @:<OptionalParList> T_COMMA <OptionalPar>
    //
    // <OptionalPar> ::= <DummyArgName>

    @Override public void visitASTOptionalStmtNode(ASTOptionalStmtNode node)
    {
        ASTOptionalParListNode list = node.getOptionalParList();
        for (int i = 0; i < list.size(); i++)
            bind(list.getOptionalPar(i).getDummyArgName().getTIdent());
    }

    // # R522
    // <AccessStmt> ::=
    // <LblDef> <AccessSpec> ( T_COLON T_COLON )? <AccessIdList> T_EOS
    // | <LblDef> <AccessSpec> T_EOS
    //
    // # R523
    // <AccessIdList> ::=
    // <AccessId>
    // | @:<AccessIdList> T_COMMA <AccessId>
    //
    // <AccessId> ::=
    // <GenericName>
    // | <GenericSpec>

    @Override public void visitASTAccessStmtNode(final ASTAccessStmtNode node)
    {
        ASTAccessIdListNode list = node.getAccessIdList();
        if (list == null) return; // This case handled in DefinitionCollector

        for (int i = 0; i < list.size(); i++)
        {
            if (list.getAccessId(i).getGenericName() != null)
            {
                List<PhotranTokenRef> bindings = bind(list.getAccessId(i).getGenericName().getTIdent());

                try
                {
	                for (PhotranTokenRef tr : bindings)
	                {
	                	Definition def = vpg.getDefinitionFor(tr);
	                	def.setVisibility(node.getAccessSpec());
	                	vpg.setDefinitionFor(tr, def);
	                }
                }
                catch (Exception e)
                {
                	throw new Error(e);
                }
            }
        }
    }

    // # R524
    // <SaveStmt> ::=
    // <LblDef> T_SAVE T_EOS
    // | <LblDef> T_SAVE ( T_COLON T_COLON )? <SavedEntityList> T_EOS
    //
    // # R525
    // <SavedEntityList> ::=
    // <SavedEntity>
    // | @:<SavedEntityList> T_COMMA <SavedEntity>
    //
    // <SavedEntity> ::=
    // <VariableName>
    // | <SavedCommonBlock>
    //
    // <SavedCommonBlock> ::= T_SLASH <CommonBlockName> T_SLASH

    @Override public void visitASTSaveStmtNode(ASTSaveStmtNode node)
    {
        ASTSavedEntityListNode list = node.getSavedEntityList();
        if (list == null) return;
        for (int i = 0; i < list.size(); i++)
        {
            ASTSavedEntityNode entity = list.getSavedEntity(i);
            if (entity.getVariableName() != null)
                bind(entity.getVariableName().getTIdent());
            else if (entity.getSavedCommonBlock() != null)
                bind(entity.getSavedCommonBlock().getCommonBlockName().getTIdent());
        }
    }

    // # R526
    // <DimensionStmt> ::=
    // <LblDef> T_DIMENSION ( T_COLON T_COLON )? <ArrayDeclaratorList> T_EOS
    //
    // <ArrayDeclaratorList> ::=
    // <ArrayDeclarator>
    // | @:<ArrayDeclaratorList> T_COMMA <ArrayDeclarator>
    //
    // <ArrayDeclarator> ::= <VariableName> T_LPAREN <ArraySpec> T_RPAREN

    @Override public void visitASTDimensionStmtNode(final ASTDimensionStmtNode node)
    {
        final ASTArrayDeclaratorListNode decls = node.getArrayDeclaratorList();
        for (int i = 0; i < decls.size(); i++)
        {
            List<PhotranTokenRef> bindings = bind(decls.getArrayDeclarator(i).getVariableName().getTIdent());

            try
            {
	            for (PhotranTokenRef tr : bindings)
	            {
	            	Definition def = vpg.getDefinitionFor(tr);
	            	def.setArraySpec(decls.getArrayDeclarator(i).getArraySpec());
	            	vpg.setDefinitionFor(tr, def);
	            }
            }
            catch (Exception e)
            {
            	throw new Error(e);
            }
        }
    }

    // # R527
    // <AllocatableStmt> ::=
    // <LblDef> T_ALLOCATABLE ( T_COLON T_COLON )? <ArrayAllocationList> T_EOS
    //
    // <ArrayAllocationList> ::=
    // <ArrayAllocation>
    // | @:<ArrayAllocationList> T_COMMA <ArrayAllocation>
    //
    // <ArrayAllocation> ::=
    // <ArrayName>
    // | <ArrayName> T_LPAREN <DeferredShapeSpecList> T_RPAREN

    @Override public void visitASTAllocatableStmtNode(ASTAllocatableStmtNode node)
    {
        ASTArrayAllocationListNode list = node.getArrayAllocationList();
        for (int i = 0; i < list.size(); i++)
            bind(list.getArrayAllocation(i).getArrayName().getTIdent());
    }

    // # R528 /* <ObjectName> renamed to <PointerName> to simplify Sem. Anal. */
    // <PointerStmt> ::=
    // <LblDef> T_POINTER ( T_COLON T_COLON )? <PointerStmtObjectList> T_EOS
    //
    // <PointerStmtObjectList> ::=
    // <PointerStmtObject>
    // | @:<PointerStmtObjectList> T_COMMA <PointerStmtObject>
    //
    // <PointerStmtObject> ::=
    // <PointerName>
    // | <PointerName> T_LPAREN <DeferredShapeSpecList> T_RPAREN
    //
    // <PointerName> ::= T_IDENT

    @Override public void visitASTPointerStmtNode(ASTPointerStmtNode node)
    {
        ASTPointerStmtObjectListNode list = node.getPointerStmtObjectList();
        for (int i = 0; i < list.size(); i++)
            bind(list.getPointerStmtObject(i).getPointerName().getTIdent());
    }

    //
    //
    // # R529 /* <ObjectName> renamed to <TargetName> to simplify Sem. Anal. */
    // <TargetStmt> ::=
    // <LblDef> T_TARGET ( T_COLON T_COLON )? <TargetObjectList> T_EOS
    //
    // <TargetObjectList> ::=
    // <TargetObject>
    // | @:<TargetObjectList> T_COMMA <TargetObject>
    //
    // <TargetObject> ::=
    // <TargetName>
    // | <TargetName> T_LPAREN <ArraySpec> T_RPAREN
    //
    // <TargetName> ::= T_IDENT

    @Override public void visitASTTargetStmtNode(ASTTargetStmtNode node)
    {
        ASTTargetObjectListNode list = node.getTargetObjectList();
        for (int i = 0; i < list.size(); i++)
            bind(list.getTargetObject(i).getTargetName().getTIdent());
    }

    // # R530
    // <ParameterStmt> ::=
    // <LblDef> T_PARAMETER T_LPAREN <NamedConstantDefList> T_RPAREN T_EOS
    //
    // # R531
    // <NamedConstantDefList> ::=
    // <NamedConstantDef>
    // | @:<NamedConstantDefList> T_COMMA <NamedConstantDef>
    //
    // <NamedConstantDef> ::= <NamedConstant> T_EQUALS <Expr>

    @Override public void visitASTParameterStmtNode(ASTParameterStmtNode node)
    {
        ASTNamedConstantDefListNode list = node.getNamedConstantDefList();
        for (int i = 0; i < list.size(); i++)
            bind(list.getNamedConstantDef(i).getNamedConstant().getTIdent());
    }
}
