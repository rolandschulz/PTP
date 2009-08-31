/*******************************************************************************
 * Copyright (c) 2007-2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.util.IterableWrapper;
import org.eclipse.photran.core.vpg.util.OffsetLength;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.Token.FakeToken;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTImplicitStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.IProgramUnit;
import org.eclipse.photran.internal.core.parser.ISpecificationPartConstruct;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.RenameRefactoring;
import org.eclipse.text.edits.ReplaceEdit;

import bz.over.vpg.TokenRef;
import bz.over.vpg.VPGLog;

/**
 * Superclass for all refactorings in Photran.
 * <p>
 * In addition to implementing the LTK refactoring interface, this class provides a number of methods to subclasses,
 * including methods to display error messages, find enclosing nodes in an AST, check identifier tokens, etc.
 *
 * @author Jeff Overbey, Timofey Yuvashev
 */
public abstract class AbstractFortranRefactoring extends Refactoring
{
    // Preconditions toward bottom of file

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

	/** The OS-dependent end-of-line sequence (\n or \r\n) */
	protected static final String EOL = System.getProperty("line.separator");

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    protected PhotranVPG vpg;
    protected CompositeChange allChanges = null;

    ///////////////////////////////////////////////////////////////////////////
    // LTK Refactoring Implementation
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm)
    {
        RefactoringStatus status = new RefactoringStatus();

        status.addWarning("C preprocessor directives are IGNORED by the refactoring engine.  Use at your own risk.");

        pm.beginTask("Ensuring virtual program graph is up-to-date", IProgressMonitor.UNKNOWN);
        vpg.ensureVPGIsUpToDate(pm);
       	pm.done();

       	status = getAbstractSyntaxTree(status);

       	if(status.hasFatalError())
       	    return status;

        //pm.beginTask("Checking initial preconditions", IProgressMonitor.UNKNOWN);
        try
        {
        	doCheckInitialConditions(status, new ForwardingProgressMonitor(pm));
        }
        catch (PreconditionFailure f)
        {
        	status.addFatalError(f.getMessage());
        }
        //pm.done();

        return status;
    }

    protected abstract RefactoringStatus getAbstractSyntaxTree(RefactoringStatus status);

    protected abstract void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    protected void logVPGErrors(RefactoringStatus status)
    {
		for (VPGLog<Token, PhotranTokenRef>.Entry entry : vpg.log.getEntries())
		{
			if (entry.isWarning())
				status.addWarning(entry.getMessage(), createContext(entry.getTokenRef()));
			else
				status.addError(entry.getMessage(), createContext(entry.getTokenRef()));
		}
	}

	@Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm)
    {
        allChanges = new CompositeChange(getName());

		RefactoringStatus status = new RefactoringStatus();
		//pm.beginTask("Checking final preconditions; please wait...", IProgressMonitor.UNKNOWN);
        try
        {
        	doCheckFinalConditions(status, new ForwardingProgressMonitor(pm));
        }
        catch (PreconditionFailure f)
        {
        	status.addFatalError(f.getMessage());
        }
        //pm.done();
        return status;
    }

	protected abstract void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

	/**
	 * To get text to display in the GUI, the precondition checking methods
	 * must call {@link IProgressMonitor#setTaskName(String)} rather than
	 * {@link IProgressMonitor#subTask(String)}.  However, the change creation
	 * method <i>can</i> call {@link IProgressMonitor#subTask(String)}.  This
	 * &quot;forwards&quot; calls to {@link #subTask(String)} to
	 * {@link #setTaskName(String)}.
	 *
	 * @author Jeff Overbey
	 */
	protected static class ForwardingProgressMonitor implements IProgressMonitor
	{
	    private IProgressMonitor pm;

        public ForwardingProgressMonitor(IProgressMonitor pm)
        {
            this.pm = pm;
        }

        public void beginTask(String name, int totalWork) { pm.beginTask(name, totalWork); }
        public void done() { pm.done(); }
        public void internalWorked(double work) { pm.internalWorked(work); }
        public boolean isCanceled() { return pm.isCanceled(); }
        public void setCanceled(boolean value) { pm.setCanceled(value); }
        public void setTaskName(String name) { pm.setTaskName(name); }
        public void worked(int work) { pm.worked(work); }

        public void subTask(String name)
        {
            pm.setTaskName(name);
        }
    }

    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    	assert pm != null;

        //pm.beginTask("Constructing workspace transformation; please wait...", IProgressMonitor.UNKNOWN);
        // allChanges constructed above in #checkFinalConditions
        doCreateChange(pm);
        //pm.done();
        return allChanges;
    }

    protected abstract void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    ///////////////////////////////////////////////////////////////////////////
    // Utilities for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A <code>PreconditionFailure</code> is thrown (e.g., by {@link AbstractFortranRefactoring#fail(String)})
     * to indicate an error severe enough that the refactoring cannot be completed.
     */
    protected static class PreconditionFailure extends Exception
    {
		private static final long serialVersionUID = 1L;

		public PreconditionFailure(String message)
    	{
    		super(message);
    	}
    }

    /**
     * Throws a <code>PreconditionFailure</code>, indicating an error severe enough
     * that the refactoring cannot be completed.
     *
     * @param message an error message to display to the user
     */
    protected void fail(String message) throws PreconditionFailure
    {
    	throw new PreconditionFailure(message);
    }

    // CODE EXTRACTION ////////////////////////////////////////////////////////

    /**
     * Parses the given Fortran statement.
     * <p>
     * Internally, <code>string</code> is embedded into the following program
     * <pre>
     * program p
     *   (string is placed here)
     * end program
     * </pre>
     * which is parsed and its body extracted and returned,
     * so <code>string</code> must "make sense" (syntactically) in this context.
     * No semantic analysis is done; it is only necessary that the
     * program be syntactically correct.
     */
    protected IBodyConstruct parseLiteralStatement(String string)
    {
        return parseLiteralStatementSequence(string).get(0);
    }

    /**
     * Parses the given Fortran statement, or returns <code>null</code> if the
     * statement cannot be parsed.
     *
     * @see #parseLiteralStatement(String)
     */
    protected IBodyConstruct parseLiteralStatementNoFail(String string)
    {
        try
        {
            return parseLiteralStatement(string);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    /**
     * Parses the given Fortran expression.
     * <p>
     * Internally, <code>string</code> is embedded into the following program
     * <pre>
     * program p
     *   x = (string is placed here)
     * end program
     * </pre>
     * which is parsed and the resulting expression extracted and returned,
     * so <code>string</code> must "make sense" (syntactically) in this context.
     * No semantic analysis is done; it is only necessary that the
     * program be syntactically correct.
     */
    protected IExpr parseLiteralExpression(String string)
    {
        return ((ASTAssignmentStmtNode)parseLiteralStatement("x = " + string)).getRhs();
    }

    /**
     * Parses the given list of Fortran statements.
     * <p>
     * @see parseLiteralStatement
     */
    protected IASTListNode<IBodyConstruct> parseLiteralStatementSequence(String string)
    {
        string = "program p\n" + string + "\nend program";
        return ((ASTMainProgramNode)parseLiteralProgramUnit(string)).getBody();
    }

    /** @return a CONTAINS statement */
    protected ASTContainsStmtNode createContainsStmt()
    {
        String string = "program p\ncontains\nsubroutine s\nend subroutine\nend program";
        return ((ASTMainProgramNode)parseLiteralProgramUnit(string)).getContainsStmt();
    }

    /**
     * Parses the given Fortran program unit.
     * <p>
     * No semantic analysis is done; it is only necessary that the
     * program unit be syntactically correct.
     */
    protected IProgramUnit parseLiteralProgramUnit(String string)
    {
        try
        {
            IAccumulatingLexer lexer = LexerFactory.createLexer(
                new ByteArrayInputStream(string.getBytes()), null, "(none)",
                SourceForm.UNPREPROCESSED_FREE_FORM, true);
            if (parser == null) parser = new Parser();

            FortranAST ast = new FortranAST(null, parser.parse(lexer), lexer.getTokenList());
            return ast.getRoot().getProgramUnitList().get(0);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }
    private Parser parser = null;

    // USER INTERACTION ///////////////////////////////////////////////////////

    protected String describeToken(Token token)
    {
        return "\"" + token.getText() + "\" " + this.describeTokenPos(token);
    }

    protected String describeTokenPos(Token token)
    {
        return "(line " + token.getLine() + ", column " + token.getCol() + ")";
    }

    // REFACTORING STATUS /////////////////////////////////////////////////////

	protected RefactoringStatusContext createContext(TokenRef<Token> tokenRef)
	{
		if (tokenRef == null) return null;

		IFile file = PhotranVPG.getIFileForFilename(tokenRef.getFilename());
		if (file == null) return null;

		return new FileStatusContext(file,
		                             new Region(tokenRef.getOffset(), tokenRef.getLength()));
	}

    protected RefactoringStatusContext createContext(Token token)
    {
        return createContext(token.getTokenRef());
    }

    // CHANGE CREATION ////////////////////////////////////////////////////////

	/**
	 * This method should be called from within the <code>doCreateChange</code>
	 * method after all of the changes to a file's AST have been made.
	 */
    protected void addChangeFromModifiedAST(IFile file, IProgressMonitor pm)
    {
        try
        {
            IFortranAST ast = vpg.acquireTransientAST(file);
            TextFileChange changeThisFile = new TextFileChange(getName() + " - " + file.getFullPath().toOSString(), file);
            changeThisFile.initializeValidationData(pm);
            changeThisFile.setEdit(new ReplaceEdit(0, getSizeOf(file), SourcePrinter.getSourceCodeFromAST(ast)));
            allChanges.add(changeThisFile);
            //FortranWorkspace.getInstance().releaseTU(file);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    private int getSizeOf(IFile file) throws CoreException, IOException
    {
        int size = 0;
        InputStream in = file.getContents();
        while (in.read() > -1)
            size++;
        return size;
    }

    // TEXT<->TREE MAPPING ////////////////////////////////////////////////////

    protected Definition findUnambiguousTokenDefinition(Token t)
    {
        if(t == null)
            return null;

        List<Definition> defs = t.resolveBinding();
        if(defs.size() <= 0 || defs.size() > 1)
            return null;
        return defs.get(0);
    }

    protected Token findEnclosingToken(IFortranAST ast, final ITextSelection selection)
    {
        Token prevToken = null;
        for (Token token : new IterableWrapper<Token>(ast))
        {
        	if (OffsetLength.contains(token.getFileOffset(), token.getLength(),
        	                          selection.getOffset(), selection.getLength()))
        	{
                String tokenText = token.getText();
                //If we get whitespace, that means we want the previous token (cursor was put AFTER
                // the identifier we want to rename
                if(tokenText.length() == 1 && Character.isWhitespace(tokenText.charAt(0)))
                {
                    return prevToken;
                }
                else
                    return token;
        	}
        	prevToken = token;
        }
        return null;
    }

    protected IASTNode findEnclosingNode(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));
        if (firstToken == null || lastToken == null) return null;

        for (IASTNode parent = lastToken.getParent(); parent != null; parent = parent.getParent())
            if (contains(parent, firstToken))
                return parent;

        return null;
    }

    protected boolean nodeExactlyEnclosesRegion(IASTNode parent, Token firstToken, Token lastToken)
    {
        return parent.findFirstToken() == firstToken && parent.findLastToken() == lastToken;
    }

    protected boolean nodeExactlyEnclosesRegion(IASTNode node, IFortranAST ast, ITextSelection selection)
    {
        Token firstInNode = node.findFirstToken();
        Token lastInNode = node.findLastToken();

        Token firstInSel = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastInSel = this.findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));

        return firstInNode != null
            && lastInNode != null
            && firstInSel != null
            && lastInSel != null
            && firstInNode == firstInSel
            && lastInNode == lastInSel;
    }

//    protected IASTNode findEnclosingNode(IFortranAST ast, ITextSelection selection, Nonterminal nodeType, boolean allowNesting)
//    {
//        IASTNode smallestEnclosure = findEnclosingNode(ast, selection);
//        if (smallestEnclosure == null) return null;
//
//        for (IASTNode n = smallestEnclosure; n != null; n = n.getParent())
//        {
//            if (n.getNonterminal() == nodeType)
//            {
//                if (allowNesting)
//                    return n;
//                else if (n.getParent() == null)
//                    return null;
//                else if (n.getParent().getNonterminal() != nodeType)
//                    return n;
//                else if (n.getParent().getNonterminal() == nodeType)
//                    continue;
//            }
//        }
//
//        return null;
//    }

    private boolean contains(IASTNode target, Token token)
    {
        for (IASTNode node = token.getParent(); node != null; node = node.getParent())
            if (node == target)
                return true;
        return false;
    }

    private Token findFirstTokenAfter(IFortranAST ast, final int targetFileOffset)
    {
        for (Token token : new IterableWrapper<Token>(ast))
            if (token.isOnOrAfterFileOffset(targetFileOffset))
                return token;
        return null;
    }

    private Token findLastTokenBefore(IFortranAST ast, final int targetFileOffset)
    {
        Token previousToken = null;
        for (Token token : new IterableWrapper<Token>(ast))
        {
            if (token.isOnOrAfterFileOffset(targetFileOffset))
                return previousToken;
            else
                previousToken = token;
        }
        return null;
    }

    protected static class StatementSequence
    {
        public final ScopingNode enclosingScope;
        public final IASTListNode<? extends IASTNode> listContainingStmts;
        public final int startIndex;
        public final int endIndex;
        public final List<IASTNode> selectedStmts;

        private StatementSequence(ScopingNode enclosingScope, IASTListNode<? extends IASTNode> body, int startIndex, int endIndex)
        {
            this.enclosingScope = enclosingScope;
            this.listContainingStmts = body;
            this.startIndex = startIndex;
            this.endIndex = endIndex;

//            this.precedingStmts = new ArrayList<IASTNode>();
//            for (int i = 1; i < startIndex; i++)
//                this.precedingStmts.add(body.get(i));

            this.selectedStmts = new ArrayList<IASTNode>();
            for (int i = startIndex; i <= endIndex; i++)
                if (body.get(i) != null)
                    this.selectedStmts.add(body.get(i));

//            this.followingStmts = new ArrayList<IASTNode>();
//            for (int i = endIndex + 1; i < body.size(); i++)
//                this.followingStmts.add(body.get(i));
        }

        public IASTNode firstStmt()
        {
            return selectedStmts.get(0);
        }

        public Token firstToken()
        {
            return firstStmt().findFirstToken();
        }

        public IASTNode lastStmt()
        {
            return selectedStmts.get(selectedStmts.size()-1);
        }

        public Token lastToken()
        {
            return lastStmt().findLastToken();
        }
    }

    protected ASTProperLoopConstructNode getLoopNode(IFortranAST ast, ITextSelection selection)
    {
        /*Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
        if (firstToken == null || lastToken == null)
            return null;

        return getLoopNode(firstToken, lastToken);*/
        return (ASTProperLoopConstructNode)getNode(ast, selection, ASTProperLoopConstructNode.class);
    }

    protected ASTNode getNode(IFortranAST ast, ITextSelection selection, Class<? extends Parser.ASTNode> node)
    {
        Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
        if (firstToken == null || lastToken == null)
            return null;
        return getNode(firstToken, lastToken, node);
    }

    protected ASTNode getNode(Token firstToken, Token lastToken, Class<? extends Parser.ASTNode> node)
    {
        assert(firstToken != null);
        assert(lastToken  != null);
        ASTNode firstTokenNode = firstToken.findNearestAncestor(node);
        ASTNode lastTokenNode = lastToken.findNearestAncestor(node);
        if(firstTokenNode == null || lastTokenNode == null || firstTokenNode != lastTokenNode)
            return null;
        return firstTokenNode;
        //return null;
    }

    protected ASTProperLoopConstructNode getLoopNode(Token firstToken, Token lastToken)
    {
        /*assert(firstToken != null);
        assert(lastToken  != null);
        ASTProperLoopConstructNode loopContainingFirstToken = firstToken.findNearestAncestor(ASTProperLoopConstructNode.class);
        ASTProperLoopConstructNode loopContainingLastToken = lastToken.findNearestAncestor(ASTProperLoopConstructNode.class);
        if (loopContainingFirstToken == null || loopContainingLastToken == null || loopContainingFirstToken != loopContainingLastToken)
            return null;

        return loopContainingFirstToken;*/
        return (ASTProperLoopConstructNode)getNode(firstToken, lastToken, ASTProperLoopConstructNode.class);
    }

    @SuppressWarnings("unchecked")
    protected StatementSequence findEnclosingStatementSequence(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
        if (firstToken == null || lastToken == null) return null;

        IASTListNode<? extends IASTNode> listContainingFirstToken = firstToken.findNearestAncestor(IASTListNode.class);
        IASTListNode<? extends IASTNode> listContainingLastToken = lastToken.findNearestAncestor(IASTListNode.class);
        if (listContainingFirstToken == null || listContainingLastToken == null || listContainingFirstToken != listContainingLastToken) return null;

        IASTListNode<? extends IASTNode> listContainingStmts = listContainingFirstToken;
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < listContainingStmts.size(); i++)
        {
            IASTNode node = listContainingStmts.get(i);
            if (contains(node, firstToken))
                startIndex = i;
            if (contains(node, lastToken))
                endIndex = i;
        }
        if (startIndex < 0 || endIndex < 0 || endIndex < startIndex)
            throw new Error("INTERNAL ERROR: Unable to locate selected statements in IASTListNode");

        return new StatementSequence(
            listContainingStmts.findNearestAncestor(ScopingNode.class),
            listContainingStmts,
            startIndex,
            endIndex);
    }

//    private IASTListNode<? extends IASTNode> findEnclosingBodyNode(Token token)
//    {
//        ScopingNode scope = token.findNearestAncestor(ScopingNode.class);
//        return scope == null ? null : scope.getBody();
//    }
//
//    private boolean isBodyNode(IASTNode currentNode)
//    {
//        return currentNode instanceof ASTBodyNode;
//    }
//
//    private boolean isNestedBodyNode(IASTNode currentNode)
//    {
//        return isBodyNode(currentNode)
//               && currentNode.getParent() != null
//               && currentNode.getParent() instanceof ASTBodyNode;
//    }

    protected int findIndexToInsertTypeDeclaration(IASTListNode<? extends IASTNode> body)
    {
        IASTNode node = null;
        Iterator<? extends IASTNode> iterator = body.iterator();
        while(iterator.hasNext())
        {
            node = iterator.next();
            if (!(node instanceof ASTUseStmtNode) && !(node instanceof ASTImplicitStmtNode))
            {
                break;
            }
        }
        //If there are no other nodes besides use statements and implicit none, then increment the index
        if (node instanceof ASTUseStmtNode || node instanceof ASTImplicitStmtNode)
        {
            return body.indexOf(node) + 1;
        }else
        {
            return body.indexOf(node);
        }
    }

    protected int findIndexToInsertStatement(IASTListNode<? extends IASTNode> body)
    {
        IASTNode node = null;
        Iterator<? extends IASTNode> iterator = body.iterator();
        while(iterator.hasNext())
        {
            node = iterator.next();
            if (!(node instanceof ISpecificationPartConstruct))
            {
                break;
            }
        }
        //If there are no other nodes besides those that implement ISpecificationPartConstruct,
        //then increment the index
        if (node instanceof ISpecificationPartConstruct)
        {
            return body.indexOf(node) + 1;
        }else
        {
            return body.indexOf(node);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    // P R E C O N D I T I O N S
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    protected abstract void ensureProjectHasRefactoringEnabled(RefactoringStatus status) throws PreconditionFailure;

    protected boolean isIdentifier(Token token)
    {
        return token != null && token.getTerminal() == Terminal.T_IDENT;
    }

    protected boolean isPreprocessed(Token token)
    {
        return token.getPreprocessorDirective() != null;
    }

    protected boolean isValidIdentifier(String name)
    {
        return Pattern.matches("[A-Za-z][A-Za-z0-9_]*", name);
    }

    protected boolean isBoundIdentifier(Token t)
    {
        return isIdentifier(t) && !t.resolveBinding().isEmpty();
    }

    protected boolean isUniquelyDefinedIdentifer(Token t)
    {
        return isBoundIdentifier(t) && t.resolveBinding().size() == 1;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Check for conflicting bindings
    ///////////////////////////////////////////////////////////////////////////

    public static final class Conflict
    {
        public final String name;
        public final PhotranTokenRef tokenRef;

        public Conflict(String name, PhotranTokenRef tokenRef)
        {
            this.name = name;
            this.tokenRef = tokenRef;
        }
    }


    public static interface IConflictingBindingCallback
    {
        void addConflictError(List<Conflict> conflictingDef);
        void addConflictWarning(List<Conflict> conflictingDef);
        void addReferenceWillChangeError(String newName, Token reference);
    }

    /**
     * Given a {@link Definition} and a list of references to that Definition
     * (see {@link Definition#findAllReferences(boolean)}), checks if any of
     * the <code>newNames</code> will conflict in the scope of any of the given
     * references; if so, the given callback is invoked to record an error or
     * warning.
     * <p>
     * This is the fundamental precondition check for {@link RenameRefactoring}.
     */
    protected void checkForConflictingBindings(
        IConflictingBindingCallback callback,
        Definition definitionToCheck,
        Collection<PhotranTokenRef> allReferences,
        String... newNames)
    {
        checkForConflictingBindings(new NullProgressMonitor(), callback, definitionToCheck, allReferences, newNames);
    }

    /**
     * Given a {@link Definition} and a list of references to that Definition
     * (see {@link Definition#findAllReferences(boolean)}), checks if any of
     * the <code>newNames</code> will conflict in the scope of any of the given
     * references; if so, the given callback is invoked to record an error or
     * warning.
     * <p>
     * This is the fundamental precondition check for {@link RenameRefactoring}.
     */
    protected void checkForConflictingBindings(
        IProgressMonitor pm,
        IConflictingBindingCallback callback,
        Definition definitionToCheck,
        Collection<PhotranTokenRef> allReferences,
        String... newNames)
    {
        checkForConflictingBindings(pm, callback, definitionToCheck, allReferences, Arrays.asList(newNames));
    }

    /**
     * Given a {@link Definition} and a list of references to that Definition
     * (see {@link Definition#findAllReferences(boolean)}), checks if any of
     * the <code>newNames</code> will conflict in the scope of any of the given
     * references; if so, the given callback is invoked to record an error or
     * warning.
     * <p>
     * This is the fundamental precondition check for {@link RenameRefactoring}.
     */
    protected void checkForConflictingBindings(
        IProgressMonitor pm,
        IConflictingBindingCallback callback,
        Definition definitionToCheck,
        Collection<PhotranTokenRef> allReferences,
        Collection<String> newNames)
    {
        new CheckForConflictBindings().check(pm, callback, definitionToCheck, allReferences, newNames);
    }

    private final class CheckForConflictBindings
    {
        private IProgressMonitor pm = null;
        private Definition definitionToCheck = null;
        private Collection<String> newNames = null;

        public void check(IProgressMonitor pm,
                          IConflictingBindingCallback callback,
                          Definition definitionToCheck,
                          Collection<PhotranTokenRef> allReferences,
                          Collection<String> newNames)
        {
            this.pm = pm;
            this.definitionToCheck = definitionToCheck;
            this.newNames = newNames;

            checkForConflictingDefinitionOrShadowing(callback);

            for (PhotranTokenRef ref : findReferencesToShadowedDefinitions())
                checkIfReferenceBindingWillChange(callback, ref, false);

            for (PhotranTokenRef ref : allReferences)
                checkIfReferenceBindingWillChange(callback, ref, true);
        }

        /** Check whether the new definition will either conflict with or shadow an existing definition */
        private void checkForConflictingDefinitionOrShadowing(IConflictingBindingCallback callback)
        {
            List<Conflict> conflictingDef = findAllPotentiallyConflictingDefinitions();
            if (!conflictingDef.isEmpty())
                callback.addConflictError(conflictingDef);

            conflictingDef = findAllPotentiallyConflictingUnboundSubprogramCalls();
            if (!conflictingDef.isEmpty())
                callback.addConflictWarning(conflictingDef);
        }

        private List<Conflict> findAllPotentiallyConflictingDefinitions()
        {
            List<Conflict> conflicts = new ArrayList<Conflict>();

            // Cannot call a main program (or function, etc.) X if it has an internal subprogram named X,
            // even if that subprogram is never used (in which case it wouldn't be caught below)
            if (definitionToCheck.isMainProgram() || definitionToCheck.isSubprogram() || definitionToCheck.isModule())
                findAllPotentiallyConflictingDefinitionsInScope(conflicts, definitionToCheck.getTokenRef().findToken().findNearestAncestor(ScopingNode.class), false);
            for (String newName : newNames)
                if (definitionToCheck.isInternalSubprogramDefinition() && scopeContainingInternalSubprogram().isNamed(newName))
                    conflicts.add(new Conflict(newName, scopeContainingInternalSubprogram().getNameToken().getTokenRef()));

            for (ScopingNode importingScope : scopeItselfAndAllScopesThatImport(scopeOfDefinitionToCheck()))
            {
                pm.subTask("Checking for conflicting definitions in " + importingScope.describe());
                findAllPotentiallyConflictingDefinitionsInScope(conflicts, importingScope, true);
            }

            return conflicts;
        }

        private ScopingNode scopeContainingInternalSubprogram()
        {
            return definitionToCheck.getTokenRef().findToken().getEnclosingScope();
        }

        /**
         * Cannot call a function X if it is defined in or imported into a scope with a function X already defined
         * <p>
         * The third parameter indicates whether or not we should check that the definition to check is actually imported
         * into the target scope (it may not be if there is a USE statement with a Rename or ONLY list).
         */
        private void findAllPotentiallyConflictingDefinitionsInScope(List<Conflict> conflicts, ScopingNode importingScope, boolean shouldCheckIfDefinitionImportedIntoScope)
        {
            for (String newName : newNames)
            {
                Token newNameToken = new FakeToken(definitionToCheck.getTokenRef().findToken(), newName);

                List<PhotranTokenRef> definitionsLocalToScope = collectLocalDefinitions(importingScope);

                if (isProgramOrSubprogramOrModuleScope(importingScope) && shouldCheckIfDefinitionImportedIntoScope)
                {
                    // Cannot call a variable X inside a function named X
                    if (importingScope.isNamed(newName) && definitionsLocalToScope.contains(definitionToCheck.getTokenRef()))
                    {
                        conflicts.add(new Conflict(newName, importingScope.getNameToken().getTokenRef()));
                    }
                    // Cannot call a variable X inside a function named Y inside a module named X
                    else
                    {
                        ScopingNode parent = importingScope.findNearestAncestor(ScopingNode.class);
                        if (parent != null && parent.isNamed(newName))
                        {
                            List<PhotranTokenRef> definitionsLocalToParent = collectLocalDefinitions(parent);
                            if (definitionsLocalToParent.contains(definitionToCheck.getTokenRef()))
                                conflicts.add(new Conflict(newName, parent.getNameToken().getTokenRef()));
                        }
                    }
                }

                // Cannot call a function X if it is defined in or imported into a scope with a function X already defined
                for (PhotranTokenRef conflict : importingScope.manuallyResolveInLocalScope(newNameToken))
                {
                    if (definitionsLocalToScope.contains(conflict))
                    {
                        if (shouldCheckIfDefinitionImportedIntoScope)
                        {
                            if (definitionsLocalToScope.contains(definitionToCheck.getTokenRef()))
                                conflicts.add(new Conflict(newName, conflict));
                        }
                        else
                        {
                            conflicts.add(new Conflict(newName, conflict));
                        }
                    }
                }
            }
        }

        /** Check whether the new definition will either conflict with or shadow an existing definition */
        private List<PhotranTokenRef> findReferencesToShadowedDefinitions()
        {
            List<PhotranTokenRef> referencesToShadowedDefinitions = new LinkedList<PhotranTokenRef>();

            for (String newName : newNames)
            {
                Token token = new FakeToken(definitionToCheck.getTokenRef().findToken(), newName);

                List<PhotranTokenRef> shadowedDefinitions = scopeOfDefinitionToCheck().manuallyResolve(token);
                // TODO: Does not consider rename or only lists (need to tell if this SPECIFIC definition will be imported)
                for (ScopingNode importingScope : scopeOfDefinitionToCheck().findImportingScopes())
                {
                    pm.subTask("Checking for references to " + newName + " in " + importingScope.describe());
                    shadowedDefinitions.addAll(importingScope.manuallyResolve(token));
                }

                for (PhotranTokenRef def : shadowedDefinitions)
                    referencesToShadowedDefinitions.addAll(vpg.getDefinitionFor(def).findAllReferences(false));
            }

            return referencesToShadowedDefinitions;
        }

        private void checkIfReferenceBindingWillChange(IConflictingBindingCallback callback, PhotranTokenRef ref, boolean shouldReferenceRenamedDefinition)
        {
            pm.subTask("Checking for binding conflicts in " + ref.getFilename());

            Token reference = ref.findToken();

            ScopingNode scopeOfDefinitionToRename = reference.findScopeDeclaringOrImporting(definitionToCheck);
            if (scopeOfDefinitionToRename == null) return;

            for (String newName : newNames)
            {
                for (PhotranTokenRef existingBinding : new FakeToken(reference, newName).manuallyResolveBinding())
                {
                    ScopingNode scopeOfExistingBinding = existingBinding.findToken().getEnclosingScope();

                    boolean willReferenceRenamedDefinition = scopeOfExistingBinding.isParentScopeOf(scopeOfDefinitionToRename);
                    if (shouldReferenceRenamedDefinition != willReferenceRenamedDefinition)
                        callback.addReferenceWillChangeError(newName, reference);
                }
            }
        }

        private List<Conflict> findAllPotentiallyConflictingUnboundSubprogramCalls()
        {
            final List<Conflict> conflictingDef = new ArrayList<Conflict>();

            for (ScopingNode importingScope : scopeItselfAndAllScopesThatImport(scopeOfDefinitionToCheck()))
            {
                pm.subTask("Checking for subprogram binding conflicts in " + importingScope.describe());

                importingScope.accept(new GenericASTVisitor()
                {
                    @Override public void visitASTVarOrFnRefNode(ASTVarOrFnRefNode node)
                    {
                        if (node.getName() != null)
                            checkForConflict(node.getName().getName());
                    }

                    @Override public void visitASTCallStmtNode(ASTCallStmtNode node)
                    {
                        if (node.getSubroutineName() != null)
                            checkForConflict(node.getSubroutineName());
                    }

                    private void checkForConflict(Token name)
                    {
                        for (String newName : newNames)
                            if (name != null && name.getText().equals(newName) && name.resolveBinding().isEmpty())
                                conflictingDef.add(new Conflict(newName, name.getTokenRef()));
                    }
                });
            }

            return conflictingDef;
        }

        private ScopingNode scopeOfDefinitionToCheck()
        {
            return definitionToCheck.getTokenRef().findToken().getEnclosingScope();
        }

        private Iterable<ScopingNode> scopeItselfAndAllScopesThatImport(final ScopingNode scope)
        {
            if (scope == null) return Collections.emptySet();

            return new Iterable<ScopingNode>()
            {
                public Iterator<ScopingNode> iterator()
                {
                    return new Iterator<ScopingNode>()
                    {
                        private ScopingNode first = scope;
                        private Iterator<ScopingNode> rest = scope.findImportingScopes().iterator();

                        public boolean hasNext()
                        {
                            if (first != null)
                                return true;
                            else
                                return rest.hasNext();
                        }

                        public ScopingNode next()
                        {
                            if (first != null)
                            {
                                ScopingNode result = first;
                                first = null;
                                return result;
                            }
                            else return rest.next();
                        }

                        public void remove() { throw new UnsupportedOperationException(); }
                    };
                }
            };
        }

        private List<PhotranTokenRef> collectLocalDefinitions(ScopingNode importingScope)
        {
            List<PhotranTokenRef> definitionsLocalToScope = new ArrayList<PhotranTokenRef>();
            for (Definition def : importingScope.getAllDefinitions())
                if (!def.isIntrinsic())
                    definitionsLocalToScope.add(def.getTokenRef());
            return definitionsLocalToScope;
        }

        private boolean isProgramOrSubprogramOrModuleScope(ScopingNode scope)
        {
            return scope instanceof ASTMainProgramNode
                || scope instanceof ASTFunctionSubprogramNode
                || scope instanceof ASTSubroutineSubprogramNode
                || scope instanceof ASTModuleNode;
        }
    }
}
