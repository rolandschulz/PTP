/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.core.vpg;

import org.eclipse.ui.IStartup;

/**
 * Called by Eclipse when the VPG plug-in is loaded
 * (see the org.eclipse.ui.startup extension point).
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGStartup implements IStartup
{
	public void earlyStartup()
	{
		// Load the VPG and the parser, and start the indexer thread
		PhotranVPG.getInstance().start();
	}
}
