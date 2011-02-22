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

import org.eclipse.photran.internal.core.analysis.types.FunctionType;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAcImpliedDoNode;
import org.eclipse.photran.internal.core.parser.ASTAllocateObjectNode;
import org.eclipse.photran.internal.core.parser.ASTArrayElementNode;
import org.eclipse.photran.internal.core.parser.ASTAssignStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignedGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCPrimaryNode;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCaseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockObjectNode;
import org.eclipse.photran.internal.core.parser.ASTCommonStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCycleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDataImpliedDoNode;
import org.eclipse.photran.internal.core.parser.ASTDataRefNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtValueNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEditElementNode;
import org.eclipse.photran.internal.core.parser.ASTElseIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTElseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTElseWhereStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndBlockDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndForallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndInterfaceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndSelectStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndWhereStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExitStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFieldSelectorNode;
import org.eclipse.photran.internal.core.parser.ASTFinalBindingNode;
import org.eclipse.photran.internal.core.parser.ASTForallTripletSpecListNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionArgListNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionArgNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionParNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTGenericBindingNode;
import org.eclipse.photran.internal.core.parser.ASTInputImpliedDoNode;
import org.eclipse.photran.internal.core.parser.ASTIoControlSpecNode;
import org.eclipse.photran.internal.core.parser.ASTLoopControlNode;
import org.eclipse.photran.internal.core.parser.ASTMaskedElseWhereStmtNode;
import org.eclipse.photran.internal.core.parser.ASTModuleProcedureStmtNode;
import org.eclipse.photran.internal.core.parser.ASTNamedConstantUseNode;
import org.eclipse.photran.internal.core.parser.ASTNamelistGroupsNode;
import org.eclipse.photran.internal.core.parser.ASTNamelistStmtNode;
import org.eclipse.photran.internal.core.parser.ASTOutputImpliedDoNode;
import org.eclipse.photran.internal.core.parser.ASTPointerFieldNode;
import org.eclipse.photran.internal.core.parser.ASTPointerObjectNode;
import org.eclipse.photran.internal.core.parser.ASTProcedureNameListNode;
import org.eclipse.photran.internal.core.parser.ASTSFDataRefNode;
import org.eclipse.photran.internal.core.parser.ASTSFExprListNode;
import org.eclipse.photran.internal.core.parser.ASTSFVarNameNode;
import org.eclipse.photran.internal.core.parser.ASTScalarVariableNode;
import org.eclipse.photran.internal.core.parser.ASTSectionSubscriptNode;
import org.eclipse.photran.internal.core.parser.ASTSpecificBindingNode;
import org.eclipse.photran.internal.core.parser.ASTStmtFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTStructureComponentNode;
import org.eclipse.photran.internal.core.parser.ASTStructureConstructorNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineArgNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineParNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeAttrSpecNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.ASTUFPrimaryNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.ASTVariableNode;
import org.eclipse.photran.internal.core.parser.ASTWaitSpecNode;
import org.eclipse.photran.internal.core.parser.ASTWaitStmtNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.util.Pair;

/**
 * Phase 7 of name-binding analysis.
 * <p> 
 * Visits an AST, collecting variables references outside declaration and
 * specification statements.  Also marks variable accesses as reads, writes,
 * or both.
 * <p>
 * Note: Fields in derived types (e.g., <FieldSelector>) and named function arguments are NOT handled.
 * 
 * @author Jeff Overbey
 * @see Binder
 */
class ReferenceCollector extends BindingCollector
{
    private void markAccess(Token ident, VariableAccess access)
    {
        vpgProvider.markAccess(ident, access);
    }

    // Occurs only in the context of an <SFExprList>, which provides the
    // argument expressions in an array access (but not a function call)
    @Override public void visitASTSFDataRefNode(ASTSFDataRefNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getName();
        if (ident != null)
        {
            bind(ident);
            markAccess(ident, VariableAccess.READ);
        }
    }

    // Occurs in the context of a <RdFmtId> or a <CExpr>.
    // A <CExpr> occurs in the context of:
    //   <ConnectSpec>, <CloseSpec>, <FormatIdentifier>, <InquireSpec>
    @Override public void visitASTCPrimaryNode(ASTCPrimaryNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null)
        {
            Token ident = node.getName().getName();
            bind(ident);
            markAccess(ident, VariableAccess.RW);
        }
    }

    // Occurs in the context of <UFExpr>,
    // which occurs in the context of a <UnitIdentifier>, <RdUnitId>, <RdFmtIdExpr>
    @Override public void visitASTUFPrimaryNode(ASTUFPrimaryNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null)
        {
            Token ident = node.getName().getName();
            bind(ident);
            markAccess(ident, VariableAccess.RW);
        }
    }

    // <NamedConstantUse> ::= T_IDENT

    @Override public void visitASTNamedConstantUseNode(ASTNamedConstantUseNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getName();
        bind(ident);
        markAccess(ident, VariableAccess.READ);
    }

    // # R430
    // <EndTypeStmt> ::=
    // <LblDef> T_ENDTYPE <TypeName>? T_EOS
    // | <LblDef> T_END T_TYPE <TypeName>? T_EOS

    @Override public void visitASTEndTypeStmtNode(ASTEndTypeStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getTypeName() != null) bind(node.getTypeName().getTypeName());
    }

    // # R431
    // <StructureConstructor> ::= <TypeName> T_LPAREN <ExprList> T_RPAREN

    @Override public void visitASTStructureConstructorNode(ASTStructureConstructorNode node)
    {
        super.traverseChildren(node);
        bind(node.getTypeName());
    }

    // # R434
    // <AcImpliedDo> ::=
    // T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <AcImpliedDo> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <AcImpliedDo> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTAcImpliedDoNode(ASTAcImpliedDoNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getImpliedDoVariable().getImpliedDoVariable();
        bind(ident);
        markAccess(ident, VariableAccess.IMPLIED_DO);
    }

    // # R502
    // <TypeSpec> ::=
    // T_INTEGER
    // | T_REAL
    // | T_DOUBLEPRECISION
    // | T_COMPLEX
    // | T_LOGICAL
    // | T_CHARACTER
    // | T_INTEGER <KindSelector>
    // | T_REAL <KindSelector>
    // | T_DOUBLE T_PRECISION
    // | T_COMPLEX <KindSelector>
    // | T_CHARACTER <CharSelector>
    // | T_LOGICAL <KindSelector>
    // | T_TYPE T_LPAREN <TypeName> T_RPAREN

    @Override public void visitASTTypeSpecNode(ASTTypeSpecNode node)
    {
        super.traverseChildren(node);
        if (node.getTypeName() != null) bind(node.getTypeName());
    }

    // # R535 <Expr> must be scalar-int-expr
    // <DataImpliedDo> ::=
    // T_LPAREN <DataIDoObjectList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <DataIDoObjectList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTDataImpliedDoNode(ASTDataImpliedDoNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getImpliedDoVariable();
        bind(ident);
        markAccess(ident, VariableAccess.IMPLIED_DO);
    }

    // <DataStmtValue> ::=
    // <DataStmtConstant>
    // | T_ICON T_ASTERISK <DataStmtConstant>
    // | <NamedConstantUse> T_ASTERISK <DataStmtConstant>

    // Occurs in the context of a <DataStmtValueList>, which occurs in the context of
    // a <DataStmtSet>
    @Override public void visitASTDataStmtValueNode(ASTDataStmtValueNode node)
    {
        super.traverseChildren(node);
        if (node.getNamedConstKind() != null)
        {
            Token ident = node.getNamedConstKind().getName();
            bind(ident);
            markAccess(ident, VariableAccess.READ);
        }
    }

    // # R544
    // <NamelistStmt> ::=
    // <LblDef> T_NAMELIST <NamelistGroups> T_EOS
    //
    // <NamelistGroups> ::=
    // T_SLASH <NamelistGroupName> T_SLASH <NamelistGroupObject>
    // | @:<NamelistGroups> T_SLASH <NamelistGroupName> T_SLASH <NamelistGroupObject>
    // | @:<NamelistGroups> T_COMMA T_SLASH <NamelistGroupName> T_SLASH <NamelistGroupObject>
    // | @:<NamelistGroups> T_COMMA <NamelistGroupObject>
    //
    // # R545
    // <NamelistGroupObject> ::= <VariableName>

    @Override public void visitASTNamelistStmtNode(ASTNamelistStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTNamelistGroupsNode> groups = node.getNamelistGroups();
        for (int i = 0; i < groups.size(); i++)
        {
            bind(groups.get(i).getVariableName());
        }
    }

    // # R549
    // <CommonStmt> ::=
    // <LblDef> T_COMMON <Comlist> T_EOS
    //
    // <Comlist> ::=
    // <CommonBlockObject>
    // | <Comblock> <CommonBlockObject>
    // | @:<Comlist> T_COMMA <CommonBlockObject>
    // | @:<Comlist> <Comblock> <CommonBlockObject>
    // | @:<Comlist> T_COMMA <Comblock> <CommonBlockObject>
    //
    // <Comblock> ::=
    // T_SLASH T_SLASH
    // | T_SLASH <CommonBlockName> T_SLASH
    //
    // # R550
    // <CommonBlockObject> ::=
    // <VariableName>
    // | <ArrayDeclarator>

    @Override public void visitASTCommonStmtNode(ASTCommonStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTCommonBlockNode> list = node.getCommonBlockList();
        if (list == null) return;
        
        for (int i = 0; i < list.size(); i++)
        {
            IASTListNode<ASTCommonBlockObjectNode> objects = list.get(i).getCommonBlockObjectList();
            for (int j = 0; j < objects.size(); j++)
            {
                ASTCommonBlockObjectNode obj = objects.get(j);
                
                List<PhotranTokenRef> bindings = bind(obj.getVariableName());
                
                if (obj.getArraySpec() != null)
                {
                    try
                    {
                        for (PhotranTokenRef tr : bindings)
                        {
                            Definition def = vpg.getDefinitionFor(tr);
                            def.setArraySpec(obj.getArraySpec());
                            vpgProvider.setDefinitionFor(tr, def);
                        }
                    }
                    catch (Exception e)
                    {
                        throw new Error(e);
                    }
                }
            }
        }
    }
    
    // <ScalarVariable> ::=
    // <VariableName>
    // | <ArrayElement>

    // Occurs in the context of <ConnectSpec>, <CloseSpec>, <IOControlSpec>, <PositionSpec>, <InquireStmt>, <InquireSpec>
    
    @Override public void visitASTScalarVariableNode(ASTScalarVariableNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getVariableName();
        if (ident != null)
        {
            bind(ident);
            markAccess(ident, VariableAccess.WRITE);
        }
    }

    // # R612
    // <DataRef> ::=
    // varName:<Name>
    // | @:<DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN )? T_PERCENT componentName:<Name>
    //
    // # R601 The various forms of variables have to be recognized semantically;
    // <Variable> ::= 
    //     <DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )?
    //   | <SubstrConst>

    @Override public void visitASTDataRefNode(ASTDataRefNode node)
    {
        super.traverseChildren(node);
        
        Token ident = node.getName();
        if (ident != null)
        {
            // <Variable> is the only context where a <DataRef> does not refer to a member of a derived type 
        	if (!node.hasDerivedTypeComponentName() && node.getParent().getParent() instanceof ASTVariableNode)
        	{
        		bind(ident);
        		// FIXME: Variable access
        	}
        	else if (!node.hasDerivedTypeComponentName() && node.getParent().getParent() instanceof ASTCallStmtNode)
        	{
                bind(ident);
                // FIXME: Variable access
        	}
        	else
        	{
        		dontbind(ident);
        	}
        }
        if (node.getName/*getComponentName*/() != null)
            dontbind(node.getName/*getComponentName*/());
    }

    // # R614
    // <StructureComponent> ::=
    // <VariableName> <FieldSelector>
    // | @:<StructureComponent> <FieldSelector>

    // Occurs in the context of <DataIDoObject>, nested <StructureComponent>s, <ArrayElement>, 
    @Override
    public void visitASTStructureComponentNode(ASTStructureComponentNode node)
    {
        super.traverseChildren(node);
        
        if (node.getVariableName() != null)
        {
            Token ident = node.getVariableName().getVariableName();
            bind(ident);
            markAccess(ident, VariableAccess.READ);
        }
    }

    // <FieldSelector> ::=
    //   T_LPAREN <SectionSubscriptList> T_RPAREN T_PERCENT <Name>
    // | T_PERCENT <Name>
    
    @Override public void visitASTFieldSelectorNode(ASTFieldSelectorNode node)
    {
        super.traverseChildren(node);
        dontbind(node.getName/*getComponentName*/());
    }
    
    // # R615
    // <ArrayElement> ::=
    // <VariableName> T_LPAREN <SectionSubscriptList> T_RPAREN
    // | <StructureComponent> T_LPAREN <SectionSubscriptList> T_RPAREN

    @Override public void visitASTArrayElementNode(ASTArrayElementNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getVariableName();
        if (ident != null)
        {
            bind(ident);
            markAccess(ident, VariableAccess.READ);
        }
    }

    // <AllocateObject> ::=
    // <VariableName>
    // | @:<AllocateObject> <FieldSelector>

    @Override public void visitASTAllocateObjectNode(ASTAllocateObjectNode node)
    {
        super.traverseChildren(node);
        
        if (node.getVariableName() != null)
        {
            Token ident = node.getVariableName().getVariableName();
            bind(ident);
            markAccess(ident, VariableAccess.WRITE);
        }
    }

    // # R630
    // <PointerObject> ::=
    // <Name>
    // | <PointerField>
    //
    // <PointerField> ::=
    // <Name> T_LPAREN <SFExprList> T_RPAREN T_PERCENT <Name>
    // | <Name> T_LPAREN <SFDummyArgNameList> T_RPAREN T_PERCENT <Name>
    // | <Name> T_PERCENT <Name>
    // | @:<PointerField> <FieldSelector>

    // Occurs in the context of a NULLIFY statement
    @Override public void visitASTPointerObjectNode(ASTPointerObjectNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null)
        {
            Token ident = node.getName().getName();
            bind(ident);
            markAccess(ident, VariableAccess.WRITE);
        }
        else
        {
            IASTListNode<ASTPointerFieldNode> list = node.getPointerField();
            for (int i = 0; i < list.size(); i++)
            {
                if (list.get(i).getName() != null)
                {
                    Token ident = list.get(i).getName().getName();
                    bind(ident);
                    markAccess(ident, VariableAccess.WRITE);
                }
                
                if (list.get(i).getComponentName() != null)
                    dontbind(list.get(i).getComponentName().getName());
                
                if (list.get(i).getSFDummyArgNameList() != null)
                {
                    for (int j = 0; j < list.get(i).getSFDummyArgNameList().size(); j++)
                    {
                        Token ident = list.get(i).getSFDummyArgNameList().get(j).getName/*getVariableName*/();
                        bind(ident);
                        markAccess(ident, VariableAccess.READ);
                    }
                }
            }
        }
    }

    // # R701
    // <Primary> ::=
    // <LogicalConstant>
    // | T_SCON
    // | <UnsignedArithmeticConstant>
    // | <ArrayConstructor>
    // | <Name> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )?
    // | <Name> T_PERCENT <DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )?
    // | <Name> T_LPAREN <SectionSubscriptList> T_RPAREN T_PERCENT <DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )?
    // | <FunctionReference> ( <SubstringRange> )?
    // | <FunctionReference> T_PERCENT <DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )?
    // | T_LPAREN <Expr> T_RPAREN
    // | <SubstrConst>
    // # JO -- Added substring of constant strings

    @Override public void visitASTVarOrFnRefNode(ASTVarOrFnRefNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null && node.getName().getName() != null && node.getName().getName().getText().trim().length() > 0)
        {
            bind(node.getName().getName());
            markAccess(node.getName().getName(), determineAccess(node));
            // TODO: If this is a function call, we shouldn't mark it with a read access
        }

        IASTListNode<ASTFunctionArgListNode> list = node.getFunctionArgList();
        if (list != null)
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getFunctionArg() != null)
                    dontbind(list.get(i).getFunctionArg().getName());
    }

    private VariableAccess determineAccess(ASTVarOrFnRefNode node)
    {
        if (isVariableExpr(node) && isSubprogramInvocationArg(node))
            return getSubprogramArgIntent(getSubprogramArgInfo(node));
        else
            return VariableAccess.READ;
    }

    private boolean isVariableExpr(ASTVarOrFnRefNode node)
    {
        return node.getName() != null
            && node.getFunctionArgList() == null
            && node.getPrimarySectionSubscriptList() == null
            && node.getSubstringRange() == null
            && node.getDerivedTypeComponentRef() == null
            && node.getComponentSectionSubscriptList() == null
            && node.getSubstringRange2() == null;
    }

    private boolean isSubprogramInvocationArg(ASTVarOrFnRefNode node)
    {
        return getSubprogramArgInfo(node) != null;
    }

    private Pair<Token, ? extends Object> getSubprogramArgInfo(ASTVarOrFnRefNode node)
    {
        Pair<Token, ? extends Object> result = getSubroutineArgInfo(node);
        if (result != null) return result;
        
        result = getFunctionArgInfo(node);
        if (result != null) return result;
        
        result = getPrimarySectionSubcriptInfo(node);
        return result;
    }
    
    private Pair<Token, ? extends Object> getSubroutineArgInfo(ASTVarOrFnRefNode node)
    {
        IASTNode parent = node.getParent();
        IASTNode grandparent = parent.getParent(); if (grandparent == null) return null;
        IASTNode greatGrandparent = grandparent.getParent(); if (greatGrandparent == null) return null;

        if (parent instanceof ASTSubroutineArgNode && greatGrandparent instanceof ASTCallStmtNode)
        {
            ASTCallStmtNode callStmt = (ASTCallStmtNode)greatGrandparent;
            ASTSubroutineArgNode argNode = (ASTSubroutineArgNode)parent;
            if (argNode.getName() != null)
            {
                return new Pair<Token, String>(
                    callStmt.getSubroutineName(),
                    argNode.getName().getText());
            }
            else
            {
                return new Pair<Token, Integer>(
                    callStmt.getSubroutineName(),
                    callStmt.getArgList().indexOf(parent));
            }
        }
        return null;
    }
    
    private Pair<Token, String> getFunctionArgInfo(ASTVarOrFnRefNode node)
    {
        IASTNode parent = node.getParent();
        IASTNode grandparent = parent.getParent(); if (grandparent == null) return null;
        IASTNode greatGrandparent = grandparent.getParent(); if (greatGrandparent == null) return null;
        IASTNode greatGreatGrandparent = greatGrandparent.getParent(); if (greatGreatGrandparent == null) return null;

        if (node.getParent() instanceof ASTFunctionArgNode
            && grandparent instanceof ASTFunctionArgListNode
            && greatGreatGrandparent instanceof ASTVarOrFnRefNode)
        {
            ASTVarOrFnRefNode fnCall = (ASTVarOrFnRefNode)greatGreatGrandparent;
            if (fnCall.getFunctionArgList() != null)
            {
                return new Pair<Token, String>(
                    fnCall.getName().getName(),
                    //fnCall.getFunctionArgList().indexOf(grandparent));
                    ((ASTFunctionArgNode)parent).getName().getText());
            }
        }
        return null;
    }

    private Pair<Token, Integer> getPrimarySectionSubcriptInfo(ASTVarOrFnRefNode node)
    {
        IASTNode parent = node.getParent();
        IASTNode grandparent = parent.getParent(); if (grandparent == null) return null;
        IASTNode greatGrandparent = grandparent.getParent(); if (greatGrandparent == null) return null;

        if (parent instanceof ASTSectionSubscriptNode && greatGrandparent instanceof ASTVarOrFnRefNode)
        {
            ASTVarOrFnRefNode fnCall = (ASTVarOrFnRefNode)greatGrandparent;
            if (fnCall.getPrimarySectionSubscriptList() != null
                    && fnCall.getPrimarySectionSubscriptList().contains(parent))
                return new Pair<Token, Integer>(
                    fnCall.getName().getName(),
                    fnCall.getPrimarySectionSubscriptList().indexOf(parent));
        }
        
        return null;
    }

    private VariableAccess getSubprogramArgIntent(Pair<Token, ? extends Object> subprogramArgInfo)
    {
        Token subprogramName = subprogramArgInfo.fst;
        if (subprogramName != null)
        {
            List<PhotranTokenRef> bindings = bind(subprogramName); // List<Definition> bindings = subprogramName.resolveBinding();
            if (bindings.size() >= 1)
                return getSubprogramArgIntentFromDef(vpg.getDefinitionFor(bindings.get(0)), subprogramArgInfo.snd);
        }
        return VariableAccess.RW;
    }

    private VariableAccess getSubprogramArgIntentFromDef(Definition def, Object subprogramArg)
    {
        if (def != null && def.isSubprogram())
        {
            Type fnType = def.getType();
            if (fnType instanceof FunctionType)
            {
                FunctionType functionType = (FunctionType)fnType;
                if (subprogramArg instanceof Integer)
                    return functionType.getArgumentAccess((Integer)subprogramArg);
                else if (subprogramArg instanceof String)
                    return functionType.getArgumentAccess((String)subprogramArg);
            }

            return VariableAccess.RW; // Should never happen, but default to intent(inout) if it does
        }
        // Not a subprogram
        {
            return VariableAccess.READ;
        }
    }

    @Override public void visitASTSFExprListNode(ASTSFExprListNode node)
    {
        super.traverseChildren(node);
        
        if (node.getSFDummyArgNameList() != null)
        {
            for (int j = 0; j < node.getSFDummyArgNameList().size(); j++)
            {
                Token ident = node.getSFDummyArgNameList().get(j).getName/*getVariableName*/();
                bind(ident);
                markAccess(ident, VariableAccess.READ);
            }
        }
    }
    
    @Override public void visitASTSFVarNameNode(ASTSFVarNameNode node)
    {
        super.traverseChildren(node);
        
        Token ident = node.getName().getName();
        bind(ident);
        markAccess(ident, VariableAccess.READ);
    }
    
    // # R735 - JO - Macro substituted
    // <AssignmentStmt> ::=
    // <LblDef> <Name> T_EQUALS <Expr> T_EOS
    // | <LblDef> <Name> T_LPAREN <SFExprList> T_RPAREN T_EQUALS <Expr> T_EOS
    // | <LblDef> <Name> T_LPAREN <SFExprList> T_RPAREN <SubstringRange> T_EQUALS <Expr> T_EOS
    // | <LblDef> <Name> T_LPAREN <SFDummyArgNameList> T_RPAREN <SubstringRange> T_EQUALS <Expr> T_EOS
    // | <LblDef> <Name> T_PERCENT <DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )? T_EQUALS <Expr> T_EOS
    // | <LblDef> <Name> T_LPAREN <SFExprList> T_RPAREN T_PERCENT <DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )? T_EQUALS <Expr> T_EOS
    // | <LblDef> <Name> T_LPAREN <SFDummyArgNameList> T_RPAREN T_PERCENT <DataRef> ( T_LPAREN <SectionSubscriptList> T_RPAREN ( <SubstringRange> )? )? T_EQUALS
    // <Expr> T_EOS

    @Override public void visitASTAssignmentStmtNode(ASTAssignmentStmtNode node)
    {
        super.traverseChildren(node);
        
        Token lhsIdent = node.getLhsVariable().getName();
        bind(lhsIdent);
        markAccess(lhsIdent, VariableAccess.WRITE);
        
        if (node.getLhsNameList() != null)
        {
            for (int j = 0; j < node.getLhsNameList().size(); j++)
            {
                Token ident = node.getLhsNameList().get(j).getName();
                bind(ident);
                markAccess(ident, VariableAccess.READ);
            }
        }
    }

    // # R744
    // <MaskedElsewhereStmt> ::=
    // <LblDef> T_ELSEWHERE T_LPAREN <MaskExpr> T_RPAREN ( <EndName> )? T_EOS

    @Override public void visitASTMaskedElseWhereStmtNode(ASTMaskedElseWhereStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R745
    // <ElsewhereStmt> ::=
    // <LblDef> T_ELSEWHERE ( <EndName> )? T_EOS

    @Override public void visitASTElseWhereStmtNode(ASTElseWhereStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R746
    // <EndWhereStmt> ::=
    // <LblDef> T_ENDWHERE ( <EndName> )? T_EOS
    // | <LblDef> T_END T_WHERE ( <EndName> )? T_EOS

    @Override public void visitASTEndWhereStmtNode(ASTEndWhereStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R750
    // <ForallTripletSpecList> ::=
    //     | <Name> -:T_EQUALS Lb:<Subscript> -:T_COLON Ub:<Subscript>
    //     | <Name> -:T_EQUALS Lb:<Subscript> -:T_COLON Ub:<Subscript> -:T_COLON stepExpr:<Expr>

    @Override public void visitASTForallTripletSpecListNode(ASTForallTripletSpecListNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getName().getName();
        bind(ident);
        markAccess(ident, VariableAccess.FORALL);
    }
    
    // # R753
    // <EndForallStmt> ::=
    // <LblDef> T_END T_FORALL ( <EndName> )? T_EOS
    // | <LblDef> T_ENDFORALL ( <EndName> )? T_EOS

    @Override public void visitASTEndForallStmtNode(ASTEndForallStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R804
    // <ElseIfStmt> ::=
    // <LblDef> T_ELSEIF T_LPAREN <Expr> T_RPAREN T_THEN T_EOS
    // | <LblDef> T_ELSEIF T_LPAREN <Expr> T_RPAREN T_THEN <EndName> T_EOS
    // | <LblDef> T_ELSE T_IF T_LPAREN <Expr> T_RPAREN T_THEN T_EOS
    // | <LblDef> T_ELSE T_IF T_LPAREN <Expr> T_RPAREN T_THEN <EndName> T_EOS

    @Override public void visitASTElseIfStmtNode(ASTElseIfStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R805
    // <ElseStmt> ::=
    // <LblDef> T_ELSE T_EOS
    // | <LblDef> T_ELSE <EndName> T_EOS

    @Override public void visitASTElseStmtNode(ASTElseStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R806
    // <EndIfStmt> ::=
    // <LblDef> T_ENDIF ( <EndName> )? T_EOS
    // | <LblDef> T_END T_IF ( <EndName> )? T_EOS

    @Override public void visitASTEndIfStmtNode(ASTEndIfStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R810
    // <CaseStmt> ::=
    // <LblDef> T_CASE <CaseSelector> T_EOS
    // | <LblDef> T_CASE <CaseSelector> <Name> T_EOS

    @Override public void visitASTCaseStmtNode(ASTCaseStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null) bind(node.getName().getName());
    }

    // # R811
    // <EndSelectStmt> ::=
    // <LblDef> T_ENDSELECT ( <EndName> )? T_EOS
    // | <LblDef> T_END T_SELECT ( <EndName> )? T_EOS

    @Override public void visitASTEndSelectStmtNode(ASTEndSelectStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // <LoopControl> ::=
    // <VariableName> T_EQUALS <Expr> T_COMMA <Expr>
    // | <VariableName> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr>
    // | T_WHILE T_LPAREN <Expr> T_RPAREN

    @Override public void visitASTLoopControlNode(ASTLoopControlNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getVariableName();
        if (ident != null)
        {
            bind(ident);
            markAccess(ident, VariableAccess.WRITE);
        }
    }

    // # R825
    // <EndDoStmt> ::=
    // <LblDef> T_ENDDO ( <EndName> )? T_EOS
    // | <LblDef> T_END T_DO ( <EndName> )? T_EOS

    @Override public void visitASTEndDoStmtNode(ASTEndDoStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R834
    // <CycleStmt> ::=
    // <LblDef> T_CYCLE ( <Name> )? T_EOS

    @Override public void visitASTCycleStmtNode(ASTCycleStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null) bind(node.getName());
    }

    // # R835
    // <ExitStmt> ::=
    // <LblDef> T_EXIT ( <Name> )? T_EOS

    @Override public void visitASTExitStmtNode(ASTExitStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null) bind(node.getName());
    }

    // <IoControlSpec> ::=
    // T_FMTEQ <FormatIdentifier>
    // | T_UNITEQ <UnitIdentifier>
    // | T_RECEQ <Expr>
    // | T_ENDEQ <LblRef>
    // | T_ERREQ <LblRef>
    // | T_IOSTATEQ <ScalarVariable>
    // | T_NMLEQ <NamelistGroupName>
    // | T_ADVANCEEQ <CExpr>
    // | T_SIZEEQ <Variable>
    // | T_EOREQ <LblRef>

    @Override public void visitASTIoControlSpecNode(ASTIoControlSpecNode node)
    {
        super.traverseChildren(node);
        if (node.getNamelistGroupName() != null)
        {
            Token ident = node.getNamelistGroupName().getNamelistGroupName();
            bind(ident);
            markAccess(ident, VariableAccess.WRITE); // TODO: Is WRITE what we want?
        }
    }

    // # R916
    // <InputImpliedDo> ::=
    // T_LPAREN <InputItemList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <InputItemList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTInputImpliedDoNode(ASTInputImpliedDoNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getImpliedDoVariable();
        bind(ident);
        markAccess(ident, VariableAccess.IMPLIED_DO);
    }

    // <OutputImpliedDo> ::=
    // T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <OutputItemList1> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <OutputItemList1> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTOutputImpliedDoNode(ASTOutputImpliedDoNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getImpliedDoVariable();
        bind(ident);
        markAccess(ident, VariableAccess.IMPLIED_DO);
    }

    // <EditElement> ::=
    // T_FCON
    // | T_SCON
    // | T_IDENT
    // | T_LPAREN <FmtSpec> T_RPAREN
    // | T_HCON

    @Override public void visitASTEditElementNode(ASTEditElementNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getIdentifier();
        if (ident != null)
        {
            bind(ident); // TODO: Is ANY correct?
            markAccess(ident, VariableAccess.READ);
        }
    }

    // # R1103
    // <EndProgramStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDPROGRAM ( <EndName> )? T_EOS
    // | <LblDef> T_END T_PROGRAM ( <EndName> )? T_EOS

    @Override public void visitASTEndProgramStmtNode(ASTEndProgramStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R1106
    // <EndModuleStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDMODULE ( <EndName> )? T_EOS
    // | <LblDef> T_END T_MODULE ( <EndName> )? T_EOS

    @Override public void visitASTEndModuleStmtNode(ASTEndModuleStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R1114
    // <EndBlockDataStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDBLOCKDATA ( <EndName> )? T_EOS
    // | <LblDef> T_END T_BLOCKDATA ( <EndName> )? T_EOS
    // | <LblDef> T_ENDBLOCK T_DATA ( <EndName> )? T_EOS
    // | <LblDef> T_END T_BLOCK T_DATA ( <EndName> )? T_EOS

    @Override public void visitASTEndBlockDataStmtNode(ASTEndBlockDataStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R1204
    // <EndInterfaceStmt> ::=
    // <LblDef> T_ENDINTERFACE (<EndName>)? T_EOS
    // | <LblDef> T_END T_INTERFACE (<EndName>)? T_EOS

    @Override public void visitASTEndInterfaceStmtNode(ASTEndInterfaceStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName().getEndName());
    }

    // # R1206
    // <ModuleProcedureStmt> ::=
    // <LblDef> T_MODULE T_PROCEDURE <ProcedureNameList> T_EOS
    //
    // <ProcedureNameList> ::=
    // <ProcedureName>
    // | @:<ProcedureNameList> T_COMMA <ProcedureName>
    //
    // <ProcedureName> ::= T_IDENT

    @Override public void visitASTModuleProcedureStmtNode(ASTModuleProcedureStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTProcedureNameListNode> list = node.getProcedureNameList();
        for (int i = 0; i < list.size(); i++)
            bind(list.get(i).getProcedureName());
    }

    // # R1211
    // <CallStmt> ::=
    // <LblDef> T_CALL <SubroutineNameUse> T_EOS
    // | <LblDef> T_CALL <SubroutineNameUse> T_LPAREN <SubroutineArgList> T_RPAREN T_EOS
    //
    // <SubroutineArgList> ::=
    // /empty/
    // | <SubroutineArg>
    // | @:<SubroutineArgList> T_COMMA <SubroutineArg>
    //
    // <SubroutineArg> ::=
    // <Expr>
    // | T_ASTERISK <LblRef>
    // | <Name> T_EQUALS <Expr>
    // | <Name> T_EQUALS T_ASTERISK <LblRef>
    // | T_HCON
    // | <Name> T_EQUALS T_HCON

    @Override public void visitASTCallStmtNode(ASTCallStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getSubroutineName() != null)
            bind(node.getSubroutineName());

        IASTListNode<ASTSubroutineArgNode> list = node.getArgList();
        if (list != null)
            for (int i = 0; i < list.size(); i++)
                if (list.get(i) != null && list.get(i).getName() != null)
                    dontbind(list.get(i).getName());
    }

    // # R1217 chain rule deleted
    // <FunctionStmt> ::=
    // <LblDef> <FunctionPrefix> <FunctionName> T_LPAREN <FunctionPars> T_RPAREN ( T_RESULT T_LPAREN <Name> T_RPAREN )? T_EOS
    // | <LblDef> <FunctionPrefix> <FunctionName> /error/ T_EOS
    //
    // <FunctionPars> ::=
    // /empty/
    // | <FunctionPar>
    // | @:<FunctionPars> T_COMMA <FunctionPar>
    //
    // <FunctionPar> ::= <DummyArgName>
    //
    // # R1218
    // <FunctionPrefix> ::=
    // T_FUNCTION
    // | <PrefixSpecList> T_FUNCTION
    //
    // <PrefixSpecList> ::=
    // <PrefixSpec>
    // | @:<PrefixSpecList> <PrefixSpec>
    //
    // # R1219
    // <PrefixSpec> ::=
    // <TypeSpec>
    // | T_RECURSIVE
    // | T_PURE
    // | T_ELEMENTAL

    @Override public void visitASTFunctionStmtNode(ASTFunctionStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTFunctionParNode> list = node.getFunctionPars();
        if (list != null)
            for (int i = 0; i < list.size(); i++)
                bindAsParam(list.get(i).getVariableName());
        
        if (node.hasResultClause())
            bind(node.getName());
    }

    // # R1220
    // <EndFunctionStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDFUNCTION ( <EndName> )? T_EOS
    // | <LblDef> T_END T_FUNCTION ( <EndName> )? T_EOS

    @Override public void visitASTEndFunctionStmtNode(ASTEndFunctionStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R1222
    // <SubroutineStmt> ::=
    // <LblDef> <SubroutinePrefix> <SubroutineName> ( T_LPAREN <SubroutinePars> T_RPAREN )? T_EOS
    // | <LblDef> <SubroutinePrefix> <SubroutineName> /error/ T_EOS
    //
    // <SubroutinePrefix> ::=
    // T_SUBROUTINE
    // | <PrefixSpecList> T_SUBROUTINE
    //
    // <SubroutinePars> ::=
    // /empty/
    // | <SubroutinePar>
    // | @:<SubroutinePars> T_COMMA <SubroutinePar>
    //
    // # R1223
    // <SubroutinePar> ::=
    // <DummyArgName>
    // | T_ASTERISK

    @Override public void visitASTSubroutineStmtNode(ASTSubroutineStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getSubroutinePars() != null)
        {
            IASTListNode<ASTSubroutineParNode> list = node.getSubroutinePars();
            for (int i = 0; i < list.size(); i++)
                if (list.get(i) != null && !list.get(i).isAsterisk())
                    bindAsParam(list.get(i).getVariableName());
        }
    }

    // # R1224
    // <EndSubroutineStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDSUBROUTINE ( <EndName> )? T_EOS
    // | <LblDef> T_END T_SUBROUTINE ( <EndName> )? T_EOS

    @Override public void visitASTEndSubroutineStmtNode(ASTEndSubroutineStmtNode node)
    {
        super.traverseChildren(node);
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R1228
    // # This may turn out to be an assignment statement, but the form given here
    // # allows for name analysis in the case that it actually IS a statement
    // # function definition;
    // <StmtFunctionStmt> ::= <LblDef> <Name> <StmtFunctionRange>
    //
    // <StmtFunctionRange> ::= T_LPAREN T_RPAREN T_EQUALS <Expr> T_EOS
    //
    // <StmtFunctionRange> ::= T_LPAREN <SFDummyArgNameList> T_RPAREN T_EQUALS <Expr> T_EOS
    //
    // <SFDummyArgNameList> ::=
    // <SFDummyArgName>
    // | @:<SFDummyArgNameList> T_COMMA <SFDummyArgName>

    @Override public void visitASTStmtFunctionStmtNode(ASTStmtFunctionStmtNode node)
    {
        super.traverseChildren(node);
        
        // Assume this is actually an assignment statement instead of a statement function
        
        //addDefinition(node.getName(), Type.UNKNOWN);
        Token functionName = node.getName().getName();
        bind(functionName);

        if (node.getSFDummyArgNameList() != null)
        {
            for (int j = 0; j < node.getSFDummyArgNameList().size(); j++)
            {
                Token argName = node.getSFDummyArgNameList().get(j).getName();
                bind(argName);
                markAccess(functionName, VariableAccess.STMT_FUNCTION_ARG);
            }
        }
    }

    // #/* Assign Statement */
    // <AssignStmt> ::=
    // <LblDef> T_ASSIGN <LblRef> T_TO <VariableName> T_EOS

    @Override public void visitASTAssignStmtNode(ASTAssignStmtNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getVariableName();
        bind(ident);
        markAccess(ident, VariableAccess.WRITE);
    }

    // #/* Assigned GOTO Statement */
    // <AssignedGotoStmt> ::=
    // <LblDef> <GoToKw> <VariableName> T_EOS
    // | <LblDef> <GoToKw> <VariableName> T_LPAREN <LblRefList> T_RPAREN T_EOS
    // | <LblDef> <GoToKw> <VariableComma> T_LPAREN <LblRefList> T_RPAREN T_EOS
    //
    // <VariableComma> ::= <VariableName> T_COMMA

    @Override public void visitASTAssignedGotoStmtNode(ASTAssignedGotoStmtNode node)
    {
        super.traverseChildren(node);
        Token ident = node.getVariableName();
        bind(ident);
        markAccess(ident, VariableAccess.READ);
    }

    // F03
    @Override public void visitASTDerivedTypeStmtNode(ASTDerivedTypeStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getTypeAttrSpecList() != null)
            for (ASTTypeAttrSpecNode spec : node.getTypeAttrSpecList())
                if (spec.isExtends())
                    bind(spec.getParentTypeName());
    }
    
    // F03
    @Override public void visitASTSpecificBindingNode(ASTSpecificBindingNode node)
    {
        super.visitASTSpecificBindingNode(node);
        
        if (node.getInterfaceName() != null)
            bind(node.getInterfaceName());
        
        if (node.getProcedureName() != null)
            setTypeBoundProcedureAttribInDefinition(bind(node.getProcedureName()), true);
        else
            setTypeBoundProcedureAttribInDefinition(bind(node.getBindingName()), false);
    }
    
    private void setTypeBoundProcedureAttribInDefinition(List<PhotranTokenRef> definitionTokenRefs, boolean renamed)
    {
        for (PhotranTokenRef tokenRef : definitionTokenRefs)
        {
            Definition def = PhotranVPG.getInstance().getDefinitionFor(tokenRef);
            def.markAsTypeBoundProcedure(renamed);
            vpgProvider.setDefinitionFor(tokenRef, def);
        }
    }

    // F03
    @Override public void visitASTGenericBindingNode(ASTGenericBindingNode node)
    {
        super.visitASTGenericBindingNode(node);
        
        for (Token name : node.getBindingNameList())
            bind(name);
    }
    
    // F03
    @Override public void visitASTFinalBindingNode(ASTFinalBindingNode node)
    {
        super.visitASTFinalBindingNode(node);
        
        for (Token name : node.getFinalSubroutineNameList())
            bind(name);
    }
    
    // F03
    @Override public void visitASTWaitStmtNode(ASTWaitStmtNode node)
    {
        super.traverseChildren(node);
        
        for (ASTWaitSpecNode waitSpec : node.getWaitSpecList())
        {
            if (waitSpec.getKeyword() != null)
            {
                String keyword = waitSpec.getKeyword().getText().toLowerCase();
                if (keyword.equals("id") || keyword.equals("iomsg") || keyword.equals("iostat")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                {
                    Token variable = waitSpec.getExpr().findFirstToken();
                    if (variable != null && variable.getTerminal() == Terminal.T_IDENT)
                    {
                        bind(variable);
                        markAccess(variable, VariableAccess.WRITE);
                    }
                }
            }
        }
    }
}
