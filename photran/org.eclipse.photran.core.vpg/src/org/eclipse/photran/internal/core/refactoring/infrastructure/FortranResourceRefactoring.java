/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.FortranAST;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
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
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.IProgramUnit;
import org.eclipse.photran.internal.core.parser.ISpecificationPartConstruct;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.util.IterableWrapper;
import org.eclipse.photran.internal.core.util.Notification;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
import org.eclipse.rephraserengine.core.util.OffsetLength;
import org.eclipse.rephraserengine.core.vpg.refactoring.VPGResourceRefactoring;

/**
 * This is a base class for all Photran refactorings that apply to multiple files
 * @author Jeff Overbey, Timofey Yuvashev
 */
public abstract class FortranResourceRefactoring
    extends VPGResourceRefactoring<IFortranAST, Token, PhotranVPG>
    implements IResourceRefactoring
{
    // TEMPORARY -- So we can continue working on fixed form refactoring while effectively disabling it in the public 6.0 release
    public static final boolean FIXED_FORM_REFACTORING_ENABLED = System.getenv("ENABLE_FIXED_FORM_REFACTORING") != null;
    
    @Override
    protected final PhotranVPG getVPG()
    {
        return PhotranVPG.getInstance();
    }

    @Override
    protected final void preCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        //status.addWarning("C preprocessor directives are IGNORED by the refactoring engine.  Use at your own risk.");
     
        if (FIXED_FORM_REFACTORING_ENABLED)
        {
            for (IFile file : this.selectedFiles)
            {
                if (org.eclipse.photran.internal.core.lexer.sourceform.SourceForm.isFixedForm(file))
                {
                    status.addWarning("Indentation and line length is NOT checked when refactoring FIXED form files. " +
                        "Use at your own risk.");
                    return;
                }
            }
        }
    }

    @Override
    protected final String getSourceCodeFromAST(IFortranAST ast)
    {
        return SourcePrinter.getSourceCodeFromAST(ast);
    }

    protected void ensureProjectHasRefactoringEnabled(RefactoringStatus status) throws PreconditionFailure
    {
        if (FortranCorePlugin.inTestingMode()) return;

        HashSet<IFile> filesToBeRemoved = new HashSet<IFile>();

        for (IFile f : this.selectedFiles)
        {
            if (!PhotranVPG.getInstance().doesProjectHaveRefactoringEnabled(f))
            {
                if (f.getProject() == null)
                {
                    status.addWarning("The file " + f.getName() + " cannot be refactored because " +
                        "it is not inside a Fortran project.");
                    filesToBeRemoved.add(f);
                }
                else
                {
                    status.addWarning("Please enable analysis and refactoring in the project " +
                        "properties for " + f.getProject().getName() + ".");
                    filesToBeRemoved.add(f);
                }
            }
        }
        //Remove files that didn't have Refactoring enabled in their projects
        this.selectedFiles.removeAll(filesToBeRemoved);
    }

    protected void removeFixedFormFilesFrom(Collection<IFile> files, RefactoringStatus status)
    {
        if (FIXED_FORM_REFACTORING_ENABLED) return;
        
        Set<IFile> filesToRemove = new HashSet<IFile>();

        for (IFile file : files)
        {
            if (!filesToRemove.contains(file) && org.eclipse.photran.internal.core.lexer.sourceform.SourceForm.isFixedForm(file))
            {
                status.addError("The fixed form file " + file.getName() + " will not be refactored.");
                filesToRemove.add(file);
            }
        }

        files.removeAll(filesToRemove);
    }
    
    protected void removeCpreprocessedFilesFrom(Collection<IFile> files, RefactoringStatus status)
    {
        Set<IFile> filesToRemove = new HashSet<IFile>();

        for (IFile file : files)
        {
            if (!filesToRemove.contains(file) && org.eclipse.photran.internal.core.lexer.sourceform.SourceForm.isCPreprocessed(file))
            {
                status.addError("The C-preprocessed file " + file.getName() + " will not be refactored.");
                filesToRemove.add(file);
            }
        }

        files.removeAll(filesToRemove);
    }

    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    //
    // U T I L I T Y   M E T H O D S
    //
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    // REFACTORING STATUS /////////////////////////////////////////////////////

    protected RefactoringStatusContext createContext(Token token)
    {
        return createContext(token.getTokenRef());
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
    protected static IBodyConstruct parseLiteralStatement(String string)
    {
        return parseLiteralStatementSequence(string).get(0);
    }

    /**
     * Parses the given Fortran statement, or returns <code>null</code> if the
     * statement cannot be parsed.
     *
     * @see #parseLiteralStatement(String)
     */
    protected static IBodyConstruct parseLiteralStatementNoFail(String string)
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
    protected static IExpr parseLiteralExpression(String string)
    {
        return ((ASTAssignmentStmtNode)parseLiteralStatement("x = " + string)).getRhs();
    }

    /**
     * Parses the given list of Fortran statements.
     * <p>
     * @see parseLiteralStatement
     */
    protected static IASTListNode<IBodyConstruct> parseLiteralStatementSequence(String string)
    {
        string = "program p\n" + string + "\nend program";
        return ((ASTMainProgramNode)parseLiteralProgramUnit(string)).getBody();
    }

    /** @return a CONTAINS statement */
    protected static ASTContainsStmtNode createContainsStmt()
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
    protected static IProgramUnit parseLiteralProgramUnit(String string)
    {
        try
        {
            IAccumulatingLexer lexer = new ASTLexerFactory().createLexer(
                new StringReader(string), null, "(none)");
            Parser parser = new Parser();

            FortranAST ast = new FortranAST(null, parser.parse(lexer), lexer.getTokenList());
            return ast.getRoot().getProgramUnitList().get(0);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    // USER INTERACTION ///////////////////////////////////////////////////////

    protected static String describeToken(Token token)
    {
        return "\"" + token.getText() + "\" " + describeTokenPos(token);
    }

    protected static String describeTokenPos(Token token)
    {
        return "(line " + token.getLine() + ", column " + token.getCol() + ")";
    }

    // TEXT<->TREE MAPPING ////////////////////////////////////////////////////

    protected static Definition findUnambiguousDeclaration(Token t)
    {
        if(t == null)
            return null;

        List<Definition> defs = t.resolveBinding();
        if(defs.size() <= 0 || defs.size() > 1)
            return null;
        return defs.get(0);
    }

    protected static Token findEnclosingToken(IFortranAST ast, final ITextSelection selection)
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

    protected static IASTNode findEnclosingNode(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));
        if (firstToken == null || lastToken == null) return null;

        for (IASTNode parent = lastToken.getParent(); parent != null; parent = parent.getParent())
            if (contains(parent, firstToken))
                return parent;

        return null;
    }

    protected static boolean nodeExactlyEnclosesRegion(IASTNode parent, Token firstToken, Token lastToken)
    {
        return parent.findFirstToken() == firstToken && parent.findLastToken() == lastToken;
    }

    protected static boolean nodeExactlyEnclosesRegion(IASTNode node, IFortranAST ast, ITextSelection selection)
    {
        Token firstInNode = node.findFirstToken();
        Token lastInNode = node.findLastToken();

        Token firstInSel = findFirstTokenAfter(ast, selection.getOffset());
        Token lastInSel = findLastTokenBefore(ast, OffsetLength.getPositionPastEnd(selection.getOffset(), selection.getLength()));

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

    private static boolean contains(IASTNode target, Token token)
    {
        for (IASTNode node = token.getParent(); node != null; node = node.getParent())
            if (node == target)
                return true;
        return false;
    }

    private static Token findFirstTokenAfter(IFortranAST ast, final int targetFileOffset)
    {
        for (Token token : new IterableWrapper<Token>(ast))
            if (token.isOnOrAfterFileOffset(targetFileOffset))
                return token;
        return null;
    }

    private static Token findLastTokenBefore(IFortranAST ast, final int targetFileOffset)
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

    protected static ASTProperLoopConstructNode getLoopNode(IFortranAST ast, ITextSelection selection)
    {
        /*Token firstToken = this.findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = this.findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
        if (firstToken == null || lastToken == null)
            return null;

        return getLoopNode(firstToken, lastToken);*/
        return (ASTProperLoopConstructNode)getNode(ast, selection, ASTProperLoopConstructNode.class);
    }

    protected static ASTNode getNode(IFortranAST ast, ITextSelection selection, Class<? extends ASTNode> node)
    {
        Token firstToken = findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
        if (firstToken == null || lastToken == null)
            return null;
        return getNode(firstToken, lastToken, node);
    }

    protected static ASTNode getNode(Token firstToken, Token lastToken, Class<? extends ASTNode> node)
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

    protected static ASTProperLoopConstructNode getLoopNode(Token firstToken, Token lastToken)
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
    protected static StatementSequence findEnclosingStatementSequence(IFortranAST ast, ITextSelection selection)
    {
        Token firstToken = findFirstTokenAfter(ast, selection.getOffset());
        Token lastToken = findLastTokenBefore(ast, selection.getOffset()+selection.getLength());
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

    protected static int findIndexToInsertTypeDeclaration(IASTListNode<? extends IASTNode> body)
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

    protected static int findIndexToInsertStatement(IASTListNode<? extends IASTNode> body)
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

    protected static boolean isIdentifier(Token token)
    {
        return token != null && token.getTerminal() == Terminal.T_IDENT;
    }

    protected static boolean isPreprocessed(Token token)
    {
        return token.getPreprocessorDirective() != null;
    }

    protected static boolean isValidIdentifier(String name)
    {
        return Pattern.matches("[A-Za-z$][A-Za-z0-9$_]*", name);
    }

    protected static boolean isBoundIdentifier(Token t)
    {
        return isIdentifier(t) && !t.resolveBinding().isEmpty();
    }

    protected static boolean isUniquelyDefinedIdentifer(Token t)
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
     * This is the fundamental precondition check for Photran's Rename refactoring.
     */
    protected static void checkForConflictingBindings(
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
     * This is the fundamental precondition check for Photran's Rename refactoring.
     */
    protected static void checkForConflictingBindings(
        IProgressMonitor pm,
        IConflictingBindingCallback callback,
        Definition definitionToCheck,
        Collection<PhotranTokenRef> allReferences,
        Collection<String> newNames)
    {
        new CheckForConflictBindings(definitionToCheck, allReferences, newNames).check(pm, callback);
    }

    /**
     * Determines whether a declaration with the given <code>name</code> can be added to the given scope.
     */
    protected static boolean checkIfDeclarationCanBeAddedToScope(
        String name,
        ScopingNode scope,
        IProgressMonitor pm)
    {
        try
        {
            IConflictingBindingCallback callback = new IConflictingBindingCallback()
            {
                public void addConflictError(List<Conflict> conflictingDef)
                {
                    throw new Notification(Boolean.FALSE);
                }

                public void addConflictWarning(List<Conflict> conflictingDef)
                {
                    throw new Notification(Boolean.FALSE);
                }

                public void addReferenceWillChangeError(String newName, Token reference)
                {
                    throw new Notification(Boolean.FALSE);
                }
            };

            new CheckForConflictBindings(scope, Collections.singleton(name)).check(pm, callback);
        }
        catch (Notification n)
        {
            return (Boolean)n.getResult();
        }

        return true;
    }

    private static final class CheckForConflictBindings
    {
        private IProgressMonitor pm = null;
        private Definition definitionToCheck = null;
        private ScopingNode scopeOfDefinitionToCheck = null;
        private Collection<String> newNames = null;
        private Collection<PhotranTokenRef> allReferences = null;

        public CheckForConflictBindings(Definition definitionToCheck,
                                        Collection<PhotranTokenRef> allReferences,
                                        Collection<String> newNames)
        {
            this.definitionToCheck = definitionToCheck;
            this.scopeOfDefinitionToCheck = definitionToCheck.getTokenRef().findToken().getEnclosingScope();
            this.allReferences = allReferences;
            this.newNames = newNames;
        }

        public CheckForConflictBindings(ScopingNode checkInScope,
                                        Collection<String> newNames)
        {
            this.definitionToCheck = null;
            this.scopeOfDefinitionToCheck = checkInScope;
            this.allReferences = Collections.emptySet();
            this.newNames = newNames;
        }

        public void check(IProgressMonitor pm, IConflictingBindingCallback callback)
        {
            this.pm = pm;

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

            if (definitionToCheck != null)
            {
                // Cannot call a main program (or function, etc.) X if it has an internal subprogram named X,
                // even if that subprogram is never used (in which case it wouldn't be caught below)
                if (definitionToCheck.isMainProgram()
                    || definitionToCheck.isSubprogram()
                    || definitionToCheck.isModule())
                {
                    findAllPotentiallyConflictingDefinitionsInScope(
                        conflicts,
                        definitionToCheck.getTokenRef().findToken().findNearestAncestor(ScopingNode.class),
                        false);
                }
                for (String newName : newNames)
                {
                    if (definitionToCheck.isInternalSubprogramDefinition()
                        && scopeContainingInternalSubprogram().isNamed(newName))
                    {
                        conflicts.add(
                            new Conflict(
                                newName,
                                scopeContainingInternalSubprogram().getNameToken().getTokenRef()));
                    }
                }
            }

            for (ScopingNode importingScope : scopeItselfAndAllScopesThatImport(scopeOfDefinitionToCheck))
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
                List<PhotranTokenRef> definitionsLocalToScope = collectLocalDefinitions(importingScope);

                if (isProgramOrSubprogramOrModuleScope(importingScope) && shouldCheckIfDefinitionImportedIntoScope)
                {
                    // Cannot call a variable X inside a function named X
                    if (importingScope.isNamed(newName))
                    {
                        if (definitionToCheck == null || definitionsLocalToScope.contains(definitionToCheck.getTokenRef()))
                        {
                            conflicts.add(new Conflict(newName, importingScope.getNameToken().getTokenRef()));
                        }
                    }
                    // Cannot call a variable X inside a function named Y inside a module named X
                    else
                    {
                        ScopingNode parent = importingScope.findNearestAncestor(ScopingNode.class);
                        if (parent != null && parent.isNamed(newName))
                        {
                            List<PhotranTokenRef> definitionsLocalToParent = collectLocalDefinitions(parent);
                            if (definitionToCheck == null || definitionsLocalToParent.contains(definitionToCheck.getTokenRef()))
                            {
                                conflicts.add(new Conflict(newName, parent.getNameToken().getTokenRef()));
                            }
                        }
                    }
                }

                // Cannot call a function X if it is defined in or imported into a scope with a function X already defined
                Token newNameToken = definitionToCheck == null ? new FakeToken(scopeOfDefinitionToCheck, newName) : new FakeToken(definitionToCheck.getTokenRef().findToken(), newName);
                for (PhotranTokenRef conflict : importingScope.manuallyResolveInLocalScope(newNameToken))
                {
                    if (definitionsLocalToScope.contains(conflict))
                    {
                        if (shouldCheckIfDefinitionImportedIntoScope)
                        {
                            if (definitionToCheck == null
                                || definitionsLocalToScope.contains(definitionToCheck.getTokenRef()))
                            {
                                conflicts.add(new Conflict(newName, conflict));
                            }
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
                Token token = definitionToCheck == null ? new FakeToken(scopeOfDefinitionToCheck, newName) : new FakeToken(definitionToCheck.getTokenRef().findToken(), newName);

                List<PhotranTokenRef> shadowedDefinitions = scopeOfDefinitionToCheck.manuallyResolve(token);
                // TODO: Does not consider rename or only lists (need to tell if this SPECIFIC definition will be imported)
                for (ScopingNode importingScope : scopeOfDefinitionToCheck.findImportingScopes())
                {
                    pm.subTask("Checking for references to " + newName + " in " + importingScope.describe());
                    shadowedDefinitions.addAll(importingScope.manuallyResolve(token));
                }

                for (PhotranTokenRef def : shadowedDefinitions)
                {
                    Definition definition = PhotranVPG.getInstance().getDefinitionFor(def);
                    if (definition != null)
                        referencesToShadowedDefinitions.addAll(definition.findAllReferences(false));
                }
            }

            return referencesToShadowedDefinitions;
        }

        private void checkIfReferenceBindingWillChange(IConflictingBindingCallback callback, PhotranTokenRef ref, boolean shouldReferenceRenamedDefinition)
        {
            pm.subTask("Checking for binding conflicts in " + PhotranVPG.lastSegmentOfFilename(ref.getFilename()));

            Token reference = ref.findToken();

            if (definitionToCheck != null)
            {
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
            else
            {
                if (scopeOfDefinitionToCheck == reference.getLocalScope()
                    || scopeOfDefinitionToCheck.isParentScopeOf(reference.getLocalScope()))
                {
                    for (String newName : newNames)
                    {
                        for (PhotranTokenRef existingBinding : new FakeToken(reference, newName).manuallyResolveBinding())
                        {
                            ScopingNode scopeOfExistingBinding = existingBinding.findToken().getEnclosingScope();

                            boolean willReferenceRenamedDefinition = scopeOfExistingBinding.isParentScopeOf(scopeOfDefinitionToCheck);
                            if (shouldReferenceRenamedDefinition != willReferenceRenamedDefinition)
                                callback.addReferenceWillChangeError(newName, reference);
                        }
                    }
                }
            }
        }

        private List<Conflict> findAllPotentiallyConflictingUnboundSubprogramCalls()
        {
            final List<Conflict> conflictingDef = new ArrayList<Conflict>();

            for (ScopingNode importingScope : scopeItselfAndAllScopesThatImport(scopeOfDefinitionToCheck))
            {
                pm.subTask("Checking for subprogram binding conflicts in " + importingScope.describe());

                importingScope.accept(new GenericASTVisitor()
                {
                    @Override public void visitASTVarOrFnRefNode(ASTVarOrFnRefNode node)
                    {
                        if (node.getName() != null && node.getName().getName() != null)
                            checkForConflict(node.getName().getName());
                    }

                    @Override public void visitASTCallStmtNode(ASTCallStmtNode node)
                    {
                        if (node.getSubroutineName() != null)
                            checkForConflict(node.getSubroutineName());
                    }

                    private void checkForConflict(Token name)
                    {
                        if (name.getLogicalFile() != null)
                            for (String newName : newNames)
                                if (name != null && name.getText().equals(newName) && name.resolveBinding().isEmpty())
                                    conflictingDef.add(new Conflict(newName, name.getTokenRef()));
                    }
                });
            }

            return conflictingDef;
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
