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
package org.eclipse.rephraserengine.core.analysis.dependence;

/**
 * A variable reference in a {@link Dependence}.
 * <p>
 * THIS IS PRELIMINARY AND EXPERIMENTAL.  IT IS NOT APPROPRIATE FOR PRODUCTION USE.
 * 
 * @author Jeff Overbey
 * 
 * @since 2.0
 */
public interface IVariableReference
{
    boolean isRead();
    boolean isWrite();
}
