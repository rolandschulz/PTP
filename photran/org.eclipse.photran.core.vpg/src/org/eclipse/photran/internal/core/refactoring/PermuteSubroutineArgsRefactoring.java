/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Fotzler, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineArgNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineParNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * A refactoring that changes the signature of a subroutine subprogram,
 * swapping the arguments around at all declarations and call sites.
 * 
 * @author Matthew Fotzler
 */
public class PermuteSubroutineArgsRefactoring extends FortranEditorRefactoring
{
    private ASTSubroutineStmtNode selectedSubroutine;
    private List<ASTSubroutineParNode> oldParameterList;
    private List<ASTSubroutineParNode> newParameterList;
    private List<Integer> sigma;

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
    throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        //ASTSubroutineSubprogramNode s = getNode(astOfFileInEditor, selectedRegionInEditor, ASTSubroutineSubprogramNode.class);

        newParameterList = new ArrayList<ASTSubroutineParNode>();
        if(sigma == null) sigma = new ArrayList<Integer>();

        IASTNode temporaryNode = findEnclosingNode(astOfFileInEditor, selectedRegionInEditor);

        if(temporaryNode == null)
            fail(Messages.PermuteSubroutineArgsRefactoring_selectedTextNotSubroutine);

        if(temporaryNode instanceof ASTSubroutineSubprogramNode)
            selectedSubroutine = ((ASTSubroutineSubprogramNode)temporaryNode).getSubroutineStmt();
        else if(temporaryNode instanceof ASTSubroutineStmtNode)
        {
            if(temporaryNode.findNearestAncestor(ASTSubroutineSubprogramNode.class) == null)
                fail(Messages.PermuteSubroutineArgsRefactoring_selectSubroutineError);
            selectedSubroutine = (ASTSubroutineStmtNode)temporaryNode;
        }
        else
            fail(Messages.PermuteSubroutineArgsRefactoring_selectedTextNotSubroutine);

        oldParameterList = getSubroutineParameters();
        newParameterList = getSubroutineParameters();
        
        if(!matchingDeclarationsInInterfacesUniquelyBind())
            status.addWarning(Messages.PermuteSubroutineArgsRefactoring_matchingDeclarationsDoNotUniquelyBind);
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
    throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {

    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
    OperationCanceledException
    {
        buildNewParameterListFromSigma();
        
        // steps 1-3
        permuteDummyArguments(selectedSubroutine);

        permuteCallSites();

        permuteSubroutineInInterfaceBlocks();

        addChangeFromModifiedAST(fileInEditor, pm);
        vpg.releaseAST(fileInEditor);
    }
    
    public void setSigma(List<Integer> sigma)
    {
        this.sigma = sigma;
    }
    
    // used for the junit test marker
    public void buildNewParameterListFromSigma()
    {
        newParameterList = new ArrayList<ASTSubroutineParNode>();
        
        for(int i : sigma)
            newParameterList.add(oldParameterList.get(i));
    }

    protected void permuteDummyArguments(ASTSubroutineStmtNode node)
    {
        ASTSeparatedListNode<ASTSubroutineParNode> newParameterList = new ASTSeparatedListNode<ASTSubroutineParNode>(new Token(Terminal.T_COMMA, ","), this.newParameterList); //$NON-NLS-1$
        node.setSubroutinePars(newParameterList);
    }

    private void permuteCallSites()
    {
        for(ASTCallStmtNode callStmt : getCallSites())
        {
            int m = 0;

            if(callStmt.getArgList() != null)
            {
                m = callStmt.getArgList().size();
            }

            // 2a
            boolean K = false;

            // 2b
            ArrayList<ASTSubroutineArgNode> L_prime = new ArrayList<ASTSubroutineArgNode>();

            // step 2c
            for(int i : sigma)
            {
                ASTSubroutineParNode desiredPar = oldParameterList.get(i);
                ASTSubroutineArgNode A_i = getActualArgFromCallStmt(callStmt, desiredPar.getVariableName(), i);
                
                if(i > m)
                    K = true;
                
                // 2cii
                if(A_i != null)
                {
                    // 2ciiA
                    if(A_i.getName() != null)
                        K = true;
                    
                    // 2ciiB
                    if(K == false || A_i.getName() != null)
                        L_prime.add(A_i);
                    
                    // 2ciiC
                    if(K == true && A_i.getName() == null)
                    {
                        A_i.setName(new Token(Terminal.T_IDENT, desiredPar.getVariableName().getText()));
                        L_prime.add(A_i);
                    }
                }
            }
            
            ASTSeparatedListNode<ASTSubroutineArgNode> newArgList = new ASTSeparatedListNode<ASTSubroutineArgNode>(new Token(Terminal.T_COMMA, ","), L_prime); //$NON-NLS-1$
            callStmt.setArgList(newArgList);
        }
    }

    private ASTSubroutineArgNode getActualArgFromCallStmt(ASTCallStmtNode callStmt, Token desiredParName, int desiredParIndex)
    {
        for(int i = 0; i < callStmt.getArgList().size(); i++)
        {
            ASTSubroutineArgNode argument = callStmt.getArgList().get(i);
            if(argument.getName() == null || desiredParName == null)
            {
                if(i == desiredParIndex)
                    return argument;
            }
            else
            {
                String argumentName = PhotranVPG.canonicalizeIdentifier(argument.getName().getText());
                String parameterName = PhotranVPG.canonicalizeIdentifier(desiredParName.getText());
                if(argumentName.equals(parameterName))
                    return argument;
            }
        }
        return null;
    }

    private void permuteSubroutineInInterfaceBlocks()
    {
        for(Definition declaration : getInterfaceDeclarations())
        {
            ASTSubroutineStmtNode subroutineStmt = declaration.getTokenRef().findToken().findNearestAncestor(ASTSubroutineStmtNode.class);
            
            if (subroutineStmt != null &&
                subroutineStmt.getSubroutinePars() != null && 
                subroutineStmt.getSubroutinePars().size() == newParameterList.size())
                    permuteDummyArguments(subroutineStmt);
        }
    }
    
    private Collection<Definition> getInterfaceDeclarations()
    {
        List<Definition> subroutineDefinitions = selectedSubroutine.getSubroutineName().getSubroutineName().resolveBinding();
        
        if(subroutineDefinitions.size() != 1)
            return new ArrayList<Definition>();
        
        return subroutineDefinitions.get(0).findMatchingDeclarationsInInterfaces();
    }
    
    private boolean matchingDeclarationsInInterfacesUniquelyBind()
    {
        for(Definition declaration : getInterfaceDeclarations())
            if(declaration.resolveInterfaceBinding().size() != 1)
                return false;
        
        return true;
    }

    @Override
    public String getName()
    {
        return Messages.PermuteSubroutineArgsRefactoring_name;
    }

    public List<ASTSubroutineParNode> getSubroutineParameters()
    {
        if(selectedSubroutine.getSubroutinePars() != null)
            return selectedSubroutine.getSubroutinePars();

        return new ArrayList<ASTSubroutineParNode>();
    }

    private Set<ASTCallStmtNode> getCallSites()
    {
        List<Definition> subroutineDefinitions = selectedSubroutine.getSubroutineName().getSubroutineName().resolveBinding();
        HashSet<ASTCallStmtNode> result = new HashSet<ASTCallStmtNode>();

        if (subroutineDefinitions.size() != 1)
            return result; //probably should throw an error of some sort!

        for(PhotranTokenRef tokenRef : subroutineDefinitions.get(0).findAllReferences(true))
        {
            Token token = tokenRef.findToken();

            ASTCallStmtNode callStmtNode = token.findNearestAncestor(ASTCallStmtNode.class);

            if(callStmtNode != null)
                result.add(callStmtNode);
        }

        return result;
    }

    public boolean isUsedWithKeywordInCallStmt(ASTSubroutineParNode parameterNode)
    {
        for(ASTCallStmtNode callStmtNode : getCallSites())
            for(ASTSubroutineArgNode argument : callStmtNode.getArgList())
                if(argument.getName() != null && parameterNode.getVariableName() != null)
                {
                    String argumentName = PhotranVPG.canonicalizeIdentifier(argument.getName().getText());
                    String parameterName = PhotranVPG.canonicalizeIdentifier(parameterNode.getVariableName().getText());
                    if(argumentName.equals(parameterName))
                        return true;
                }

        return false;
    }

}
