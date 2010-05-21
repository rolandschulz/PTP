/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.analysis.dependence;

/**
 * Thrown when dependence testing cannot succeed.
 * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
 * 
 * @author Jeff Overbey
 * 
 * @since 2.0
 */
@SuppressWarnings("serial")
public class DependenceTestFailure extends Error
{
    public DependenceTestFailure(String message)
    {
        super(message);
    }
}