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
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.util.IterableWrapper;
import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.core.vpg.util.OffsetLength;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTBodyConstructNode;
import org.eclipse.photran.internal.core.parser.ASTBodyNode;
import org.eclipse.photran.internal.core.parser.ASTExprNode;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.parser.Parser.Nonterminal;
import org.eclipse.text.edits.ReplaceEdit;

import bz.over.vpg.TokenRef;
import bz.over.vpg.VPGErrorOrWarning;

/**
 * Superclass for all refactorings in Photran.
 * <p>
 * In addition to implementing the LTK refactoring interface, this class provides a number of methods to subclasses,
 * including methods to display error messages, find enclosing nodes in an AST, check identifier tokens, etc.
 * 
 * @author Jeff Overbey
 */
public abstract class FortranRefactoring extends Refactoring
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
    
    protected IFile fileInEditor;
    protected final boolean inFixedFormEditor = false;
    protected ITextSelection selectedRegionInEditor;
    protected IFortranAST astOfFileInEditor;
    
    private CompositeChange allChanges;
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    public FortranRefactoring(IFile file, /* boolean isFixedForm, */ ITextSelection selection)
    {
    	assert file != null && file.isAccessible();
    	assert selection != null;

    	this.vpg = PhotranVPG.getInstance();
    	
        this.fileInEditor = file;
        //this.isFixedForm = isFixedForm;
        this.selectedRegionInEditor = selection;
    }

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
        
        pm.beginTask("Analyzing program", IProgressMonitor.UNKNOWN);
        this.astOfFileInEditor = vpg.acquireTransientAST(fileInEditor);
        logVPGErrors(status);
        if (astOfFileInEditor == null)
        {
        	status.addFatalError("The file in the editor cannot be parsed.");
        	return status;
        }
        
        pm.beginTask("Checking initial preconditions", IProgressMonitor.UNKNOWN);
        try
        {
        	doCheckInitialConditions(status, pm);
        }
        catch (PreconditionFailure f)
        {
        	status.addFatalError(f.getMessage());
        }
        pm.done();
        
        return status;
    }

    protected abstract void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    private void logVPGErrors(RefactoringStatus status)
    {
		for (VPGErrorOrWarning<Token> entry : vpg.getErrorLog())
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
		RefactoringStatus status = new RefactoringStatus();
		pm.beginTask("Checking final preconditions; please wait...", IProgressMonitor.UNKNOWN);
        try
        {
        	doCheckFinalConditions(status, pm);
        }
        catch (PreconditionFailure f)
        {
        	status.addFatalError(f.getMessage());
        }
        pm.done();
        return status;
    }

	protected abstract void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure;

    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
    	assert pm != null;
    	
        pm.beginTask("Constructing workspace transformation; please wait...", IProgressMonitor.UNKNOWN);
        allChanges = new CompositeChange(getName());
        doCreateChange(pm);
        pm.done();
        return allChanges;
    }

    protected abstract void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException;

    ///////////////////////////////////////////////////////////////////////////
    // Utilities for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A <code>PreconditionFailure</code> is thrown (e.g., by {@link FortranRefactoring#fail(String)})
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
    protected ASTBodyConstructNode parseLiteralStatement(String string)
    {
        return parseLiteralStatementSequence(string).getBodyConstruct(0);
    }
    
    /**
     * Parses the given list of Fortran statements.
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
     * </pre>
     */
    protected ASTBodyNode parseLiteralStatementSequence(String string)
    {
        string = "program p\n" + string + "\nend program";
        try
        {
            IAccumulatingLexer lexer = LexerFactory.createLexer(new ByteArrayInputStream(string.getBytes()), "(none)", SourceForm.UNPREPROCESSED_FREE_FORM);
            return new FortranAST(null, new Parser().parse(lexer), lexer.getTokenList()).getRoot().getProgramUnit(0).getMainProgram().getBody();
        }
        catch (Exception e)
        {
            throw new Error(e);
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
    protected ASTExprNode parseLiteralExpression(String string)
    {
        ASTBodyConstructNode stmt = parseLiteralStatement("x = " + string);
        return stmt.getExecutableConstruct().getAssignmentStmt().getExpr();
    }

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
    
    @SuppressWarnings("unchecked")
    protected Token findEnclosingToken(IFortranAST ast, final ITextSelection selection)
    {
        for (Token token : new IterableWrapper<Token>(ast))
        	if (OffsetLength.contains(token.getFileOffset(), token.getLength(),
        	                          selection.getOffset(), selection.getLength()))
                return token;
        return null;
    }
    
    protected InteriorNode findEnclosingNode(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));
        if (firstToken == null || lastToken == null) return null;

        for (InteriorNode parent = lastToken.getParent(); parent != null; parent = parent.getParent())
            if (contains(parent, firstToken))
                return parent;
        
        return null;
    }
    
    protected boolean nodeExactlyEnclosesRegion(InteriorNode parent, Token firstToken, Token lastToken)
    {
        return findFirstTokenIn(parent) == firstToken && findLastTokenIn(parent) == lastToken;
    }
    
    protected boolean nodeExactlyEnclosesRegion(InteriorNode node, IFortranAST ast, ITextSelection selection)
    {
        Token firstInNode = this.findFirstTokenIn(node);
        Token lastInNode = this.findLastTokenIn(node);
        
        Token firstInSel = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastInSel = this.findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));
        
        return firstInNode != null
            && lastInNode != null
            && firstInSel != null
            && lastInSel != null
            && firstInNode == firstInSel
            && lastInNode == lastInSel;
    }

    protected InteriorNode findEnclosingNode(IFortranAST ast, ITextSelection selection, Nonterminal nodeType, boolean allowNesting)
    {
        InteriorNode smallestEnclosure = findEnclosingNode(ast, selection);
        if (smallestEnclosure == null) return null;
        
        for (InteriorNode n = smallestEnclosure; n != null; n = n.getParent())
        {
            if (n.getNonterminal() == nodeType)
            {
                if (allowNesting)
                    return n;
                else if (n.getParent() == null)
                    return null;
                else if (n.getParent().getNonterminal() != nodeType)
                    return n;
                else if (n.getParent().getNonterminal() == nodeType)
                    continue;
            }
        }
        
        return null;
    }
    
    private boolean contains(InteriorNode target, Token token)
    {
        for (InteriorNode node = token.getParent(); node != null; node = node.getParent())
            if (node == target)
                return true;
        return false;
    }

    protected ASTBodyNode findEnclosingBodyNode(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
        if (firstToken == null || lastToken == null) return null;

        ASTBodyNode bodyAtBeginning = this.findEnclosingBodyNode(firstToken);
        ASTBodyNode bodyAtEnd = this.findEnclosingBodyNode(lastToken);
        if (bodyAtBeginning == null || bodyAtEnd == null || bodyAtBeginning != bodyAtEnd) return null;

        return bodyAtBeginning;
    }
    
    private ASTBodyNode findEnclosingBodyNode(Token token)
    {
        InteriorNode currentNode = token.getParent();
        
        while (currentNode != null && (!isBodyNode(currentNode) || isNestedBodyNode(currentNode)))
            currentNode = currentNode.getParent();

        return (ASTBodyNode)currentNode;
    }

    private boolean isBodyNode(InteriorNode currentNode)
    {
        return currentNode instanceof ASTBodyNode;
    }

    private boolean isNestedBodyNode(InteriorNode currentNode)
    {
        return isBodyNode(currentNode)
               && currentNode.getParent() != null
               && currentNode.getParent() instanceof ASTBodyNode;
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
        public ASTBodyNode body = null;
        public List<ASTBodyConstructNode> statements = new ArrayList<ASTBodyConstructNode>();
    }
    
    protected StatementSequence findEnclosingStatementSequence(IFortranAST ast, ITextSelection selection)
    {
        StatementSequence stmtSeq = new StatementSequence();
        
        stmtSeq.body = this.findEnclosingBodyNode(ast, selection);
        if (stmtSeq.body == null) return null;
        
        for (int i = 0; i < stmtSeq.body.size(); i++)
        {
            ASTBodyConstructNode thisBodyConstruct = stmtSeq.body.getBodyConstruct(i);
            
            Token firstToken = findFirstTokenIn(thisBodyConstruct);
            Token lastToken = findLastTokenIn(thisBodyConstruct);
            
            boolean containsStart = OffsetLength.contains(selection.getOffset(), selection.getLength(), firstToken.getFileOffset(), firstToken.getLength());
            boolean containsEnd = OffsetLength.contains(selection.getOffset(), selection.getLength(), lastToken.getFileOffset(), lastToken.getLength());
            
            if (containsStart != containsEnd) return null; // "You must select entire statements.");
            
            if (containsStart && containsEnd) stmtSeq.statements.add(thisBodyConstruct);
        }
        
        return stmtSeq;
    }

    // PARSE TREE SEARCHING ///////////////////////////////////////////////////
    
    protected Token findFirstTokenIn(InteriorNode node)
    {
        try
        {
            node.visitUsing(new GenericParseTreeVisitor()
            {
                @Override
                public void visitToken(Token token)
                {
                    throw new Notification(token);
                }
                
            });
        }
        catch (Notification n)
        {
            return (Token)n.getResult();
        }
        return null;
    }
    
    private final static class LastTokenVisitor extends GenericParseTreeVisitor
    {
        private Token lastToken;
        
        @Override
        public void visitToken(Token token)
        {
            lastToken = token;
        }
        
        public Token getLastToken() { return lastToken; }
    }

    protected Token findLastTokenIn(InteriorNode node)
    {
        LastTokenVisitor lastTokenVisitor = new LastTokenVisitor();
        node.visitUsing(lastTokenVisitor);
        return lastTokenVisitor.getLastToken();
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    // P R E C O N D I T I O N S
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
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
}
