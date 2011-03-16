/*******************************************************************************
 * Copyright (c) 2010 Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, 
 * Balaji Ambresh Rajkumar and Paramvir Singh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, Balaji Ambresh Rajkumar
 * and Paramvir Singh - Initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAssignStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignedGotoStmtNode;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTCaseConstructNode;
import org.eclipse.photran.internal.core.parser.ASTNameNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Refactoring to remove assigned GOTO statements in selected Fortran files.
 * 
 * @author Andrea Dranberg
 * @author John Hammonds
 * @author Rajashekhar Arasanal
 * @author Balaji Ambresh Rajkumar
 * @author Paramvir Singh
 */
public class RemoveAssignedGotoRefactoring extends FortranResourceRefactoring
{
    /**
     * Maintains the lists of goto, assign and action statements that are required to do the actual
     * refactoring for an IFile. Instances are created as part of initial precondition check and
     * destroyed once the refactoring is done.
     */
    public static class FileInfo
    {
        // All Assigned statements in the Fortran file
        private List<ASTAssignStmtNode> assignedStmtList;

        // All GoTo statements in the Fortran file
        private List<ASTAssignedGotoStmtNode> assignedGotoStmtList;

        // All action statements in the Fortran file
        private List<IActionStmt> actionStmtList;

        private boolean isVariableInActionStmt;

        private IFile file;

        private PhotranVPG vpg;

        private Set<String> labelAddresses;

        protected FileInfo(PhotranVPG vpg, IFile file) throws PreconditionFailure
        {
            setVpg(vpg);
            setFile(file);
            assignedStmtList = new LinkedList<ASTAssignStmtNode>();
            assignedGotoStmtList = new LinkedList<ASTAssignedGotoStmtNode>();
            actionStmtList = new LinkedList<IActionStmt>();
            labelAddresses = new TreeSet<String>();
            initialize();
        }

        /**
         * Collects all the assigned goto statements for the entire file and checks that there is at
         * least one valid assigned goto to refactor.
         * 
         * @throws PreconditionFailure
         */
        private void initialize() throws PreconditionFailure
        {
            IFile file = getFile();
            IFortranAST ast = (IFortranAST)getVpg().acquirePermanentAST(file);
            if (ast == null) { throw new PreconditionFailure(Messages.bind(
                Messages.RemoveAssignedGoToRefactoring_SelectedFileCannotBeParsed, file.getName())); }
            collectAllAssignedGoTos(ast.getRoot());
            ensureLabelAddressesArePresent(ast.getRoot());
        }

        public void cleanUp()
        {
            assignedStmtList.clear();
            assignedGotoStmtList.clear();
            actionStmtList.clear();
            isVariableInActionStmt = false;
            vpg.releaseAST(file);

        }

        public List<ASTAssignStmtNode> getAssignedStmtList()
        {
            return assignedStmtList;
        }

        public void setAssignedStmtList(List<ASTAssignStmtNode> assignedStmtList)
        {
            this.assignedStmtList = assignedStmtList;
        }

        public List<ASTAssignedGotoStmtNode> getAssignedGotoStmtList()
        {
            return assignedGotoStmtList;
        }

        public void setAssignedGotoStmtList(List<ASTAssignedGotoStmtNode> assignedGotoStmtList)
        {
            this.assignedGotoStmtList = assignedGotoStmtList;
        }

        public List<IActionStmt> getActionStmtList()
        {
            return actionStmtList;
        }

        public void setActionStmtList(List<IActionStmt> actionStmtList)
        {
            this.actionStmtList = actionStmtList;
        }

        public boolean isVariableInActionStmt()
        {
            return isVariableInActionStmt;
        }

        public void setVariableInActionStmt(boolean isVariableUsed)
        {
            this.isVariableInActionStmt = isVariableUsed;
        }

        public IFile getFile()
        {
            return file;
        }

        public void setFile(IFile file)
        {
            this.file = file;
        }

        public PhotranVPG getVpg()
        {
            return vpg;
        }

        public void setVpg(PhotranVPG vpg)
        {
            this.vpg = vpg;
        }

        /**
         * Remove assign statements whose variable name is used in any action statement.
         * 
         * @return String of removed variable names.
         */
        private String removeVariablesUsedInActionStmt()
        {
            Set<String> removedVariables = new TreeSet<String>();
            List<ASTAssignStmtNode> assignStmtList = getAssignedStmtList();
            
            for (int i = 0; i < assignStmtList.size(); i++)
            {
                List<IActionStmt> actionStmtList = getActionStmtList();
                for (IActionStmt actionStmtNode : actionStmtList)
                {
                    String variable = ((ASTAssignStmtNode)assignStmtList.get(i)).getVariableName()
                        .getText();
                    if (doesActionStmtUseVariable(actionStmtNode, variable))
                    {
                        assignStmtList.remove(i);
                        removedVariables.add(variable);
                        --i;
                        break;
                    }
                }
            }
            String returnString;
            if (removedVariables.size() == 0)
                returnString = ""; //$NON-NLS-1$
            else
                returnString = removedVariables.toString();
            return returnString;
        }

        /**
         * Determines if the variable is used within an action statement.
         * 
         * @param node The action statement node to search
         * @param variable The label to be used in the goto statement
         * @return true if label is in the action statement, otherwise false.
         */
        private boolean doesActionStmtUseVariable(IActionStmt node, final String variable)
        {
            setVariableInActionStmt(false);
            node.accept(new ASTVisitorWithLoops()
            {
                @Override
                public void visitASTNameNode(ASTNameNode nameNode)
                {
                    if (nameNode.getName().getText().equalsIgnoreCase(variable))
                    {
                        setVariableInActionStmt(true);
                    }
                }
            });
            return isVariableInActionStmt();
        }

        /**
         * Sorts the assignment statements by label address.
         */
        private void sortAssignedStmtList()
        {
            int maxIndex = getAssignedStmtList().size();
            for (int i = 0; i < maxIndex; i++)
            {
                for (int j = 0; j < maxIndex; j++)
                {
                    ASTAssignStmtNode assignNode1 = (ASTAssignStmtNode)getAssignedStmtList().get(i);
                    ASTAssignStmtNode assignNode2 = (ASTAssignStmtNode)getAssignedStmtList().get(j);
                    String ref1 = assignNode1.getAssignedLblRef().getLabel().getText();
                    String ref2 = assignNode2.getAssignedLblRef().getLabel().getText();
                    if (ref1.compareTo(ref2) < 0)
                    {
                        ASTAssignStmtNode temp = assignNode1;
                        getAssignedStmtList().remove(assignNode1);
                        getAssignedStmtList().add(j, temp);
                    }
                }
            }
        }

        /**
         * Remove duplicate assign statements. 2 Assign statements are considered to be duplicates if
         * they have the same label addresses and variable names.
         */
        private void removeDuplicateAssigns()
        {
            int maxIndex = getAssignedStmtList().size();
            for (int i = 0; i < maxIndex; i++)
            {
                for (int j = i + 1; j < maxIndex; j++)
                {
                    ASTAssignStmtNode assignNode1 = (ASTAssignStmtNode)getAssignedStmtList().get(i);
                    ASTAssignStmtNode assignNode2 = (ASTAssignStmtNode)getAssignedStmtList().get(j);
                    String ref1 = assignNode1.getAssignedLblRef().getLabel().getText();
                    String ref2 = assignNode2.getAssignedLblRef().getLabel().getText();
                    String var1 = assignNode1.getVariableName().getText();
                    String var2 = assignNode2.getVariableName().getText();

                    if (ref1.equals(ref2) && var1.equals(var2))
                    {
                        getAssignedStmtList().remove(j);
                        maxIndex--;
                        j--;
                    }
                }
            }
        }

        /**
         * Removes all goto labels used in an action statement. Checks if there is at least one
         * assigned goto label left to refactor.
         * @param scope
         * @throws PreconditionFailure
         */
        private void ensureLabelAddressesArePresent(ScopingNode scope) throws PreconditionFailure
        {
            scope.accept(new ASTVisitorWithLoops()
            {
                @Override
                public void visitIActionStmt(IActionStmt node)
                {
                    Token address = node.getLabel();
                    if (address instanceof Token)
                    {
                        labelAddresses.remove(address.getText());
                    }
                    traverseChildren(node);
                }
            });
            if (labelAddresses.size() != 0)
                throw new PreconditionFailure(Messages.RemoveAssignedGotoRefactoring_LabelNotFound + file.getFullPath()); 
        }

        private void collectAllAssignedGoTos(ScopingNode scope)
        {
            scope.accept(new ASTVisitorWithLoops()
            {
                @Override
                public void visitASTAssignStmtNode(ASTAssignStmtNode node)
                {
                    Token address = node.getAssignedLblRef().getLabel();
                    if (address instanceof Token)
                    {
                        labelAddresses.add(address.getText());
                    }
                    getAssignedStmtList().add(0, node);
                    traverseChildren(node);
                }

                // goto 100 --> ASTGotoStmtNode
                // goto label --> ASTAssignedGotoStmtNode
                @Override
                public void visitASTAssignedGotoStmtNode(ASTAssignedGotoStmtNode node)
                {
                    getAssignedGotoStmtList().add(0, node);
                    traverseChildren(node);
                }

                /**
                 * Collect action statements that are not ASTAssignedGotoStmtNode or
                 * ASTAssignStmtNode.
                 */
                @Override
                public void visitIActionStmt(IActionStmt node)
                {
                    if (!(node instanceof ASTAssignedGotoStmtNode)
                        && !(node instanceof ASTAssignStmtNode))
                    {
                        getActionStmtList().add(0, node);
                        traverseChildren(node);
                    }
                }
            });
        }
    }
    
    /**
     * Keeps track of FileInfo for all the selected files.
     * The store is cleared when refactoring starts, ends, when the refactoring is 
     * canceled from the input page  or when an error occurs.
     */
    public static class FileInfoFactory
    {
        private static final Map<IFile, FileInfo> store = new HashMap<IFile, RemoveAssignedGotoRefactoring.FileInfo>();

        public static FileInfo getInstance(IFile file, PhotranVPG vpg) throws PreconditionFailure
        {
            FileInfo storedInfo = store.get(file);

            if (storedInfo != null)
            {
                return storedInfo;
            }
            FileInfo newInstance = new FileInfo(vpg, file);

            store.put(file, newInstance);

            return newInstance;
        }

        public static void reset()
        {
            for (Map.Entry<IFile, FileInfo> entry : store.entrySet())
            {
                entry.getValue().cleanUp();
            }
            store.clear();
        }

        public static void remove(FileInfo instance)
        {
            instance.cleanUp();
            store.remove(instance.getFile());
        }

        public static void removeInstance(IFile file)
        {
            FileInfo instance = store.get(file);
            remove(instance);
        }
    }

    // Initialized to true by default since the button change listener from the
    // input page would be triggered only when a selection is made.
    // If the user directly clicks to OK button without making a choice, the button
    // listener would not be invoked. Refer RemoveAssignedGotoInputPage for more info.
    protected boolean isDefaultCaseRequired = true;

    @Override
    public String getName()
    {
        return Messages.RemoveAssignedGoToRefactoring_Name;
    }

    public void setDefaultSelected(boolean isDefaultCaseRequired)
    {
        this.isDefaultCaseRequired = isDefaultCaseRequired;
    }

    /**
     * Checks the initial conditions before starting refactoring 
     * 1. Checks if refactoring is enabled for this project 
     * 2. Remove files with fixed form, from list of files to be refactored 
     * 3. Remove C-Preprocessed files from list of files to be refactored
     * 4. Initializes the FileInfo for all selected files. 
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(selectedFiles, status);
        removeCpreprocessedFilesFrom(selectedFiles, status);
        FileInfoFactory.reset();
        try
        {
            checkFilesForAssign(status);
        }
        catch (PreconditionFailure pre)
        {
            FileInfoFactory.reset();
            status.addError(pre.getMessage());
            throw pre;
        }

    }

    /**
     * Each selected file is checked for an assign statement. Any file that doesn't contain the
     * assigned goto statement is considered to violate the precondition check.
     * 
     * @throws PreconditionFailure.
     */
    protected void checkFilesForAssign(RefactoringStatus status) throws PreconditionFailure
    {
        for (IFile file : selectedFiles)
        {
            FileInfo newInstance = FileInfoFactory.getInstance(file, vpg);
            if (newInstance.getAssignedStmtList().isEmpty())
            {
                throw new PreconditionFailure(
                    Messages.bind(
                        Messages.RemoveAssignedGotoRefactoring_NothingToBeRefactored,
                        file.getFullPath()));
            }
        }

    }

    /**
     * Iterates over the list of selected files for refactoring. For each file 
     * 1. It gets AST for that file 
     * 2. Puts together a list of all Assign and GoTo statements 
     * 3. Does actual refactoring for each of the Assign Gotos 
     * 4. Releases the AST for that file.
     */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        for (IFile file : selectedFiles)
        {
            FileInfo data = FileInfoFactory.getInstance(file, vpg);
            String removedVariables = data.removeVariablesUsedInActionStmt();

            if (data.getAssignedStmtList().isEmpty())
            {
                FileInfoFactory.reset();
                throw new PreconditionFailure(
                    Messages.RemoveAssignedGotoRefactoring_AllLabelsUsedInActionStatement + file.getFullPath());
            }
            else if (removedVariables.length() != 0)
            {
                status
                    .addWarning(new StringBuffer(
                        Messages.RemoveAssignedGotoRefactoring_TheFollowingLabelsCannotBeRefactored)
                        .append(removedVariables).append(Messages.RemoveAssignedGotoRefactoring_ForFile).append(file.getFullPath())
                        .toString());
            }
        }
    }

    /**
     * First try to create the case body by going through the list of ASSIGN statements. If we
     * don't find any ASSIGN statement with the variable, return null.
     */
    private ASTCaseConstructNode createSelectCaseConstruct(
        ASTAssignedGotoStmtNode assignedGotoNode, FileInfo data)
    {
        ASTCaseConstructNode caseStmt;
        String caseConstructString = ""; //$NON-NLS-1$
        String defaultCaseString = ""; //$NON-NLS-1$
        String endSelectString = "end select"; //$NON-NLS-1$
        
        String caseBodyString = createSelectCaseBody(assignedGotoNode, data);

        if (caseBodyString.length() == 0)
        {
            return null;
        }
        if (isDefaultCaseRequired)
        {
            defaultCaseString = "case default; stop \"Unknown label\"\n"; //$NON-NLS-1$
        }
        // Create the select case string.
        String selCaseString = "select case (" + assignedGotoNode.getVariableName().getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$

        // Create the end select.
        caseConstructString = selCaseString + "\n" + caseBodyString + defaultCaseString //$NON-NLS-1$
            + endSelectString + "\n"; //$NON-NLS-1$
        caseStmt = (ASTCaseConstructNode)parseLiteralStatement(caseConstructString);
        
        return caseStmt;
    }
    
    private String createSelectCaseBody(ASTAssignedGotoStmtNode assignedGotoNode, FileInfo data)
    {
        String caseBodyString = ""; //$NON-NLS-1$
        List<ASTAssignStmtNode> list = data.getAssignedStmtList();
        for (ASTAssignStmtNode assignNode : list)
        {
            if (assignNode.getVariableName().getText()
                .equals(assignedGotoNode.getVariableName().getText()))
            {
                // There is at least one matching ASSIGN statement for the label.
                caseBodyString += "case (" + assignNode.getAssignedLblRef().getLabel().getText() + "); "; //$NON-NLS-1$ //$NON-NLS-2$
                caseBodyString += "goto " + assignNode.getAssignedLblRef().getLabel().getText() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return caseBodyString;
    }

    /**
     * Does the actual change to the selected files to replace the assigned goto statements.
     * 
     * @throws PreconditionFailure
     */
    private void makeChangesTo(IFile fileInEditor, IFortranAST ast, RefactoringStatus status,
        IProgressMonitor pm) throws PreconditionFailure
    {
        if (ast == null)
        { 
            return; 
        }
        FileInfo data = FileInfoFactory.getInstance(fileInEditor, vpg);
        
        List<ASTAssignStmtNode> list = data.getAssignedStmtList();
        for (ASTAssignStmtNode assignStmtNode : list)
        {
            replaceAssignedWithAssignment(ast, assignStmtNode);
        }

        data.sortAssignedStmtList();

        // We also need to remove the duplicate ASSIGN statements so that the
        // case construct should not have duplicate case statements in the body.
        data.removeDuplicateAssigns();

        List<ASTAssignedGotoStmtNode> assignedGotoList = data.getAssignedGotoStmtList();
        for (ASTAssignedGotoStmtNode assignedGotoNode : assignedGotoList)
        {
            ASTCaseConstructNode caseConstructNode = createSelectCaseConstruct(assignedGotoNode,
                data);

            if (caseConstructNode != null)
            {
                caseConstructNode.getSelectCaseStmt().setLabel(assignedGotoNode.getLabel());
                copyCommentsFromOldNode(assignedGotoNode, caseConstructNode);
                assignedGotoNode.replaceWith(caseConstructNode);
                Reindenter.reindent(caseConstructNode, ast, Strategy.SHIFT_ENTIRE_BLOCK);
            }
        }

        this.addChangeFromModifiedAST(fileInEditor, pm);
    }

    /**
     * Builds and replaces an existing assign statement an assignment statement.
     * 1. Create the label
     * 2. Build the basic assignment string
     * 3. Create the node from the strings.
     * 4. Replace the old with the new
     * 5. Properly format the new node
     * 
     * @param ast The AST in use
     * @param node The assign statement node to replace
     */
    private void replaceAssignedWithAssignment(IFortranAST ast, ASTAssignStmtNode node)
    {
        String labelStr = (node.getLabel() != null) ? node.getLabel().getText() : ""; //$NON-NLS-1$

        String assignmentStr = node.getVariableName().getText() + " = " //$NON-NLS-1$
            + node.getAssignedLblRef().getLabel().getText();
        ASTAssignmentStmtNode assignmentStmt = (ASTAssignmentStmtNode)parseLiteralStatement(labelStr
            + assignmentStr);
        node.replaceWith(assignmentStmt);

        Reindenter.reindent(assignmentStmt.findFirstToken(), assignmentStmt.findLastToken(), ast);
        copyCommentsFromOldNode(node, assignmentStmt);
    }

    /**
     * Does the refactoring for each selected file.
     * Cleans up the FileInfo of the visited files.
     * 
     * @throws OperationCanceledException if the refactoring fails.
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        RefactoringStatus status = new RefactoringStatus();
        for (IFile file : selectedFiles)
        {
            IFortranAST ast = vpg.acquirePermanentAST(file);
            try
            {
                makeChangesTo(file, ast, status, pm);
                FileInfoFactory.removeInstance(file);
            }
            catch (PreconditionFailure e)
            {
                FileInfoFactory.reset();
                throw new OperationCanceledException(e.getMessage());
            }
        }
    }

    /**
     * Copies the formatting and comments from the oldNode to the new one to preserve the structure
     * when replacing a node.
     * 
     * @param oldNode The node replaced by the refactoring
     * @param newNode The node inserted with the refactoring
     */
    private void copyCommentsFromOldNode(IASTNode oldNode, IASTNode newNode)
    {
        // Copy the formatting and comments before the statement. 
        newNode.findFirstToken().setWhiteBefore(oldNode.findFirstToken().getWhiteBefore());

        // Copy the comments from end of the statement. 
        newNode.findLastToken().setWhiteBefore(oldNode.findLastToken().getWhiteBefore());
    }
    
    public PhotranVPG getVpg()
    {
        return vpg;
    }
}
