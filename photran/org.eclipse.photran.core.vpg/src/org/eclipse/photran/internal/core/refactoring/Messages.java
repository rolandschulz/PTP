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

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.core.refactoring.messages"; //$NON-NLS-1$

    public static String AddOnlyToUseStmtRefactoring_AddingWouldChangeMeaningOf;

    public static String AddOnlyToUseStmtRefactoring_Analyzing;

    public static String AddOnlyToUseStmtRefactoring_CheckingForConflicts;

    public static String AddOnlyToUseStmtRefactoring_CreatingChangeObject;

    public static String AddOnlyToUseStmtRefactoring_FindingReferences;

    public static String AddOnlyToUseStmtRefactoring_InsertingUseStmt;

    public static String AddOnlyToUseStmtRefactoring_ModuleNameInUseStmtNotSelected;

    public static String AddOnlyToUseStmtRefactoring_ModuleNodeNodeFound;

    public static String AddOnlyToUseStmtRefactoring_ModuleTokenNotFound;

    public static String AddOnlyToUseStmtRefactoring_MultipleDefinitionsOfModule;

    public static String AddOnlyToUseStmtRefactoring_Name;

    public static String AddOnlyToUseStmtRefactoring_NameConflicts;

    public static String AddOnlyToUseStmtRefactoring_NameMightConflict;

    public static String AddOnlyToUseStmtRefactoring_NoDeclarationsInModule;

    public static String AddOnlyToUseStmtRefactoring_NoFilesContainModule;

    public static String AddOnlyToUseStmtRefactoring_NoModuleNamed;

    public static String AddOnlyToUseStmtRefactoring_NoModuleNameSelected;

    public static String AddOnlyToUseStmtRefactoring_Parsing;

    public static String AddOnlyToUseStmtRefactoring_PleaseSelectModuleName;

    public static String AddOnlyToUseStmtRefactoring_ProjectDoesNotExist;

    public static String AddOnlyToUseStmtRefactoring_SelectModuleName;

    public static String AddUseOfNamedEntitiesToModule_refactoringName;
    public static String EncapsulateVariableRefactoring_CannotEncapsulateArrays;

    public static String EncapsulateVariableRefactoring_CannotEncapsulatePARAMETER;

    public static String EncapsulateVariableRefactoring_CannotEncapsulatePointers;

    public static String EncapsulateVariableRefactoring_CannotEncapsulateTARGET;

    public static String EncapsulateVariableRefactoring_CannotRefactorFixedFormFile;

    public static String EncapsulateVariableRefactoring_CouldNotFindToken;

    public static String EncapsulateVariableRefactoring_CouldNotFindTokenForVarDef;

    public static String EncapsulateVariableRefactoring_CouldNotFindTokenForVarRef;

    public static String EncapsulateVariableRefactoring_Name;

    public static String EncapsulateVariableRefactoring_NameConflicts;

    public static String EncapsulateVariableRefactoring_NameMightConflict;

    public static String EncapsulateVariableRefactoring_NotAnExpression;

    public static String EncapsulateVariableRefactoring_NoUniqueDefinition;

    public static String EncapsulateVariableRefactoring_PleaseSelectAnIdentifier;

    public static String EncapsulateVariableRefactoring_VariableNotSelected;

    public static String EncapsulateVariableRefactoring_WarningFunctionArgument;

    public static String EncapsulateVariableRefactoring_WarningWillNotChangeReference;
    public static String ExtractProcedureRefactoring_PleaseSelectContiguousStatements;

    public static String ExtractProcedureRefactoring_CanOnlyExtractFromSubprogramOrMainProgram;

    public static String ExtractProcedureRefactoring_ExtractionWouldRequirePointerParameter;

    public static String ExtractProcedureRefactoring_InvalidIdentifier;

    public static String ExtractProcedureRefactoring_Name;

    public static String ExtractProcedureRefactoring_NameConflicts;

    public static String ExtractProcedureRefactoring_OnlyExecutableStatementsCanBeExtracted;

    public static String ExtractProcedureRefactoring_ProcedureContainsLabels;

    public static String ExtractProcedureRefactoring_StatementCannotBeExtracted;

    public static String MinOnlyListRefactoring_ModuleIsEmpty;

    public static String MinOnlyListRefactoring_ModuleNodeNotFound;

    public static String MinOnlyListRefactoring_ModuleNotFoundWithName;

    public static String MinOnlyListRefactoring_ModuleTokenNotFound;

    public static String MinOnlyListRefactoring_Name;

    public static String MinOnlyListRefactoring_NoFilesContainModuleNamed;

    public static String MinOnlyListRefactoring_NoModuleNameSelected;

    public static String MinOnlyListRefactoring_PleaseSelectModuleName;

    public static String MinOnlyListRefactoring_PleaseSelectModuleNameInUSEStatement;

    public static String MinOnlyListRefactoring_ProjectDoesNotExist;

    public static String MinOnlyListRefactoring_USEStatementNotFound;

    public static String MoveSavedToCommonBlockRefactoring_AbsentOrAmbiguousDefinition;

    public static String MoveSavedToCommonBlockRefactoring_CouldNotFindArrayDeclaration;

    public static String MoveSavedToCommonBlockRefactoring_CouldNotFindDeclarationNode;

    public static String MoveSavedToCommonBlockRefactoring_CouldNotFindTypeSpecificationNode;

    public static String MoveSavedToCommonBlockRefactoring_Name;

    public static String MoveSavedToCommonBlockRefactoring_OnlyInternalSubprogramsSupported;

    public static String MoveSavedToCommonBlockRefactoring_PleaseSelectSubprogram;

    public static String MoveSavedToCommonBlockRefactoring_PleaseSelectSubprogramNotInINTERFACE;

    public static String RenameRefactoring_CannotRename;

    public static String RenameRefactoring_CannotRenameAmbiguous;

    public static String RenameRefactoring_CannotRenameInINCLUDEFile;

    public static String RenameRefactoring_CannotRenameIntrinsicProcedure;

    public static String RenameRefactoring_CannotRenameSubprogramArgs;

    public static String RenameRefactoring_CannotRenameTypeBoundProcedures;

    public static String RenameRefactoring_CannotRenameUsedInINCLUDEFile;

    public static String RenameRefactoring_ChangingNameWouldChangeMeaning;

    public static String RenameRefactoring_StatusCheckingIfReferencesInFileCanBeRenamed;

    public static String RenameRefactoring_CPreprocessedFileWillNotBeRefactored;

    public static String RenameRefactoring_FixedFormFileWillNotBeRefactored;

    public static String RenameRefactoring_InvalidIdentifier;

    public static String RenameRefactoring_MultipleDeclarationsFoundFor;

    public static String RenameRefactoring_Name;

    public static String RenameRefactoring_NameConflicts;

    public static String RenameRefactoring_NameMightConflict;

    public static String RenameRefactoring_NewNameIsExactlyTheSame;

    public static String RenameRefactoring_NoDeclarationFoundFor;

    public static String RenameRefactoring_PleaseSelectAnIdentifier;

    public static String RenameRefactoring_StatusModifyingFile;

    public static String RenameRefactoring_StatusRenaming;

    public static String PermuteSubroutineArgsRefactoring_matchingDeclarationsDoNotUniquelyBind;

    public static String PermuteSubroutineArgsRefactoring_name;

    public static String PermuteSubroutineArgsRefactoring_selectedTextNotSubroutine;

    public static String PermuteSubroutineArgsRefactoring_selectSubroutineError;

    public static String PermuteSubroutineArgsRefactoring_subroutineParameterDefinitionError;

    public static String CommonVarNamesRefactoring_Name;

    public static String CommonVarNamesRefactoring_NameConflictsWith;

    public static String CommonVarNamesRefactoring_NameMightConflictWithSubprogram;

    public static String CommonVarNamesRefactoring_NoCommonBlockFoundWithName;

    public static String CommonVarNamesRefactoring_NoFilesFoundContainingCommonBlock;

    public static String CommonVarNamesRefactoring_ProjectDoesNotExist;

    public static String CommonVarNamesRefactoring_SelectCommonBlockName;

    public static String CommonVarNamesRefactoring_VariableTypesDiffer;
    public static String DataToParameterRefactoring_AmbiguouslyDefined;

    public static String DataToParameterRefactoring_ArraysNotSupported;

    public static String DataToParameterRefactoring_DefinitionNotFound;

    public static String DataToParameterRefactoring_EmptyDataListInNode;

    public static String DataToParameterRefactoring_ImpliedDoNotSupported;

    public static String DataToParameterRefactoring_Name;

    public static String DataToParameterRefactoring_PointersNotSupported;

    public static String DataToParameterRefactoring_RefactorNotConsideringVarAssignment;

    public static String DataToParameterRefactoring_SelectedFileCannotBeParsed;
    public static String ExtractLocalVariableRefactoring_DeclarationDoesNotDeclareSingleVar;

    public static String ExtractLocalVariableRefactoring_DeclarationMustNotContainInitialization;

    public static String ExtractLocalVariableRefactoring_EnterDeclarationForExtractedVar;

    public static String ExtractLocalVariableRefactoring_ErrorSelectingPartOfExpression;

    public static String ExtractLocalVariableRefactoring_ExpressionNotInExtractableStmt;

    public static String ExtractLocalVariableRefactoring_ExpressionTypeNotBeAutoDetermined;

    public static String ExtractLocalVariableRefactoring_ExtractionMayNotPreserveBehavior;

    public static String ExtractLocalVariableRefactoring_InvalidTypeDeclStmt;

    public static String ExtractLocalVariableRefactoring_Name;

    public static String ExtractLocalVariableRefactoring_NameConflictsWith;

    public static String ExtractLocalVariableRefactoring_NameMightConflictWithSubprogram;

    public static String ExtractLocalVariableRefactoring_SelectExpressionToExtract;

    public static String ExtractLocalVariableRefactoring_VarsExtractedOnlyFromActionStmt;

    public static String ExtractLocalVariableRefactoring_VarsOnlyExtractedFromStmtsIn;

    public static String InterchangeLoopsRefactoring_Name;

    public static String InterchangeLoopsRefactoring_SelectTwoPerfNextedLoops;

    public static String InterchangeLoopsRefactoring_UncheckedTransNotGuaranteedToPreserve;

    public static String IntroImplicitNoneRefactoring_Name;

    public static String IntroImplicitNoneRefactoring_SelectedFileCannotBeParsed;

    public static String KeywordCaseRefactoring_Name;

    public static String KeywordCaseRefactoring_SelectedFileCannotBeParsed;

    public static String LoopAlignmentRefactoring_LoopAlignmentName;

    public static String LoopAlignmentRefactoring_LoopsNotCompatible;

    public static String FuseLoopsRefactoring_CycleExitFails;

    public static String FuseLoopsRefactoring_IncompatibleLoopErorrMessage;

    public static String FuseLoopsRefactoring_InvalidLoopBounds;

    public static String FuseLoopsRefactoring_InvalidStepError;

    public static String FuseLoopsRefactoring_LoopFusionName;

    public static String FuseLoopsRefactoring_NoSecondLoopErrorMsg;

    public static String FuseLoopsRefactoring_SelectLoopsWithoutLabels;

    public static String FuseLoopsRefactoring_SelectLoopWithIntegers;

    public static String TileLoopRefactoring_CantTileLoopsWithStep;

    public static String TileLoopRefactoring_InvalidTileSize;

    public static String TileLoopRefactoring_InvalidTilingOffset;

    public static String TileLoopRefactoring_LoopTilingName;

    public static String TileLoopRefactoring_SelectLoopWithOnlyOneNestedLoop;

    public static String TileLoopRefactoring_UnableToCreateNewIndex;

    public static String UnrollLoopRefactoring_cannotUnrollLoopWithLabel;

    public static String UnrollLoopRefactoring_InvalidStepError;

    public static String UnrollLoopRefactoring_LoopUnrollingName;

    public static String UnrollLoopRefactoring_LoopWritesToIndexVariable;

    public static String UnrollLoopRefactoring_SelectLoopWithExplicitBound;

    public static String UnrollLoopRefactoring_unableToCreateUpperBound;

    public static String UnrollLoopRefactoring_UnableToReplaceIndexVariable;
    public static String MakePrivateEntityPublicRefactoring_DoesNotSupportExternalEntities;

    public static String MakePrivateEntityPublicRefactoring_DoesNotSupportInterfaceDeclarations;

    public static String MakePrivateEntityPublicRefactoring_DoesNotSupportIntrinsicEntities;

    public static String MakePrivateEntityPublicRefactoring_HighlightPrivateEntityName;

    public static String MakePrivateEntityPublicRefactoring_Name;

    public static String MakePrivateEntityPublicRefactoring_NoPrivateEntitySelected;

    public static String MakePrivateEntityPublicRefactoring_PublicEntitySelectedSelectPrivate;

    public static String MakePrivateEntityPublicRefactoring_SelectPrivateEntityName;
    
    public static String MoveCommonToModuleRefactoring_InvalidIdentifier;

    public static String MoveCommonToModuleRefactoring_Name;

    public static String MoveCommonToModuleRefactoring_SelectVarOrBlockInCommonStmt;

    public static String MoveFromModuleRefactoring_Name;

    public static String MoveFromModuleRefactoring_selectionNotInsideModuleError;

    public static String MoveFromModuleRefactoring_textSelectionError;
    
    public static String RemoveArithmeticIfRefactoring_Error;

    public static String RemoveArithmeticIfRefactoring_Name;

    public static String RemoveComputedGoToRefactoring_Name;

    public static String RemoveComputedGoToRefactoring_PleaseSelectComputedGotoStmt;

    public static String RemoveUnusedVariablesRefactoring_CouldNotCompleteOperation;

    public static String RemoveUnusedVariablesRefactoring_DoesNotRemovedUnusedVarsWithDefsOnAnotherLine;

    public static String RemoveUnusedVariablesRefactoring_Name;

    public static String RemoveUnusedVariablesRefactoring_RefactorAgainToRemoveAllUnusedVars;

    public static String RemoveUnusedVariablesRefactoring_Scope;

    public static String RemoveUnusedVariablesRefactoring_SelectedFileCannotBeParsed;

    public static String RemoveUnusedVariablesRefactoring_SelectedFilesCannotBeParsed;

    public static String RemoveUnusedVariablesRefactoring_SelectedFilesMustBeImplicitNone;

    public static String RemoveUnusedVariablesRefactoring_UnusedVarsRemovedFromFile;

    public static String RemoveUnusedVariablesRefactoring_VariableUnusedAndWillBeRemoved;

    public static String ReplaceCharacterToCharacterLenRefactoring_CharacterStarDeclNotSelected;

    public static String ReplaceCharacterToCharacterLenRefactoring_Name;

    public static String ReplaceOldStyleDoLoopRefactoring_AmbiguousLabel;

    public static String ReplaceOldStyleDoLoopRefactoring_EndOfLoopError;

    public static String ReplaceOldStyleDoLoopRefactoring_MissingLabel;

    public static String ReplaceOldStyleDoLoopRefactoring_Name;

    public static String ReplaceOldStyleDoLoopRefactoring_ThereMustBeAtLeastOneOldStyleDoLoop;

    public static String RepObsOpersRefactoring_Name;

    public static String RepObsOpersRefactoring_SelectedFileCannotBeParsed;

    public static String ReverseLoopRefactoring_Name;

    public static String ReverseLoopRefactoring_SelectDoLoop;

    public static String SafeDeleteInternalSubprogramRefactoring_MultipleDefinitions;

    public static String SafeDeleteInternalSubprogramRefactoring_NoDefinition;

    public static String SafeDeleteInternalSubprogramRefactoring_NoSubroutineSelected;

    public static String SafeDeleteInternalSubprogramRefactoring_NotAnInternalSubprogram;

    public static String SafeDeleteInternalSubprogramRefactoring_SubroutineMustHaveOnlyInternalReferences;

    public static String StandardizeStatementsRefactoring_Name;

    public static String StandardizeStatementsRefactoring_SelectedFileCannotBeParsed;
    
    public static String RemoveUnreferencedLabelsRefactoring_Name;
    
    public static String RemoveUnreferencedLabelsRefactoring_ThereMustBeAtLeastOneLabeledStatement;
    
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
