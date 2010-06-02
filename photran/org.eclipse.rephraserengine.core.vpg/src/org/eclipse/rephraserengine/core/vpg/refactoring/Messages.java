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
package org.eclipse.rephraserengine.core.vpg.refactoring;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings
 * .
 * @since 2.0
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.core.vpg.refactoring.messages"; //$NON-NLS-1$

    public static String VPGEditorRefactoring_FileInTheEditorCannotBeParsed;

    public static String VPGPreservationBasedEditorRefactoring_CheckingFinalPreconditions;

    public static String VPGRefactoring_EnsuringIndexIsUpToDate;

    public static String VPGRefactoring_FileInTheEditorIsNotAccessible;

    public static String VPGRefactoring_FileInTheEditorIsReadOnly;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
