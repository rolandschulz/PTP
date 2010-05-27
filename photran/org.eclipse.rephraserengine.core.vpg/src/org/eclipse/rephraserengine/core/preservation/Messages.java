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
package org.eclipse.rephraserengine.core.preservation;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.core.preservation.messages"; //$NON-NLS-1$

    public static String PreservationAnalysis_EdgeWillChange;

    public static String PreservationAnalysis_EnteringHypotheticalMode;

    public static String PreservationAnalysis_ExitingHypotheticalMode;

    public static String PreservationAnalysis_FromHere;

    public static String PreservationAnalysis_TheFollowingFilesWillNotCompile;

    public static String PreservationAnalysis_ToHere;

    public static String PreservationAnalysis_TransformationWillChange;

    public static String PreservationAnalysis_TransformationWillEliminate;

    public static String PreservationAnalysis_TransformationWillIntroduce;

    public static String PreservationAnalysis_WillPointHereInstead;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
