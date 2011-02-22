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

import java.io.Serializable;

/**
 * A dependency in a VPG between two files.
 * <a href="../../../overview-summary.html#DEA">More Information</a>
 *
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 *
 * @since 1.0
 */
public class VPGDependency<A, T, R extends IVPGNode<T>> implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String dependentFile;
	private String dependsOnFile;

    /**
     * Constructor. Creates a dependency between two files in the given VPG.
     * <p>
     * The dependency is <i>not</i> added to the VPG database automatically.
     * 
     * @since 3.0
     */
	public VPGDependency(String dependencyFrom, String dependsOn)
	{
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
