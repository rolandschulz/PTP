/*******************************************************************************
 * Copyright (c) 2009 UFSM - Universidade Federal de Santa Maria (www.ufsm.br).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDataImpliedDoNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtValueNode;
import org.eclipse.photran.internal.core.parser.ASTDatalistNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTVariableNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IDataStmtObject;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.core.reindenter.Reindenter;

/**
 * Data To Parameter: refactoring to transform variables declared as data in variables declared
 * with parameter attribute, when these are intended to be constants in source code.
 * Often, developers who want to use constants can confuse the data statement with the
 * attribute parameter, which is the most suitable in these cases. Making the substitution
 * can generate performance gains, because it decreases the access to variables.
 * 
 * @author Gustavo Rissetti
 * @author Timofey Yuvashev
 * @author Jeff Overbey
 * @author Ashley Kasza - externalized strings
 **/
public class DataToParameterRefactoring extends FortranResourceRefactoring
{
    boolean changesWereMade = false;
    
    @Override
    public String getName()
    {
        return Messages.DataToParameterRefactoring_Name;
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
        removeCpreprocessedFilesFrom(this.selectedFiles, status);
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try
        {
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if (ast == null)
                {
                    status.addError(Messages.bind(Messages.DataToParameterRefactoring_SelectedFileCannotBeParsed, file.getName()));
                }
                else
                {
                    makeChangesTo(file, ast, status, pm);
                    vpg.releaseAST(file);
                }
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }
    }

    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        for (ScopingNode scope : ast.getRoot().getAllContainedScopes())
            new ScopeConverter().convert(scope, ast, status);

        if (changesWereMade)
        {
            status.addWarning(Messages.DataToParameterRefactoring_RefactorNotConsideringVarAssignment);
            addChangeFromModifiedAST(file, pm);
        }
    }

    private class ScopeConverter
    {
        private IASTListNode<IASTNode> scopeBody;
        private IFortranAST ast;
        
        private List<IASTNode> dataAndParameterStmts = new LinkedList<IASTNode>();
        private List<IASTNode> nodesToDelete = new LinkedList<IASTNode>();
        
        @SuppressWarnings("unchecked")
        public void convert(ScopingNode scope, IFortranAST ast, RefactoringStatus status) throws PreconditionFailure
        {
            if (scope instanceof ASTExecutableProgramNode || scope instanceof ASTDerivedTypeDefNode)
                return;
            
            this.ast = ast;
            this.scopeBody = (IASTListNode<IASTNode>)scope.getBody();
            
            convert(status);
        }

        private void convert(RefactoringStatus status) throws PreconditionFailure
        {
            List<String> assignedVars = determineAssignedVariables();
            
            for (IASTNode node : scopeBody)
                if (node instanceof ASTDataStmtNode)
                   convertDataStmt((ASTDataStmtNode)node, assignedVars, status);

            insertAndDeleteStmts();
            
            removeLeadingComma();
        }

        /**
         * In order to convert  <pre>data name / value /</pre>
         * into <pre>parameter ( name = value )</pre>, the variable
         * <i>name</i> cannot be assigned in the program, only read.
         * 
         * @return a list of variables that appear on the left-hand side of an assignment statement
         */
        private List<String> determineAssignedVariables()
        {
            List<String> assignedVars = new LinkedList<String>();
            
            for (IASTNode node : scopeBody)
                if (node instanceof ASTAssignmentStmtNode)
                    assignedVars.add(((ASTAssignmentStmtNode)node).getLhsVariable().getName().getText());
            
            return assignedVars;
        }
        
        private void convertDataStmt(ASTDataStmtNode node, List<String> assignedVars, RefactoringStatus status) throws PreconditionFailure
        {
            if (node.getDatalist() == null)
                throw new PreconditionFailure(Messages.DataToParameterRefactoring_EmptyDataListInNode);

            int size = node.getDatalist().size();
            for (ASTDatalistNode dataList : node.getDatalist())
                size = new DataListConverter().convert(dataList, size, assignedVars, this, status);
        }

        private void insertAndDeleteStmts()
        {
            // Inserts all Parameter nodes created.
            for (int i = 0; i<dataAndParameterStmts.size(); i+=2)
            {
                scopeBody.insertAfter(dataAndParameterStmts.get(i), dataAndParameterStmts.get(i+1));
                Reindenter.reindent(dataAndParameterStmts.get(i+1), ast);
            }
            // Delete Data nodes which were empty.
            for (int i = 0; i<nodesToDelete.size(); i++)
            {
                ASTDataStmtNode delete = (ASTDataStmtNode)nodesToDelete.get(i);
                if(scopeBody.contains(delete)){
                    delete.removeFromTree();
                }
            }
        }

        /**
         * If any statement has been changed to
         * <pre>data ,val /value/</pre>
         * this removes the comma after the DATA keyword.
         */
        private void removeLeadingComma()
        {
            for (IASTNode node : scopeBody)
            {                        
                if (node instanceof ASTDataStmtNode)
                {
                    IASTNode comma = node;
                    String source_comma = SourcePrinter.getSourceCodeFromASTNode(comma);
                    String[] source_comma_split = source_comma.split("\n"); //$NON-NLS-1$
                    // Find the Data statement.
                    String statement = source_comma_split[source_comma_split.length-1].trim();
                    String data = statement.substring(0, 4);
                    String list_data = statement.substring(4);                            
                    list_data = list_data.trim();
                    if(list_data.startsWith(",")) //$NON-NLS-1$
                    {
                        // Remove the comma that is left.
                        list_data = list_data.substring(1);
                        list_data = list_data.trim();
                        String new_source = new String(""); //$NON-NLS-1$
                        for(int i=0; i<source_comma_split.length-1; i++)
                        {
                            new_source += source_comma_split[i] + "\n"; //$NON-NLS-1$
                        }
                        new_source += data + " " + list_data; //$NON-NLS-1$
                        // Create the new node and replaces the old.
                        IASTNode without_comma = parseLiteralStatement(new_source);
                        comma.replaceWith(without_comma);
                        Reindenter.reindent(without_comma, ast);
                    }
                }
            }
        }
        
        // These are used by DataListConverter, below

        public void prependCommentsToLastParameterStmt(String comments)
        {
            IASTNode lastParameterStmt = dataAndParameterStmts.get(lastParameterStmtIndex());
            IASTNode parameterStmtWithComments = parseLiteralStatement(comments + SourcePrinter.getSourceCodeFromASTNode(lastParameterStmt));
            
            replaceLastParameterStmtWith(parameterStmtWithComments);
        }
        
        private int lastParameterStmtIndex()
        {
            return dataAndParameterStmts.size()-1;
        }

        private void replaceLastParameterStmtWith(IASTNode newParameterStmt)
        {
            dataAndParameterStmts.remove(lastParameterStmtIndex());
            dataAndParameterStmts.add(newParameterStmt);
        }

        public void addDataStmtAndParameterStmt(IASTNode dataStmt, IASTNode parameter)
        {
            // Reference to where the new node should be inserted in the AST.
            dataAndParameterStmts.add(dataStmt);
            // New node to be inserted in the AST.
            dataAndParameterStmts.add(parameter);
        }

        /** Adds the given node to the list of nodes to delete */
        public void addNodeToDelete(ASTDataStmtNode dataStmt)
        {
            nodesToDelete.add(dataStmt);
        }
    }
    
    private class DataListConverter
    {
        private ScopeConverter scopeConverter;
        private ASTDatalistNode dataList;
        private IASTListNode<IDataStmtObject> objectList;
        private IASTListNode<ASTDataStmtValueNode> valueList;
        private ASTDataStmtNode dataStmt;
        
        private List<IDataStmtObject> objectsToDelete;
        private List<ASTDataStmtValueNode> valuesToDelete;
        
        public int convert(ASTDatalistNode dataList, int numDataLists, List<String> assignedVars, ScopeConverter scopeConverter, RefactoringStatus status) throws PreconditionFailure
        {
            this.dataList = dataList;
            this.objectList = dataList.getDataStmtSet().getDataStmtObjectList();
            this.valueList = dataList.getDataStmtSet().getDataStmtValueList();
            this.dataStmt = (ASTDataStmtNode)dataList.getParent().getParent();
            this.scopeConverter = scopeConverter;
            
            this.objectsToDelete = new LinkedList<IDataStmtObject>();
            this.valuesToDelete = new LinkedList<ASTDataStmtValueNode>();

            transformToParameters(dataList, assignedVars, status);

            return removeASTEntries(numDataLists);
        }

        protected void transformToParameters(ASTDatalistNode dataList, List<String> assignedVars, RefactoringStatus status)
            throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
        {
            for (int i = 0; i < dataList.getDataStmtSet().getDataStmtObjectList().size(); i++)
            {
                String parameterName = objectList.get(i).toString().trim();
                
                if (objectList.get(i) instanceof ASTDataImpliedDoNode)
                {
                    status.addError(Messages.DataToParameterRefactoring_ImpliedDoNotSupported);
                    break;
                }
                else
                {
                    Definition definition = getDefinition((ASTVariableNode)objectList.get(i));
                    
                    if (definition.getArraySpec() != null)
                    {
                        status.addError(Messages.DataToParameterRefactoring_ArraysNotSupported);
                        break;
                    }
                    
                    if (definition.isPointer())
                    {
                        status.addError(Messages.DataToParameterRefactoring_PointersNotSupported);
                        break;
                    }
                    
                    if (!assignedVars.contains(parameterName))
                    {
                        changesWereMade = true;
                        
                        IASTNode parameterStmt = createParameterStmt(i, parameterName);
                        
                        scopeConverter.addDataStmtAndParameterStmt(dataStmt, parameterStmt);
                        
                        valuesToDelete.add(valueList.get(i));
                        objectsToDelete.add(objectList.get(i));
                    }
                }
            }
        }

        private Definition getDefinition(ASTVariableNode astVariableNode) throws PreconditionFailure
        {
            List<Definition> definitionList = astVariableNode.findFirstToken().resolveBinding();
            
            if (definitionList.size() == 0)
                throw new PreconditionFailure(Messages.DataToParameterRefactoring_DefinitionNotFound);
            
            if (definitionList.size() > 1)
                throw new PreconditionFailure(Messages.DataToParameterRefactoring_AmbiguouslyDefined);

            return definitionList.get(0);
        }
        
        private IASTNode createParameterStmt(int index, String parameterName)
        {
            StringBuffer parameterStmt = new StringBuffer("parameter ( "); //$NON-NLS-1$
            parameterStmt.append(parameterName + " = "); //$NON-NLS-1$
            String value = valueList.get(index).getConstant().toString().trim();
            parameterStmt.append(value);
            parameterStmt.append(" )"); //$NON-NLS-1$
            parameterStmt.append(trailingComments());
            return parseLiteralStatement(parameterStmt.toString());
        }

        private String trailingComments()
        {
            // TODO: Use dataStmt.findLastToken().getWhiteBefore()?
            String source = SourcePrinter.getSourceCodeFromASTNode(dataStmt);
            String[] sourceSplit = source.split("\n"); //$NON-NLS-1$
            String lastLine = sourceSplit[sourceSplit.length-1];
            for (int index_comment = 0; index_comment < lastLine.length(); index_comment++)
                if (lastLine.charAt(index_comment) == '!')
                    return " " + lastLine.substring(index_comment); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }

        private int removeASTEntries(int numDataLists)
        {
            if (objectList.size() == objectsToDelete.size())
                return removeEntireDataList(numDataLists);
            else
                return removeSpecifiedObjectsOnly(numDataLists);
        }

        private int removeEntireDataList(int numDataLists)
        {
            dataList.removeFromTree();
            numDataLists--;
            
            // If a node has all its data removed, it is necessary to recover the
            // comments that were before it and put them in place.
            if (numDataLists == 0)
            {
                scopeConverter.prependCommentsToLastParameterStmt(leadingComments());
                scopeConverter.addNodeToDelete(dataStmt);
            }
            
            return numDataLists;
        }

        private String leadingComments()
        {
            String source = SourcePrinter.getSourceCodeFromASTNode(dataStmt);
            String[] sourceSplit = source.split("\n"); //$NON-NLS-1$
            String commentsBeforeLine = ""; //$NON-NLS-1$
            for (int i = 0; i < sourceSplit.length - 1; i++)
                commentsBeforeLine += sourceSplit[i]+"\n"; //$NON-NLS-1$
            return commentsBeforeLine;
        }

        private int removeSpecifiedObjectsOnly(int numDataLists)
        {
            objectList.removeAll(objectsToDelete);
            //A bit of a hack... This adds a white space before each remaining element in the data section.
            //It is needed to prevent "clumping together" of the key-word "data" and the following variables
            for (IDataStmtObject n : objectList)
                n.findFirstToken().setWhiteBefore(" "); //$NON-NLS-1$

            valueList.removeAll(valuesToDelete);
            
            return numDataLists;
        }
    }
    
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        // The change is made in method makeChangesTo(...).
    }
}
