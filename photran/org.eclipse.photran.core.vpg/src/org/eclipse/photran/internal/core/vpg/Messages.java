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
package org.eclipse.photran.internal.core.vpg;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.core.vpg.messages"; //$NON-NLS-1$

    public static String PhotranVPG_AnalysisRefactoringNotEnabled;

    public static String PhotranVPG_ControlFlow;

    public static String PhotranVPG_DefaultVisibilityForScopeIsPrivate;

    public static String PhotranVPG_Definition;

    public static String PhotranVPG_DefinitionIsPrivateInScope;

    public static String PhotranVPG_DefinitionScopeRelationship;

    public static String PhotranVPG_DefinitionScopeRelationshipDueToModuleImport;

    public static String PhotranVPG_FileIsNotInAFortranProject;

    public static String PhotranVPG_GlobalScope;

    public static String PhotranVPG_IllegalShadowing;

    public static String PhotranVPG_ImplicitSpecForScope;

    public static String PhotranVPG_ModuleSymbolTableEntry;

    public static String PhotranVPG_ModuleSymbolTableEntryCount;

    public static String PhotranVPG_ModuleTokenRef;

    public static String PhotranVPG_NameBinding;

    public static String PhotranVPG_NotAFortranSourceFile;

    public static String PhotranVPG_OffsetN;

    public static String PhotranVPG_PhotranIndexer;

    public static String PhotranVPG_ProjectIsNotAccessible;

    public static String PhotranVPG_ProjectIsNotAFortranProject;

    public static String PhotranVPG_RenamedBinding;

    public static String PhotranVPG_ScopeIsInternal;

    public static String PhotranVPG_Type;

    public static String PhotranVPG_VariableAccess;

    public static String PhotranVPGBuilder_ErrorParsingFile;

    public static String PhotranVPGBuilder_ErrorParsingFileMessage;

    public static String PhotranVPGBuilder_FileContainsSyntaxErrors;

    public static String PhotranVPGSerializer_AnnotationCorrupted;

    public static String PhotranVPGStartup_FlushingDatabase;

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
