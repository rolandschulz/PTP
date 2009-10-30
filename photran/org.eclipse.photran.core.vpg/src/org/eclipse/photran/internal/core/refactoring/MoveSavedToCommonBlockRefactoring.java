/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.Token.FakeToken;
import org.eclipse.photran.internal.core.parser.ASTArrayDeclaratorNode;
import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecSeqNode;
import org.eclipse.photran.internal.core.parser.ASTCommonStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDimensionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTInitializationNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTSaveStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSavedEntityNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.ISpecificationStmt;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;

/**
 * Refactoring to move saved variables of a subprogram to a global common block in Fortran programs.
 * The current implementation assumes that the subprogram is in the CONTAINS section of the PROGRAM.
 *
 * @author Stas Negara
 */
public class MoveSavedToCommonBlockRefactoring extends SingleFileFortranRefactoring
{
    /** The OS-dependent end-of-line sequence (\n or \r\n) */
    private static final String EOL = System.getProperty("line.separator");

    private static final String SELECT_SUBPROGRAM_WARNING = "Please select a subroutine or a function (place the cursor in its statement).";
    private static final String SELECT_NON_INTERFACE_SUBPROGRAM_WARNING = "The subroutine's or the function's statement should not be the interface declaration.";

    private String subprogramName = null;
    private ScopingNode subprogramNode = null;
    private IASTListNode<IBodyConstruct> subprogramBodyNode = null;
    private ASTMainProgramNode mainProgramNode = null;

    @Override
    public String getName()
    {
        return "Move Saved Variables to Common Block";
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        Token token = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (token == null) fail(SELECT_SUBPROGRAM_WARNING);

        if (token.findNearestAncestor(ASTInterfaceBlockNode.class) != null){
            fail(SELECT_NON_INTERFACE_SUBPROGRAM_WARNING);
        }

        ASTSubroutineStmtNode subroutineStmtNode = token.findNearestAncestor(ASTSubroutineStmtNode.class);
        if (subroutineStmtNode == null){
            ASTFunctionStmtNode functionStmtNode = token.findNearestAncestor(ASTFunctionStmtNode.class);
            if (functionStmtNode == null){
                fail(SELECT_SUBPROGRAM_WARNING);
            }
            subprogramName = functionStmtNode.getFunctionName().getFunctionName().getText();
            subprogramNode = (ASTFunctionSubprogramNode) functionStmtNode.getParent();
            subprogramBodyNode = ((ASTFunctionSubprogramNode) subprogramNode).getBody();
        }else{
            subprogramName = subroutineStmtNode.getSubroutineName().getSubroutineName().getText();
            subprogramNode = (ASTSubroutineSubprogramNode) subroutineStmtNode.getParent();
            subprogramBodyNode = ((ASTSubroutineSubprogramNode) subprogramNode).getBody();
        }

        //TODO: Consider situations when a subprogram is 1) in the same file but outside the program and 2) in a separate file

        ScopingNode enclosingScope = subprogramNode.getEnclosingScope();
        if (!(enclosingScope instanceof ASTMainProgramNode)){
            fail("The current implementation handles only subprograms that are in the CONTAINS section of the PROGRAM.");
        }
        mainProgramNode = (ASTMainProgramNode) enclosingScope;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        //No input from the user => no final conditions to check
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    //TODO: Consider when the program and the subprogram are in different files
    @Override
    protected void doCreateChange(IProgressMonitor progressMonitor) throws CoreException, OperationCanceledException
    {
        assert subprogramNode != null;
        assert subprogramBodyNode != null;
        assert subprogramName != null;
        assert mainProgramNode != null;

        //TODO: What about common blocks and saved common blocks?
        try{
            if (subprogramBodyNode == null)
                return; //no body => no variables

            TreeSet<Definition> savedVariableDefinitions = getSavedVariableDefinitions();

            StringBuffer globalVariableDeclarations = new StringBuffer();
            StringBuffer commonBlockVariables = new StringBuffer();

            Iterator<Definition> definitionIterator = savedVariableDefinitions.iterator();
            while(definitionIterator.hasNext()){
                Definition definition = definitionIterator.next();
                String globalTypeDeclarationString = constructGlobalTypeDeclarationFor(definition);
                if (globalTypeDeclarationString != null){
                    globalVariableDeclarations.append(EOL).append(globalTypeDeclarationString).append(EOL);
                    //Should be the new name already
                    commonBlockVariables.append(definition.getTokenRef().findToken().getText()).append(",");
                }
            }
            if (globalVariableDeclarations.length() > 0){
                createGlobalCommonBlock(globalVariableDeclarations, commonBlockVariables);
            }
            this.addChangeFromModifiedAST(this.fileInEditor, progressMonitor);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally{
            vpg.releaseAllASTs();
        }
    }

    private void createGlobalCommonBlock(StringBuffer globalVariableDeclarations, StringBuffer commonBlockVariables){
        String commonBlockName = generateUniqueCommonBlockName();
        //Create COMMON spec, removing trailing comma from the sequence of variables
        String commonBlock = EOL + "COMMON /" + commonBlockName + "/ " +
                             commonBlockVariables.toString().substring(0, commonBlockVariables.length() - 1) + EOL;
        globalVariableDeclarations.append(commonBlock);

        IASTListNode<IBodyConstruct> mainProgramListNode = parseLiteralStatementSequence(globalVariableDeclarations.toString());
        mainProgramNode.getBody().addAll(findIndexToInsertTypeDeclaration(mainProgramNode.getBody()), mainProgramListNode);

        IASTListNode<IBodyConstruct> subprogramListNode = parseLiteralStatementSequence(commonBlock);
        subprogramBodyNode.addAll(findIndexToInsertTypeDeclaration(subprogramBodyNode), subprogramListNode);

        Reindenter.reindent(mainProgramListNode, astOfFileInEditor);
        Reindenter.reindent(subprogramListNode, astOfFileInEditor);
    }

    private String generateUniqueCommonBlockName(){
        String commonBlockName = subprogramName + "_common1";
        int counter = 1;
        //Give it a Token to keep happy FakeToken creation
        while (isConflictingOrShadowing(mainProgramNode.findFirstToken(), commonBlockName)){
            counter++;
            commonBlockName = subprogramName + "_common" + counter;
        }
        return commonBlockName;
    }

    private Definition getUniqueDefinitionOrFail(Token variableName) throws PreconditionFailure{
        List<Definition> variableDefinitions = variableName.resolveBinding();
        if (variableDefinitions.size() > 1 || variableDefinitions.size() == 0){
            fail("Absent or ambiguous definition for variable \"" + variableName.getText() + "\".");
        }
        return variableDefinitions.get(0);
    }

    /**
     * Has a side-effect - removes the processed saveNode from the tree.
     * @param saveNode
     * @param savedVariableDefinitions
     * @throws PreconditionFailure
     */
    private void collectSaveStmtVariableDefinitions(ASTSaveStmtNode saveNode, TreeSet<Definition> savedVariableDefinitions) throws PreconditionFailure{
        IASTListNode<ASTSavedEntityNode> variableList = saveNode.getVariableList();
        if (variableList == null){
            // All variables are saved => all should be moved, but continue to remove other potential SAVE nodes
            savedVariableDefinitions.addAll(subprogramNode.getAllDefinitions());
        }else{
            Iterator<ASTSavedEntityNode> variableIterator = variableList.iterator();
            while(variableIterator.hasNext()){
                ASTSavedEntityNode savedVariable = variableIterator.next();
                savedVariableDefinitions.add(getUniqueDefinitionOrFail(savedVariable.getVariableName()));
            }
        }
        saveNode.removeFromTree();
    }

    /**
     * Has a side-effect - removes SAVE specification of the type declaration, if present.
     * @param typeDeclaration
     * @param savedVariableDefinitions
     * @throws PreconditionFailure
     */
    private void collectTypeDeclarationVariableDefinitions(ASTTypeDeclarationStmtNode typeDeclaration, TreeSet<Definition> savedVariableDefinitions) throws PreconditionFailure{
        //Check for SAVE specification
        IASTListNode<ASTAttrSpecSeqNode> attrSpecSeq = typeDeclaration.getAttrSpecSeq();
        if (attrSpecSeq != null){
            Iterator<ASTAttrSpecSeqNode> specIterator = attrSpecSeq.iterator();
            while(specIterator.hasNext()){
                ASTAttrSpecSeqNode attrSpecSeqNode = specIterator.next();
                ASTAttrSpecNode attrSpecNode = attrSpecSeqNode.getAttrSpec();
                if (attrSpecNode != null && attrSpecNode.isSave()){
                    //All variables of this declaration are saved
                    IASTListNode<ASTEntityDeclNode> entityDeclList = typeDeclaration.getEntityDeclList();
                    Iterator<ASTEntityDeclNode> declIterator = entityDeclList.iterator();
                    while(declIterator.hasNext()){
                        ASTEntityDeclNode variableDeclaration = declIterator.next();
                        savedVariableDefinitions.add(getUniqueDefinitionOrFail(variableDeclaration.getObjectName().getObjectName()));
                    }
                    //Remove SAVE spec
                    attrSpecSeq.remove(attrSpecSeqNode);
                    return;
                }
            }
        }
        //Check for initialized variables (they are also saved)
        IASTListNode<ASTEntityDeclNode> entityDeclList = typeDeclaration.getEntityDeclList();
        Iterator<ASTEntityDeclNode> declIterator = entityDeclList.iterator();
        while(declIterator.hasNext()){
            ASTEntityDeclNode variableDeclaration = declIterator.next();
            if (variableDeclaration.getInitialization() != null){
                savedVariableDefinitions.add(getUniqueDefinitionOrFail(variableDeclaration.getObjectName().getObjectName()));
            }
        }
    }

    private TreeSet<Definition> getSavedVariableDefinitions() throws PreconditionFailure{
        //Use sorted set to facilitate the unit testing (this way the variables order will not be volatile as in HashSet)
        TreeSet<Definition> savedVariableDefinitions = new TreeSet<Definition>();
        Iterator< ? extends IASTNode> childrenIterator = subprogramBodyNode.getChildren().iterator();
        while(childrenIterator.hasNext()){
            IASTNode node = childrenIterator.next();
            if (node instanceof ASTSaveStmtNode){
                collectSaveStmtVariableDefinitions((ASTSaveStmtNode) node, savedVariableDefinitions);
            }else if (node instanceof ASTTypeDeclarationStmtNode){
                collectTypeDeclarationVariableDefinitions((ASTTypeDeclarationStmtNode) node, savedVariableDefinitions);
            }
        }
        return savedVariableDefinitions;
    }

    /**
     * Besides constructing the declaration to be inserted in the main program, removes initializations of variables that are moved to
     * the global common block. Also generates the unique name for the moved variable and renames all references accordingly.
     * May return null value, if the variable definition should not be moved (e.g., if it is a PARAMETER variable)
     */
    private String constructGlobalTypeDeclarationFor(Definition variableDefinition) throws PreconditionFailure
    {
        //Ignore dummy variables (subprogram's formal parameters)
        if (variableDefinition.isSubprogramArgument()){
            return null;
        }
        StringBuffer result = new StringBuffer();
        //gfortran will allow duplicates in stand alone specs (with exceptions, e.g., DIMENSION should not be duplicated),
        //but will complain about duplicates in type declaration, so, keep track of the used specs
        HashSet<String> usedSpecs = new HashSet<String>();

        String variableInitAndArraySpec = processOriginalTypeDeclaration(variableDefinition, result, usedSpecs);

        if (variableInitAndArraySpec == null){
            //This is null only if the global declaration should not be created
            return null;
        }

        //At this point we know that the variable will be moved, so, generate a unique name for it and rename all occurences
        String newVariableName = generateUniqueVariableNameAndUpdateDefinition(variableDefinition);

        processSpecifications(newVariableName, variableDefinition, result, usedSpecs);

        result.append(" :: ").append(newVariableName).append(variableInitAndArraySpec);
        return result.toString();
    }

    private String processOriginalTypeDeclaration(Definition variableDefinition, StringBuffer result, HashSet<String> usedSpecs)
                                                  throws PreconditionFailure{
        Token definitionToken = variableDefinition.getTokenRef().findToken();
        ASTTypeDeclarationStmtNode typeDeclaration = definitionToken.findNearestAncestor(ASTTypeDeclarationStmtNode.class);
        if (typeDeclaration == null){
            //The type of the variable is not specified
            result.append("TYPE(UNKNOWN)");
        }else{
            ASTTypeSpecNode typeSpecNode = typeDeclaration.getTypeSpec();
            if (typeSpecNode == null){
                //Why could this happen?
                fail("Could not find type specification node for variable \"" + variableDefinition.getCanonicalizedName() + "\".");
            }
            result.append(SourcePrinter.getSourceCodeFromASTNode(typeSpecNode).trim().toUpperCase());

            IASTListNode<ASTAttrSpecSeqNode> attrSpecSeq = typeDeclaration.getAttrSpecSeq();
            if (attrSpecSeq != null){
                Iterator<ASTAttrSpecSeqNode> iterator = attrSpecSeq.iterator();
                while(iterator.hasNext()){
                    ASTAttrSpecSeqNode attrSpecSeqNode = iterator.next();
                    ASTAttrSpecNode attrSpecNode = attrSpecSeqNode.getAttrSpec();
                    //SAVE spec was removed from the list, not the tree, which takes the immediate effect, so, no need to check for it
                    //If other specs are correct for the subprogram, they should be correct outside it too
                    //E.g., for the case of INTENT - it is illegal to SAVE those variables, so the control flow should not get here
                    if (attrSpecNode != null ){
                        if (attrSpecNode.isParameter()){
                            //PARAMETER variables should not be moved
                            return null;
                        }
                        String spec = SourcePrinter.getSourceCodeFromASTNode(attrSpecNode).trim().toUpperCase();
                        result.append(", ").append(spec);
                        usedSpecs.add(spec);
                    }
                }
            }
        }
        return processEntityDeclNode(variableDefinition);
    }

    private String processEntityDeclNode(Definition variableDefinition) throws PreconditionFailure{
        StringBuffer returnString = new StringBuffer();
        Token definitionToken = variableDefinition.getTokenRef().findToken();
        ASTEntityDeclNode declarationNode = definitionToken.findNearestAncestor(ASTEntityDeclNode.class);
        if (declarationNode == null){
            //really this should not happen
            fail("Could not find declaration node for variable \"" + variableDefinition.getCanonicalizedName() + "\".");
        }
        ASTInitializationNode initializationNode = declarationNode.getInitialization();
        if (initializationNode != null){
            returnString.append(SourcePrinter.getSourceCodeFromASTNode(initializationNode));
            declarationNode.setInitialization(null);
        }
        ASTArraySpecNode arraySpecNode= declarationNode.getArraySpec();
        if (arraySpecNode != null){
            returnString.append("(").append(SourcePrinter.getSourceCodeFromASTNode(arraySpecNode).trim()).append(")");
        }
        return returnString.toString();
    }

    private void processSpecifications(String newVariableName, Definition variableDefinition, StringBuffer result, HashSet<String> usedSpecs)
                                       throws PreconditionFailure{
        for (PhotranTokenRef tokenReference : variableDefinition.findAllReferences(false)){
            //Rename all occurences of the variable to the generated unique name
            tokenReference.findToken().setText(newVariableName);

            ISpecificationStmt enclosingSpecStmt = tokenReference.findToken().findNearestAncestor(ISpecificationStmt.class);
            if (enclosingSpecStmt != null){
                //Special processing for DIMENSION, because we need to take into account the dimension itself, besides DIMENSION keyword
                //Note: DIMENSION is not allowed to be duplicated, so do not check here
                if (enclosingSpecStmt instanceof ASTDimensionStmtNode){
                    ASTArrayDeclaratorNode arrayDeclaratorNode = tokenReference.findToken().findNearestAncestor(ASTArrayDeclaratorNode.class);
                    ASTArraySpecNode arraySpec = null;
                    if (arrayDeclaratorNode == null || (arraySpec = arrayDeclaratorNode.getArraySpec()) == null ){
                        //really this should not happen
                        fail("Could not find array declaration for DIMENSION specification of variable \"" + variableDefinition.getCanonicalizedName() + "\".");
                    }
                    result.append(", DIMENSION(").append(SourcePrinter.getSourceCodeFromASTNode(arraySpec)).append(")");
                //Filter out COMMON and SAVE specs (although SAVE was removed from the tree, the references were not updated yet)
                }else if (!(enclosingSpecStmt instanceof ASTSaveStmtNode) && !(enclosingSpecStmt instanceof ASTCommonStmtNode)){
                    //For all other specs the keyword is enough (again, INTENT should not appear here)
                    String spec = enclosingSpecStmt.findFirstToken().getText().trim().toUpperCase();
                    if (!usedSpecs.contains(spec)){
                        result.append(", ").append(spec);
                        usedSpecs.add(spec);
                    }
                }
            }
        }
    }

    private String generateUniqueVariableNameAndUpdateDefinition(Definition variableDefinition){
        Token definitionToken = variableDefinition.getTokenRef().findToken();
        String newVariableName = variableDefinition.getCanonicalizedName() + "_xxx1";
        int counter = 1;
        while (isConflictingOrShadowing(definitionToken, newVariableName)){
            counter++;
            newVariableName = variableDefinition.getCanonicalizedName() + "_xxx" + counter;
        }
        definitionToken.setText(newVariableName);
        return newVariableName;
    }

    //The newVariableName should have no appearance in the program code
    private boolean isConflictingOrShadowing(Token baseToken, String newVariableName){
        Token fakeToken = new FakeToken(baseToken, newVariableName);

        List<PhotranTokenRef> conflictingDef = subprogramNode.manuallyResolve(fakeToken);
        conflictingDef.addAll(mainProgramNode.manuallyResolve(fakeToken));

        for (ScopingNode importingScope : subprogramNode.findImportingScopes()){
            conflictingDef.addAll(importingScope.manuallyResolve(fakeToken));
        }
        for (ScopingNode importingScope : mainProgramNode.findImportingScopes()){
            conflictingDef.addAll(importingScope.manuallyResolve(fakeToken));
        }
        return !conflictingDef.isEmpty();
    }
}
