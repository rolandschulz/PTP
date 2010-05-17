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
package org.eclipse.photran.internal.cdtinterface.launch;

import org.eclipse.cdt.launch.internal.LocalCDILaunchDelegate;

/**
 * Photran's replacement for {@link LocalCDILaunchDelegate}.
 * <p>
 * This class does not add or change any functionality; rather, it is here to that the reference in
 * <code>plugin.xml</code> won't produce a "discouraged access" warning.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class FortranCDILaunchDelegate extends LocalCDILaunchDelegate
{
}
