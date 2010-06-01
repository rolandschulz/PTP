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
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
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

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
