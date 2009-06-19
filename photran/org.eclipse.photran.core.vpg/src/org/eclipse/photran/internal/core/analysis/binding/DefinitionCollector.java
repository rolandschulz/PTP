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

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.PhotranVPGBuilder;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTAssociationNode;
import org.eclipse.photran.internal.core.parser.ASTBlockDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNode;
import org.eclipse.photran.internal.core.parser.ASTCommonStmtNode;
import org.eclipse.photran.internal.core.parser.ASTComponentDeclNode;
import org.eclipse.photran.internal.core.parser.ASTDataComponentDefStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTEntryStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEnumeratorDefStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEnumeratorNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTExternalNameListNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTForallConstructStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTIfThenStmtNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBodyNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicListNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicStmtNode;
import org.eclipse.photran.internal.core.parser.ASTLabelDoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTNamelistGroupsNode;
import org.eclipse.photran.internal.core.parser.ASTNamelistStmtNode;
import org.eclipse.photran.internal.core.parser.ASTPrivateSequenceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSelectCaseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSelectTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTStmtFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeAttrSpecNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeParamDeclNode;
import org.eclipse.photran.internal.core.parser.ASTTypeParamDefStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.ASTWhereConstructStmtNode;
import org.eclipse.photran.internal.core.parser.IInterfaceSpecification;
import org.eclipse.photran.internal.core.parser.IProgramUnit;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;

/**
 * Visits an AST, binding identifier (T_IDENT) tokens in declaration statements.
 * <p> 
 * Operators are NOT included in the symbol table.
 * 
 * @author Jeff Overbey
 */
class DefinitionCollector extends BindingCollector
{
    protected IFile file;

    public DefinitionCollector(IFile file)
    {
    	this.vpg = (PhotranVPGBuilder)PhotranVPG.getInstance();
    	
        this.file = file;
    }

    // --VISITOR METHODS-------------------------------------------------

    // # R423
    // <DerivedTypeStmt> ::=
    // <LblDef> T_TYPE <TypeName> T_EOS
    // | <LblDef> T_TYPE T_COLON T_COLON <TypeName> T_EOS
    // | <LblDef> T_TYPE T_COMMA <AccessSpec> T_COLON T_COLON <TypeName> T_EOS

    @Override public void visitASTDerivedTypeStmtNode(ASTDerivedTypeStmtNode node)
    {
        super.traverseChildren(node);
        
        Definition d = addDefinition(node.getTypeName(), Definition.Classification.DERIVED_TYPE, Type.VOID);
        
        ScopingNode enclosingScope = node.findNearestAncestor(ScopingNode.class);
        
//        if (node.getAccessSpec() != null)
//            d.setVisibility(node.getAccessSpec());
        // Change for Fortran 2003
        if (node.getTypeAttrSpecList() != null)
            for (ASTTypeAttrSpecNode attrSpec : node.getTypeAttrSpecList())
                if (attrSpec.getAccessSpec() != null)
                    d.setVisibility(attrSpec.getAccessSpec(), enclosingScope);
        
        // F03 -- Don't bind derived type parameters since we don't bind derived type components yet
        // (so there is no scope for the derived type).  When we do, should also bind
        // ASTTypeParamDefStmtNode 
//        if (node.getTypeParamNameList() != null)
//            for (ASTTypeParamNameNode typeParam : node.getTypeParamNameList())
//                addDefinition(typeParam.getTypeParamName(), Definition.Classification.DERIVED_TYPE_PARAMETER);
    }

    // # R424
    // <PrivateSequenceStmt> ::=
    //     <LblDef> T_PRIVATE T_EOS
    //  | <LblDef> T_SEQUENCE T_EOS
    
    @Override public void visitASTPrivateSequenceStmtNode(ASTPrivateSequenceStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.isPrivate())
        {
        	try
        	{
        		setScopeDefaultVisibilityToPrivate(node.getPrivateToken().getEnclosingScope());
        	}
        	catch (Exception e)
        	{
        		throw new Error(e);
        	}
        }
    }

    // # R425
    // <ComponentDefStmt> ::=
    // <LblDef> <TypeSpec> T_COMMA <ComponentAttrSpecList> T_COLON T_COLON <ComponentDeclList> T_EOS
    // | <LblDef> <TypeSpec> T_COLON T_COLON <ComponentDeclList> T_EOS
    // | <LblDef> <TypeSpec> <ComponentDeclList> T_EOS
    //
    // <ComponentDecl> ::=
    // <ComponentName> T_LPAREN <ComponentArraySpec> T_RPAREN T_ASTERISK <CharLength> <ComponentInitialization>
    // | <ComponentName> T_LPAREN <ComponentArraySpec> T_RPAREN <ComponentInitialization>
    // | <ComponentName> T_ASTERISK <CharLength> <ComponentInitialization>
    // | <ComponentName> <ComponentInitialization>
    // | <ComponentName> T_LPAREN <ComponentArraySpec> T_RPAREN T_ASTERISK <CharLength>
    // | <ComponentName> T_LPAREN <ComponentArraySpec> T_RPAREN
    // | <ComponentName> T_ASTERISK <CharLength>
    // | <ComponentName>

    @Override public void visitASTDataComponentDefStmtNode(ASTDataComponentDefStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTComponentDeclNode> decls = node.getComponentDeclList();
        for (int i = 0; i < decls.size(); i++)
            addDefinition(decls.get(i).getComponentName().getComponentName(),
                          Definition.Classification.DERIVED_TYPE_COMPONENT,
                          Type.parse(node.getTypeSpec()));
    }

    // # R501
    // <TypeDeclarationStmt> ::=
    // <LblDef> <TypeSpec> <AttrSpecSeq> T_COLON T_COLON <EntityDeclList> T_EOS
    // | <LblDef> <TypeSpec> T_COLON T_COLON <EntityDeclList> T_EOS
    // | <LblDef> <TypeSpec> <EntityDeclList> T_EOS
    //
    // # R504
    // <EntityDeclList> ::=
    //   <EntityDecl>
    // | @:<EntityDeclList> T_COMMA <EntityDecl>

    @Override public void visitASTTypeDeclarationStmtNode(ASTTypeDeclarationStmtNode node)
    {
        super.traverseChildren(node);
        
        ScopingNode enclosingScope = node.findNearestAncestor(ScopingNode.class);
        
        IASTListNode<ASTEntityDeclNode> decls = node.getEntityDeclList();
        for (int i = 0; i < decls.size(); i++)
        {
            ASTEntityDeclNode entityDecl = decls.get(i);
            Token objectNameIdent = getObjectNameIdent(entityDecl);
            
            Definition def = addDefinition(objectNameIdent,
                                           Definition.Classification.VARIABLE_DECLARATION,
                                           Type.parse(node.getTypeSpec()));
            def.setAttributes(node.getAttrSpecSeq(), enclosingScope);
            def.setArraySpec(getArraySpec(entityDecl)); // (p.119) This overrides the DIMENSION attribute
            setDefinition(objectNameIdent, def);
        }
    }

    // <EntityDecl> ::=
    //   <ObjectName> ( T_LPAREN <ArraySpec> T_RPAREN )? ( T_ASTERISK <CharLength> )? <Initialization>?
    // | <InvalidEntityDecl>
    //
    // <InvalidEntityDecl> ::=
    //   <ObjectName> T_ASTERISK <CharLength> T_LPAREN <ArraySpec> T_RPAREN <Initialization>?
    
    private Token getObjectNameIdent(ASTEntityDeclNode entityDecl)
    {
        super.traverseChildren(entityDecl);
        return entityDecl.getObjectName().getObjectName();
    }
    
    private ASTArraySpecNode getArraySpec(ASTEntityDeclNode entityDecl)
    {
        super.traverseChildren(entityDecl);
        return entityDecl.getArraySpec();
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
        super.traverseChildren(node);
        
        if (node.getAccessIdList() == null)
        {
        	if (node.getAccessSpec().isPrivate())
        	{
        		try
        		{
        			setScopeDefaultVisibilityToPrivate(node.getAccessSpec().findFirstToken().getEnclosingScope());
        		}
        		catch (Exception e)
        		{
        			throw new Error(e);
        		}
        	}
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
            Token name = groups.get(i).getNamelistGroupName();
            //Token object = groups.get(i).getVariableName();
            if (name != null) addDefinition(name, Definition.Classification.NAMELIST);
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
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i).getName() != null)
            {
                Token commonBlockName = list.get(i).getName().getCommonBlockName();
                addDefinition(commonBlockName, Definition.Classification.COMMON_BLOCK);
                vpg.markFileAsUsingCommonBlock(file, commonBlockName.getText());
            }
        }
    }

    // # R740
    // <WhereConstructStmt> ::=
    // <LblDef> <Name> T_COLON T_WHERE T_LPAREN <MaskExpr> T_RPAREN T_EOS
    // | <LblDef> T_WHERE T_LPAREN <MaskExpr> T_RPAREN T_EOS

    @Override public void visitASTWhereConstructStmtNode(ASTWhereConstructStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null) addDefinition(node.getName(),
                                                     Definition.Classification.WHERE,
                                                     Type.VOID);
    }

    // # R748
    // <ForallConstructStmt> ::=
    // <LblDef> T_FORALL <ForallHeader> T_EOS
    // | <LblDef> <Name> T_COLON T_FORALL <ForallHeader> T_EOS

    @Override public void visitASTForallConstructStmtNode(ASTForallConstructStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null) addDefinition(node.getName(),
                                                  Definition.Classification.FORALL);
    }

    // # R803
    // <IfThenStmt> ::=
    // <LblDef> T_IF T_LPAREN <Expr> T_RPAREN T_THEN T_EOS
    // | <LblDef> <Name> T_COLON T_IF T_LPAREN <Expr> T_RPAREN T_THEN T_EOS

    @Override public void visitASTIfThenStmtNode(ASTIfThenStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null) addDefinition(node.getName(),
                                                             Definition.Classification.IF);
    }

    // # R809 chain rule eliminated
    // <SelectCaseStmt> ::=
    // <LblDef> <Name> T_COLON T_SELECTCASE T_LPAREN <Expr> T_RPAREN T_EOS
    // | <LblDef> T_SELECTCASE T_LPAREN <Expr> T_RPAREN T_EOS
    // | <LblDef> <Name> T_COLON T_SELECT T_CASE T_LPAREN <Expr> T_RPAREN T_EOS
    // | <LblDef> T_SELECT T_CASE T_LPAREN <Expr> T_RPAREN T_EOS

    @Override public void visitASTSelectCaseStmtNode(ASTSelectCaseStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null) addDefinition(node.getName(),
                                                                 Definition.Classification.SELECT);
    }

    // # R818
    // <LabelDoStmt> ::=
    // <LblDef> T_DO <LblRef> <CommaLoopControl> T_EOS
    // | <LblDef> T_DO <LblRef> T_EOS
    // | <LblDef> T_DO <CommaLoopControl> T_EOS
    // | <LblDef> T_DO T_EOS
    // | <LblDef> <Name> T_COLON T_DO <LblRef> <CommaLoopControl> T_EOS
    // | <LblDef> <Name> T_COLON T_DO <LblRef> T_EOS
    // | <LblDef> <Name> T_COLON T_DO <CommaLoopControl> T_EOS
    // | <LblDef> <Name> T_COLON T_DO T_EOS

    @Override public void visitASTLabelDoStmtNode(ASTLabelDoStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getName() != null) addDefinition(node.getName(),
                                                             Definition.Classification.DO);
    }

    // # R1102
    // <ProgramStmt> ::=
    // <LblDef> T_PROGRAM <ProgramName> T_EOS

    @Override public void visitASTProgramStmtNode(ASTProgramStmtNode node)
    {
        super.traverseChildren(node);
        
        //NO!!! exitScope(); // We already pushed a Scope for a main program, just in case there was no ASTProgramStmt
        addDefinition(node.getProgramName().getProgramName(), Definition.Classification.MAIN_PROGRAM, Type.VOID);
    }

    // # R1105
    // <ModuleStmt> ::=
    // <LblDef> T_MODULE <ModuleName> T_EOS

    @Override public void visitASTModuleStmtNode(ASTModuleStmtNode node)
    {
        super.traverseChildren(node);
        
        addDefinition(node.getModuleName().getModuleName(), Definition.Classification.MODULE, Type.VOID);
        try
        {
            Token moduleNameToken = node.getModuleName().getModuleName();
            markModuleExport(file, moduleNameToken);
        }
        catch (Exception e) { throw new Error(e); }
    }
    
    
    // # R1113
    // <BlockDataStmt> ::=
    // <LblDef> T_BLOCKDATA <BlockDataName> T_EOS
    // | <LblDef> T_BLOCKDATA T_EOS

    @Override public void visitASTBlockDataStmtNode(ASTBlockDataStmtNode node)
    {
        super.traverseChildren(node);
        
        Token token = node.getBlockDataName() == null ? null : node.getBlockDataName().getBlockDataName();
        addDefinition(token, Definition.Classification.BLOCK_DATA, Type.VOID);
    }

    // # R1203
    // <InterfaceStmt> ::=
    // <LblDef> T_INTERFACE <GenericName> T_EOS
    // | <LblDef> T_INTERFACE <GenericSpec> T_EOS
    // | <LblDef> T_INTERFACE T_EOS

    @Override public void visitASTInterfaceStmtNode(ASTInterfaceStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getGenericName() != null)
            addDefinition(node.getGenericName().getGenericName(), Definition.Classification.INTERFACE, Type.UNKNOWN);
        else if (node.getGenericSpec() != null)
            addDefinition(null, Definition.Classification.INTERFACE, Type.UNKNOWN);
    }

    // # R1208
    // <ExternalStmt> ::=
    // <LblDef> T_EXTERNAL <ExternalNameList> T_EOS
    // | <LblDef> T_EXTERNAL T_COLON T_COLON <ExternalNameList> T_EOS
    //
    // <ExternalNameList> ::=
    // <ExternalName>
    // | @:<ExternalNameList> T_COMMA <ExternalName>

    @Override public void visitASTExternalStmtNode(ASTExternalStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTExternalNameListNode> list = node.getExternalNameList();
        for (int i = 0; i < list.size(); i++)
            addDefinition(list.get(i).getExternalName(), Definition.Classification.EXTERNAL, Type.UNKNOWN);
        // TODO: addExternalDefinition
    }

    // # R1209
    // <IntrinsicStmt> ::=
    // <LblDef> T_INTRINSIC <IntrinsicList> T_EOS
    // | <LblDef> T_INTRINSIC T_COLON T_COLON <IntrinsicList> T_EOS
    //
    // <IntrinsicList> ::=
    // <IntrinsicProcedureName>
    // | @:<IntrinsicList> T_COMMA <IntrinsicProcedureName>

    @Override public void visitASTIntrinsicStmtNode(ASTIntrinsicStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTIntrinsicListNode> list = node.getIntrinsicList();
        for (int i = 0; i < list.size(); i++)
            addDefinition(list.get(i).getIntrinsicProcedureName(), Definition.Classification.INTRINSIC, Type.UNKNOWN);
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
        
        Type type = Type.UNKNOWN;
        if (node.getPrefixSpecList() != null)
        {
            for (int i = 0; i < node.getPrefixSpecList().size(); i++)
            {
                ASTTypeSpecNode typeSpec = node.getPrefixSpecList().get(i).getTypeSpec();
                if (typeSpec != null) type = Type.parse(typeSpec);
            }
        }
        
        Token functionName = node.getFunctionName().getFunctionName();
        
        if (node.hasResultClause())
        {
            addDefinition(functionName, Definition.Classification.FUNCTION, type);
        }
        else
        {
        	// TODO: Type
            addDefinition(functionName, Definition.Classification.FUNCTION, Type.UNKNOWN);
        }
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
        
        addDefinition(node.getSubroutineName().getSubroutineName(), Definition.Classification.SUBROUTINE, Type.VOID);
    }

    // # R1225 - JO - Macro substituted
    // <EntryStmt> ::=
    // <LblDef> T_ENTRY <EntryName> ( T_LPAREN <SubroutinePars> T_RPAREN )? T_EOS

    @Override public void visitASTEntryStmtNode(ASTEntryStmtNode node)
    {
        super.traverseChildren(node);
        
        // TODO Implement ENTRY statements
        
//        // TO-DO: No syntax for function entries (with result parameter)
//        // Then declare local result variable with entry name if no result exists
//
//        // TO-DO: Entries should have the same scope as the function in which they are defined
//        addDefinition(node.getEntryName().getTIdent(), Definition.Classification.ENTRY, Type.UNKNOWN);
//
//        if (node.getSubroutinePars() != null)
//        {
//            ASTSubroutineParsNode list = node.getSubroutinePars();
//            for (int i = 0; i < list.size(); i++)
//                addDefinition(list.getSubroutinePar(i).getDummyArgName().getTIdent(), Definition.Classification.SUBPROGRAM_ARGUMENT);
//        }
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
    }

    @Override public void visitASTExecutableProgramNode(ASTExecutableProgramNode node)
    {
        super.visitASTExecutableProgramNode(node);
        markExternalSubprogramExports(node);
    }
    
    // F03
    @Override public void visitASTTypeParamDefStmtNode(ASTTypeParamDefStmtNode node)
    {
        super.traverseChildren(node);
        
        IASTListNode<ASTTypeParamDeclNode> list = node.getTypeParamDeclList();
        for (int i = 0; i < list.size(); i++)
            bind(list.get(i).getTypeParamName());
    }

    // F03
    @Override public void visitASTEnumeratorDefStmtNode(ASTEnumeratorDefStmtNode node)
    {
        super.traverseChildren(node);
        
        for (ASTEnumeratorNode enumNode : node.getEnumeratorList())
            addDefinition(enumNode.getNamedConstant().getNamedConstant(), Definition.Classification.ENUMERATOR, Type.INTEGER);
    }

    // F03  TODO: associate-construct-name
    @Override public void visitASTAssociationNode(ASTAssociationNode node)
    {
        super.traverseChildren(node);
        
        addDefinition(node.getAssociateName(), Definition.Classification.VARIABLE_DECLARATION, Type.UNKNOWN); // TODO: Type
    }
    
    // F03  TODO: select-construct-name
    @Override public void visitASTSelectTypeStmtNode(ASTSelectTypeStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getAssociateName() != null)
            addDefinition(node.getAssociateName(), Definition.Classification.VARIABLE_DECLARATION, Type.UNKNOWN); // TODO: Type
    }

    private void markExternalSubprogramExports(ASTExecutableProgramNode node)
    {
        for (IProgramUnit pu : node.getProgramUnitList())
        {
            if (pu instanceof ASTSubroutineSubprogramNode)
            {
                ASTSubroutineSubprogramNode subroutine = (ASTSubroutineSubprogramNode)pu;
                markSubprogramExport(subroutine.getSubroutineStmt().getSubroutineName().getSubroutineName());
            }
            else if (pu instanceof ASTFunctionSubprogramNode)
            {
                ASTFunctionSubprogramNode function = (ASTFunctionSubprogramNode)pu;
                markSubprogramExport(function.getFunctionStmt().getFunctionName().getFunctionName());
            }
        }
    }
    
    private void markSubprogramExport(Token subprogramNameToken)
    {
        try
        {
            markSubprogramExport(file, subprogramNameToken);
        }
        catch (Exception e) { throw new Error(e); }
    }

    @Override public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node)
    {
        super.visitASTInterfaceBlockNode(node);
        if (node.getInterfaceStmt().getGenericName() == null
            && node.getInterfaceStmt().getGenericSpec() == null)
            markExternalSubprogramImports(node);
    }

    private void markExternalSubprogramImports(ASTInterfaceBlockNode node)
    {
        for (IInterfaceSpecification pu : node.getInterfaceBlockBody())
        {
            if (pu instanceof ASTInterfaceBodyNode)
            {
                ASTInterfaceBodyNode b = (ASTInterfaceBodyNode)pu;
                
                Token name;
                if (b.getFunctionStmt() != null)
                    name = b.getFunctionStmt().getFunctionName().getFunctionName();
                else
                    name = b.getSubroutineStmt().getSubroutineName().getSubroutineName();
                
                markSubprogramImport(name);
            }
        }
    }
    
    private void markSubprogramImport(Token subprogramNameToken)
    {
        try
        {
            markSubprogramImport(file, subprogramNameToken);
        }
        catch (Exception e) { throw new Error(e); }
    }
}
