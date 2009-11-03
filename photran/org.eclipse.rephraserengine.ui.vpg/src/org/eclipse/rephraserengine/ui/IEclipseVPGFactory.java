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
package org.eclipse.rephraserengine.ui;

import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * Interface implemented by classes contributed to the <i>vpg</i> extension point.
 *
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public interface IEclipseVPGFactory
{
    /**
     * @return an {@link EclipseVPG}, which may be a Singleton (i.e., it is permissible for this
     * method to return the same VPG object on every invocation, as well as for different
     * {@link IEclipseVPGFactory} instantiations to return the same VPG object)
     */
    @SuppressWarnings("unchecked") EclipseVPG getVPG();
}
