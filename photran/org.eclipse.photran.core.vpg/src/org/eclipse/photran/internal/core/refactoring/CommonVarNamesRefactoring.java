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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockObjectNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;

/**
 * Refactoring to make COMMON block variable names consistent between
 * program, modules, subroutines, etc.
 * 
 * @author Kurt Hendle
 */
public class CommonVarNamesRefactoring extends SingleFileFortranRefactoring
{   
    private String commonBlockName = null;
    private HashMap<String, Integer> oldVarNames = new HashMap<String, Integer>();
    private HashMap<Integer, String> newVarNames = new HashMap<Integer, String>();
    private HashMap<Integer, Definition> varDefs = new HashMap<Integer, Definition>();
    private int numCommonVars = 0;
    
    private ArrayList<String> oldNames = new ArrayList<String>();
    private ArrayList<String> newNames = new ArrayList<String>();

    private ASTCommonBlockNode commonBlockNode = null;
    private List<IFile> filesContainingCommonBlock = null;
    
    public CommonVarNamesRefactoring(IFile file, ITextSelection selection)
    {
        super(file, selection);
    }
    
    /* (non-Javadoc) auto-generated */
    @Override
    public String getName()
    {
        return "Make COMMON variable names consistent";
    }
    
    public int getNumCommonVars()
    {
        return numCommonVars;
    }
    
    public ArrayList<String> getOldVarNames()
    {
        return oldNames;
    }
    
    public ArrayList<String> getNewVarNames()
    {
        return newNames;
    }
    
    public void modifyNewName(int varNum, String newName)
    {
        newVarNames.put(varNum, PhotranVPG.canonicalizeIdentifier(newName));
    }

    /* (non-Javadoc) auto-generated */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        commonBlockName = this.selectedRegionInEditor.getText();
                
        Token token = findEnclosingToken();
        
        commonBlockNode = token.findNearestAncestor(ASTCommonBlockNode.class);
        if(commonBlockNode == null)
            fail("No COMMON block found with name '" + commonBlockName +"'.");
        
        //find all files in the project containing the block
        filesContainingCommonBlock = PhotranVPG.getInstance().findFilesThatUseCommonBlock(commonBlockName);
        if(filesContainingCommonBlock.isEmpty())
            fail("No files found containing the specified COMMON block."); //should never execute
        else
            filterCommonBlockFileList();
        
        numCommonVars = commonBlockNode.getCommonBlockObjectList().size();
        
        hashOldAndNewNames();
        getVariableTypes();
    }
    
    //modified from RenameRefactoring.java
    private Token findEnclosingToken() throws PreconditionFailure
    {
        Token selectedToken = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selectedToken == null) 
            fail("Please select a COMMON block name (highlight the name, excluding /'s)");
        return selectedToken;
    }
    
    private void filterCommonBlockFileList() throws PreconditionFailure
    {
        IProject projectInEditor = this.fileInEditor.getProject();  //current project
        
        if(projectInEditor == null) fail("Project does not exist!");
        
        //filter out files not in the project
        int i = 0;
        while(i < filesContainingCommonBlock.size())
        {
            if(filesContainingCommonBlock.get(i) == null
                || !filesContainingCommonBlock.get(i).getProject().equals(projectInEditor))
                filesContainingCommonBlock.remove(i);   //shifts all elements left, don't increment i
            else
                i++;
        }
    }
    
    private void hashOldAndNewNames()
    {
        //get the old variable names, create new ones
        IASTListNode<ASTCommonBlockObjectNode> commonObjects = commonBlockNode.getCommonBlockObjectList();
        Iterator<ASTCommonBlockObjectNode> iter = commonObjects.iterator();
        int varNameNumber = 0;
        String varName, newName;
        
        while(iter.hasNext())
        {
            varName = PhotranVPG.canonicalizeIdentifier(iter.next().getVariableName().getText());
            oldNames.add(varName);
            
            varName = varName.replaceAll("_common", "");
            newName = varName.concat("_common");
            newNames.add(newName);
            
            oldVarNames.put(varName, varNameNumber);
            newVarNames.put(varNameNumber, newName);
            varNameNumber++;
        }
    }
    
    private void getVariableTypes()
    {
        IASTListNode<ASTCommonBlockObjectNode> commonObjects = commonBlockNode.getCommonBlockObjectList();
        int varNameNumber = 0;
        Definition originalDef = null;
        
        for (ASTCommonBlockObjectNode current : commonObjects)
        {
            originalDef = current.getVariableName().resolveBinding().get(0);
            varDefs.put(varNameNumber, originalDef);
            
            varNameNumber++;
        }
    }
    
    private void checkConflictingBindings(ASTCommonBlockNode node, RefactoringStatus status)
    {
        Definition defToRename = null;
        Collection<String> newNames = newVarNames.values();
        Collection<PhotranTokenRef> allReferences = null;
        
        Iterator<ASTCommonBlockObjectNode> blockIter = node.getCommonBlockObjectList().iterator();
        Iterator<String> nameIter = newNames.iterator();
        
        String oldName, newName;
        
        //check if each common variable can be renamed
        while(blockIter.hasNext() && nameIter.hasNext())
        {
            defToRename = blockIter.next().getVariableName().resolveBinding().get(0);
            allReferences = defToRename.findAllReferences(true);
            
            oldName = defToRename.getCanonicalizedName();
            newName = PhotranVPG.canonicalizeIdentifier(nameIter.next());
            
            if(!oldName.equalsIgnoreCase(newName))
            {
                checkForConflictingBindings(new ConflictingBindingErrorHandler(status),
                    defToRename,
                    allReferences,
                    newName);
            }
        }
    }

    /* auto-generated */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        //changes made in doCheckFinalConditions
    }
    
    /* (non-Javadoc) auto-generated */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        assert filesContainingCommonBlock != null;
        
        try
        {
            for (IFile file : filesContainingCommonBlock)
                makeChangesTo(file, pm, status);
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }
    
    private void makeChangesTo(IFile file, IProgressMonitor pm, RefactoringStatus status) throws PreconditionFailure 
    {
        IFortranAST ast = vpg.acquirePermanentAST(file);
        if(ast == null) return;
            
        try
        {
            ConsistencyVisitor replacer = new ConsistencyVisitor(status);
            ast.accept(replacer);
    
            addChangeFromModifiedAST(file, pm);
        }
        catch(TypeError e)
        {
            fail(e.getMessage());
        }
            
        vpg.releaseAST(file);
    }
    
    /** This class is adapted/taken from the code in RenameRefactoring.java */
    private final class ConsistencyVisitor extends GenericASTVisitor
    {
        RefactoringStatus status = null;
        @SuppressWarnings("unused")
        private boolean changedAST = false;
        private boolean changeNames = false;
        private HashMap<String, Integer> oldVarNameHash = new HashMap<String, Integer>();
        private HashMap<Integer, Definition> blockVarDefs = new HashMap<Integer, Definition>();
        
        public ConsistencyVisitor(RefactoringStatus status)
        {
            this.status = status;
        }
        
        @Override public void visitASTCommonBlockNode(ASTCommonBlockNode node)
        {   
            //make sure we aren't looking for null name
            if(node.getName() == null && !commonBlockName.equals("")) 
                return;
            
            if((node.getName() == null && commonBlockName.equals("")) || 
                commonBlockName.equalsIgnoreCase(node.getName().getCommonBlockName().getText()))
            {
                checkConflictingBindings(node, status);
                hashVarNames(node);
            }
        }
        
        @Override public void visitToken(Token node)
        {
            if(changeNames)
            {
                for (Definition variable : blockVarDefs.values())
                {
                    if (variable.findAllReferences(true).contains(node.getTokenRef())
                        || variable.getTokenRef().equals(node.getTokenRef()))
                    //if(oldVarNameHash.get(node.getText()) != null)
                    {
                        try
                        {
                            changeName(node);
                        }
                        catch(TypeError e)
                        {
                            throw new TypeError(e.getMessage());
                        }    
                    }
                }
            }
        }
        
        private void hashVarNames(ASTCommonBlockNode node)
        {
                //hash old names to new names and indicate name changes should be made
                IASTListNode<ASTCommonBlockObjectNode> objects = node.getCommonBlockObjectList();
                Definition currentDef;
                String currentVarName;
                int varNameNumber = 0;
                
                for (ASTCommonBlockObjectNode current : objects)
                {
                    currentVarName = current.getVariableName().getText();
                    oldVarNameHash.put(PhotranVPG.canonicalizeIdentifier(currentVarName), varNameNumber);
                    
                    currentDef = current.getVariableName().resolveBinding().get(0);
                    blockVarDefs.put(varNameNumber, currentDef);
                    
                    varNameNumber++;
                }
                
                changeNames = true;
        }
        
        private  void changeName(Token node)
        {
            int newNameNumber = oldVarNameHash.get(node.getText());
            Definition origDef = varDefs.get(newNameNumber);
            Definition thisDef = blockVarDefs.get(newNameNumber);
            
            if(origDef != null && thisDef != null) //skips nodes with null definitions
            {
               if((thisDef.getType().equals(origDef.getType())
                   || (thisDef.isImplicit() || origDef.isImplicit())))
                {
                    String name = newVarNames.get(newNameNumber);
                    node.setText(name);
                    changedAST = true;
                }
                else
                    throw new TypeError("Variable types differ in different uses of the specified" +
                        " COMMON block. Refactoring will not proceed.");
            }
        }
    }
    
    //borrowed (slightly modified) from RenameRefactoring.java
    private final class ConflictingBindingErrorHandler implements IConflictingBindingCallback
    {
        private final RefactoringStatus status;

        private ConflictingBindingErrorHandler(RefactoringStatus status) { this.status = status; }

        public void addConflictError(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = "The name \"" + conflict.name + "\" conflicts with " + vpg.getDefinitionFor(conflict.tokenRef);
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addError(msg, context);
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = "The name \"" + conflict.name + "\" might conflict with the name of an invoked subprogram";
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addWarning(msg, context);
        }

        public void addReferenceWillChangeError(String newName, Token reference)
        {
            //
        }
    }
    
    //custom error class to avoid catching others
    private final class TypeError extends Error
    {
        private static final long serialVersionUID = 1L;

        public TypeError(String message)
        {
            super(message);
        }
    }
}
