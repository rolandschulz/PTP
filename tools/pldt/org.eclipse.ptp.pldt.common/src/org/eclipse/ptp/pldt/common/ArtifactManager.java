/**********************************************************************
 * Copyright (c) 2005,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common;

import java.util.HashMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.internal.common.IDs;

/**
 * Manage the artifacts: be able to retrieve an artifact from the id in a marker, for example.
 * <p>
 * Note that there can be more than one ArtifactManager, distinguished by an ID, probably a markerID. That is, all artifacts of the
 * same markerID (probably shown in the same view) are managed by the same (distinct) ArtifactManager.
 * 
 * TODO Question: why can't we just store the objects in an attribute??
 * 
 * @author Beth Tibbitts
 * 
 * 
 */
public class ArtifactManager {

	/** to be able to look up artifacts by unique ID */
	static HashMap hashMap = new HashMap();

	/** ID for this registry */
	private String id_;

	/**
	 * Registry of artifact managers. IDs are probably markerIDs for the type of
	 * artifacts that this ArtifactManager holds.
	 */
	public static HashMap registry = new HashMap();

	/**
	 * Create ArtifactManager from id
	 * @param id
	 */
	public ArtifactManager(String id) {
		this.id_ = id;
		registry.put(id, this);

	}

	/** disallow default ctor */
	@SuppressWarnings("unused")
	private ArtifactManager() {
	}

	/**
	 * return the ArtifactManager used for managing artifacts of type "id"
	 * (probably a marker id)
	 * 
	 * @param id
	 * @return
	 */
	public static ArtifactManager getManager(String id) {
		Object o = (Object) registry.get(id);
		if (o == null)
			new ArtifactManager(id);
		return (ArtifactManager) registry.get(id);
	}

	/**
	 * return the artifact for a marker; involves looking up the
	 * Artifact Manager, then the Artifact within that.
	 * 
	 * Maybe we just need a big hashtable or other big Map instead???
	 * 
	 * @param marker
	 * @return
	 * @throws CoreException
	 */
	public static IArtifact getArtifact(IMarker marker) throws CoreException {
		String id = (String) marker.getAttribute(IDs.UNIQUE_ID);
		String markerID = marker.getAttribute(IDs.ID).toString();
		ArtifactManager aMgr = getManager(markerID);
		IArtifact a = aMgr.getArtifact(id);

		return a;
	}

	/**
	 * Add an artifact to the hashtable for future lookup
	 * 
	 * @param artifact
	 */
	public void addArtifactToHash(IArtifact a) {
		String id = a.getId(); // for debugging access only
		hashMap.put(id, a);
	}

	/**
	 * retrieve artifact by unique ID. Used for retrieving artifacts when needed
	 * from the Markers - since markers can't hold on to them
	 * 
	 * @param id
	 * @return
	 */
	public IArtifact getArtifact(String id) {
		IArtifact artifact = (Artifact) hashMap.get(id);
		return artifact;
	}

	public Object[] getArtifacts() {
		Object[] artifacts = hashMap.values().toArray();
		return artifacts;
	}

	/**
	 * get hashtable of artifact map
	 * @return
	 */
	public HashMap getArtifactMap() {
		return hashMap;
	}

	/**
	 * Remove artifact by artifact object
	 * 
	 * @param a the artifact to remove
	 * @return true if it was removed
	 */
	public boolean removeArtifact(IArtifact a) {
		return removeArtifact(a.getId());
	}

	/**
	 * Remove artifact by key
	 * 
	 * @param key
	 * @return
	 */
	public boolean removeArtifact(String key) {
		if (hashMap.containsKey(key)) {
			hashMap.remove(key);
			return true;
		}
		return false;
	}

	/**
	 * Remove all the artifacts
	 * 
	 */
	public void clear() {
		hashMap.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ArtifactManager for " + id_; //$NON-NLS-1$
	}

}
