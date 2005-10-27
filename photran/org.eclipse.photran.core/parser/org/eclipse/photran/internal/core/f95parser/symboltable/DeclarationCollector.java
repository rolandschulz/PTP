package org.eclipse.photran.internal.core.f95parser.symboltable;

import java.util.LinkedList;

import org.eclipse.photran.internal.core.f95parser.AbstractParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.ParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.Terminal;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.AbstractSubprogramEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.BlockDataEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.CommonBlockEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.DerivedTypeEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.ExternalEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.FunctionEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.InterfaceEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.IntrinsicEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.MainProgramEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.ModuleEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.NamelistEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.SubroutineEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.VariableEntry;

/**
 * A <code>ParseTreeVisitor</code> used to create an initial
 * <code>SymbolTable</code> from a Fortran parse tree by collecting
 * explicitly-defined variables as well as functions, subroutines,
 * function/subroutine parameters, modules, derived types, and named block data.
 * 
 * The resulting symbol table hierarchy should be further populated by a
 * ReferenceCollector, which will pick up all references as well as
 * implicitly-defined variables.
 * 
 * Called by the factory method SymbolTable#createSymbolTableFor
 * 
 * @author joverbey
 */
final class DeclarationCollector extends ParseTreeVisitor
{
    //private ParseTreeNode parseTreeRoot = null;

    private SymbolTable rootSymbolTable = null;

    /**
     * Called by SymbolTable#createSymbolTableFor
     * 
     * @return the top-level symbol table for the parse tree
     */
    SymbolTable getSymbolTable()
    {
        return rootSymbolTable;
    }

    /**
     * Create a <code>SymbolTable</code> for the given parse tree, which is
     * expected to be the entire parse tree for a translation unit (file). The
     * resulting <code>SymbolTable</code> can be fetched via
     * <code>getSymbolTable</code>.
     * 
     * @param parseTree
     */
    DeclarationCollector(ParseTreeNode parseTree)
    {
        //this.parseTreeRoot = parseTree;
        this.rootSymbolTable = new SymbolTable(null, null);
        parseTree.visitUsing(this);
    }

    // As we traverse the tree, we keep stacks of parents

    private LinkedList/* <ParseTreeNode> */parentParseTreeNodeStack = new LinkedList();

    private LinkedList/* <SymbolTable> */parentSymbolTableStack = new LinkedList();

    private SymbolTable getCurrentSymTbl()
    {
        if (parentSymbolTableStack.isEmpty())
            return rootSymbolTable;
        else
            return (SymbolTable)parentSymbolTableStack.getLast();
    }

    private boolean isCurrentSymTblNode(ParseTreeNode node)
    {
        if (parentParseTreeNodeStack.isEmpty())
            return false;
        else
            return node == (ParseTreeNode)parentParseTreeNodeStack.getLast();
    }

    private void addEntry(SymbolTableEntry entry)
    {
        getCurrentSymTbl().addEntry(entry);
    }

    private void addEntryAndSetAsNewParent(SymbolTableEntry entry)
    {
        addEntry(entry);
        beginAddingChildrenFor(entry);
    }

    private void beginAddingChildrenFor(SymbolTableEntry entry)
    {
        parentParseTreeNodeStack.addLast(entry.getCorrespondingParseTreeNode());
        parentSymbolTableStack.addLast(entry.getChildTable());
    }

    private void doneAddingChildrenFor(ParseTreeNode node)
    {
        if (isCurrentSymTblNode(node))
        {
            parentParseTreeNodeStack.removeLast();
            parentSymbolTableStack.removeLast();
        }
    }

    //--VISITOR METHODS-------------------------------------------------

    public void preparingToVisitChildrenOf(ParseTreeNode node)
    {
        // beginAddingChildrenFor is called in addEntry
    }

    public void doneVisitingChildrenOf(ParseTreeNode node)
    {
        doneAddingChildrenFor(node);
    }

    public void visitXmainprogram(ParseTreeNode node)
    {
        // # R1101 desn"t ensure ordering as the standard requires;
        // <xMainProgram> ::=
        //   <xMainRange> |
        //   <xProgramStmt> <xMainRange> ;
        // # R1102
        // <xProgramStmt> ::=
        //   <xLblDef> T_PROGRAM <xProgramName> T_EOS ;
        ParseTreeNode programStmt = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XPROGRAMSTMT);
        Token name;
        if (programStmt != null)
	        name = ParseTreeSearcher.findFirstIdentifierIn(node);
        else
        {
	        name = new Token();
	        name.setText("(Anonymous Main Program)");
        }
        addEntryAndSetAsNewParent(new MainProgramEntry(getCurrentSymTbl(), name, node));
    }

    public void visitXmodule(ParseTreeNode node)
    {
        // # R1104
        // <xModule> ::=
        //   <xModuleStmt> <xModuleBlock> ;
        // # R1105
        // <xModuleStmt> ::=
        //   <xLblDef> T_MODULE <xModuleName> T_EOS ;
        Token name = ParseTreeSearcher.findFirstIdentifierIn(node);
        addEntryAndSetAsNewParent(new ModuleEntry(getCurrentSymTbl(), name, node));
    }
    
    /**
     * Sets the return type, RECURSIVE, PURE, and ELEMENTAL attributes
     * for a <code>FunctionEntry</code> or <code>SubroutineEntry</code>
     * based on an xPrefixSpec node in the parse tree.
     */
    private static final class PrefixSpecVisitor extends ParseTreeVisitor
    {
        // # R1219
        // <xPrefixSpec> ::=
        //   <xTypeSpec>  |
        //   T_RECURSIVE |
        //   T_PURE |
        //   T_ELEMENTAL ;
        
        private AbstractSubprogramEntry entry;
        
        PrefixSpecVisitor(AbstractSubprogramEntry functionEntryOrSubroutineEntry)
        {
            this.entry = functionEntryOrSubroutineEntry;
        }
        
    	public void visitXprefixspec(ParseTreeNode node)
    	{
    	    AbstractParseTreeNode child = (AbstractParseTreeNode)node.getChildren().getFirst();
    	    
    	    if (child instanceof ParseTreeNode && ((ParseTreeNode)child).getRootNonterminal() == Nonterminal.XTYPESPEC)
    	        entry.setReturnType(SymbolTableType.createFromTypeSpecNode((ParseTreeNode)child));
    	    else if (child instanceof Token)
    	    {
    	        Token token = (Token)child;
    	        if (token.getTerminal() == Terminal.T_RECURSIVE)
    	            entry.setRecursive(true);
    	        else if (token.getTerminal() == Terminal.T_PURE)
    	            entry.setPure(true);
    	        else if (token.getTerminal() == Terminal.T_ELEMENTAL)
    	            entry.setElemental(true);
    	    }
    	}
    }

    public void visitXfunctionsubprogram(ParseTreeNode node)
    {
        // #R1216
        // <xFunctionSubprogram> ::=
        //   <xFunctionStmt> <xFunctionRange> ;
        ParseTreeNode functionStmt = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XFUNCTIONSTMT);

        // # R1217 chain rule deleted
        // <xFunctionStmt> ::=
        //   <xLblDef> <xFunctionPrefix> <xFunctionName>
        //      T_LPAREN <xFunctionPars> T_RPAREN [ T_RESULT T_LPAREN <xName> T_RPAREN ]  T_EOS ;
        
        ParseTreeNode functionName = ParseTreeSearcher.findFirstNodeIn(functionStmt, Nonterminal.XFUNCTIONNAME);
        Token functionNameToken = ParseTreeSearcher.findFirstIdentifierIn(functionName);

        final FunctionEntry functionEntry = new FunctionEntry(getCurrentSymTbl(), functionNameToken, node);
        addEntryAndSetAsNewParent(functionEntry);

        boolean hasResultClause = ParseTreeSearcher.containsImmediateChild(functionStmt,
            Terminal.T_RESULT);
        if (hasResultClause)
        {
            ParseTreeNode resultName = ParseTreeSearcher.findLastNodeIn(functionStmt, Nonterminal.XNAME);
            Token resultNameToken = ParseTreeSearcher.findFirstIdentifierIn(resultName);

            VariableEntry resultEntry = new VariableEntry(getCurrentSymTbl(), resultNameToken, resultName);
            resultEntry.setFunctionResult(true);
            functionEntry.getChildTable().addEntry(resultEntry);
        }
        
        // # R1218
        // <xFunctionPrefix> ::=
        //   T_FUNCTION | <xPrefixSpecList> T_FUNCTION ;
        // <xPrefixSpecList> ::=
        //   <xPrefixSpec> | <xPrefixSpecList> <xPrefixSpec> ;

        ParseTreeNode functionPrefix = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XFUNCTIONPREFIX);
        functionPrefix.visitUsing(new PrefixSpecVisitor(functionEntry));
    }
    
    public void visitXfunctionpar(ParseTreeNode node)
    {
        // Function parameter
        //
        // <xFunctionPar> ::= <xDummyArgName> ;
        // <xDummyArgName> ::= T_IDENT ;
        
        Token name = ParseTreeSearcher.findFirstIdentifierIn(node);
        VariableEntry varEntry = new VariableEntry(getCurrentSymTbl(), name, node);
        varEntry.setFunctionOrSubroutineParameter(true);
        addEntry(varEntry);
    }

    public void visitXsubroutinesubprogram(ParseTreeNode node)
    {
        // <xSubroutineSubprogram> ::=
        //   <xSubroutineStmt> <xSubroutineRange> ;
        ParseTreeNode subroutineStmt = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XSUBROUTINESTMT);
        
        // <xSubroutineStmt> ::=
        //   <xLblDef> <xSubroutinePrefix> <xSubroutineName>
        //     [ T_LPAREN <xSubroutinePars> T_RPAREN ] T_EOS ;
        ParseTreeNode subroutineName = ParseTreeSearcher.findFirstNodeIn(subroutineStmt, Nonterminal.XSUBROUTINENAME);
        Token name = ParseTreeSearcher.findFirstIdentifierIn(subroutineName);
        SubroutineEntry subroutineEntry = new SubroutineEntry(getCurrentSymTbl(), name, node);
        addEntryAndSetAsNewParent(subroutineEntry);
        
        // <xSubroutinePrefix> ::=
        //   T_SUBROUTINE | <xPrefixSpecList> T_SUBROUTINE ;
        // ...
        // <xPrefixSpecList> ::=
        //   <xPrefixSpec> | <xPrefixSpecList> <xPrefixSpec> ;

        ParseTreeNode subroutinePrefix = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XSUBROUTINEPREFIX);
        subroutinePrefix.visitUsing(new PrefixSpecVisitor(subroutineEntry));
    }
    
    public void visitXsubroutinepar(ParseTreeNode node)
    {
        // Subroutine parameter
        //
        // <xSubroutinePar> ::= <xDummyArgName> | T_ASTERISK ;
        // <xDummyArgName> ::= T_IDENT ;

        Token name = ParseTreeSearcher.findFirstIdentifierIn(node);
        if (name != null)
        {
	        VariableEntry varEntry = new VariableEntry(getCurrentSymTbl(), name, node);
	        varEntry.setFunctionOrSubroutineParameter(true);
	        addEntry(varEntry);
        }
    }

    public void visitXblockdatasubprogram(ParseTreeNode node)
    {
        // # R1112
        // <xBlockDataSubprogram> ::=
        //   <xBlockDataStmt> <xBlockDataBody> <xEndBlockDataStmt> |
        //   <xBlockDataStmt> <xEndBlockDataStmt> ;
        ParseTreeNode blockDataStmt = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XBLOCKDATASTMT);

        // # R1113
        // <xBlockDataStmt> ::=
        //   <xLblDef> T_BLOCKDATA <xBlockDataName> T_EOS |
        //   <xLblDef> T_BLOCKDATA T_EOS ;
        ParseTreeNode blockDataName = ParseTreeSearcher.findFirstNodeIn(blockDataStmt, Nonterminal.XBLOCKDATANAME);
        if (blockDataName != null) // Block data does not necessarily have a
                                   // name
        {
            Token name = ParseTreeSearcher.findFirstIdentifierIn(blockDataName);
            addEntryAndSetAsNewParent(new BlockDataEntry(getCurrentSymTbl(), name, node));
        }
    }

    public void visitXtypedeclarationstmt(ParseTreeNode node)
    {
        // # R501
        // <xTypeDeclarationStmt> ::=
        //  <xLblDef> <xTypeSpec> <xAttrSpecSeq> T_COLON T_COLON <xEntityDeclList> T_EOS |
        //  <xLblDef> <xTypeSpec> T_COLON T_COLON <xEntityDeclList> T_EOS |
        //  <xLblDef> <xTypeSpec> <xEntityDeclList> T_EOS ;

        final ParseTreeNode typeSpec = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XTYPESPEC);

        ParseTreeNode entityDeclList = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XENTITYDECLLIST);
        entityDeclList.visitUsing(new ParseTreeVisitor()
            {
	            public void visitXobjectname(ParseTreeNode node)
	            {
	                Token name = ParseTreeSearcher.findFirstIdentifierIn(node);
	                
	                SymbolTableEntry symTblEntry;
	                VariableEntry varEntry;
	                if ((symTblEntry=getCurrentSymTbl().getImmediateEntryFor(name.getText())) != null
	                    && symTblEntry instanceof VariableEntry)
	                    varEntry = (VariableEntry)symTblEntry;
	                else
	                    varEntry = new VariableEntry(getCurrentSymTbl(), name, node);
	                
	                varEntry.setType(SymbolTableType.createFromTypeSpecNode(typeSpec));
	                addEntry(varEntry);
	            }
            });
    }

    public void visitXderivedtypedef(ParseTreeNode node)
    {
        // <xDerivedTypeDef> ::=
        //   <xDerivedTypeStmt> <xDerivedTypeBody> <xEndTypeStmt> ;
        ParseTreeNode derivedTypeStmt = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XDERIVEDTYPESTMT);

        // # R423
        // <xDerivedTypeStmt> ::=
        //   <xLblDef> T_TYPE <xTypeName> T_EOS |
        //   <xLblDef> T_TYPE T_COLON T_COLON <xTypeName> T_EOS |
        //   <xLblDef> T_TYPE T_COMMA <xAccessSpec> T_COLON T_COLON <xTypeName> T_EOS ;
        ParseTreeNode derivedTypeName = ParseTreeSearcher.findFirstNodeIn(derivedTypeStmt, Nonterminal.XTYPENAME);
        Token name = ParseTreeSearcher.findFirstIdentifierIn(derivedTypeName);
        addEntryAndSetAsNewParent(new DerivedTypeEntry(getCurrentSymTbl(), name, node));
    }
    
    public void visitXprivatesequencestmt(ParseTreeNode node)
    {
        // # R424
        // <xPrivateSequenceStmt> ::=
        //   <xLblDef> T_PRIVATE T_EOS |
        //   <xLblDef> T_SEQUENCE T_EOS ;

        Token privateToken = ParseTreeSearcher.findFirstTokenIn(node, Terminal.T_PRIVATE);
        Token sequenceToken = ParseTreeSearcher.findFirstTokenIn(node, Terminal.T_SEQUENCE);
        // The grammar guarantees that we're inside a derived type,
        // hence the DerivedTypeEntry cast below
        if (privateToken != null) ((DerivedTypeEntry)getCurrentSymTbl().getParentEntry()).setPrivate(true);
        if (sequenceToken != null) ((DerivedTypeEntry)getCurrentSymTbl().getParentEntry()).setSequence(true);
    }

    public void visitXcomponentdefstmt(ParseTreeNode node)
    {
        // The grammar guarantees that we're inside a derived type,
        // hence the DerivedTypeEntry cast below

        // # R425
        // <xComponentDefStmt> ::=
        //   <xLblDef> <xTypeSpec> T_COMMA <xComponentAttrSpecList> T_COLON T_COLON <xComponentDeclList> T_EOS |
        //   <xLblDef> <xTypeSpec> T_COLON T_COLON <xComponentDeclList> T_EOS |
        //   <xLblDef> <xTypeSpec> <xComponentDeclList> T_EOS ;

        final ParseTreeNode typeSpec = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XTYPESPEC);

        ParseTreeNode componentDeclList = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XCOMPONENTDECLLIST);
        componentDeclList.visitUsing(new ParseTreeVisitor()
            {
	            public void visitXcomponentname(ParseTreeNode node)
	            {
	                Token name = ParseTreeSearcher.findFirstIdentifierIn(node);
	                VariableEntry varEntry = new VariableEntry(getCurrentSymTbl(), name, node);
	                varEntry.setType(SymbolTableType.createFromTypeSpecNode(typeSpec));
	                addEntry(varEntry);
	            }
            });
    }
    
    public void visitXnamelistgroups(ParseTreeNode node)
    {
        // We are visiting xNamelistGroups, which comes from...
        //
        // # R544
        // <xNamelistStmt> ::=
        //   <xLblDef> T_NAMELIST <xNamelistGroups> T_EOS ;
        // <xNamelistGroups> ::=
        //   T_SLASH <xNamelistGroupName> T_SLASH <xNamelistGroupObject> |
        //   <xNamelistGroups> T_SLASH <xNamelistGroupName> T_SLASH <xNamelistGroupObject> |
        //   <xNamelistGroups> T_COMMA T_SLASH <xNamelistGroupName> T_SLASH <xNamelistGroupObject> |
        //   <xNamelistGroups> T_COMMA <xNamelistGroupObject> ;
        // ...
        // <xNamelistGroupName> ::= T_IDENT ;
        
        ParseTreeNode namelistGroupName = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XNAMELISTGROUPNAME);
        Token name;
        if (namelistGroupName != null)
	        name = ParseTreeSearcher.findFirstIdentifierIn(namelistGroupName);
        else
        {
	        name = new Token();
	        name.setText("(Anonymous Namelist)");
        }
        addEntry(new NamelistEntry(getCurrentSymTbl(), name, node));
    }
    
    public void visitXcomblock(ParseTreeNode node)
    {
        // We are visiting xComBlock, which comes from...
        //
        // # R549
        // <xCommonStmt> ::=
        //   <xLblDef> T_COMMON <xComlist> T_EOS ;
        // <xComlist> ::=
        //   <xCommonBlockObject> |
        //   <xComblock> <xCommonBlockObject> |
        //   <xComlist> T_COMMA <xCommonBlockObject> |
        //   <xComlist> <xComblock> <xCommonBlockObject> |
        //   <xComlist> T_COMMA <xComblock> <xCommonBlockObject> ;
        // <xComblock> ::= T_SLASH T_SLASH | T_SLASH <xCommonBlockName> T_SLASH ;
        
        ParseTreeNode commonBlockName = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XCOMMONBLOCKNAME);
        Token name;
        if (commonBlockName != null)
	        name = ParseTreeSearcher.findFirstIdentifierIn(commonBlockName);
        else
        {
	        name = new Token();
	        name.setText("(Anonymous Common Block)");
        }
        addEntry(new CommonBlockEntry(getCurrentSymTbl(), name, node));
    }
    
    public void visitXusestmt(ParseTreeNode node)
    {
        // # R1107
        // <xUseStmt> ::=
        //   <xLblDef> T_USE <xName> T_EOS |
        //   <xLblDef> T_USE <xName> T_COMMA <xRenameList> T_EOS |
        //   <xLblDef> T_USE <xName> T_COMMA T_ONLY T_COLON [ <xOnlyList> ] T_EOS ;
        
        Token moduleName = ParseTreeSearcher.findFirstIdentifierIn(node);
        
        if (ParseTreeSearcher.containsImmediateChild(node, Terminal.T_ONLY))
        {
            final SymbolTable.ModuleUseOnly use = new SymbolTable.ModuleUseOnly(moduleName);
            
            ParseTreeNode onlyList = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XONLYLIST);
            if (onlyList != null) fillModuleUseOnlyFromOnlyList(use, onlyList);
            
            getCurrentSymTbl().addModuleUse(use);
        }
        else
        {
            final SymbolTable.ModuleUseAll use = new SymbolTable.ModuleUseAll(moduleName);

            ParseTreeNode renameList = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XRENAMELIST);
            if (renameList != null) fillModuleUseAllFromRenameList(use, renameList);
            
            getCurrentSymTbl().addModuleUse(use);
        }
        
    }
    
    private void fillModuleUseOnlyFromOnlyList(final SymbolTable.ModuleUseOnly use, ParseTreeNode onlyList)
    {
        // <xOnlyList> ::= <xOnly> | <xOnlyList> T_COMMA <xOnly> ;
        // ...
        // # R1109
        // <xOnly> ::= <xGenericSpec> | T_IDENT T_EQGREATERTHAN <xUseName> | <xUseName> ;
        
        onlyList.visitUsing(new ParseTreeVisitor()
            {
            	public void visitXonly(ParseTreeNode node)
            	{
            	    ParseTreeNode child;
            	    if ((child=ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XGENERICSPEC)) != null)
            	    {
            	        use.importWithoutRenaming(child);
            	    }
            	    else if (ParseTreeSearcher.findFirstTokenIn(node, Terminal.T_EQGREATERTHAN) != null)
            	    {
            	        Token from = ParseTreeSearcher.findFirstIdentifierIn(node);
            	        Token to = ParseTreeSearcher.findLastIdentifierIn(node);
            	        use.importAndRename(from, to);
            	    }
            	    else if (ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XUSENAME) != null)
            	    {
            	        use.importWithoutRenaming(ParseTreeSearcher.findFirstIdentifierIn(node));
            	    }
            	}
            });
        
        // # R1207
        // <xGenericSpec> ::=
        //   T_OPERATOR T_LPAREN <xDefinedOperator> T_RPAREN |
        //   T_ASSIGNMENT T_LPAREN T_EQUALS T_RPAREN ;
    }
    
    private void fillModuleUseAllFromRenameList(final SymbolTable.ModuleUseAll use, ParseTreeNode renameList)
    {
        // <xRenameList> ::= <xRename> | <xRenameList> T_COMMA <xRename> ;
        // ...
        // # R1108
        // <xRename> ::= T_IDENT T_EQGREATERTHAN <xUseName> ;
        
        renameList.visitUsing(new ParseTreeVisitor()
            {
            	public void visitXrename(ParseTreeNode node)
            	{
        	        Token from = ParseTreeSearcher.findFirstIdentifierIn(node);
        	        Token to = ParseTreeSearcher.findLastIdentifierIn(node);
        	        use.importAndRename(from, to);
            	}
            });
    }
    
    public void visitXinterfaceblock(ParseTreeNode node)
    {
        // # R1201
        // <xInterfaceBlock> ::=
        //   <xInterfaceStmt> <xInterfaceRange> ;
        // ...
        // # R1203
        // <xInterfaceStmt> ::=
        //   <xLblDef> T_INTERFACE <xGenericName> T_EOS |
        //   <xLblDef> T_INTERFACE <xGenericSpec> T_EOS |
        //   <xLblDef> T_INTERFACE T_EOS ;
        
        ParseTreeNode interfaceStmt = ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.XINTERFACESTMT);
        
        ParseTreeNode genericName = ParseTreeSearcher.findFirstNodeIn(interfaceStmt, Nonterminal.XGENERICNAME);
        ParseTreeNode genericSpec = ParseTreeSearcher.findFirstNodeIn(interfaceStmt, Nonterminal.XGENERICSPEC);
        
        if (genericName != null)
        {
            Token name = ParseTreeSearcher.findFirstIdentifierIn(genericName);
            addEntry(new InterfaceEntry(getCurrentSymTbl(), name, node));
        }
        else if (genericSpec != null)
        {
            addEntry(new InterfaceEntry(getCurrentSymTbl(), genericSpec, node));
        }
        else
        {
            Token name = new Token();
            name.setText("(Anonymous Interface)");
            addEntry(new InterfaceEntry(getCurrentSymTbl(), name, node));
        }
    }
    
    public void visitXexternalname(ParseTreeNode node)
    {
        // We are visiting xExternalName, which comes from...
        //
        // # R1208
        // <xExternalStmt> ::=
        //   <xLblDef> T_EXTERNAL <xExternalNameList> T_EOS |
        //   <xLblDef> T_EXTERNAL T_COLON T_COLON <xExternalNameList> T_EOS ;
        // <xExternalNameList> ::= <xExternalName> | <xExternalNameList> T_COMMA <xExternalName> ;
        
        Token name = ParseTreeSearcher.findFirstIdentifierIn(node);
        addEntry(new ExternalEntry(getCurrentSymTbl(), name, node));
    }
    
    public void visitXintrinsicprocedurename(ParseTreeNode node)
    {
        // We are visiting xIntrinsicProcedureName, which comes from...
        //
        // # R1209
        // <xIntrinsicStmt> ::=
        //   <xLblDef> T_INTRINSIC <xIntrinsicList> T_EOS |
        //   <xLblDef> T_INTRINSIC T_COLON T_COLON <xIntrinsicList> T_EOS ;
        // <xIntrinsicList> ::=
        //   <xIntrinsicProcedureName> | <xIntrinsicList> T_COMMA <xIntrinsicProcedureName> ;
       
        Token name = ParseTreeSearcher.findFirstIdentifierIn(node);
        addEntry(new IntrinsicEntry(getCurrentSymTbl(), name, node));
    }
    
    public void visitTXimplicitstmt(ParseTreeNode node)
    {
        getCurrentSymTbl().setImplicitSpec(ParseTreeSearcher.findFirstNodeIn(node, Nonterminal.TXIMPLICITSPECLIST));
    }
}
