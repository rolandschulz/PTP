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
package org.eclipse.photran.internal.ui.actions;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.ui.actions.messages"; //$NON-NLS-1$

    public static String DisplaySymbolTable_WaitingForBackgroundWorkToComplete;

    public static String FindAllDeclarationsInScope_ErrorTitle;

    public static String FindAllDeclarationsInScope_NoEnclosingScope;

    public static String FindAllDeclarationsInScope_PleaseSelectAToken;

    public static String FindAllDeclarationsInScope_WaitingForBackgroundWorkToComplete;

    public static String FindMatchingInterfaceDeclarations_ErrorTitle;

    public static String FindMatchingInterfaceDeclarations_PleaseSelectAToken;

    public static String FindMatchingInterfaceDeclarations_PleaseSelectIdentifierInASubprogram;

    public static String FindMatchingInterfaceDeclarations_WaitingForBackgroundWorkToComplete;

    public static String FortranEditorASTActionDelegate_DeclDescriptionOther;

    public static String FortranEditorASTActionDelegate_DeclDescriptionSubprogramArgument;

    public static String FortranEditorASTActionDelegate_MultipleDeclarationsFoundTitle;

    public static String FortranEditorASTActionDelegate_SelectADeclarationToOpen;

    public static String FortranEditorASTActionDelegate_UnableToParseFileInEditor;

    public static String FortranEditorASTActionDelegate_UnhandledExceptionTitle;

    public static String OpenDeclaration_AnalysisRefactoringNotEnabled;

    public static String OpenDeclaration_ErrorTitle;

    public static String OpenDeclaration_ErrorX;

    public static String OpenDeclaration_UnableToCreateMarker;

    public static String OpenDeclaration_UnableToLocateDeclaration;

    public static String ResolveInterfaceBinding_ErrorTitle;

    public static String ResolveInterfaceBinding_PleaseSelectAToken;

    public static String ResolveInterfaceBinding_PleaseSelectIdentifierInASubprogram;

    public static String ResolveInterfaceBinding_WaitingForBackgroundWorkToComplete;

    public static String SelectEnclosingScope_ErrorTitle;
    public static String SelectEnclosingScope_NoEnclosingScope;
    public static String SelectEnclosingScope_NoTokensInScope;
    public static String SelectEnclosingScope_Parsing;
    public static String SelectEnclosingScope_PleaseSelectAKeywordOrIdentifier;
    public static String SelectEnclosingScope_WaitingForBackgroundWorkToComplete;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
