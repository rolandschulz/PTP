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
package org.eclipse.rephraserengine.core.vpg;

/**
 * A dependency in a VPG.
 * <a href="../../../overview-summary.html#DEA">More Information</a>
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public class VPGDependency<A, T, R extends TokenRef<T>>
{
	//private VPG<A, T, R, ?, ?> vpg;
	private String dependentFile;
	private String dependsOnFile;

    /**
     * Constructor. Creates a dependency between two files in the given VPG.
     * <p>
     * The dependency is <i>not</i> added to the VPG database automatically.
     */
	public VPGDependency(VPG<A, T, R, ?, ?> vpg,
	                        String dependencyFrom,
	                        String dependsOn)
	{
		//this.vpg = vpg;
		this.dependentFile = dependencyFrom;
		this.dependsOnFile = dependsOn;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Accessors
    ///////////////////////////////////////////////////////////////////////////

	/** @return the file which is dependent upon another file */
	public String getDependentFile()
	{
		return dependentFile;
	}

	/** @return the file which is depended upon */
	public String getDependsOnFile()
	{
		return dependsOnFile;
	}
}
