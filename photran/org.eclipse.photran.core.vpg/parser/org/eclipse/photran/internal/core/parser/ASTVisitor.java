/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;

@SuppressWarnings("all")
public class ASTVisitor implements IASTVisitor
{
    protected void traverseChildren(IASTNode node)
    {
        for (IASTNode child : node.getChildren())
            child.accept(this);
    }

    public void visitASTNode(IASTNode node) {}
    public void visitToken(Token node) {}
    public void visitASTListNode(IASTListNode<?> node) { traverseChildren(node); }
    public void visitASTAcImpliedDoNode(ASTAcImpliedDoNode node) { traverseChildren(node); }
    public void visitASTAcValueNode(ASTAcValueNode node) { traverseChildren(node); }
    public void visitASTAccessSpecNode(ASTAccessSpecNode node) { traverseChildren(node); }
    public void visitASTAccessStmtNode(ASTAccessStmtNode node) { traverseChildren(node); }
    public void visitASTAllStopStmtNode(ASTAllStopStmtNode node) { traverseChildren(node); }
    public void visitASTAllocatableStmtNode(ASTAllocatableStmtNode node) { traverseChildren(node); }
    public void visitASTAllocateCoarraySpecNode(ASTAllocateCoarraySpecNode node) { traverseChildren(node); }
    public void visitASTAllocateObjectNode(ASTAllocateObjectNode node) { traverseChildren(node); }
    public void visitASTAllocateStmtNode(ASTAllocateStmtNode node) { traverseChildren(node); }
    public void visitASTAllocatedShapeNode(ASTAllocatedShapeNode node) { traverseChildren(node); }
    public void visitASTAllocationNode(ASTAllocationNode node) { traverseChildren(node); }
    public void visitASTArithmeticIfStmtNode(ASTArithmeticIfStmtNode node) { traverseChildren(node); }
    public void visitASTArrayAllocationNode(ASTArrayAllocationNode node) { traverseChildren(node); }
    public void visitASTArrayConstructorNode(ASTArrayConstructorNode node) { traverseChildren(node); }
    public void visitASTArrayDeclaratorNode(ASTArrayDeclaratorNode node) { traverseChildren(node); }
    public void visitASTArrayElementNode(ASTArrayElementNode node) { traverseChildren(node); }
    public void visitASTArrayNameNode(ASTArrayNameNode node) { traverseChildren(node); }
    public void visitASTArraySpecNode(ASTArraySpecNode node) { traverseChildren(node); }
    public void visitASTAssignStmtNode(ASTAssignStmtNode node) { traverseChildren(node); }
    public void visitASTAssignedGotoStmtNode(ASTAssignedGotoStmtNode node) { traverseChildren(node); }
    public void visitASTAssignmentStmtNode(ASTAssignmentStmtNode node) { traverseChildren(node); }
    public void visitASTAssociateConstructNode(ASTAssociateConstructNode node) { traverseChildren(node); }
    public void visitASTAssociateStmtNode(ASTAssociateStmtNode node) { traverseChildren(node); }
    public void visitASTAssociationNode(ASTAssociationNode node) { traverseChildren(node); }
    public void visitASTAssumedShapeSpecListNode(ASTAssumedShapeSpecListNode node) { traverseChildren(node); }
    public void visitASTAssumedShapeSpecNode(ASTAssumedShapeSpecNode node) { traverseChildren(node); }
    public void visitASTAssumedSizeSpecNode(ASTAssumedSizeSpecNode node) { traverseChildren(node); }
    public void visitASTAsynchronousStmtNode(ASTAsynchronousStmtNode node) { traverseChildren(node); }
    public void visitASTAttrSpecNode(ASTAttrSpecNode node) { traverseChildren(node); }
    public void visitASTAttrSpecSeqNode(ASTAttrSpecSeqNode node) { traverseChildren(node); }
    public void visitASTBackspaceStmtNode(ASTBackspaceStmtNode node) { traverseChildren(node); }
    public void visitASTBinaryExprNode(ASTBinaryExprNode node) { traverseChildren(node); }
    public void visitASTBindStmtNode(ASTBindStmtNode node) { traverseChildren(node); }
    public void visitASTBindingAttrNode(ASTBindingAttrNode node) { traverseChildren(node); }
    public void visitASTBindingPrivateStmtNode(ASTBindingPrivateStmtNode node) { traverseChildren(node); }
    public void visitASTBlockConstructNode(ASTBlockConstructNode node) { traverseChildren(node); }
    public void visitASTBlockDataNameNode(ASTBlockDataNameNode node) { traverseChildren(node); }
    public void visitASTBlockDataStmtNode(ASTBlockDataStmtNode node) { traverseChildren(node); }
    public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node) { traverseChildren(node); }
    public void visitASTBlockDoConstructNode(ASTBlockDoConstructNode node) { traverseChildren(node); }
    public void visitASTBlockStmtNode(ASTBlockStmtNode node) { traverseChildren(node); }
    public void visitASTBodyPlusInternalsNode(ASTBodyPlusInternalsNode node) { traverseChildren(node); }
    public void visitASTBozLiteralConstNode(ASTBozLiteralConstNode node) { traverseChildren(node); }
    public void visitASTCExprNode(ASTCExprNode node) { traverseChildren(node); }
    public void visitASTCOperandNode(ASTCOperandNode node) { traverseChildren(node); }
    public void visitASTCPrimaryNode(ASTCPrimaryNode node) { traverseChildren(node); }
    public void visitASTCallStmtNode(ASTCallStmtNode node) { traverseChildren(node); }
    public void visitASTCaseConstructNode(ASTCaseConstructNode node) { traverseChildren(node); }
    public void visitASTCaseSelectorNode(ASTCaseSelectorNode node) { traverseChildren(node); }
    public void visitASTCaseStmtNode(ASTCaseStmtNode node) { traverseChildren(node); }
    public void visitASTCaseValueRangeNode(ASTCaseValueRangeNode node) { traverseChildren(node); }
    public void visitASTCharLenParamValueNode(ASTCharLenParamValueNode node) { traverseChildren(node); }
    public void visitASTCharLengthNode(ASTCharLengthNode node) { traverseChildren(node); }
    public void visitASTCharSelectorNode(ASTCharSelectorNode node) { traverseChildren(node); }
    public void visitASTCloseSpecListNode(ASTCloseSpecListNode node) { traverseChildren(node); }
    public void visitASTCloseSpecNode(ASTCloseSpecNode node) { traverseChildren(node); }
    public void visitASTCloseStmtNode(ASTCloseStmtNode node) { traverseChildren(node); }
    public void visitASTCoarraySpecNode(ASTCoarraySpecNode node) { traverseChildren(node); }
    public void visitASTCodimensionDeclNode(ASTCodimensionDeclNode node) { traverseChildren(node); }
    public void visitASTCodimensionStmtNode(ASTCodimensionStmtNode node) { traverseChildren(node); }
    public void visitASTCommaExpNode(ASTCommaExpNode node) { traverseChildren(node); }
    public void visitASTCommaLoopControlNode(ASTCommaLoopControlNode node) { traverseChildren(node); }
    public void visitASTCommonBlockBinding(ASTCommonBlockBinding node) { traverseChildren(node); }
    public void visitASTCommonBlockNameNode(ASTCommonBlockNameNode node) { traverseChildren(node); }
    public void visitASTCommonBlockNode(ASTCommonBlockNode node) { traverseChildren(node); }
    public void visitASTCommonBlockObjectNode(ASTCommonBlockObjectNode node) { traverseChildren(node); }
    public void visitASTCommonStmtNode(ASTCommonStmtNode node) { traverseChildren(node); }
    public void visitASTComplexConstNode(ASTComplexConstNode node) { traverseChildren(node); }
    public void visitASTComponentArraySpecNode(ASTComponentArraySpecNode node) { traverseChildren(node); }
    public void visitASTComponentAttrSpecNode(ASTComponentAttrSpecNode node) { traverseChildren(node); }
    public void visitASTComponentDeclNode(ASTComponentDeclNode node) { traverseChildren(node); }
    public void visitASTComponentInitializationNode(ASTComponentInitializationNode node) { traverseChildren(node); }
    public void visitASTComponentNameNode(ASTComponentNameNode node) { traverseChildren(node); }
    public void visitASTComputedGotoStmtNode(ASTComputedGotoStmtNode node) { traverseChildren(node); }
    public void visitASTConnectSpecListNode(ASTConnectSpecListNode node) { traverseChildren(node); }
    public void visitASTConnectSpecNode(ASTConnectSpecNode node) { traverseChildren(node); }
    public void visitASTConstantNode(ASTConstantNode node) { traverseChildren(node); }
    public void visitASTContainsStmtNode(ASTContainsStmtNode node) { traverseChildren(node); }
    public void visitASTContiguousStmtNode(ASTContiguousStmtNode node) { traverseChildren(node); }
    public void visitASTContinueStmtNode(ASTContinueStmtNode node) { traverseChildren(node); }
    public void visitASTCrayPointerStmtNode(ASTCrayPointerStmtNode node) { traverseChildren(node); }
    public void visitASTCrayPointerStmtObjectNode(ASTCrayPointerStmtObjectNode node) { traverseChildren(node); }
    public void visitASTCriticalConstructNode(ASTCriticalConstructNode node) { traverseChildren(node); }
    public void visitASTCriticalStmtNode(ASTCriticalStmtNode node) { traverseChildren(node); }
    public void visitASTCycleStmtNode(ASTCycleStmtNode node) { traverseChildren(node); }
    public void visitASTDataComponentDefStmtNode(ASTDataComponentDefStmtNode node) { traverseChildren(node); }
    public void visitASTDataImpliedDoNode(ASTDataImpliedDoNode node) { traverseChildren(node); }
    public void visitASTDataRefNode(ASTDataRefNode node) { traverseChildren(node); }
    public void visitASTDataStmtConstantNode(ASTDataStmtConstantNode node) { traverseChildren(node); }
    public void visitASTDataStmtNode(ASTDataStmtNode node) { traverseChildren(node); }
    public void visitASTDataStmtSetNode(ASTDataStmtSetNode node) { traverseChildren(node); }
    public void visitASTDataStmtValueNode(ASTDataStmtValueNode node) { traverseChildren(node); }
    public void visitASTDatalistNode(ASTDatalistNode node) { traverseChildren(node); }
    public void visitASTDblConstNode(ASTDblConstNode node) { traverseChildren(node); }
    public void visitASTDeallocateStmtNode(ASTDeallocateStmtNode node) { traverseChildren(node); }
    public void visitASTDeferredCoshapeSpecListNode(ASTDeferredCoshapeSpecListNode node) { traverseChildren(node); }
    public void visitASTDeferredShapeSpecListNode(ASTDeferredShapeSpecListNode node) { traverseChildren(node); }
    public void visitASTDeferredShapeSpecNode(ASTDeferredShapeSpecNode node) { traverseChildren(node); }
    public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node) { traverseChildren(node); }
    public void visitASTDerivedTypeQualifiersNode(ASTDerivedTypeQualifiersNode node) { traverseChildren(node); }
    public void visitASTDerivedTypeSpecNode(ASTDerivedTypeSpecNode node) { traverseChildren(node); }
    public void visitASTDerivedTypeStmtNode(ASTDerivedTypeStmtNode node) { traverseChildren(node); }
    public void visitASTDimensionStmtNode(ASTDimensionStmtNode node) { traverseChildren(node); }
    public void visitASTDoConstructNode(ASTDoConstructNode node) { traverseChildren(node); }
    public void visitASTDummyArgNameNode(ASTDummyArgNameNode node) { traverseChildren(node); }
    public void visitASTEditElementNode(ASTEditElementNode node) { traverseChildren(node); }
    public void visitASTElseConstructNode(ASTElseConstructNode node) { traverseChildren(node); }
    public void visitASTElseIfConstructNode(ASTElseIfConstructNode node) { traverseChildren(node); }
    public void visitASTElseIfStmtNode(ASTElseIfStmtNode node) { traverseChildren(node); }
    public void visitASTElsePartNode(ASTElsePartNode node) { traverseChildren(node); }
    public void visitASTElseStmtNode(ASTElseStmtNode node) { traverseChildren(node); }
    public void visitASTElseWhereConstructNode(ASTElseWhereConstructNode node) { traverseChildren(node); }
    public void visitASTElseWherePartNode(ASTElseWherePartNode node) { traverseChildren(node); }
    public void visitASTElseWhereStmtNode(ASTElseWhereStmtNode node) { traverseChildren(node); }
    public void visitASTEmptyProgramNode(ASTEmptyProgramNode node) { traverseChildren(node); }
    public void visitASTEndAssociateStmtNode(ASTEndAssociateStmtNode node) { traverseChildren(node); }
    public void visitASTEndBlockDataStmtNode(ASTEndBlockDataStmtNode node) { traverseChildren(node); }
    public void visitASTEndBlockStmtNode(ASTEndBlockStmtNode node) { traverseChildren(node); }
    public void visitASTEndCriticalStmtNode(ASTEndCriticalStmtNode node) { traverseChildren(node); }
    public void visitASTEndDoStmtNode(ASTEndDoStmtNode node) { traverseChildren(node); }
    public void visitASTEndEnumStmtNode(ASTEndEnumStmtNode node) { traverseChildren(node); }
    public void visitASTEndForallStmtNode(ASTEndForallStmtNode node) { traverseChildren(node); }
    public void visitASTEndFunctionStmtNode(ASTEndFunctionStmtNode node) { traverseChildren(node); }
    public void visitASTEndIfStmtNode(ASTEndIfStmtNode node) { traverseChildren(node); }
    public void visitASTEndInterfaceStmtNode(ASTEndInterfaceStmtNode node) { traverseChildren(node); }
    public void visitASTEndModuleStmtNode(ASTEndModuleStmtNode node) { traverseChildren(node); }
    public void visitASTEndMpSubprogramStmtNode(ASTEndMpSubprogramStmtNode node) { traverseChildren(node); }
    public void visitASTEndNameNode(ASTEndNameNode node) { traverseChildren(node); }
    public void visitASTEndProgramStmtNode(ASTEndProgramStmtNode node) { traverseChildren(node); }
    public void visitASTEndSelectStmtNode(ASTEndSelectStmtNode node) { traverseChildren(node); }
    public void visitASTEndSelectTypeStmtNode(ASTEndSelectTypeStmtNode node) { traverseChildren(node); }
    public void visitASTEndSubmoduleStmtNode(ASTEndSubmoduleStmtNode node) { traverseChildren(node); }
    public void visitASTEndSubroutineStmtNode(ASTEndSubroutineStmtNode node) { traverseChildren(node); }
    public void visitASTEndTypeStmtNode(ASTEndTypeStmtNode node) { traverseChildren(node); }
    public void visitASTEndWhereStmtNode(ASTEndWhereStmtNode node) { traverseChildren(node); }
    public void visitASTEndfileStmtNode(ASTEndfileStmtNode node) { traverseChildren(node); }
    public void visitASTEntityDeclNode(ASTEntityDeclNode node) { traverseChildren(node); }
    public void visitASTEntryNameNode(ASTEntryNameNode node) { traverseChildren(node); }
    public void visitASTEntryStmtNode(ASTEntryStmtNode node) { traverseChildren(node); }
    public void visitASTEnumDefNode(ASTEnumDefNode node) { traverseChildren(node); }
    public void visitASTEnumDefStmtNode(ASTEnumDefStmtNode node) { traverseChildren(node); }
    public void visitASTEnumeratorDefStmtNode(ASTEnumeratorDefStmtNode node) { traverseChildren(node); }
    public void visitASTEnumeratorNode(ASTEnumeratorNode node) { traverseChildren(node); }
    public void visitASTEquivalenceObjectListNode(ASTEquivalenceObjectListNode node) { traverseChildren(node); }
    public void visitASTEquivalenceObjectNode(ASTEquivalenceObjectNode node) { traverseChildren(node); }
    public void visitASTEquivalenceSetNode(ASTEquivalenceSetNode node) { traverseChildren(node); }
    public void visitASTEquivalenceStmtNode(ASTEquivalenceStmtNode node) { traverseChildren(node); }
    public void visitASTErrorConstructNode(ASTErrorConstructNode node) { traverseChildren(node); }
    public void visitASTErrorProgramUnitNode(ASTErrorProgramUnitNode node) { traverseChildren(node); }
    public void visitASTExecutableProgramNode(ASTExecutableProgramNode node) { traverseChildren(node); }
    public void visitASTExitStmtNode(ASTExitStmtNode node) { traverseChildren(node); }
    public void visitASTExplicitCoshapeSpecNode(ASTExplicitCoshapeSpecNode node) { traverseChildren(node); }
    public void visitASTExplicitShapeSpecNode(ASTExplicitShapeSpecNode node) { traverseChildren(node); }
    public void visitASTExternalNameListNode(ASTExternalNameListNode node) { traverseChildren(node); }
    public void visitASTExternalNameNode(ASTExternalNameNode node) { traverseChildren(node); }
    public void visitASTExternalStmtNode(ASTExternalStmtNode node) { traverseChildren(node); }
    public void visitASTFieldSelectorNode(ASTFieldSelectorNode node) { traverseChildren(node); }
    public void visitASTFinalBindingNode(ASTFinalBindingNode node) { traverseChildren(node); }
    public void visitASTFmtSpecNode(ASTFmtSpecNode node) { traverseChildren(node); }
    public void visitASTForallConstructNode(ASTForallConstructNode node) { traverseChildren(node); }
    public void visitASTForallConstructStmtNode(ASTForallConstructStmtNode node) { traverseChildren(node); }
    public void visitASTForallHeaderNode(ASTForallHeaderNode node) { traverseChildren(node); }
    public void visitASTForallStmtNode(ASTForallStmtNode node) { traverseChildren(node); }
    public void visitASTForallTripletSpecListNode(ASTForallTripletSpecListNode node) { traverseChildren(node); }
    public void visitASTFormatEditNode(ASTFormatEditNode node) { traverseChildren(node); }
    public void visitASTFormatIdentifierNode(ASTFormatIdentifierNode node) { traverseChildren(node); }
    public void visitASTFormatStmtNode(ASTFormatStmtNode node) { traverseChildren(node); }
    public void visitASTFormatsepNode(ASTFormatsepNode node) { traverseChildren(node); }
    public void visitASTFunctionArgListNode(ASTFunctionArgListNode node) { traverseChildren(node); }
    public void visitASTFunctionArgNode(ASTFunctionArgNode node) { traverseChildren(node); }
    public void visitASTFunctionInterfaceRangeNode(ASTFunctionInterfaceRangeNode node) { traverseChildren(node); }
    public void visitASTFunctionNameNode(ASTFunctionNameNode node) { traverseChildren(node); }
    public void visitASTFunctionParNode(ASTFunctionParNode node) { traverseChildren(node); }
    public void visitASTFunctionPrefixNode(ASTFunctionPrefixNode node) { traverseChildren(node); }
    public void visitASTFunctionRangeNode(ASTFunctionRangeNode node) { traverseChildren(node); }
    public void visitASTFunctionReferenceNode(ASTFunctionReferenceNode node) { traverseChildren(node); }
    public void visitASTFunctionStmtNode(ASTFunctionStmtNode node) { traverseChildren(node); }
    public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node) { traverseChildren(node); }
    public void visitASTGenericBindingNode(ASTGenericBindingNode node) { traverseChildren(node); }
    public void visitASTGenericNameNode(ASTGenericNameNode node) { traverseChildren(node); }
    public void visitASTGenericSpecNode(ASTGenericSpecNode node) { traverseChildren(node); }
    public void visitASTGoToKwNode(ASTGoToKwNode node) { traverseChildren(node); }
    public void visitASTGotoStmtNode(ASTGotoStmtNode node) { traverseChildren(node); }
    public void visitASTIfConstructNode(ASTIfConstructNode node) { traverseChildren(node); }
    public void visitASTIfStmtNode(ASTIfStmtNode node) { traverseChildren(node); }
    public void visitASTIfThenStmtNode(ASTIfThenStmtNode node) { traverseChildren(node); }
    public void visitASTImageSelectorNode(ASTImageSelectorNode node) { traverseChildren(node); }
    public void visitASTImageSetNode(ASTImageSetNode node) { traverseChildren(node); }
    public void visitASTImplicitSpecNode(ASTImplicitSpecNode node) { traverseChildren(node); }
    public void visitASTImplicitStmtNode(ASTImplicitStmtNode node) { traverseChildren(node); }
    public void visitASTImpliedDoVariableNode(ASTImpliedDoVariableNode node) { traverseChildren(node); }
    public void visitASTImportStmtNode(ASTImportStmtNode node) { traverseChildren(node); }
    public void visitASTInitializationNode(ASTInitializationNode node) { traverseChildren(node); }
    public void visitASTInputImpliedDoNode(ASTInputImpliedDoNode node) { traverseChildren(node); }
    public void visitASTInquireSpecListNode(ASTInquireSpecListNode node) { traverseChildren(node); }
    public void visitASTInquireSpecNode(ASTInquireSpecNode node) { traverseChildren(node); }
    public void visitASTInquireStmtNode(ASTInquireStmtNode node) { traverseChildren(node); }
    public void visitASTIntConstNode(ASTIntConstNode node) { traverseChildren(node); }
    public void visitASTIntentParListNode(ASTIntentParListNode node) { traverseChildren(node); }
    public void visitASTIntentParNode(ASTIntentParNode node) { traverseChildren(node); }
    public void visitASTIntentSpecNode(ASTIntentSpecNode node) { traverseChildren(node); }
    public void visitASTIntentStmtNode(ASTIntentStmtNode node) { traverseChildren(node); }
    public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node) { traverseChildren(node); }
    public void visitASTInterfaceBodyNode(ASTInterfaceBodyNode node) { traverseChildren(node); }
    public void visitASTInterfaceRangeNode(ASTInterfaceRangeNode node) { traverseChildren(node); }
    public void visitASTInterfaceStmtNode(ASTInterfaceStmtNode node) { traverseChildren(node); }
    public void visitASTIntrinsicListNode(ASTIntrinsicListNode node) { traverseChildren(node); }
    public void visitASTIntrinsicProcedureNameNode(ASTIntrinsicProcedureNameNode node) { traverseChildren(node); }
    public void visitASTIntrinsicStmtNode(ASTIntrinsicStmtNode node) { traverseChildren(node); }
    public void visitASTInvalidEntityDeclNode(ASTInvalidEntityDeclNode node) { traverseChildren(node); }
    public void visitASTIoControlSpecListNode(ASTIoControlSpecListNode node) { traverseChildren(node); }
    public void visitASTIoControlSpecNode(ASTIoControlSpecNode node) { traverseChildren(node); }
    public void visitASTKindParamNode(ASTKindParamNode node) { traverseChildren(node); }
    public void visitASTKindSelectorNode(ASTKindSelectorNode node) { traverseChildren(node); }
    public void visitASTLabelDoStmtNode(ASTLabelDoStmtNode node) { traverseChildren(node); }
    public void visitASTLabelNode(ASTLabelNode node) { traverseChildren(node); }
    public void visitASTLanguageBindingSpecNode(ASTLanguageBindingSpecNode node) { traverseChildren(node); }
    public void visitASTLblDefNode(ASTLblDefNode node) { traverseChildren(node); }
    public void visitASTLblRefListNode(ASTLblRefListNode node) { traverseChildren(node); }
    public void visitASTLblRefNode(ASTLblRefNode node) { traverseChildren(node); }
    public void visitASTLockStmtNode(ASTLockStmtNode node) { traverseChildren(node); }
    public void visitASTLogicalConstNode(ASTLogicalConstNode node) { traverseChildren(node); }
    public void visitASTLoopControlNode(ASTLoopControlNode node) { traverseChildren(node); }
    public void visitASTLowerBoundNode(ASTLowerBoundNode node) { traverseChildren(node); }
    public void visitASTMainProgramNode(ASTMainProgramNode node) { traverseChildren(node); }
    public void visitASTMainRangeNode(ASTMainRangeNode node) { traverseChildren(node); }
    public void visitASTMaskExprNode(ASTMaskExprNode node) { traverseChildren(node); }
    public void visitASTMaskedElseWhereConstructNode(ASTMaskedElseWhereConstructNode node) { traverseChildren(node); }
    public void visitASTMaskedElseWhereStmtNode(ASTMaskedElseWhereStmtNode node) { traverseChildren(node); }
    public void visitASTModuleBlockNode(ASTModuleBlockNode node) { traverseChildren(node); }
    public void visitASTModuleNameNode(ASTModuleNameNode node) { traverseChildren(node); }
    public void visitASTModuleNatureNode(ASTModuleNatureNode node) { traverseChildren(node); }
    public void visitASTModuleNode(ASTModuleNode node) { traverseChildren(node); }
    public void visitASTModuleProcedureStmtNode(ASTModuleProcedureStmtNode node) { traverseChildren(node); }
    public void visitASTModuleStmtNode(ASTModuleStmtNode node) { traverseChildren(node); }
    public void visitASTMpSubprogramRangeNode(ASTMpSubprogramRangeNode node) { traverseChildren(node); }
    public void visitASTMpSubprogramStmtNode(ASTMpSubprogramStmtNode node) { traverseChildren(node); }
    public void visitASTNameNode(ASTNameNode node) { traverseChildren(node); }
    public void visitASTNamedConstantDefNode(ASTNamedConstantDefNode node) { traverseChildren(node); }
    public void visitASTNamedConstantNode(ASTNamedConstantNode node) { traverseChildren(node); }
    public void visitASTNamedConstantUseNode(ASTNamedConstantUseNode node) { traverseChildren(node); }
    public void visitASTNamelistGroupNameNode(ASTNamelistGroupNameNode node) { traverseChildren(node); }
    public void visitASTNamelistGroupObjectNode(ASTNamelistGroupObjectNode node) { traverseChildren(node); }
    public void visitASTNamelistGroupsNode(ASTNamelistGroupsNode node) { traverseChildren(node); }
    public void visitASTNamelistStmtNode(ASTNamelistStmtNode node) { traverseChildren(node); }
    public void visitASTNestedExprNode(ASTNestedExprNode node) { traverseChildren(node); }
    public void visitASTNullifyStmtNode(ASTNullifyStmtNode node) { traverseChildren(node); }
    public void visitASTObjectNameListNode(ASTObjectNameListNode node) { traverseChildren(node); }
    public void visitASTObjectNameNode(ASTObjectNameNode node) { traverseChildren(node); }
    public void visitASTOnlyNode(ASTOnlyNode node) { traverseChildren(node); }
    public void visitASTOpenStmtNode(ASTOpenStmtNode node) { traverseChildren(node); }
    public void visitASTOperatorNode(ASTOperatorNode node) { traverseChildren(node); }
    public void visitASTOptionalParListNode(ASTOptionalParListNode node) { traverseChildren(node); }
    public void visitASTOptionalParNode(ASTOptionalParNode node) { traverseChildren(node); }
    public void visitASTOptionalStmtNode(ASTOptionalStmtNode node) { traverseChildren(node); }
    public void visitASTOutputImpliedDoNode(ASTOutputImpliedDoNode node) { traverseChildren(node); }
    public void visitASTOutputItemList1Node(ASTOutputItemList1Node node) { traverseChildren(node); }
    public void visitASTOutputItemListNode(ASTOutputItemListNode node) { traverseChildren(node); }
    public void visitASTParameterStmtNode(ASTParameterStmtNode node) { traverseChildren(node); }
    public void visitASTParentIdentifierNode(ASTParentIdentifierNode node) { traverseChildren(node); }
    public void visitASTParenthesizedSubroutineArgListNode(ASTParenthesizedSubroutineArgListNode node) { traverseChildren(node); }
    public void visitASTPauseStmtNode(ASTPauseStmtNode node) { traverseChildren(node); }
    public void visitASTPointerFieldNode(ASTPointerFieldNode node) { traverseChildren(node); }
    public void visitASTPointerNameNode(ASTPointerNameNode node) { traverseChildren(node); }
    public void visitASTPointerObjectNode(ASTPointerObjectNode node) { traverseChildren(node); }
    public void visitASTPointerStmtNode(ASTPointerStmtNode node) { traverseChildren(node); }
    public void visitASTPointerStmtObjectNode(ASTPointerStmtObjectNode node) { traverseChildren(node); }
    public void visitASTPositionSpecListNode(ASTPositionSpecListNode node) { traverseChildren(node); }
    public void visitASTPositionSpecNode(ASTPositionSpecNode node) { traverseChildren(node); }
    public void visitASTPrefixSpecNode(ASTPrefixSpecNode node) { traverseChildren(node); }
    public void visitASTPrintStmtNode(ASTPrintStmtNode node) { traverseChildren(node); }
    public void visitASTPrivateSequenceStmtNode(ASTPrivateSequenceStmtNode node) { traverseChildren(node); }
    public void visitASTProcComponentAttrSpecNode(ASTProcComponentAttrSpecNode node) { traverseChildren(node); }
    public void visitASTProcComponentDefStmtNode(ASTProcComponentDefStmtNode node) { traverseChildren(node); }
    public void visitASTProcDeclNode(ASTProcDeclNode node) { traverseChildren(node); }
    public void visitASTProcInterfaceNode(ASTProcInterfaceNode node) { traverseChildren(node); }
    public void visitASTProcedureDeclarationStmtNode(ASTProcedureDeclarationStmtNode node) { traverseChildren(node); }
    public void visitASTProcedureNameListNode(ASTProcedureNameListNode node) { traverseChildren(node); }
    public void visitASTProcedureNameNode(ASTProcedureNameNode node) { traverseChildren(node); }
    public void visitASTProgramNameNode(ASTProgramNameNode node) { traverseChildren(node); }
    public void visitASTProgramStmtNode(ASTProgramStmtNode node) { traverseChildren(node); }
    public void visitASTProtectedStmtNode(ASTProtectedStmtNode node) { traverseChildren(node); }
    public void visitASTRdCtlSpecNode(ASTRdCtlSpecNode node) { traverseChildren(node); }
    public void visitASTRdFmtIdExprNode(ASTRdFmtIdExprNode node) { traverseChildren(node); }
    public void visitASTRdFmtIdNode(ASTRdFmtIdNode node) { traverseChildren(node); }
    public void visitASTRdIoCtlSpecListNode(ASTRdIoCtlSpecListNode node) { traverseChildren(node); }
    public void visitASTRdUnitIdNode(ASTRdUnitIdNode node) { traverseChildren(node); }
    public void visitASTReadStmtNode(ASTReadStmtNode node) { traverseChildren(node); }
    public void visitASTRealConstNode(ASTRealConstNode node) { traverseChildren(node); }
    public void visitASTRenameNode(ASTRenameNode node) { traverseChildren(node); }
    public void visitASTReturnStmtNode(ASTReturnStmtNode node) { traverseChildren(node); }
    public void visitASTRewindStmtNode(ASTRewindStmtNode node) { traverseChildren(node); }
    public void visitASTSFDataRefNode(ASTSFDataRefNode node) { traverseChildren(node); }
    public void visitASTSFDummyArgNameListNode(ASTSFDummyArgNameListNode node) { traverseChildren(node); }
    public void visitASTSFDummyArgNameNode(ASTSFDummyArgNameNode node) { traverseChildren(node); }
    public void visitASTSFExprListNode(ASTSFExprListNode node) { traverseChildren(node); }
    public void visitASTSFExprNode(ASTSFExprNode node) { traverseChildren(node); }
    public void visitASTSFFactorNode(ASTSFFactorNode node) { traverseChildren(node); }
    public void visitASTSFPrimaryNode(ASTSFPrimaryNode node) { traverseChildren(node); }
    public void visitASTSFTermNode(ASTSFTermNode node) { traverseChildren(node); }
    public void visitASTSFVarNameNode(ASTSFVarNameNode node) { traverseChildren(node); }
    public void visitASTSaveStmtNode(ASTSaveStmtNode node) { traverseChildren(node); }
    public void visitASTSavedCommonBlockNode(ASTSavedCommonBlockNode node) { traverseChildren(node); }
    public void visitASTSavedEntityNode(ASTSavedEntityNode node) { traverseChildren(node); }
    public void visitASTScalarMaskExprNode(ASTScalarMaskExprNode node) { traverseChildren(node); }
    public void visitASTScalarVariableNode(ASTScalarVariableNode node) { traverseChildren(node); }
    public void visitASTSectionSubscriptNode(ASTSectionSubscriptNode node) { traverseChildren(node); }
    public void visitASTSelectCaseRangeNode(ASTSelectCaseRangeNode node) { traverseChildren(node); }
    public void visitASTSelectCaseStmtNode(ASTSelectCaseStmtNode node) { traverseChildren(node); }
    public void visitASTSelectTypeBodyNode(ASTSelectTypeBodyNode node) { traverseChildren(node); }
    public void visitASTSelectTypeConstructNode(ASTSelectTypeConstructNode node) { traverseChildren(node); }
    public void visitASTSelectTypeStmtNode(ASTSelectTypeStmtNode node) { traverseChildren(node); }
    public void visitASTSeparateModuleSubprogramNode(ASTSeparateModuleSubprogramNode node) { traverseChildren(node); }
    public void visitASTSignNode(ASTSignNode node) { traverseChildren(node); }
    public void visitASTSpecificBindingNode(ASTSpecificBindingNode node) { traverseChildren(node); }
    public void visitASTStmtFunctionRangeNode(ASTStmtFunctionRangeNode node) { traverseChildren(node); }
    public void visitASTStmtFunctionStmtNode(ASTStmtFunctionStmtNode node) { traverseChildren(node); }
    public void visitASTStopStmtNode(ASTStopStmtNode node) { traverseChildren(node); }
    public void visitASTStringConstNode(ASTStringConstNode node) { traverseChildren(node); }
    public void visitASTStructureComponentNode(ASTStructureComponentNode node) { traverseChildren(node); }
    public void visitASTStructureConstructorNode(ASTStructureConstructorNode node) { traverseChildren(node); }
    public void visitASTSubmoduleBlockNode(ASTSubmoduleBlockNode node) { traverseChildren(node); }
    public void visitASTSubmoduleNode(ASTSubmoduleNode node) { traverseChildren(node); }
    public void visitASTSubmoduleStmtNode(ASTSubmoduleStmtNode node) { traverseChildren(node); }
    public void visitASTSubroutineArgNode(ASTSubroutineArgNode node) { traverseChildren(node); }
    public void visitASTSubroutineInterfaceRangeNode(ASTSubroutineInterfaceRangeNode node) { traverseChildren(node); }
    public void visitASTSubroutineNameNode(ASTSubroutineNameNode node) { traverseChildren(node); }
    public void visitASTSubroutineNameUseNode(ASTSubroutineNameUseNode node) { traverseChildren(node); }
    public void visitASTSubroutineParNode(ASTSubroutineParNode node) { traverseChildren(node); }
    public void visitASTSubroutinePrefixNode(ASTSubroutinePrefixNode node) { traverseChildren(node); }
    public void visitASTSubroutineRangeNode(ASTSubroutineRangeNode node) { traverseChildren(node); }
    public void visitASTSubroutineStmtNode(ASTSubroutineStmtNode node) { traverseChildren(node); }
    public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node) { traverseChildren(node); }
    public void visitASTSubscriptNode(ASTSubscriptNode node) { traverseChildren(node); }
    public void visitASTSubscriptTripletNode(ASTSubscriptTripletNode node) { traverseChildren(node); }
    public void visitASTSubstrConstNode(ASTSubstrConstNode node) { traverseChildren(node); }
    public void visitASTSubstringRangeNode(ASTSubstringRangeNode node) { traverseChildren(node); }
    public void visitASTSyncAllStmtNode(ASTSyncAllStmtNode node) { traverseChildren(node); }
    public void visitASTSyncImagesStmtNode(ASTSyncImagesStmtNode node) { traverseChildren(node); }
    public void visitASTSyncMemoryStmtNode(ASTSyncMemoryStmtNode node) { traverseChildren(node); }
    public void visitASTSyncStatNode(ASTSyncStatNode node) { traverseChildren(node); }
    public void visitASTTargetNameNode(ASTTargetNameNode node) { traverseChildren(node); }
    public void visitASTTargetNode(ASTTargetNode node) { traverseChildren(node); }
    public void visitASTTargetObjectNode(ASTTargetObjectNode node) { traverseChildren(node); }
    public void visitASTTargetStmtNode(ASTTargetStmtNode node) { traverseChildren(node); }
    public void visitASTThenPartNode(ASTThenPartNode node) { traverseChildren(node); }
    public void visitASTTypeAttrSpecNode(ASTTypeAttrSpecNode node) { traverseChildren(node); }
    public void visitASTTypeBoundProcedurePartNode(ASTTypeBoundProcedurePartNode node) { traverseChildren(node); }
    public void visitASTTypeDeclarationStmtNode(ASTTypeDeclarationStmtNode node) { traverseChildren(node); }
    public void visitASTTypeGuardStmtNode(ASTTypeGuardStmtNode node) { traverseChildren(node); }
    public void visitASTTypeNameNode(ASTTypeNameNode node) { traverseChildren(node); }
    public void visitASTTypeParamAttrSpecNode(ASTTypeParamAttrSpecNode node) { traverseChildren(node); }
    public void visitASTTypeParamDeclNode(ASTTypeParamDeclNode node) { traverseChildren(node); }
    public void visitASTTypeParamDefStmtNode(ASTTypeParamDefStmtNode node) { traverseChildren(node); }
    public void visitASTTypeParamNameNode(ASTTypeParamNameNode node) { traverseChildren(node); }
    public void visitASTTypeParamSpecNode(ASTTypeParamSpecNode node) { traverseChildren(node); }
    public void visitASTTypeParamValueNode(ASTTypeParamValueNode node) { traverseChildren(node); }
    public void visitASTTypeSpecNode(ASTTypeSpecNode node) { traverseChildren(node); }
    public void visitASTUFExprNode(ASTUFExprNode node) { traverseChildren(node); }
    public void visitASTUFFactorNode(ASTUFFactorNode node) { traverseChildren(node); }
    public void visitASTUFPrimaryNode(ASTUFPrimaryNode node) { traverseChildren(node); }
    public void visitASTUFTermNode(ASTUFTermNode node) { traverseChildren(node); }
    public void visitASTUnaryExprNode(ASTUnaryExprNode node) { traverseChildren(node); }
    public void visitASTUnitIdentifierNode(ASTUnitIdentifierNode node) { traverseChildren(node); }
    public void visitASTUnlockStmtNode(ASTUnlockStmtNode node) { traverseChildren(node); }
    public void visitASTUnprocessedIncludeStmtNode(ASTUnprocessedIncludeStmtNode node) { traverseChildren(node); }
    public void visitASTUpperBoundNode(ASTUpperBoundNode node) { traverseChildren(node); }
    public void visitASTUseNameNode(ASTUseNameNode node) { traverseChildren(node); }
    public void visitASTUseStmtNode(ASTUseStmtNode node) { traverseChildren(node); }
    public void visitASTValueStmtNode(ASTValueStmtNode node) { traverseChildren(node); }
    public void visitASTVarOrFnRefNode(ASTVarOrFnRefNode node) { traverseChildren(node); }
    public void visitASTVariableCommaNode(ASTVariableCommaNode node) { traverseChildren(node); }
    public void visitASTVariableNameNode(ASTVariableNameNode node) { traverseChildren(node); }
    public void visitASTVariableNode(ASTVariableNode node) { traverseChildren(node); }
    public void visitASTVolatileStmtNode(ASTVolatileStmtNode node) { traverseChildren(node); }
    public void visitASTWaitSpecNode(ASTWaitSpecNode node) { traverseChildren(node); }
    public void visitASTWaitStmtNode(ASTWaitStmtNode node) { traverseChildren(node); }
    public void visitASTWhereConstructNode(ASTWhereConstructNode node) { traverseChildren(node); }
    public void visitASTWhereConstructStmtNode(ASTWhereConstructStmtNode node) { traverseChildren(node); }
    public void visitASTWhereRangeNode(ASTWhereRangeNode node) { traverseChildren(node); }
    public void visitASTWhereStmtNode(ASTWhereStmtNode node) { traverseChildren(node); }
    public void visitASTWriteStmtNode(ASTWriteStmtNode node) { traverseChildren(node); }
    public void visitIAccessId(IAccessId node) {}
    public void visitIActionStmt(IActionStmt node) {}
    public void visitIBindEntity(IBindEntity node) {}
    public void visitIBlockDataBodyConstruct(IBlockDataBodyConstruct node) {}
    public void visitIBodyConstruct(IBodyConstruct node) {}
    public void visitICaseBodyConstruct(ICaseBodyConstruct node) {}
    public void visitIComponentDefStmt(IComponentDefStmt node) {}
    public void visitIDataIDoObject(IDataIDoObject node) {}
    public void visitIDataStmtObject(IDataStmtObject node) {}
    public void visitIDeclarationConstruct(IDeclarationConstruct node) {}
    public void visitIDefinedOperator(IDefinedOperator node) {}
    public void visitIDerivedTypeBodyConstruct(IDerivedTypeBodyConstruct node) {}
    public void visitIExecutableConstruct(IExecutableConstruct node) {}
    public void visitIExecutionPartConstruct(IExecutionPartConstruct node) {}
    public void visitIExpr(IExpr node) {}
    public void visitIForallBodyConstruct(IForallBodyConstruct node) {}
    public void visitIInputItem(IInputItem node) {}
    public void visitIInterfaceSpecification(IInterfaceSpecification node) {}
    public void visitIInternalSubprogram(IInternalSubprogram node) {}
    public void visitIModuleBodyConstruct(IModuleBodyConstruct node) {}
    public void visitIModuleSubprogram(IModuleSubprogram node) {}
    public void visitIModuleSubprogramPartConstruct(IModuleSubprogramPartConstruct node) {}
    public void visitIObsoleteActionStmt(IObsoleteActionStmt node) {}
    public void visitIObsoleteExecutionPartConstruct(IObsoleteExecutionPartConstruct node) {}
    public void visitIProcBindingStmt(IProcBindingStmt node) {}
    public void visitIProgramUnit(IProgramUnit node) {}
    public void visitISelector(ISelector node) {}
    public void visitISpecificationPartConstruct(ISpecificationPartConstruct node) {}
    public void visitISpecificationStmt(ISpecificationStmt node) {}
    public void visitIUnsignedArithmeticConst(IUnsignedArithmeticConst node) {}
    public void visitIWhereBodyConstruct(IWhereBodyConstruct node) {}
}
