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
import org.eclipse.photran.internal.core.parser.ASTFunctionArgListNode;
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
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Phase 6 of name-binding analysis.
 * <p> 
 * Visits an AST, collecting variables references outside declaration and
 * specification statements.
 * <p>
 * Note: Fields in derived types (e.g., <FieldSelector>) and named function arguments are NOT handled.
 * 
 * @author Jeff Overbey
 * @see Binder
 */
class ReferenceCollector extends BindingCollector
{
    @Override public void visitASTSFDataRefNode(ASTSFDataRefNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null)
            bind(node.getName());
    }

    @Override public void visitASTCPrimaryNode(ASTCPrimaryNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null)
            bind(node.getName().getName());
    }

    @Override public void visitASTUFPrimaryNode(ASTUFPrimaryNode node)
    {
        super.traverseChildren(node);
        if (node.getName() != null)
            bind(node.getName().getName());
    }

    // <NamedConstantUse> ::= T_IDENT

    @Override public void visitASTNamedConstantUseNode(ASTNamedConstantUseNode node)
    {
        super.traverseChildren(node);
        bind(node.getName());
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
        bind(node.getImpliedDoVariable().getImpliedDoVariable());
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
        bind(node.getImpliedDoVariable());
    }

    // <DataStmtValue> ::=
    // <DataStmtConstant>
    // | T_ICON T_ASTERISK <DataStmtConstant>
    // | <NamedConstantUse> T_ASTERISK <DataStmtConstant>

    @Override public void visitASTDataStmtValueNode(ASTDataStmtValueNode node)
    {
        super.traverseChildren(node);
        if (node.getNamedConstKind() != null) bind(node.getNamedConstKind().getName());
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
    }
    
    // <ScalarVariable> ::=
    // <VariableName>
    // | <ArrayElement>

    @Override public void visitASTScalarVariableNode(ASTScalarVariableNode node)
    {
        super.traverseChildren(node);
        if (node.getVariableName() != null) bind(node.getVariableName());
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
        
        if (node.getName() != null)
        {
            // <Variable> is the only context where a <DataRef> does not refer to a member of a derived type 
        	if (!node.hasDerivedTypeComponentName() && node.getParent().getParent() instanceof ASTVariableNode)
        		bind(node.getName());
        	else if (!node.hasDerivedTypeComponentName() && node.getParent().getParent() instanceof ASTCallStmtNode)
                bind(node.getName());
        	else
        		dontbind(node.getName());
        }
        if (node.getName/*getComponentName*/() != null)
            dontbind(node.getName/*getComponentName*/());
    }

    // # R614
    // <StructureComponent> ::=
    // <VariableName> <FieldSelector>
    // | @:<StructureComponent> <FieldSelector>

    @Override
    public void visitASTStructureComponentNode(ASTStructureComponentNode node)
    {
        super.traverseChildren(node);
        
        if (node.getVariableName() != null)
            bind(node.getVariableName().getVariableName());
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
        if (node.getVariableName() != null) bind(node.getVariableName());
    }

    // <AllocateObject> ::=
    // <VariableName>
    // | @:<AllocateObject> <FieldSelector>

    @Override public void visitASTAllocateObjectNode(ASTAllocateObjectNode node)
    {
        super.traverseChildren(node);
        
        if (node.getVariableName() != null)
            bind(node.getVariableName().getVariableName());
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

    @Override public void visitASTPointerObjectNode(ASTPointerObjectNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null)
            bind(node.getName().getName());
        else
        {
            IASTListNode<ASTPointerFieldNode> list = node.getPointerField();
            for (int i = 0; i < list.size(); i++)
            {
                if (list.get(i).getName() != null)
                    bind(list.get(i).getName().getName());
                if (list.get(i).getComponentName() != null)
                    dontbind(list.get(i).getComponentName().getName());
                if (list.get(i).getSFDummyArgNameList() != null)
                    for (int j = 0; j < list.get(i).getSFDummyArgNameList().size(); j++)
                        bind(list.get(i).getSFDummyArgNameList().get(j).getName/*getVariableName*/());
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
            bind(node.getName().getName());

        IASTListNode<ASTFunctionArgListNode> list = node.getFunctionArgList();
        if (list != null)
            for (int i = 0; i < list.size(); i++)
                if (list.get(i).getFunctionArg() != null)
                    dontbind(list.get(i).getFunctionArg().getName());
    }

    @Override public void visitASTSFExprListNode(ASTSFExprListNode node)
    {
        super.traverseChildren(node);
        
        if (node.getSFDummyArgNameList() != null)
            for (int j = 0; j < node.getSFDummyArgNameList().size(); j++)
                bind(node.getSFDummyArgNameList().get(j).getName/*getVariableName*/());
    }
    
    @Override public void visitASTSFVarNameNode(ASTSFVarNameNode node)
    {
        super.traverseChildren(node);
        
        bind(node.getName().getName());
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
        
        bind(node.getLhsVariable().getName());
        if (node.getLhsNameList() != null)
            for (int j = 0; j < node.getLhsNameList().size(); j++)
                bind(node.getLhsNameList().get(j).getName());
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
        if (node.getVariableName() != null) bind(node.getVariableName());
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
        if (node.getNamelistGroupName() != null) bind(node.getNamelistGroupName().getNamelistGroupName());
    }

    // # R916
    // <InputImpliedDo> ::=
    // T_LPAREN <InputItemList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <InputItemList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTInputImpliedDoNode(ASTInputImpliedDoNode node)
    {
        super.traverseChildren(node);
        bind(node.getImpliedDoVariable());
    }

    // <OutputImpliedDo> ::=
    // T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <OutputItemList1> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <OutputItemList1> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTOutputImpliedDoNode(ASTOutputImpliedDoNode node)
    {
        super.traverseChildren(node);
        bind(node.getImpliedDoVariable());
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
        if (node.getIdentifier() != null) bind(node.getIdentifier()); // TODO: Is ANY correct?
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
                if (list.get(i) != null)
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
        bind(node.getName().getName());

        if (node.getSFDummyArgNameList() != null)
            for (int j = 0; j < node.getSFDummyArgNameList().size(); j++)
                bind(node.getSFDummyArgNameList().get(j).getName());
    }

    // #/* Assign Statement */
    // <AssignStmt> ::=
    // <LblDef> T_ASSIGN <LblRef> T_TO <VariableName> T_EOS

    @Override public void visitASTAssignStmtNode(ASTAssignStmtNode node)
    {
        super.traverseChildren(node);
        bind(node.getVariableName());
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
        bind(node.getVariableName());
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
            vpg.setDefinitionFor(tokenRef, def);
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
                if (keyword.equals("id") || keyword.equals("iomsg") || keyword.equals("iostat"))
                {
                    Token variable = waitSpec.getExpr().findFirstToken();
                    if (variable != null && variable.getTerminal() == Terminal.T_IDENT)
                        bind(variable);
                }
            }
        }
    }
}
