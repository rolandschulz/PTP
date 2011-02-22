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
package org.eclipse.rephraserengine.core.vpg.eclipse;

import org.eclipse.core.resources.WorkspaceJob;

/**
 * A {@link WorkspaceJob} which accesses a VPG.
 * <p>
 * The job will be scheduled such that only one VPG-accessing job is running at any point in time
 * (using a {@link VPGSchedulingRule}).
 * <p>
 * <a href="../../../../overview-summary.html#Eclipse">More Information</a>
 * <p>
 * This class is intended to be subclassed directly.
 * 
 * @author Jeff Overbey
 * @see org.eclipse.core.resources.WorkspaceJob
 * 
 * @since 1.0
 */
public abstract class VPGJob<A, T> extends WorkspaceJob
{
	public VPGJob(String name)
	{
		super(name);
		setRule(VPGSchedulingRule.getInstance());
	}
}