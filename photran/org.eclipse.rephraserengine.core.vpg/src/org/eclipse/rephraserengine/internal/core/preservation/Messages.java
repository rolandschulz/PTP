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
package org.eclipse.rephraserengine.internal.core.preservation;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings.
 * 
 * @author Jeff Overbey
 */
class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.eclipse.rephraserengine.internal.core.preservation.messages"; //$NON-NLS-1$

    public static String Model_Computing;

    public static String Model_Differencing;

    public static String Model_Normalizing;

    public static String Model_PreparingToCompute;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
    }
}
