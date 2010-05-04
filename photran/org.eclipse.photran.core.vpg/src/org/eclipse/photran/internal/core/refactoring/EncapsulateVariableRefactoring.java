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
package org.eclipse.photran.internal.core.refactoring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Classification;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecSeqNode;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTContainsStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTGenericNameNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineArgNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.ASTVarOrFnRefNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IAccessId;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter.Strategy;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.rephraserengine.core.refactorings.UserInputBoolean;
import org.eclipse.rephraserengine.core.refactorings.UserInputString;


/**
 * Refactoring to create getter and setter subprograms for a module variable and replace variable
 * accesses with calls to those subprograms.
 * <p>
 * This refactoring accepts a module variable declaration, makes that declaration PRIVATE, adds
 * getter and setter procedures to the module, and then replaces accesses to the variable outside
 * the module with calls to the getter and setter routines.
 *
 * @author Tim Yuvashev
 * @author Jeff Overbey
 */
public class EncapsulateVariableRefactoring extends FortranEditorRefactoring
{
    private PhotranTokenRef selectedReference = null;
    private Definition selectedTokenDef = null;
    private boolean isUsedAsArgument = false;
    //private PhotranTokenRef refInArgList = null;
    private Set<PhotranTokenRef> allRefs = null;
    private String getterName = null;
    private String setterName = null;
    private boolean wereMethodsCreated = false;
    private boolean replaceAccessesInDeclaringModule = false;

    public static final String AMBIGUOUS_DEF = "Could not find definition for this identifier, "+
                                                "or its definition was ambiguous.";

    ///////////////////////////////////////////////////////////
    ///             Public methods                         ///
    /////////////////////////////////////////////////////////
    public String getDefaultGetterName()
    {
        String capitalizedIdentName = getCapitalizedIdentName();
        return "get"+capitalizedIdentName;
    }

    public String getDefaultSetterName()
    {
        String capitalizedIdentName = getCapitalizedIdentName();
        return "set"+capitalizedIdentName;
    }

    @UserInputString(label="Getter method name ", defaultValueMethod="getDefaultGetterName")
    public void setGetterName(String gName)
    {
        String parens = gName.length() < 2 ? "" : gName.substring(gName.length()-2, gName.length());
        if(parens.equalsIgnoreCase("()"))
            getterName = gName.substring(0, gName.length()-2);
        else
            getterName = gName;
    }

    @UserInputString(label="Setter method name ", defaultValueMethod="getDefaultSetterName")
    public void setSetterName(String sName)
    {
        String parens = sName.length() < 2 ? "" : sName.substring(sName.length()-2, sName.length());
        if(parens.equalsIgnoreCase("()"))
            setterName = sName.substring(0, sName.length()-2);
        else
            setterName = sName;
    }

    public String getGetterName()
    {
        return getterName == null ? getDefaultGetterName() : getterName;
    }

    public String getSetterName()
    {
        return setterName == null ? getDefaultSetterName() : setterName;
    }

    @UserInputBoolean(label="Replace accesses in declaring module", defaultValue=false)
    public void replaceAccessesInDeclaringModule(boolean replaceAccessesInDeclaringModule)
    {
        this.replaceAccessesInDeclaringModule = replaceAccessesInDeclaringModule;
    }

    public boolean isArgument()
    {
        return isUsedAsArgument;
    }

    @Override
    public String getName()
    {
        return "Encapsulate Variable";
    }


    ///////////////////////////////////////////////////////////
    ///            Initial Precondition check              ///
    /////////////////////////////////////////////////////////
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        Token t = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if(t == null)
            fail("Could not find token based on selection.");

        selectedReference = t.getTokenRef();
        Terminal term = t.getTerminal();
        if(term == null || term != Terminal.T_IDENT)
            fail("Please select an identifier to encapsulate");

        selectedTokenDef = findUnambiguousDeclaration(t);
        if(selectedTokenDef == null)
            fail(AMBIGUOUS_DEF);

        checkCanBeEncapsulated(selectedTokenDef);

        allRefs = selectedTokenDef.findAllReferences(true);
        //processTokenRefs(allRefs);
    }

    protected void checkCanBeEncapsulated(Definition def) throws PreconditionFailure
    {
        if(!isDefinedInModule(def) ||
                !def.getClassification().equals(Classification.VARIABLE_DECLARATION))
            fail("The selected entity is not a variable.  " +
                 "Please select a variable defined in a module to encapsulate.");
        if(def.isParameter())
            fail("Variables with the PARAMETER attribute cannot be encapsulated.");
        if(def.isArray())
            fail("Arrays cannot be encapsulated.");
        if(def.isPointer())
            fail("Pointers cannot be encapsulated.");
        if(def.isTarget())
            fail("Variables with the TARGET attribute cannot be encapsulated.");
    }

    protected boolean isDefinedInModule(Definition def)
    {
        Token t = def.getTokenRef().findTokenOrReturnNull();
        if(t == null)
            return false;
        ASTModuleNode modNode = t.findNearestAncestor(ASTModuleNode.class);
        return modNode != null;
    }

    protected void processTokenRefs(Set<PhotranTokenRef> refs) throws PreconditionFailure
    {
        for(PhotranTokenRef ref : refs)
            checkForFixedForm(ref.getFile());
    }

    protected void checkForFixedForm(IFile file) throws PreconditionFailure
    {
        if(FortranCorePlugin.hasFixedFormContentType(file))
        {
            fail("Fixed form files cannot currently be refactored. " +
                    "File " + file.getName() + " is in fixed form and "+
                    " contains a reference to the variable you want to encapsulate");
        }
    }

    ///////////////////////////////////////////////////////////
    ///            Final Precondition check                ///
    /////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        checkForConflictingBindings(pm,
                                    new ConflictingBindingErrorHandler(status),
                                    selectedTokenDef,
                                    allRefs,
                                    getGetterName(),
                                    getSetterName());

        try
        {
            Token varDefTok = declarationToken();

            ASTModuleNode declaringModule = varDefTok.findNearestAncestor(ASTModuleNode.class);
            if (declaringModule == null) throw new IllegalStateException();

            IFile defFile = varDefTok.getLogicalFile();
            vpg.acquirePermanentAST(defFile);

            for (IFile file : filesIn(allRefs))
            {
                for (PhotranTokenRef ref : allRefs)
                {
                    if (ref.getFile().equals(file))
                    {
                        Token t = ref.findTokenOrReturnNull();
                        if(t == null)
                            fail("Could not find a token associated with the variable reference.");

                        if(!isUsedAsArgument)
                            detectIfUsedAsArgument(t, status);

                        if(replaceAccessesInDeclaringModule || !isInModule(t, declaringModule))
                            replaceWithGetOrSet(t, status);
                    }
                }

                if (!file.equals(defFile))
                    this.addChangeFromModifiedAST(file, pm);
            }

            this.addChangeFromModifiedAST(defFile, pm);
            vpg.releaseAST(defFile);
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private Set<IFile> filesIn(Set<PhotranTokenRef> refs)
    {
        Set<IFile> result = new HashSet<IFile>(64);
        for (PhotranTokenRef r : refs)
            result.add(r.getFile());
        return result;
    }

    private boolean isInModule(Token t, ASTModuleNode m)
    {
        return t.findNearestAncestor(ASTModuleNode.class) == m;
    }

    private Token declarationToken() throws PreconditionFailure
    {
        Token varDefTok = selectedTokenDef.getTokenRef().findTokenOrReturnNull();
        if(varDefTok == null)
            fail("Could not find a token corresponding to the variable definition");
        return varDefTok;
    }

    protected void detectIfUsedAsArgument(Token t, RefactoringStatus status) throws PreconditionFailure
    {
        //If reference is used as a parameter, then it has to be inside this node
        ASTSubroutineArgNode subNode = t.findNearestAncestor(ASTSubroutineArgNode.class);

        if(subNode != null)
        {
            if(!isUsedAsArgument) //No need to keep re-setting values after one
                                  //instance was found
            {
                String message =
                    "!!!WARNING!!!" + EOL +
                    "Variable you want to encapsulate is used as an argument in a function." + EOL +
                    "Encapsulating this variable might change the expected behavior of that function." + EOL +
                    "Proceed at your own disgression." + EOL +
                    "File: "+t.getPhysicalFile()+" line: "+t.getLine()+EOL;

                RefactoringStatusContext context = createContext(t.getTokenRef()); // Highlights problematic definition in file
                status.addWarning(message, context);
                isUsedAsArgument = true;
            }
        }
    }

    protected void replaceWithGetOrSet(Token t, RefactoringStatus status) throws PreconditionFailure
    {
        if(isTokenRead(t))
        {
            replaceWithGetter(t);
            if(!wereMethodsCreated)
            {
                setGetterAndSetter();
                wereMethodsCreated = true;
            }
        }
        else if(isTokenWrittenTo(t))
        {
            replaceWithSetter(t);
            if(!wereMethodsCreated)
            {
                setGetterAndSetter();
                wereMethodsCreated = true;
            }
        }
        else //Neither written nor read (i.e. Access declaration (private,public))
        {
            String message = "!!!WARNING!!!" + EOL +
                             "The following reference to the selected variable will not be changed" + EOL +
                             "since it is neither written nor read: " + t.getPhysicalFile() +
                             " line " + t.getLine();
            RefactoringStatusContext context = createContext(t.getTokenRef());
            status.addWarning(message, context);
        }
    }

    protected boolean isTokenRead(Token t)
    {
        ASTVarOrFnRefNode expressionNode = t.findNearestAncestor(ASTVarOrFnRefNode.class);
        if(expressionNode == null)
            return false;
        return true;
    }

    protected void replaceWithGetter(Token t) throws PreconditionFailure
    {
        checkIfCanEncapsulateWithGetter(t);

        IExpr newExpr = parseLiteralExpression(getGetterName() + "()");
        newExpr.findFirstToken().setWhiteBefore("");
        IExpr oldExpr = t.findNearestAncestor(IExpr.class);
        oldExpr.replaceWith(newExpr);
        //newExpr.setParent(oldExpr.getParent());
    }

    protected void checkIfCanEncapsulateWithGetter(Token t) throws PreconditionFailure
    {
        IExpr expr = t.findNearestAncestor(IExpr.class);
        if(expr == null)
        {
            fail("Currently can only encapsulate variables that appear as "+
                "expressions. This variable is used as a non-expression in " +
                t.getPhysicalFile() + " line " + t.getLine());
        }
    }


    @SuppressWarnings("unchecked")
    protected void setGetterAndSetter() throws PreconditionFailure
    {
        Token varDefTok = declarationToken();
        ASTModuleNode mod = varDefTok.findNearestAncestor(ASTModuleNode.class);
        IASTListNode lst = mod.getBody();

        addPrivateStatement(varDefTok, lst);
        //TODO: Check for conflict names of functions
        addGetterFunction(selectedTokenDef, lst);
        addSetterFunction(selectedTokenDef, lst);
    }

    //TODO: Possibly re-factor this function (too long?)
    @SuppressWarnings("unchecked")
    protected void addPrivateStatement(Token varDefTok, IASTListNode lst) throws PreconditionFailure
    {
        ASTTypeDeclarationStmtNode typeDec = varDefTok.findNearestAncestor(ASTTypeDeclarationStmtNode.class);
        for(int i = 0; i < lst.size(); i++)
        {
            IASTNode node = (IASTNode)lst.get(i);
            if(node instanceof ASTTypeDeclarationStmtNode)
            {
                ASTTypeDeclarationStmtNode possibleTypeDec = (ASTTypeDeclarationStmtNode)node;
                if(possibleTypeDec != null && possibleTypeDec == typeDec)
                {
                    ASTTypeDeclarationStmtNode newDeclNode = removePublicDeclarationIfNeeded(varDefTok, possibleTypeDec, lst);
                    ASTAccessStmtNode newAccessNode =
                        (ASTAccessStmtNode)parseLiteralStatement("private :: " + varDefTok.getText() + EOL);

                    newAccessNode.setParent(lst);

                    if(newDeclNode != null)
                    {
                        lst.insertAfter(newDeclNode, newAccessNode);
                        Reindenter.reindent(newDeclNode,
                            vpg.acquireTransientAST(varDefTok.getLogicalFile()),
                            Strategy.REINDENT_EACH_LINE);
                    }
                    else
                        lst.insertAfter(possibleTypeDec, newAccessNode);

                    Reindenter.reindent(newAccessNode,
                                        vpg.acquireTransientAST(varDefTok.getLogicalFile()),
                                        Strategy.REINDENT_EACH_LINE);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected ASTTypeDeclarationStmtNode removePublicDeclarationIfNeeded(
                Token varDefTok,
                ASTTypeDeclarationStmtNode declNode,
                IASTListNode lst)
    {
        if(declNode.getAttrSpecSeq() != null)
        {
            if(containsPublicInDecl(declNode.getAttrSpecSeq()))
            {
                return redeclareVariable(varDefTok, declNode);
            }
        }

        boolean wasRemoved = false;
        for(int i = 0; i < lst.size() && !wasRemoved; i++)
        {
            if(lst.get(i) instanceof ASTAccessStmtNode)
            {
                ASTAccessStmtNode accessNode = (ASTAccessStmtNode)lst.get(i);
                wasRemoved = removeTokenFromPublicVarDecl(accessNode, varDefTok);
                if(wasRemoved && accessNode.getAccessIdList().size() <= 0)
                {
                    lst.remove(accessNode);
                    break;
                }

            }
        }

        return null;
    }


    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCreateChange(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {

    }

    protected boolean isTokenWrittenTo(Token t)
    {
        ASTAssignmentStmtNode assign = t.findNearestAncestor(ASTAssignmentStmtNode.class);
        if(assign == null)
            return false;
        return true;
    }



    protected void replaceWithSetter(Token t)
    {
        String whiteBeforeOld = t.getWhiteBefore();

        ASTAssignmentStmtNode oldAssignNode = t.findNearestAncestor(ASTAssignmentStmtNode.class);

        //String rhsString = oldAssignNode.getRhs().toString().trim();

        String setterString = "call " + getSetterName() + "()";
        ASTCallStmtNode newCallNode = (ASTCallStmtNode)parseLiteralStatement(setterString);
        newCallNode.findFirstToken().setWhiteBefore(whiteBeforeOld);

        IASTListNode<ASTSubroutineArgNode> argList = convertToArguments(oldAssignNode);
        argList.setParent(newCallNode);

        newCallNode.setArgList(argList);
        oldAssignNode.replaceWith(newCallNode);
        //newCallNode.setParent(oldAssignNode.getParent());
    }

    protected IASTListNode<ASTSubroutineArgNode> convertToArguments(ASTAssignmentStmtNode oldAssignNode)
    {
        ASTSubroutineArgNode args = new ASTSubroutineArgNode();
        Token tok = oldAssignNode.getRhs().findFirstToken();
        tok.setWhiteBefore("");

        args.setExpr(oldAssignNode.getRhs());
        oldAssignNode.getRhs().setParent(args);

        ArrayList<ASTSubroutineArgNode> argList = new ArrayList<ASTSubroutineArgNode>();
        argList.add(args);
        ASTSeparatedListNode<ASTSubroutineArgNode> sepList = new ASTSeparatedListNode<ASTSubroutineArgNode>(null, argList, true);

        args.setParent(sepList);
        return sepList;
    }

    @SuppressWarnings("unchecked")
    protected int findOrCreateContainsIndex(IASTListNode lst)
    {
        boolean isFound = false;
        int i = 0;
        for(; i < lst.size(); i++)
        {
            IASTNode node = (IASTNode)lst.get(i);
            if(node instanceof ASTContainsStmtNode)
            {
                isFound = true;
                break;
            }
        }
        if(!isFound)
        {
            ASTContainsStmtNode containsStmt = createContainsStmt();
            lst.add(containsStmt);
            i = lst.size() - 1;
        }
        return i;
    }

    @SuppressWarnings("unchecked")
    protected void addGetterFunction(Definition def, IASTListNode lst)
    {
        int index = findOrCreateContainsIndex(lst);
        ASTFunctionSubprogramNode funNode = createGetterFunction(def);
        funNode.setParent(lst);
        lst.insertAfter(lst.get(index), funNode);
        Reindenter.reindent(funNode,
            vpg.acquireTransientAST(def.getTokenRef().getFile()),
            Strategy.SHIFT_ENTIRE_BLOCK);
    }

    @SuppressWarnings("unchecked")
    protected void addSetterFunction(Definition def, IASTListNode lst)
    {
        int index = findOrCreateContainsIndex(lst);
        ASTSubroutineSubprogramNode funNode = createSetterFunction(def);
        funNode.setParent(lst);
        lst.insertAfter(lst.get(index), funNode);
        Reindenter.reindent(funNode,
            vpg.acquireTransientAST(def.getTokenRef().getFile()),
            Strategy.SHIFT_ENTIRE_BLOCK);
    }

    protected ASTFunctionSubprogramNode createGetterFunction(Definition def)
    {
        String type = def.getType().toString();
        String getterFunction = type + " function " + getGetterName() + "()" + EOL +
                                "    implicit none" + EOL +
                                "    " + getGetterName() + " = " + def.getTokenRef().getText() +EOL +
                                "end function" + EOL;
        ASTFunctionSubprogramNode newFunNode = (ASTFunctionSubprogramNode)parseLiteralProgramUnit(getterFunction);
        return newFunNode;
    }

    protected ASTSubroutineSubprogramNode createSetterFunction(Definition def)
    {
        String type = def.getType().toString();
        String valueName = "value";
        String setterFunction = "subroutine " + getSetterName() + "(" + valueName + ")" + EOL +
                                "    implicit none" + EOL +
                                "    " + type + ", intent(in) :: " + valueName + EOL +
                                "    " + def.getTokenRef().getText() +" = " + valueName + EOL +
                                "end subroutine" + EOL;
        ASTSubroutineSubprogramNode newSubNode = (ASTSubroutineSubprogramNode)parseLiteralProgramUnit(setterFunction);
        return newSubNode;
    }

    @SuppressWarnings("unchecked")
    protected boolean containsPublicInDecl(IASTListNode lst)
    {
        for(int i = 0; i < lst.size(); i++)
        {
            if(lst.get(i) instanceof ASTAttrSpecSeqNode)
            {
                ASTAttrSpecSeqNode secNode = (ASTAttrSpecSeqNode)lst.get(i);
                if(secNode.getAttrSpec().getAccessSpec().isPublic())
                    return true;
            }
        }
        return false;
    }

    protected ASTTypeDeclarationStmtNode createNewDeclaration(Token varDefTok, ASTTypeSpecNode typeSpec)
    {
        String newDecl = findUnambiguousDeclaration(varDefTok).getType().toString() + " :: " + varDefTok.getText();
        ASTTypeDeclarationStmtNode declNode = (ASTTypeDeclarationStmtNode)parseLiteralStatement(newDecl);
        return declNode;
    }

    protected ASTTypeDeclarationStmtNode removeAllAndRedeclare(Token varDefTok, ASTTypeDeclarationStmtNode oldDeclNode)
    {
        ASTTypeDeclarationStmtNode newDeclNode = createNewDeclaration(varDefTok, oldDeclNode.getTypeSpec());
        oldDeclNode.replaceWith(newDeclNode);
        //newDeclNode.setParent(oldDeclNode.getParent());
        return newDeclNode;
    }

    @SuppressWarnings("unchecked")
    protected ASTTypeDeclarationStmtNode removeAndRedeclare(Token varDefTok, ASTTypeDeclarationStmtNode oldDeclNode)
    {
        ASTTypeDeclarationStmtNode newDeclNode = createNewDeclaration(varDefTok, oldDeclNode.getTypeSpec());
        IASTListNode<ASTEntityDeclNode> lst = oldDeclNode.getEntityDeclList();
        for(ASTEntityDeclNode declNode : lst)
        {
           if(declNode.findFirstToken() == varDefTok)
           {
               lst.remove(declNode);
               break;
           }
        }
        newDeclNode.setParent(oldDeclNode.getParent());
        IASTListNode parent = (IASTListNode)oldDeclNode.getParent();
        parent.insertAfter(oldDeclNode, newDeclNode);
        return newDeclNode;
    }

    @SuppressWarnings("unchecked")
    protected ASTTypeDeclarationStmtNode redeclareVariable(Token varDefTok, ASTTypeDeclarationStmtNode declNode)
    {
        IASTListNode declList = declNode.getEntityDeclList();
        if(declList.size() == 1)
            return removeAllAndRedeclare(varDefTok, declNode);
        else
            return removeAndRedeclare(varDefTok, declNode);
    }

    protected boolean removeTokenFromPublicVarDecl(ASTAccessStmtNode accessNode, Token varDefTok)
    {
        if(accessNode.getAccessSpec().isPublic())
        {
            IASTListNode<IAccessId> varList = accessNode.getAccessIdList();
            for(IAccessId id : varList)
            {
                if(id instanceof ASTGenericNameNode)
                {
                    ASTGenericNameNode name = (ASTGenericNameNode)id;
                    String nameLabel = name.findFirstToken().getText();
                    if(nameLabel.equalsIgnoreCase(varDefTok.getText()))
                    {
                        varList.remove(id);
                        return true;
                    }

                }
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////
    ///             Private methods                        ///
    /////////////////////////////////////////////////////////
    private String getCapitalizedIdentName()
    {
        String identName = selectedReference.getText();
        String fstLetter = identName.substring(0,1);
        String upCase = fstLetter.toUpperCase();
        String capitalizedIdentName = upCase.concat(identName.substring(1));
        return capitalizedIdentName;
    }


    ///////////////////////////////////////////////////////////
    ///           Conflict error hanlder class             ///
    /////////////////////////////////////////////////////////
    private final class ConflictingBindingErrorHandler implements IConflictingBindingCallback
    {
        private final RefactoringStatus status;

        private ConflictingBindingErrorHandler(RefactoringStatus status) { this.status = status; }

        public void addConflictError(List<Conflict> conflictingDef)
        {
            for(Conflict conflict : conflictingDef)
            {
                String msg = "The name \"" + conflict.name + "\" conflicts with " + vpg.getDefinitionFor(conflict.tokenRef);
                RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
                status.addError(msg, context);
            }
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            for(Conflict conflict : conflictingDef)
            {
                String msg = "The name \"" + conflict.name + "\" might conflict with the name of an invoked subprogram";
                RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
                status.addWarning(msg, context);
            }
        }

        public void addReferenceWillChangeError(String newName, Token reference)
        {
            // The entity with the new name will shadow the definition to which this binding resolves
            /*status.addError("Changing the name to \"" + newName + "\""
                        + " would change the meaning of \"" + reference.getText() + "\" on line " + reference.getLine()
                        + " in " + reference.getTokenRef().getFilename(),
                        createContext(reference)); // Highlight problematic reference*/
        }
    }


}
