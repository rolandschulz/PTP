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
package org.eclipse.photran.internal.ui;

import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.ui.IEclipseVPGFactory;

/**
 * This class gives the Rephraser Engine's common UI actions access to
 * Photran's VPG.
 *
 * @author Jeff Overbey
 */
public class PhotranVPGContribution implements IEclipseVPGFactory
{
    @SuppressWarnings("unchecked")
    public EclipseVPG getVPG()
    {
        return PhotranVPG.getInstance();
    }
}
