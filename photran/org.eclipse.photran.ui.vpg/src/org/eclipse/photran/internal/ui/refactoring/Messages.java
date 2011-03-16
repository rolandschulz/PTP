/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *    Rita Chow - Photran Modifications
 *    Nicola Hall - Photran Modifications
 *    Jerry Hsiao - Photran Modifications
 *    Mark Mozolewski - Photran Modifications
 *    Chamil Wijenayaka - Photran Modifications
 *******************************************************************************/
package org.eclipse.photran.internal.ui.refactoring;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.ui.refactoring.messages"; //$NON-NLS-1$

    public static String AbstractFortranRefactoringActionDelegate_ErrorTitle;

    public static String AbstractFortranRefactoringActionDelegate_FileInEditorCannotBeRefactored;

    public static String AddOnlyToUseStmtInputPage_ClickOKMessage;

    public static String AddOnlyToUseStmtInputPage_SelectModuleEntitiesLabel;

    public static String PermuteSubroutineArgsInputPage_alternateReturnColumnLabel;

    public static String PermuteSubroutineArgsInputPage_alternateReturnErrorLabel;

    public static String PermuteSubroutineArgsInputPage_downButtonLabel;

    public static String PermuteSubroutineArgsInputPage_intentInLabel;

    public static String PermuteSubroutineArgsInputPage_intentLabel;

    public static String PermuteSubroutineArgsInputPage_intentOutLabel;

    public static String PermuteSubroutineArgsInputPage_keywordedColumnLabel;

    public static String PermuteSubroutineArgsInputPage_keywordErrorLabel;

    public static String PermuteSubroutineArgsInputPage_nameLabel;

    public static String PermuteSubroutineArgsInputPage_notEnoughArgumentsErrorLabel;

    public static String PermuteSubroutineArgsInputPage_optionalArgumentErrorLabel;

    public static String PermuteSubroutineArgsInputPage_optionalColumnLabel;

    public static String PermuteSubroutineArgsInputPage_parameterGroupLabel;

    public static String PermuteSubroutineArgsInputPage_typeLabel;

    public static String PermuteSubroutineArgsInputPage_upButtonLabel;

    public static String CommonVarNamesInputPage_NewNameLabel;

    public static String CommonVarNamesInputPage_OriginalNameLabel;

    public static String ExtractLocalVariableAction_DeclarationLabel;

    public static String ExtractProcedureAction_SubroutineNameLabel;

    public static String KeywordCaseInputPage_ChangeKeywordsToLabel;

    public static String KeywordCaseInputPage_ClickOKMessage;

    public static String KeywordCaseInputPage_LowerCaseLabel;

    public static String KeywordCaseInputPage_UpperCaseLabel;

    public static String MoveFromModuleInputPage_leftPanelMessage;

    public static String MoveFromModuleInputPage_Name;

    public static String MoveFromModuleInputPage_rightPanelMessage;

    public static String MoveFromModuleInputPage_selectDataMessage;

    public static String RenameAction_MatchExternalSubprograms;

    public static String RenameAction_RenameAtoB;
    
    public static String RemoveRealAndDoublePrecisionLoopCountersInputPage_ReplaceRealDoublePrecisionLoopCounter;

    public static String RemoveRealAndDoublePrecisionLoopCountersInputPage_ReplaceWithDoLoop;
    
    public static String RemoveRealAndDoublePrecisionLoopCountersInputPage_ReplaceWithDoWhileLoop;
    
    public static String RemoveRealAndDoublePrecisionLoopCountersInputPage_ClickOKMessage;
    
    public static String RemoveRealAndDoublePrecisionLoopCountersInputPage_ClickPreviewMessage;
    
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
