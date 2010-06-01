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
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockNode;
import org.eclipse.photran.internal.core.parser.ASTCommonBlockObjectNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Refactoring to make COMMON block variable names consistent between
 * program, modules, subroutines, etc.
 *
 * @author Kurt Hendle
 * @author Ashley Kasza - externalized strings
 */
public class CommonVarNamesRefactoring extends FortranEditorRefactoring
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

    @Override
    public String getName()
    {
        return Messages.CommonVarNamesRefactoring_Name;
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
            fail(Messages.bind(Messages.CommonVarNamesRefactoring_NoCommonBlockFoundWithName, commonBlockName));

        //find all files in the project containing the block
        filesContainingCommonBlock = PhotranVPG.getInstance().findFilesThatUseCommonBlock(commonBlockName);
        if(filesContainingCommonBlock.isEmpty())
            fail(Messages.CommonVarNamesRefactoring_NoFilesFoundContainingCommonBlock); //should never execute
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
            fail(Messages.CommonVarNamesRefactoring_SelectCommonBlockName);
        return selectedToken;
    }

    private void filterCommonBlockFileList() throws PreconditionFailure
    {
        IProject projectInEditor = this.fileInEditor.getProject();  //current project

        if(projectInEditor == null) fail(Messages.CommonVarNamesRefactoring_ProjectDoesNotExist);

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

            varName = varName.replaceAll("_common", ""); //$NON-NLS-1$ //$NON-NLS-2$
            newName = varName.concat("_common"); //$NON-NLS-1$
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

    private void checkConflictingBindings(ASTCommonBlockNode node, IProgressMonitor pm, RefactoringStatus status)
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
                checkForConflictingBindings(pm,
                    new ConflictingBindingErrorHandler(status),
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
            ConsistencyVisitor replacer = new ConsistencyVisitor(pm, status);
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
        private IProgressMonitor pm;
        private RefactoringStatus status;
        @SuppressWarnings("unused") private boolean changedAST = false;
        private boolean changeNames = false;
        private HashMap<String, Integer> oldVarNameHash = new HashMap<String, Integer>();
        private HashMap<Integer, Definition> blockVarDefs = new HashMap<Integer, Definition>();

        public ConsistencyVisitor(IProgressMonitor pm, RefactoringStatus status)
        {
            this.pm = pm;
            this.status = status;
        }

        @Override public void visitASTCommonBlockNode(ASTCommonBlockNode node)
        {
            //make sure we aren't looking for null name
            if(node.getName() == null && !commonBlockName.equals("")) //$NON-NLS-1$
                return;

            if((node.getName() == null && commonBlockName.equals("")) || //$NON-NLS-1$
                commonBlockName.equalsIgnoreCase(node.getName().getCommonBlockName().getText()))
            {
                checkConflictingBindings(node, pm, status);
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
                    throw new TypeError(Messages.CommonVarNamesRefactoring_VariableTypesDiffer);
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

            String msg = Messages.bind(Messages.CommonVarNamesRefactoring_NameConflictsWith, conflict.name, vpg.getDefinitionFor(conflict.tokenRef));
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addError(msg, context);
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = Messages.bind(Messages.CommonVarNamesRefactoring_NameMightConflictWithSubprogram, conflict.name);
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
