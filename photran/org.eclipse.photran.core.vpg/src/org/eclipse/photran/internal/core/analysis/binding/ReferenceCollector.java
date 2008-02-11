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
import org.eclipse.photran.internal.core.parser.ASTAcImpliedDoNode;
import org.eclipse.photran.internal.core.parser.ASTAllocateObjectNode;
import org.eclipse.photran.internal.core.parser.ASTArrayElementNode;
import org.eclipse.photran.internal.core.parser.ASTAssignStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignedGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCaseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockListNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockObjectListNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockObjectNode;
import org.eclipse.photran.internal.core.parser.ASTCommonStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCycleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDataImpliedDoNode;
import org.eclipse.photran.internal.core.parser.ASTDataRefNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtValueNode;
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
import org.eclipse.photran.internal.core.parser.ASTFunctionArgListNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionParsNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
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
import org.eclipse.photran.internal.core.parser.ASTPrimaryNode;
import org.eclipse.photran.internal.core.parser.ASTProcedureNameListNode;
import org.eclipse.photran.internal.core.parser.ASTSFExprListNode;
import org.eclipse.photran.internal.core.parser.ASTScalarVariableNode;
import org.eclipse.photran.internal.core.parser.ASTStmtFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTStructureComponentNode;
import org.eclipse.photran.internal.core.parser.ASTStructureConstructorNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineArgListNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineParsNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.Parser.Nonterminal;

/**
 * Visits an AST, collecting variables references outside declaration and
 * specification statements.
 * <p>
 * Note: Fields in derived types (e.g., <FieldSelector>) and named function arguments are NOT handled.
 * 
 * @author Jeff Overbey
 */
class ReferenceCollector extends BindingCollector
{
    // <NamedConstantUse> ::= T_IDENT

    @Override public void visitASTNamedConstantUseNode(ASTNamedConstantUseNode node)
    {
        bind(node.getName());
    }

    // # R430
    // <EndTypeStmt> ::=
    // <LblDef> T_ENDTYPE <TypeName>? T_EOS
    // | <LblDef> T_END T_TYPE <TypeName>? T_EOS

    @Override public void visitASTEndTypeStmtNode(ASTEndTypeStmtNode node)
    {
        if (node.getTypeName() != null) bind(node.getTypeName().getTypeName());
    }

    // # R431
    // <StructureConstructor> ::= <TypeName> T_LPAREN <ExprList> T_RPAREN

    @Override public void visitASTStructureConstructorNode(ASTStructureConstructorNode node)
    {
        bind(node.getTypeName().getTypeName());
    }

    // # R434
    // <AcImpliedDo> ::=
    // T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <AcImpliedDo> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <AcImpliedDo> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTAcImpliedDoNode(ASTAcImpliedDoNode node)
    {
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
        if (node.getTypeName() != null) bind(node.getTypeName());
    }

    // # R535 <Expr> must be scalar-int-expr
    // <DataImpliedDo> ::=
    // T_LPAREN <DataIDoObjectList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <DataIDoObjectList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTDataImpliedDoNode(ASTDataImpliedDoNode node)
    {
        bind(node.getImpliedDoVariable());
    }

    // <DataStmtValue> ::=
    // <DataStmtConstant>
    // | T_ICON T_ASTERISK <DataStmtConstant>
    // | <NamedConstantUse> T_ASTERISK <DataStmtConstant>

    @Override public void visitASTDataStmtValueNode(ASTDataStmtValueNode node)
    {
        if (node.hasNamedConstKind()) bind(node.getNamedConstKind().getName());
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
        ASTNamelistGroupsNode groups = node.getNamelistGroups();
        for (int i = 0; i < groups.size(); i++)
        {
            bind(groups.getNamelistGroupObjectVariableName(i));
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
        ASTCommonBlockListNode list = node.getCommonBlockList();
        if (list == null) return;
        
        for (int i = 0; i < list.size(); i++)
        {
            ASTCommonBlockObjectListNode objects = list.getCommonBlock(i).getCommonBlockObjectList();
            for (int j = 0; j < objects.size(); j++)
            {
                ASTCommonBlockObjectNode obj = objects.getCommonBlockObject(i);
                
                List<PhotranTokenRef> bindings = bind(obj.getVariableName());
                
                if (obj.hasArraySpec())
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
        
    	for (int i = 0; i < node.size(); i++)
        {
            if (node.getName(i) != null)
            {
                // <Variable> is the only context where a <DataRef> does not refer to a member of a derived type 
            	if (node.getParent().getNonterminal() == Nonterminal.VARIABLE)
            		bind(node.getName(i));
            	else
            		dontbind(node.getName(i));
            }
            if (node.getComponentName(i) != null)
                dontbind(node.getComponentName(i));
        }
    }

    // # R614
    // <StructureComponent> ::=
    // <VariableName> <FieldSelector>
    // | @:<StructureComponent> <FieldSelector>

    @Override
    public void visitASTStructureComponentNode(ASTStructureComponentNode node)
    {
        for (int i = 0; i < node.size(); i++)
            if (node.getVariableName(i) != null)
                bind(node.getVariableName(i).getVariableName());
    }

    // <FieldSelector> ::=
    //   T_LPAREN <SectionSubscriptList> T_RPAREN T_PERCENT <Name>
    // | T_PERCENT <Name>
    
    @Override public void visitASTFieldSelectorNode(ASTFieldSelectorNode node)
    {
        dontbind(node.getComponentName());
    }
    
    // # R615
    // <ArrayElement> ::=
    // <VariableName> T_LPAREN <SectionSubscriptList> T_RPAREN
    // | <StructureComponent> T_LPAREN <SectionSubscriptList> T_RPAREN

    @Override public void visitASTArrayElementNode(ASTArrayElementNode node)
    {
        if (node.getVariableName() != null) bind(node.getVariableName());
    }

    // <AllocateObject> ::=
    // <VariableName>
    // | @:<AllocateObject> <FieldSelector>

    @Override public void visitASTAllocateObjectNode(ASTAllocateObjectNode node)
    {
        for (int i = 0; i < node.size(); i++)
            if (node.getVariableName(i) != null)
                bind(node.getVariableName(i).getVariableName());
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
        if (node.getName() != null)
            bind(node.getName().getName());
        else
        {
            ASTPointerFieldNode list = node.getPointerField();
            for (int i = 0; i < list.size(); i++)
            {
                if (list.getName(i) != null)
                    bind(list.getName(i).getName());
                if (list.getComponentName(i) != null)
                    dontbind(list.getComponentName(i));
                if (list.getSFDummyArgNameList(i) != null)
                    for (int j = 0; j < list.getSFDummyArgNameList(i).size(); j++)
                        bind(list.getSFDummyArgNameList(i).getVariableName(j));
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

    @Override public void visitASTPrimaryNode(ASTPrimaryNode node)
    {
        if (node.getName() != null) bind(node.getName().getName());

        ASTFunctionArgListNode list = node.getFunctionArgList();
        if (list != null)
            for (int i = 0; i < list.size(); i++)
                if (list.getFunctionArg(i) != null)
                    dontbind(list.getFunctionArg(i).getName());
    }

    @Override public void visitASTSFExprListNode(ASTSFExprListNode node)
    {
        for (int i = 0; i < node.size(); i++)
            if (node.hasSFDummyArgNameList(i))
                for (int j = 0; j < node.getSFDummyArgNameList(i).size(); j++)
                    bind(node.getSFDummyArgNameList(i).getVariableName(j));
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
        bind(node.getLhsVariableName());
        if (node.hasLhsNameList())
            for (int j = 0; j < node.getLhsNameList().size(); j++)
                bind(node.getLhsNameList().getVariableName(j));
    }

    // # R744
    // <MaskedElsewhereStmt> ::=
    // <LblDef> T_ELSEWHERE T_LPAREN <MaskExpr> T_RPAREN ( <EndName> )? T_EOS

    @Override public void visitASTMaskedElseWhereStmtNode(ASTMaskedElseWhereStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R745
    // <ElsewhereStmt> ::=
    // <LblDef> T_ELSEWHERE ( <EndName> )? T_EOS

    @Override public void visitASTElseWhereStmtNode(ASTElseWhereStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R746
    // <EndWhereStmt> ::=
    // <LblDef> T_ENDWHERE ( <EndName> )? T_EOS
    // | <LblDef> T_END T_WHERE ( <EndName> )? T_EOS

    @Override public void visitASTEndWhereStmtNode(ASTEndWhereStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R753
    // <EndForallStmt> ::=
    // <LblDef> T_END T_FORALL ( <EndName> )? T_EOS
    // | <LblDef> T_ENDFORALL ( <EndName> )? T_EOS

    @Override public void visitASTEndForallStmtNode(ASTEndForallStmtNode node)
    {
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
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R805
    // <ElseStmt> ::=
    // <LblDef> T_ELSE T_EOS
    // | <LblDef> T_ELSE <EndName> T_EOS

    @Override public void visitASTElseStmtNode(ASTElseStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R806
    // <EndIfStmt> ::=
    // <LblDef> T_ENDIF ( <EndName> )? T_EOS
    // | <LblDef> T_END T_IF ( <EndName> )? T_EOS

    @Override public void visitASTEndIfStmtNode(ASTEndIfStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R810
    // <CaseStmt> ::=
    // <LblDef> T_CASE <CaseSelector> T_EOS
    // | <LblDef> T_CASE <CaseSelector> <Name> T_EOS

    @Override public void visitASTCaseStmtNode(ASTCaseStmtNode node)
    {
        if (node.getName() != null) bind(node.getName().getName());
    }

    // # R811
    // <EndSelectStmt> ::=
    // <LblDef> T_ENDSELECT ( <EndName> )? T_EOS
    // | <LblDef> T_END T_SELECT ( <EndName> )? T_EOS

    @Override public void visitASTEndSelectStmtNode(ASTEndSelectStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // <LoopControl> ::=
    // <VariableName> T_EQUALS <Expr> T_COMMA <Expr>
    // | <VariableName> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr>
    // | T_WHILE T_LPAREN <Expr> T_RPAREN

    @Override public void visitASTLoopControlNode(ASTLoopControlNode node)
    {
        if (node.getLoopVariableName() != null) bind(node.getLoopVariableName());
    }

    // # R825
    // <EndDoStmt> ::=
    // <LblDef> T_ENDDO ( <EndName> )? T_EOS
    // | <LblDef> T_END T_DO ( <EndName> )? T_EOS

    @Override public void visitASTEndDoStmtNode(ASTEndDoStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R834
    // <CycleStmt> ::=
    // <LblDef> T_CYCLE ( <Name> )? T_EOS

    @Override public void visitASTCycleStmtNode(ASTCycleStmtNode node)
    {
        if (node.getName() != null) bind(node.getName());
    }

    // # R835
    // <ExitStmt> ::=
    // <LblDef> T_EXIT ( <Name> )? T_EOS

    @Override public void visitASTExitStmtNode(ASTExitStmtNode node)
    {
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
        if (node.getNamelistGroupName() != null) bind(node.getNamelistGroupName().getNamelistGroupName());
    }

    // # R916
    // <InputImpliedDo> ::=
    // T_LPAREN <InputItemList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <InputItemList> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTInputImpliedDoNode(ASTInputImpliedDoNode node)
    {
        bind(node.getImpliedDoVariable());
    }

    // <OutputImpliedDo> ::=
    // T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <Expr> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <OutputItemList1> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_RPAREN
    // | T_LPAREN <OutputItemList1> T_COMMA <ImpliedDoVariable> T_EQUALS <Expr> T_COMMA <Expr> T_COMMA <Expr> T_RPAREN

    @Override public void visitASTOutputImpliedDoNode(ASTOutputImpliedDoNode node)
    {
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
        if (node.getIdentifier() != null) bind(node.getIdentifier()); // TODO: Is ANY correct?
    }

    // # R1103
    // <EndProgramStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDPROGRAM ( <EndName> )? T_EOS
    // | <LblDef> T_END T_PROGRAM ( <EndName> )? T_EOS

    @Override public void visitASTEndProgramStmtNode(ASTEndProgramStmtNode node)
    {
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R1106
    // <EndModuleStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDMODULE ( <EndName> )? T_EOS
    // | <LblDef> T_END T_MODULE ( <EndName> )? T_EOS

    @Override public void visitASTEndModuleStmtNode(ASTEndModuleStmtNode node)
    {
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
        if (node.getEndName() != null) bind(node.getEndName());
    }

    // # R1204
    // <EndInterfaceStmt> ::=
    // <LblDef> T_ENDINTERFACE (<EndName>)? T_EOS
    // | <LblDef> T_END T_INTERFACE (<EndName>)? T_EOS

    @Override public void visitASTEndInterfaceStmtNode(ASTEndInterfaceStmtNode node)
    {
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
        ASTProcedureNameListNode list = node.getProcedureNameList();
        for (int i = 0; i < list.size(); i++)
            bind(list.getProcedureName(i));
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
        bind(node.getSubroutineName());

        ASTSubroutineArgListNode list = node.getSubroutineArgList();
        if (list != null)
            for (int i = 0; i < list.size(); i++)
                if (list.getSubroutineArg(i) != null && list.getSubroutineArg(i).getName() != null)
                    dontbind(list.getSubroutineArg(i).getName());
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
        ASTFunctionParsNode list = node.getFunctionPars();
        if (list != null)
            for (int i = 0; i < list.size(); i++)
                bindAsParam(list.getFunctionPar(i).getVariableName());
        
        if (node.hasResultClause())
            bind(node.getResultName());
    }

    // # R1220
    // <EndFunctionStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDFUNCTION ( <EndName> )? T_EOS
    // | <LblDef> T_END T_FUNCTION ( <EndName> )? T_EOS

    @Override public void visitASTEndFunctionStmtNode(ASTEndFunctionStmtNode node)
    {
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
        if (node.getSubroutinePars() != null)
        {
            ASTSubroutineParsNode list = node.getSubroutinePars();
            for (int i = 0; i < list.size(); i++)
                if (list.getSubroutinePar(i) != null)
                    bindAsParam(list.getSubroutinePar(i).getVariableName());
        }
    }

    // # R1224
    // <EndSubroutineStmt> ::=
    // <LblDef> T_END T_EOS
    // | <LblDef> T_ENDSUBROUTINE ( <EndName> )? T_EOS
    // | <LblDef> T_END T_SUBROUTINE ( <EndName> )? T_EOS

    @Override public void visitASTEndSubroutineStmtNode(ASTEndSubroutineStmtNode node)
    {
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
        // Assume this is actually an assignment statement instead of a statement function
        
        //addDefinition(node.getName(), Type.UNKNOWN);
        bind(node.getName().getName());

        if (node.getSFDummyArgNameList() != null)
            for (int j = 0; j < node.getSFDummyArgNameList().size(); j++)
                bind(node.getSFDummyArgNameList().getVariableName(j));
    }

    // #/* Assign Statement */
    // <AssignStmt> ::=
    // <LblDef> T_ASSIGN <LblRef> T_TO <VariableName> T_EOS

    @Override public void visitASTAssignStmtNode(ASTAssignStmtNode node)
    {
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
        bind(node.getVariableName());
    }
}
