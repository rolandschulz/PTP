package org.eclipse.photran.internal.core.f95parser.symboltable;

import java.util.LinkedList;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.ParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.SemanticError;
import org.eclipse.photran.internal.core.f95parser.Terminal;
import org.eclipse.photran.internal.core.f95parser.Token;

/**
 * This should be called after creating an initial symbol table hierarchy via a
 * <code>DeclarationCollector</code>.
 * 
 * A <code>ReferenceCollector</code> picks up all references to symbols as well as
 * implicitly-defined variables.
 * 
 * Called by the factory method SymbolTable#createSymbolTableFor
 * 
 * TODO-Jeff: This is almost assuredly not complete and needs to be tested further.
 * 
 * @author joverbey
 */
final class ReferenceCollector extends ParseTreeVisitor
{
    private ParseTreeNode parseTreeRoot = null;

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
    ReferenceCollector(SymbolTable rootSymbolTable, ParseTreeNode parseTreeRoot)
    {
        this.rootSymbolTable = rootSymbolTable;
        this.parseTreeRoot = parseTreeRoot;
        parseTreeRoot.visitUsing(this);
    }

    // As we traverse the tree, we keep stacks of parents

    private LinkedList/* <ParseTreeNode> */parentParseTreeNodeStack = new LinkedList();

    private LinkedList/* <SymbolTable> */parentSymbolTableStack = new LinkedList();

    private SymbolTable getCurrentParent()
    {
        if (parentSymbolTableStack.isEmpty())
            return rootSymbolTable;
        else
            return (SymbolTable)parentSymbolTableStack.getLast();
    }

    private boolean isCurrentParent(ParseTreeNode node)
    {
        if (parentParseTreeNodeStack.isEmpty())
            return false;
        else
            return node == (ParseTreeNode)parentParseTreeNodeStack.getLast();
    }

    private void addEntry(SymbolTableEntry entry)
    {
        getCurrentParent().addEntry(entry);
    }

    private void enterSymbolTableFor(ParseTreeNode node)
    {
        SymbolTableEntry entry = getCurrentParent().getEntryCorrespondingTo(node);
        if (entry != null)
        {
            parentParseTreeNodeStack.addLast(node);
            parentSymbolTableStack.addLast(entry.getChildTable());
        }
    }

    private void exitSymbolTableFor(ParseTreeNode node)
    {
        if (isCurrentParent(node))
        {
            parentParseTreeNodeStack.removeLast();
            parentSymbolTableStack.removeLast();
        }
    }

    private void visitVariableReference(Token token)
    {
        if (token.getTerminal() != Terminal.T_IDENT) return;

        SymbolTableEntry entry = getCurrentParent().getEntryInHierarchyFor(token.getText());
        if (entry != null)
            entry.addReference(token);
        else if (getCurrentParent().getImplicitSpec() == null)
            throw new SemanticError(token, token.getText()
                + " is used but not defined in an \"implicit none\" context");
    }

    //--VISITOR METHODS-------------------------------------------------

    // The methods overridden here correspond to the immediate contexts
    // of T_IDENT in the LALR(1) Fortran grammar.

    public void preparingToVisitChildrenOf(ParseTreeNode node)
    {
        enterSymbolTableFor(node);
    }

    public void doneVisitingChildrenOf(ParseTreeNode node)
    {
        exitSymbolTableFor(node);
    }
    
    public void visitTXimplieddovariable(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXarrayname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXblockdataname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXcommonblockname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXcomponentname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXdummyargname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXeditelement(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXendname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXentryname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXexternalname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXfunctionname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXgenericname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXintrinsicprocedurename(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXmodulename(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXname(ParseTreeNode node)
    {
        // FIXME-Jeff: This may not always be a good idea to ignore <xName>s
        //visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
        
        if (node.getParent().getRootNonterminal() == Nonterminal.XASSIGNMENTSTMT
            || node.getParent().getRootNonterminal() == Nonterminal.XPRIMARY)
            visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));;
    }
    public void visitXnamedconstant(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXnamedconstantuse(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXnamelistgroupname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXobjectname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXonly(ParseTreeNode node)
    {
        //visitVariableReference(ParseTreeUtil.findFirstIdentifierIn(node));
    }
    public void visitXpointername(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXprocedurename(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXprogramname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXrename(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXsubroutinename(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXsubroutinenameuse(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXtargetname(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXtypename(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
    public void visitXusename(ParseTreeNode node)
    {
        //visitVariableReference(ParseTreeUtil.findFirstIdentifierIn(node));
    }
    public void visitXvariablename(ParseTreeNode node)
    {
        visitVariableReference(ParseTreeSearcher.findFirstIdentifierIn(node));
    }
}
