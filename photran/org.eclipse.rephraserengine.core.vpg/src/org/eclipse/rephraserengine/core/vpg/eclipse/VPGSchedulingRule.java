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

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * An {@link ISchedulingRule} for jobs which access a VPG.
 * <p>
 * Only one job with a <code>VPGSchedulingRule</code>
 * is allowed to run at any single point in time.
 * <p>
 * This class is a Singleton; the singleton instance is
 * accessed via the {@link #getInstance()} method.
 * <p>
 * <a href="../../../../overview-summary.html#Eclipse">More Information</a>
 *
 * @author Jeff Overbey
 */
public class VPGSchedulingRule implements ISchedulingRule
{
	private static VPGSchedulingRule instance = null;

	public static VPGSchedulingRule getInstance()
	{
		if (instance == null) instance = new VPGSchedulingRule();
		return instance;
	}

	private VPGSchedulingRule() {}

	public boolean isConflicting(ISchedulingRule rule)
	{
		return rule == this;
	}

	public boolean contains(ISchedulingRule rule)
	{
		return rule == this;
	}
}
