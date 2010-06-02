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

    public static String CommonVarNamesInputPage_NewNameLabel;

    public static String CommonVarNamesInputPage_OriginalNameLabel;

    public static String ExtractLocalVariableAction_DeclarationLabel;

    public static String ExtractProcedureAction_SubroutineNameLabel;

    public static String KeywordCaseInputPage_ChangeKeywordsToLabel;

    public static String KeywordCaseInputPage_ClickOKMessage;

    public static String KeywordCaseInputPage_LowerCaseLabel;

    public static String KeywordCaseInputPage_UpperCaseLabel;

    public static String RenameAction_MatchExternalSubprograms;

    public static String RenameAction_RenameAtoB;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
