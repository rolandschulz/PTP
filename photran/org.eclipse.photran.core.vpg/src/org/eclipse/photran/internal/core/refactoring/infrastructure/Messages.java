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
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.core.refactoring.infrastructure.messages"; //$NON-NLS-1$

    public static String FortranEditorRefactoring_AnalysisRefactoringNotEnabled;

    public static String FortranEditorRefactoring_FileInEditorCannotBeParsed;

    public static String FortranResourceRefactoring_AnalysisRefactoringNotEnabled;

    public static String FortranResourceRefactoring_CheckingForBindingConflictsIn;

    public static String FortranResourceRefactoring_CheckingForConflictingDefinitionsIn;

    public static String FortranResourceRefactoring_CheckingForReferencesTo;

    public static String FortranResourceRefactoring_CheckingForSubprogramBindingConflictsIn;

    public static String FortranResourceRefactoring_CPreprocessedFileWillNotBeRefactored;

    public static String FortranResourceRefactoring_FileIsNotInAFortranProject;

    public static String FortranResourceRefactoring_FixedFormFileWillNotBeRefactored;

    public static String FortranResourceRefactoring_IndentationAndLineLengthAreNotChecked;

    public static String FortranResourceRefactoring_LineColumn;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
