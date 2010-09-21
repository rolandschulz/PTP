/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTProperLoopConstructNode;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;
import org.eclipse.rephraserengine.core.refactorings.UserInputString;

/**
 * Loop refactoring to take a nested loop and change the data accesses to a tiled form.
 * @author Ashley Kasza
 */
public class TileLoopRefactoring extends FortranEditorRefactoring
{
    private ASTProperLoopConstructNode doLoop;
    private ASTProperLoopConstructNode secondDoLoop;
    private String newFirstIndexVar;
    private String newSecondIndexVar;
    private int tilingSize;
    private int tilingOffset;
    private boolean hasLoopDependency;

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        LoopReplacer.replaceAllLoopsIn(this.astOfFileInEditor.getRoot());
        
        doLoop = getLoopNode(this.astOfFileInEditor, this.selectedRegionInEditor);
        // fail if no do loop was selected
        if (doLoop == null)
        {
            fail(Messages.ReverseLoopRefactoring_SelectDoLoop);
        }
        secondDoLoop = findSecondDoLoop(doLoop);
        if(secondDoLoop == null){
            fail(Messages.ReverseLoopRefactoring_SelectDoLoop);
        }
        //check for nested loops.
        if(findNumberOfLoops() != 1){
            fail(Messages.TileLoopRefactoring_SelectLoopWithOnlyOneNestedLoop);
        }
        newFirstIndexVar = findNewIndexVariableName(doLoop, doLoop.getIndexVariable().getText());
        newSecondIndexVar = findNewIndexVariableName(secondDoLoop, secondDoLoop.getIndexVariable().getText());
        if(newFirstIndexVar == null || newSecondIndexVar == null){
            fail(Messages.TileLoopRefactoring_UnableToCreateNewIndex);
        }
        
        try{
            if(doLoop.getStepInt() != 1 || secondDoLoop.getStepInt() != 1){
                fail(Messages.TileLoopRefactoring_CantTileLoopsWithStep);
            }
        }catch(NumberFormatException e){
            fail(Messages.TileLoopRefactoring_CantTileLoopsWithStep);
        }
        
    }
    
    private ASTProperLoopConstructNode findSecondDoLoop(ASTProperLoopConstructNode firstLoop){
        IASTListNode<IExecutionPartConstruct> firstBody = firstLoop.getBody();
        return firstBody.findFirst(ASTProperLoopConstructNode.class);
    }
    
    private String findNewIndexVariableName(ASTProperLoopConstructNode nodeInScope, String indexVar){
        boolean canUse;
        String newIndexVar = indexVar;
        for (int i = 1; i <= 10; i++)
        {
            canUse = true;
            ScopingNode scope = ScopingNode.getLocalScope(nodeInScope);
            List<Definition> defList = scope.getAllDefinitions();
            for (Definition d : defList)
            {
                if (d.getCanonicalizedName().equals(newIndexVar.toLowerCase()))
                {
                    newIndexVar = indexVar + Integer.toString(i);
                    canUse = false;
                }
            }
            if (canUse == true)
            {
                return newIndexVar;
            }
        }
        return null;
    }
    
    
    private int findNumberOfLoops(){
        int loopCount = 0;
       IASTListNode<IExecutionPartConstruct> doLoopBody = doLoop.getBody();
       for(int i = 0; i < doLoopBody.size(); i++){
           if(doLoopBody.get(i) instanceof ASTProperLoopConstructNode){
               loopCount++;
           }
       }
       return loopCount;
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // no final conditions
        if(tilingSize <= 0){
            fail(Messages.TileLoopRefactoring_InvalidTileSize);
        }
        if(tilingOffset <0){
            fail(Messages.TileLoopRefactoring_InvalidTilingOffset);
        }
    }
    

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        ScopingNode scope = ScopingNode.getLocalScope(doLoop);
        
        hasLoopDependency = false;
        findIndexVariableInHeader(secondDoLoop.getLoopHeader(), doLoop.getIndexVariable().getText());
        
        declareNewLoopVariables(scope);
        
        @SuppressWarnings("unused")
        ASTProperLoopConstructNode node = loopTilingTransformation();
        //parseLiteralDoLoop(node.toString());
        //scope = ScopingNode.getLocalScope(node);
        
        //Reindenter.reindent(loopTilingTransformation(), this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);
        Reindenter.reindent(scope.getBody(), this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);

        this.addChangeFromModifiedAST(this.fileInEditor, pm);

        vpg.releaseAST(this.fileInEditor);
    }
    
    private void findIndexVariableInHeader(IASTNode node, final String indexVariable){
        node.accept(new ASTVisitorWithLoops()
        {
            @Override public void visitToken(Token token)
            {
                if(token.getTerminal() == Terminal.T_IDENT && (token.getText()).equalsIgnoreCase(indexVariable)){
                    hasLoopDependency = true;
                }
            }

        });
        //hasLoopDependency = false;
    }
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ASTProperLoopConstructNode loopTilingTransformation(){

        ASTProperLoopConstructNode firstElementLoop = constructNewElementLoop(doLoop, newFirstIndexVar); //inner loop
        ASTProperLoopConstructNode firstTileLoop = createNewTileLoop(doLoop, newFirstIndexVar);
        ASTProperLoopConstructNode secondElementLoop = constructNewElementLoop(secondDoLoop, newSecondIndexVar);
        ASTProperLoopConstructNode secondTileLoop = createNewTileLoop(secondDoLoop, newSecondIndexVar);
        
        if(hasLoopDependency){
            replaceBound(secondTileLoop.getLowerBoundIExpr(), doLoop.getLowerBoundIExpr().toString(), firstTileLoop.getIndexVariable().getText(), true);
            replaceBound(secondTileLoop.getUpperBoundIExpr(), doLoop.getUpperBoundIExpr().toString(), firstTileLoop.getIndexVariable().getText(), false);
        }
        
        IASTListNode body;
        body = secondElementLoop.getBody();
        body.add(secondDoLoop.getBody());
        body = firstElementLoop.getBody();
        body.add(secondElementLoop);
        body = secondTileLoop.getBody();
        body.add(firstElementLoop);
        body = firstTileLoop.getBody();
        body.add(secondTileLoop);
        
        doLoop.replaceWith(firstTileLoop);
        return firstTileLoop;
    }
    
    private void replaceBound(IExpr expr, String replace1, String replace2, boolean isLb){
        if(!(expr instanceof ASTIntConstNode))
        {
            IExpr dummyBound = (IExpr)(expr.clone());
            IExpr dummyBound2 = (IExpr)(expr.clone());
            replaceFirstLoopIndexVariable(dummyBound, doLoop.getIndexVariable().getText(), replace1);
            replaceFirstLoopIndexVariable(dummyBound2, doLoop.getIndexVariable().getText(), replace2);
            if(!(dummyBound.toString().equals(dummyBound2.toString()))){
                String replacementBound;
                if(isLb == true)
                    replacementBound = String.format("max(%s,%s)", dummyBound.toString(), dummyBound2.toString()); //$NON-NLS-1$
                else
                    replacementBound = String.format("min(%s,%s)", dummyBound.toString(), dummyBound2.toString()); //$NON-NLS-1$
                expr.replaceWith(replacementBound);
            }
        }
    }
    
    private void replaceFirstLoopIndexVariable(IASTNode node, final String indexVar, final String replacement)
    {
        node.accept(new ASTVisitor()
        {
            @Override public void visitToken(Token token)
            {
                if(token.getTerminal() == Terminal.T_IDENT && token.getText().equalsIgnoreCase(indexVar)){
                    token.replaceWith(replacement);
                }
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void declareNewLoopVariables(ScopingNode scope){
        //ScopingNode scope = ScopingNode.getLocalScope(doLoop);
        IASTListNode<IASTNode> scopeBody = (IASTListNode<IASTNode>)scope.getOrCreateBody();
        String declarationString = "integer :: " + newFirstIndexVar + ", " + newSecondIndexVar; //$NON-NLS-1$ //$NON-NLS-2$
        int insertionIndex = findIndexToInsertTypeDeclaration(scopeBody);
        scopeBody.add(insertionIndex, parseLiteralStatement(declarationString));
    }
    
    private ASTProperLoopConstructNode createNewTileLoop(ASTProperLoopConstructNode inputLoop, String newIndexVar){

        String newLb = getNewBoundsString(inputLoop.getLowerBoundIExpr());
        String newUb = getNewBoundsString(inputLoop.getUpperBoundIExpr());
        String loopHeader = String.format("do %s=%s,%s,%s\nend do\n", newIndexVar, //$NON-NLS-1$
            newLb, newUb,tilingSize); 
        return parseLiteralDoLoop(loopHeader);
    }
    
    private String getNewBoundsString(IExpr expr){
        if(expr instanceof ASTIntConstNode){
            int loopBoundInt = Integer.parseInt(expr.toString());
            int newBound = (int)Math.floor((double)(loopBoundInt-tilingOffset)/tilingSize);
            newBound = (newBound * tilingSize)+tilingOffset;
            return Integer.toString(newBound);
        }else{
            
            //interchangeWithIdentifier = true;
            return String.format("floor(real(%s-%s)/%s)*%s+%s",expr.toString(), Integer.toString(tilingOffset), Integer.toString(tilingSize), //$NON-NLS-1$ 
                Integer.toString(tilingSize), Integer.toString(tilingOffset)); 
        }
        
    }
    
    private ASTProperLoopConstructNode constructNewElementLoop(ASTProperLoopConstructNode inputLoop, String newIndexVar){
        
        String elementLoopLb = String.format("max(%s,%s)", inputLoop.getLowerBoundIExpr().toString(), newIndexVar);//$NON-NLS-1$
        String elementLoopUb = String.format("min(%s,%s+%s)", inputLoop.getUpperBoundIExpr().toString(), newIndexVar, (tilingSize-1));//$NON-NLS-1$
        String elementLoopFull = String.format("do %s=%s,%s\n end do\n", inputLoop.getIndexVariable().getText(), elementLoopLb, elementLoopUb); //$NON-NLS-1$
        ASTProperLoopConstructNode newNode = parseLiteralDoLoop(elementLoopFull);
        return newNode;
    }
    
    
    @UserInputString(label = "Enter tile size ", defaultValueMethod= "getSuggestedTilingSize")
    public void setLoopTilingStepNumber(String input)
    {
        tilingSize = Integer.parseInt(input);
    }
    public String getSuggestedTilingSize()
    {
        return "1"; //$NON-NLS-1$
    }
    @UserInputString(label = "Enter tile offset ", defaultValueMethod = "getSuggestedTilingOffset")
    public void setLoopTilingOffsetNumber(String input)
    {
        tilingOffset = Integer.parseInt(input);
    }
    public String getSuggestedTilingOffset()
    {
        return "1"; //$NON-NLS-1$
    }
    
    @Override
    public String getName()
    {
        return Messages.TileLoopRefactoring_LoopTilingName;
    }
}
