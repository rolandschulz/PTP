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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTOnlyNode;
import org.eclipse.photran.internal.core.parser.ASTUseStmtNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
/**
 * 
 * @author Kurt Hendle
 */
public class MinOnlyListRefactoring extends SingleFileFortranRefactoring
{
    private String moduleName;
    private ASTUseStmtNode useNode = null;
    private List<IFile> filesContainingModule = null;
    private List<Definition> moduleEntityDefs = new ArrayList<Definition>();
    private ArrayList<String> moduleEntityNames = new ArrayList<String>();
    private ArrayList<String> existingOnlyListNames = new ArrayList<String>();
    private ArrayList<String> onlyNamesToKeep = new ArrayList<String>();
    private int numOnlysToKeep = 0;
    
    public MinOnlyListRefactoring(IFile file, ITextSelection selection)
    {
        initialize(file, selection);
    }
    
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
        if(moduleName == null || moduleName.equals(""))
            fail("No module name selected.");
        
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
            fail("Please select the name of the module in the USE statement.");
        
        useNode = token.findNearestAncestor(ASTUseStmtNode.class);
        if(useNode == null)
            fail("Use statement node could not be found.");
    }
    
    //same as AddOnlyToUseStmtRefactoring.java
    private void checkIfModuleExistsInProject() throws PreconditionFailure
    {
      //Check to see if the module exists in the project
        filesContainingModule = vpg.findFilesThatExportModule(moduleName);
        
        if(filesContainingModule.isEmpty() || filesContainingModule == null)
            fail("No files in this project contain the module - " + moduleName);
        else if(filesContainingModule.size() > 1)
            filterFileList();
        
       //check again after the filtering happens
        if(filesContainingModule.isEmpty() || filesContainingModule == null)
            fail("No files in this project contain the module - " + moduleName);
    }
    
    //same method used in CommonVarNamesRefactoring.java
    private void filterFileList() throws PreconditionFailure
    {
        IProject projectInEditor = this.fileInEditor.getProject();  //current project
        
        if(projectInEditor == null) fail("Project does not exist!");
        
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
            fail("Please select a module name.");
        return selectedToken;
    }
    
    //pretty much the same as AddOnlyToUseStmtRefactoring.java
    private void getModuleDeclaredEntities(IProgressMonitor pm) throws PreconditionFailure
    {
        //get module declaration and check if it has declared entities
        PhotranTokenRef moduleTokenRef = vpg.getModuleTokenRef(moduleName);
        if(moduleTokenRef == null)
            fail("No module with name " + moduleName + "found.");
        
        Token moduleToken = moduleTokenRef.findTokenOrReturnNull();
        if(moduleToken == null){
            fail("Module token could not be found.");
        }
        
        ASTModuleNode moduleNode = moduleToken.findNearestAncestor(ASTModuleNode.class);
        if(moduleNode == null)
            fail("Module Node could not be found.");
        
        moduleEntityDefs = moduleNode.getAllPublicDefinitions();
        if(moduleEntityDefs.isEmpty())
        {
            fail("Module contains no declared entities. No ONLY clause is necessary." +
                "Please remove the ONLY clause from USE statement.");
        }
        else
        {
            for(int i=0; i<moduleEntityDefs.size(); i++)
                moduleEntityNames.add(moduleEntityDefs.get(i).getCanonicalizedName());
        }
    }
    
    //nearly the same as AddOnlyToUseStmtRefactoring.java
    @SuppressWarnings("unchecked")
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
    
    @SuppressWarnings("unchecked")
    private void removeOnlyList(IProgressMonitor pm, IFortranAST ast)
    {
        if(ast == null) return;
        
        ASTUseStmtNode newStmtNode = (ASTUseStmtNode)parseLiteralStatement("use " + 
            useNode.getName().getText() + System.getProperty("line.separator"));
    
        ASTListNode body = (ASTListNode)useNode.getParent();
        body.replaceChild(useNode, newStmtNode);
        Reindenter.reindent(newStmtNode, ast);
    }
    
    @SuppressWarnings("unchecked")
    private void createAndAddMinOnlyList(IProgressMonitor pm, IFortranAST ast)
    {
        if(ast == null) return;
        
        String list = "";
        String name;
        int counter = 0;
        
        while(counter < onlyNamesToKeep.size())
        {
            name = onlyNamesToKeep.get(counter);
            
            //add the new name for this renamed variable if necessary
            if(!moduleEntityNames.contains(name))
            {
                list += name + " => ";
                counter++;
                name = onlyNamesToKeep.get(counter); //update name
            }
            
            list += name;
            if(counter < onlyNamesToKeep.size()-1)
                list += ", ";
            counter++;
        }
        
        //construct the new USE node and replace the old one in the ast
        ASTUseStmtNode newStmtNode = (ASTUseStmtNode)parseLiteralStatement("use " + 
            useNode.getName().getText()+", only: " + list + System.getProperty("line.separator"));
        
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
        return "Minimize ONLY List";
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