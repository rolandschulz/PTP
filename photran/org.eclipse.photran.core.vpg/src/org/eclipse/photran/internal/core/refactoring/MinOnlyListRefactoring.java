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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTOnlyNode;
import org.eclipse.photran.internal.core.parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
/**
 *
 * @author Kurt Hendle
 * @author Jeff Overbey - Externalized strings
 */
public class MinOnlyListRefactoring extends FortranEditorRefactoring
{
    private String moduleName;
    private ASTUseStmtNode useNode = null;
    private List<IFile> filesContainingModule = null;
    private List<Definition> moduleEntityDefs = new ArrayList<Definition>();
    private ArrayList<String> moduleEntityNames = new ArrayList<String>();
    private ArrayList<String> existingOnlyListNames = new ArrayList<String>();
    private ArrayList<String> onlyNamesToKeep = new ArrayList<String>();
    private int numOnlysToKeep = 0;

    public String getModuleName()
    {
        return moduleName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCheckInitialConditions(org.eclipse.ltk.core.refactoring.RefactoringStatus, org.eclipse.core.runtime.IProgressMonitor)
     * nearly the same as AddOnlyToUseStmtRefactoring.java
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        moduleName = this.selectedRegionInEditor.getText();
        if(moduleName == null || moduleName.equals("")) //$NON-NLS-1$
            fail(Messages.MinOnlyListRefactoring_NoModuleNameSelected);

        findUseStmtNode();
        checkIfModuleExistsInProject();
        getModuleDeclaredEntities(pm);
        readExistingOnlyList();
    }

    //same as AddOnlyToUseStmtRefactoring.java
    private void findUseStmtNode() throws PreconditionFailure
    {
      //get the use statement node in case we need to add to the only list
        Token token = findEnclosingToken();
        if(token == null)
            fail(Messages.MinOnlyListRefactoring_PleaseSelectModuleNameInUSEStatement);

        useNode = token.findNearestAncestor(ASTUseStmtNode.class);
        if(useNode == null)
            fail(Messages.MinOnlyListRefactoring_USEStatementNotFound);
    }

    //same as AddOnlyToUseStmtRefactoring.java
    private void checkIfModuleExistsInProject() throws PreconditionFailure
    {
      //Check to see if the module exists in the project
        filesContainingModule = vpg.findFilesThatExportModule(moduleName);

        if(filesContainingModule.isEmpty() || filesContainingModule == null)
            fail(Messages.bind(Messages.MinOnlyListRefactoring_NoFilesContainModuleNamed, moduleName));
        else if(filesContainingModule.size() > 1)
            filterFileList();

       //check again after the filtering happens
        if(filesContainingModule.isEmpty() || filesContainingModule == null)
            fail(Messages.bind(Messages.MinOnlyListRefactoring_NoFilesContainModuleNamed, moduleName));
    }

    //same method used in CommonVarNamesRefactoring.java
    private void filterFileList() throws PreconditionFailure
    {
        IProject projectInEditor = this.fileInEditor.getProject();  //current project

        if(projectInEditor == null) fail(Messages.MinOnlyListRefactoring_ProjectDoesNotExist);

        //filter out files not in the project
        int i = 0;
        while(i < filesContainingModule.size())
        {
            if(filesContainingModule.get(i) == null
                || filesContainingModule.get(i).getProject() != projectInEditor)
                filesContainingModule.remove(i);   //shifts all elements left, don't increment i
            else
                i++;
        }
    }

    //modified from RenameRefactoring.java
    private Token findEnclosingToken() throws PreconditionFailure
    {
        Token selectedToken = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selectedToken == null)
            fail(Messages.MinOnlyListRefactoring_PleaseSelectModuleName);
        return selectedToken;
    }

    //pretty much the same as AddOnlyToUseStmtRefactoring.java
    private void getModuleDeclaredEntities(IProgressMonitor pm) throws PreconditionFailure
    {
        //get module declaration and check if it has declared entities
        PhotranTokenRef moduleTokenRef = vpg.getModuleTokenRef(moduleName);
        if(moduleTokenRef == null)
            fail(Messages.bind(Messages.MinOnlyListRefactoring_ModuleNotFoundWithName, moduleName));

        Token moduleToken = moduleTokenRef.findTokenOrReturnNull();
        if(moduleToken == null){
            fail(Messages.MinOnlyListRefactoring_ModuleTokenNotFound);
        }

        ASTModuleNode moduleNode = moduleToken.findNearestAncestor(ASTModuleNode.class);
        if(moduleNode == null)
            fail(Messages.MinOnlyListRefactoring_ModuleNodeNotFound);

        moduleEntityDefs = moduleNode.getAllPublicDefinitions();
        if(moduleEntityDefs.isEmpty())
        {
            fail(Messages.MinOnlyListRefactoring_ModuleIsEmpty);
        }
        else
        {
            for(int i=0; i<moduleEntityDefs.size(); i++)
                moduleEntityNames.add(moduleEntityDefs.get(i).getCanonicalizedName());
        }
    }

    //nearly the same as AddOnlyToUseStmtRefactoring.java
    @SuppressWarnings("rawtypes")
    private void readExistingOnlyList()
    {
        ASTSeparatedListNode existingOnlys = (ASTSeparatedListNode)useNode.getOnlyList();
        if(existingOnlys != null){
            String name;
            ASTOnlyNode onlyNode = null;
            for(int i=0; i<existingOnlys.size(); i++)
            {
                onlyNode = (ASTOnlyNode)existingOnlys.get(i);
                name = PhotranVPG.canonicalizeIdentifier(onlyNode.getName().getText().trim());
                if(moduleEntityNames.contains(name))
                {
                    if(onlyNode.isRenamed())    //add new name before original
                        existingOnlyListNames.add(onlyNode.getNewName().getText());
                    existingOnlyListNames.add(name);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCreateChange(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        IFile file = this.fileInEditor;
        IFortranAST ast = vpg.acquirePermanentAST(file);
        if(ast == null) return;

        OnlyTokenVisitor visitor = new OnlyTokenVisitor();
        ast.accept(visitor);

        //actual change takes place here after parsing the AST
        if(numOnlysToKeep == moduleEntityDefs.size())
            removeOnlyList(pm, ast);
        else if(onlyNamesToKeep.isEmpty())
            useNode.removeFromTree(); // remove use node since it is unused
        else
            createAndAddMinOnlyList(pm, ast);

        addChangeFromModifiedAST(fileInEditor, pm);
        vpg.releaseAST(file);
    }

    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring#doCheckFinalConditions(org.eclipse.ltk.core.refactoring.RefactoringStatus, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        //
    }

    @SuppressWarnings("rawtypes")
    private void removeOnlyList(IProgressMonitor pm, IFortranAST ast)
    {
        if(ast == null) return;

        ASTUseStmtNode newStmtNode = (ASTUseStmtNode)parseLiteralStatement("use " + //$NON-NLS-1$
            useNode.getName().getText() + System.getProperty("line.separator")); //$NON-NLS-1$

        ASTListNode body = (ASTListNode)useNode.getParent();
        body.replaceChild(useNode, newStmtNode);
        Reindenter.reindent(newStmtNode, ast);
    }

    @SuppressWarnings("rawtypes")
    private void createAndAddMinOnlyList(IProgressMonitor pm, IFortranAST ast)
    {
        if(ast == null) return;

        String list = ""; //$NON-NLS-1$
        String name;
        int counter = 0;

        while(counter < onlyNamesToKeep.size())
        {
            name = onlyNamesToKeep.get(counter);

            //add the new name for this renamed variable if necessary
            if(!moduleEntityNames.contains(name))
            {
                list += name + " => "; //$NON-NLS-1$
                counter++;
                name = onlyNamesToKeep.get(counter); //update name
            }

            list += name;
            if(counter < onlyNamesToKeep.size()-1)
                list += ", "; //$NON-NLS-1$
            counter++;
        }

        //construct the new USE node and replace the old one in the ast
        ASTUseStmtNode newStmtNode = (ASTUseStmtNode)parseLiteralStatement("use " + //$NON-NLS-1$
            useNode.getName().getText()+", only: " + list + System.getProperty("line.separator")); //$NON-NLS-1$ //$NON-NLS-2$

        ASTListNode body = (ASTListNode)useNode.getParent();
        body.replaceChild(useNode, newStmtNode);
        Reindenter.reindent(newStmtNode, ast);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ltk.core.refactoring.Refactoring#getName()
     */
    @Override
    public String getName()
    {
        return Messages.MinOnlyListRefactoring_Name;
    }

    private final class OnlyTokenVisitor extends GenericASTVisitor
    {
        @Override public void visitToken(Token node)
        {
            String name = PhotranVPG.canonicalizeIdentifier(node.getText());

            if((existingOnlyListNames.contains(name) || moduleEntityNames.contains(name)) &&
                !(node.getParent() instanceof ASTOnlyNode) &&
                !(node.getEnclosingScope() instanceof ASTModuleNode))
            {
                if(!onlyNamesToKeep.contains(name))
                {
                    onlyNamesToKeep.add(name);
                    numOnlysToKeep++;
                }
            }

            //add the new name and original name to keep list
            if(existingOnlyListNames.contains(name) &&
                !onlyNamesToKeep.contains(name) &&
                node.getParent() instanceof ASTOnlyNode)
            {
                ASTOnlyNode thisOnlyNode = (ASTOnlyNode)node.getParent();
                if(thisOnlyNode.isRenamed())
                {
                    onlyNamesToKeep.add(thisOnlyNode.getNewName().getText());
                    onlyNamesToKeep.add(thisOnlyNode.getName().getText());
                    numOnlysToKeep++;
                }
            }
        }
    }

}