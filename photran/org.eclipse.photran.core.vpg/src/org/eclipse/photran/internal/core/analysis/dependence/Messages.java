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
package org.eclipse.photran.internal.core.analysis.dependence;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.photran.internal.core.analysis.dependence.messages"; //$NON-NLS-1$

    public static String LoopDependences_LoopContains;

    public static String LoopDependences_LoopContainsAnAssignmentToADerivedTypeComponent;

    public static String LoopDependences_LoopNestContainsADoWhileLoop;

    public static String PerfectLoopNest_LinearFunctionOfNonIndexVariable;

    public static String PerfectLoopNest_OnlySingleSubscriptsAreCurrentlySupported;

    public static String VariableReference_AssignmentStmtCannotBeProcessed;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
