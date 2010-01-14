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
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtSetNode;
import org.eclipse.photran.internal.core.parser.ASTDataStmtValueNode;
import org.eclipse.photran.internal.core.parser.ASTDatalistNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.IDataStmtObject;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.MultipleFileFortranRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;

/**
 * Data To Parameter: refactoring to transform variables declared as data in variables declared
 * with parameter attribute, when these are intended to be constants in source code.
 * Often, developers who want to use constants can confuse the data statement with the
 * attribute parameter, which is the most suitable in these cases. Making the substitution
 * can generate performance gains, because it decreases the access to variables.
 * 
 * @author Gustavo Rissetti
 * @author Timofey Yuvashev
 **/
public class DataToParameterRefactoring extends MultipleFileFortranRefactoring
{
    private List<String> astAssignmentStmtNames = new LinkedList<String>();
    private List<IASTNode> parameter_nodes = new LinkedList<IASTNode>();
    private List<IASTNode> nodes_to_delete = new LinkedList<IASTNode>();            
    private List<ASTDataStmtValueNode> values_to_delete = new LinkedList<ASTDataStmtValueNode>();
    private List<IDataStmtObject> statements_to_delete = new LinkedList<IDataStmtObject>();
    
    private IASTListNode<IDataStmtObject> statement_list = null;
    private IASTListNode<ASTDataStmtValueNode> value_list = null;
    private int statement_count = 0;
    
    private boolean hasChanged = false;
    
    @Override
    public String getName()
    {
        return "Data To Parameter";
    }

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);
        removeFixedFormFilesFrom(this.selectedFiles, status);
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        try{
            for (IFile file : selectedFiles)
            {
                IFortranAST ast = vpg.acquirePermanentAST(file);
                if (ast == null)
                {
                    status.addError("One of the selected files (" + file.getName() +") cannot be parsed.");
                }
                makeChangesTo(file, ast, status, pm);
                vpg.releaseAST(file);
            }
        }
        finally
        {
            vpg.releaseAllASTs();
        }    
    }

    private void populateAssignmentStmtNames(IASTListNode<IASTNode> body)
    {
        // In order to make a statement like: data name / value /
        // in a statement such as: parameter ( name = value ), the variable
        // [name] can not be changed in the program, but can only be referenced.
        for(IASTNode node : body)
        {
            if(node instanceof ASTAssignmentStmtNode)
            {
                String name = ((ASTAssignmentStmtNode)node).getLhsVariable().getName().getText();
                astAssignmentStmtNames.add(name);                            
            }
        }
    }
    
    private void clearLists()
    {
        astAssignmentStmtNames.clear();
        parameter_nodes.clear();
        nodes_to_delete.clear();
        values_to_delete.clear();
        statements_to_delete.clear();
    }
    
    private void makeChangesTo(IFile file, IFortranAST ast, RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        List<ScopingNode> scopes = ast.getRoot().getAllContainedScopes();
        for(ScopingNode scope : scopes)
        {                
            clearLists();
            
            if (!(scope instanceof ASTExecutableProgramNode) && 
                !(scope instanceof ASTDerivedTypeDefNode))
            {
                IASTListNode<IASTNode> body = (IASTListNode<IASTNode>)scope.getBody();                    
                populateAssignmentStmtNames(body);
                
                for(IASTNode node : body)
                {
                    if(node instanceof ASTDataStmtNode)
                    {
                       processDataStmtNode((ASTDataStmtNode)node, ast); 
                    }
                }
                // Inserts all Parameter nodes created.
                for(int i = 0; i<parameter_nodes.size(); i+=2)
                {
                    body.insertAfter(parameter_nodes.get(i), parameter_nodes.get(i+1));
                    Reindenter.reindent(parameter_nodes.get(i+1), ast);
                }
                // Delete Data nodes which were empty.
                for(int i = 0; i<nodes_to_delete.size(); i++)
                {
                    ASTDataStmtNode delete = (ASTDataStmtNode)nodes_to_delete.get(i);
                    if(body.contains(delete)){
                        delete.removeFromTree();
                    }
                }
                
                // If any statement has been as: data ,val /value/
                // is necessary to remove the comma after the data statement.
                removeFrontComma(body, ast);   
            }                
        }   
        // Adds changes in AST.
        if(hasChanged)
        {
            status.addWarning("This refactoring is NOT considering variable assignment that could happen as a result of passing "
                + "a variable to a function/subroutine by reference.");
            addChangeFromModifiedAST(file, pm);
        }
    }
    
    private void removeFrontComma(IASTListNode<IASTNode> body, IFortranAST ast)
    {
        IASTNode comma = null;
        for(IASTNode node : body)
        {                        
            if(node instanceof ASTDataStmtNode)
            {
                comma = node;
                String source_comma = SourcePrinter.getSourceCodeFromASTNode(comma);
                String[] source_comma_split = source_comma.split("\n");
                // Find the Data statement.
                String statement = source_comma_split[source_comma_split.length-1].trim();
                String data = statement.substring(0, 4);
                String list_data = statement.substring(4);                            
                list_data = list_data.trim();
                if(list_data.startsWith(","))
                {
                    // Remove the comma that is left.
                    list_data = list_data.substring(1);
                    list_data = list_data.trim();
                    String new_source = new String("");
                    for(int i=0; i<source_comma_split.length-1; i++)
                    {
                        new_source += source_comma_split[i] + "\n";
                    }
                    new_source += data + " " + list_data;
                    // Create the new node and replaces the old.
                    IASTNode without_comma = parseLiteralStatement(new_source);
                    comma.replaceWith(without_comma);
                    Reindenter.reindent(without_comma, ast);
                }
            }
        }
    }
    
    private void transformToParameter(ASTDataStmtNode node, IFortranAST ast)
    {
        String parameter_name = statement_list.get(statement_count).toString().trim();
        
        // If the variable has not changed, then it will be transformed into a Parameter.
        if(!astAssignmentStmtNames.contains(parameter_name))
        { 
            hasChanged = true;
        
            // New node that will contain the type Parameter declaration.
            IASTNode parameter = null;
            
            String source = SourcePrinter.getSourceCodeFromASTNode(node);
            String[] source_split = source.split("\n");
            // New statement.
            StringBuffer parameter_statement = new StringBuffer("parameter ( ");                                              
            parameter_statement.append(parameter_name + " = ");                                                                                            
            String value = value_list.get(statement_count).getConstant().toString().trim();                                                
            parameter_statement.append(value + " )");
            // Get declaration end-line comments.
            String comments_end_of_line = source_split[source_split.length-1];
            boolean has_comment = false;
            int index_comment = 0;
            for(index_comment = 0; index_comment < comments_end_of_line.length(); index_comment++)
            {
                if(comments_end_of_line.charAt(index_comment) == '!')
                {
                    has_comment = true;
                    break;
                }                                                        
            }
            if(has_comment)
            {
                parameter_statement.append(" " + comments_end_of_line.substring(index_comment));
            }                                        
            // It created the new node.
            parameter = parseLiteralStatement(parameter_statement.toString());
            // Reference to where the new node should be inserted in the AST.
            parameter_nodes.add(node);
            // New node to be inserted in the AST.
            parameter_nodes.add(parameter);
            // Remove the entry of the original lists.
            ASTDataStmtValueNode value_to_remove = value_list.get(statement_count);
            IDataStmtObject statement_to_remove = statement_list.get(statement_count);
            values_to_delete.add(value_to_remove);
            statements_to_delete.add(statement_to_remove);
        }
    }
    
    private void processDataStmtNode(ASTDataStmtNode node, IFortranAST ast) throws PreconditionFailure
    {
        IASTListNode<ASTDatalistNode> data_list = node.getDatalist();
        if(data_list == null)
            throw new PreconditionFailure("Data list of a node was empty. Refactoring failed");
        
        int data_list_size = data_list.size();
        for(ASTDatalistNode data_node : data_list)
        {
            ASTDataStmtSetNode stmt_set_node = data_node.getDataStmtSet();
            statement_list = stmt_set_node.getDataStmtObjectList();
            value_list = stmt_set_node.getDataStmtValueList();
            statement_count = 0;
            
            values_to_delete.clear();
            statements_to_delete.clear();
            for(statement_count = 0; statement_count < statement_list.size(); statement_count++)
            {
                transformToParameter(node, ast);
            }
            // Remove entries from AST.
            data_list_size = removeASTEntries(node, data_node, data_list_size, ast);
        }
    }

    private int removeASTEntries(ASTDataStmtNode node, ASTDatalistNode data_node, int data_list_size, IFortranAST ast)
    {
        if(statement_list.size() == statements_to_delete.size())
        {
            data_node.removeFromTree();
            data_list_size --;                                    
            // If a node has all its data removed, it is necessary to recover the
            // comments that were before it and put them in place.
            if(data_list_size == 0)
            {
                IASTNode parameter = null;
                String source = SourcePrinter.getSourceCodeFromASTNode(node);
                String[] source_split = source.split("\n");
                String comments_before_line = new String("");
                for(int i = 0; i < source_split.length - 1; i++)
                {
                    comments_before_line += source_split[i]+"\n";
                }
                IASTNode last_parameter_node = parameter_nodes.get(parameter_nodes.size()-1);
                String last_parameter_node_source = SourcePrinter.getSourceCodeFromASTNode(last_parameter_node);
                // Added comments.
                String last_parameter_with_comments = comments_before_line + last_parameter_node_source;
                // Generated the new node.
                parameter = parseLiteralStatement(last_parameter_with_comments);
                // Replace the last node of the list (empty) by the new node,
                // containing the comments.
                parameter_nodes.remove(parameter_nodes.size()-1);
                parameter_nodes.add(parameter);
                // Add the empty node to the delete nodes list.
                nodes_to_delete.add(node);
            }
        }
        else
        {
            statement_list.removeAll(statements_to_delete);
            //A bit of a hack... This adds a white space before each remaining element in the data section.
            //It is needed to prevent "clumping together" of the key-word "data" and the following variables
            for(IDataStmtObject n : statement_list)
            {
                n.findFirstToken().setWhiteBefore(" ");
            }
            value_list.removeAll(values_to_delete);
        }
        return data_list_size;
    }
    
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        // The change is made in method makeChangesTo(...).
    }
}
