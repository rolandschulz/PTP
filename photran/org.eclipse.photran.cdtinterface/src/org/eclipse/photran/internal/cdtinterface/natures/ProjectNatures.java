/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.cdtinterface.natures;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;

/**
 * Provides constants defining the C and C++ project natures (from CDT).
 * 
 * @author Jeff Overbey
 */
public class ProjectNatures
{
    private ProjectNatures() {;}
    
    public static final String C_NATURE_ID = CProjectNature.C_NATURE_ID;
    public static final String CC_NATURE_ID = CCProjectNature.CC_NATURE_ID;
}
