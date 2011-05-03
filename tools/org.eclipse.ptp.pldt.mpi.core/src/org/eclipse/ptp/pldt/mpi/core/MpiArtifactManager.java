/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core;

import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.pldt.common.Artifact;

/**
 * @author Beth Tibbitts
 * 
 * 
 */
public class MpiArtifactManager
{
	static List repository;
	static HashMap hashMap = new HashMap(); // to be able to look up by unique ID (finding
	static String inputFileName;

	/**
	 * Add an MPI artifact to the repository
	 * 
	 * @param pi
	 */
	public static void addMpiArtifact(Artifact pi)
	{
		repository.add(pi);
	}

	/**
	 * Add an MPI artifact to the hashtable for future lookup
	 * 
	 * @param pi
	 */
	public static void addMpiArtifactToHash(Artifact pi)
	{
		String id = pi.getId(); // for debugging access only
		hashMap.put(id, pi);
	}

	/**
	 * retrieve MPI artifact by unique ID. Used for retrieving artifacts when needed from the Markers - since markers
	 * can't hold on to them
	 * 
	 * @param id
	 * @return
	 */
	public static Artifact getMpiArtifact(String id)
	{
		Artifact pi = (Artifact) hashMap.get(id);
		return pi;
	}

	public static Object[] getMpiArtifacts()
	{
		Object[] pis = hashMap.values().toArray();
		return pis;
	}

	public static HashMap getMpiArtifactMap()
	{
		return hashMap;
	}

	/**
	 * getList - get the list of mpi artifacts
	 */
	public static List getList()
	{
		return repository;
	}

	/**
	 * getInputFile - get the input file ( on which analysis is invoked )
	 * 
	 */
	public static String getInputFile()
	{
		return inputFileName;
	}

}
