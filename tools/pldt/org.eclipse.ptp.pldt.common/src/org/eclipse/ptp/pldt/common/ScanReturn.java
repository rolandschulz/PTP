/**********************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represents the collection of artifacts (e.g. MPI or OpenMP artifacts) returned from analysis,
 * which will eventually be displayed in
 * the associated view, etc. <br>
 * It can also contain other information such as return codes, generalized analysis results, parameters, etc. <br>
 * 
 * @author Beth Tibbitts
 * 
 */
public class ScanReturn
{

	private List<Artifact> artifacts = new ArrayList<Artifact>();

	/**
	 * Create a ScanReturn objct
	 */
	public ScanReturn()
	{
	}

	/**
	 * was an error found during the artifact analysis? may be overridden
	 * @return
	 */
	public boolean wasError()
	{
		return false;
	}

	/**
	 * Get list of artifacts found
	 * @return list of artifacts found
	 */
	public List<Artifact> getArtifactList()
	{
		return artifacts;
	}

	/**
	 * Add an artifact to the accumulated list
	 * @param a
	 */
	public void addArtifact(Artifact a)
	{
		artifacts.add(a);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ScanReturn with " + artifacts.size() + " artifacts";
	}

}
