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
package org.eclipse.photran.internal.ui.views.vpgproblems;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.ui.views.vpgproblems.messages"; //$NON-NLS-1$

    public static String CopyMarkedFileAction_Copy;

    public static String CopyMarkedFileAction_DescriptionLabel;

    public static String CopyMarkedFileAction_LocationLineLabel;

    public static String CopyMarkedFileAction_PathLabel;

    public static String CopyMarkedFileAction_ResourceLabel;

    public static String ErrorWarningFilterAction_Errors;

    public static String ErrorWarningFilterAction_Warnings;

    public static String OpenMarkedFileAction_GoTo;

    public static String OpenMarkedFileAction_GoToTooltip;

    public static String RemoveMarkerAction_Delete;

    public static String RemoveMarkerAction_DeleteTooltip;

    public static String SelectedResourceFilterAction_FilterBySelection;

    public static String SelectedResourceFilterAction_FilterBySelectionTooltip;

    public static String ShowFullMessageAction_EventDetails;

    public static String ShowFullMessageAction_ShowDetailsTooltip;

    public static String VPGProblemContextMenu_Problems;

    public static String VPGProblemLabelProvider_LineN;

    public static String VPGProblemView_DescriptionColumnHeader;

    public static String VPGProblemView_Errors;

    public static String VPGProblemView_nErrors;

    public static String VPGProblemView_nWarnings;

    public static String VPGProblemView_PathColumnHeader;

    public static String VPGProblemView_ResourceColumnHeader;

    public static String VPGProblemView_UpdatingProblemsViewMessage;

    public static String VPGProblemView_Warnings;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
