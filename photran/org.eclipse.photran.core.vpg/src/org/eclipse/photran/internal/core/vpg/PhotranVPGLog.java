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
package org.eclipse.photran.internal.core.vpg;

import java.io.File;

import org.eclipse.photran.internal.core.Activator;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPGLog;

/**
 * The error/warning log for Photran's VPG.
 * <p>
 * Specializes EclipseVPGLog to be parameterized with Photran's token type
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGLog extends EclipseVPGLog<Token, PhotranTokenRef>
{
    @Override protected File getLogFile()
    {
        return new File(
            Activator.getDefault().getStateLocation().addTrailingSeparator().toOSString() +
            "photran60vpg-log.txt");
    }
}
