/*******************************************************************************
 * Copyright (c) 2010 Los Alamos National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	LANL - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.ui.model;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes.State;

/**
 * This class is a temporary stand-in for a model element related to the UI
 * representation of a process.
 * <p>
 * Since processes have been removed from the runtime model in order to achieve
 * scalability, UI elements referring to processes can no longer access these
 * model elements directly. UI views, at this time, still have individual
 * objects for the visual representations of processes (as icons), and these
 * representations rely on objects that implement {@code IPElement}.
 * <p>
 * A future re-write of the UI system that no longer displays individual icons
 * for each process of a job or node will probably not need this class.
 * 
 * @author Randy M. Roberts
 * 
 */
// FIXME PProcessUI goes away when we address UI scalability. See Bug 311057
@Deprecated
public class PProcessUI implements IPElement {

	public static String getID(IPJob job, int jobRank) {
		return job.getProcessName(jobRank);
	}

	private final IPJob job;
	private final int jobRank;
	private final BitSet jobRanks;

	/**
	 * @param job
	 * @param jobRank
	 */
	public PProcessUI(IPJob job, int jobRank) {
		this.job = job;
		this.jobRank = jobRank;
		this.jobRanks = new BitSet(jobRank);
		this.jobRanks.set(jobRank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPElement#addAttribute(org.eclipse.ptp.
	 * core.attributes.IAttribute)
	 */
	public void addAttribute(IAttribute<?, ?, ?> attrValue) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPElement#addAttributes(org.eclipse.ptp
	 * .core.attributes.IAttribute<?,?,?>[])
	 */
	public void addAttributes(IAttribute<?, ?, ?>[] attrValues) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PProcessUI other = (PProcessUI) obj;
		if (job == null) {
			if (other.job != null)
				return false;
		} else if (!job.equals(other.job))
			return false;
		if (jobRank != other.jobRank)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPElement#getAttribute(org.eclipse.ptp.
	 * core.attributes.IAttributeDefinition)
	 */
	public <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> A getAttribute(D attrDef) {
		return job.getProcessAttribute(attrDef, jobRank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPElement#getAttribute(java.lang.String)
	 */
	public IAttribute<?, ?, ?> getAttribute(String attrDefId) {
		return job.getProcessAttribute(attrDefId, jobRank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPElement#getAttributeKeys()
	 */
	public IAttributeDefinition<?, ?, ?>[] getAttributeKeys() {
		final Set<IAttributeDefinition<?, ?, ?>> processAttributeKeys = job.getProcessAttributeKeys(jobRanks);
		return processAttributeKeys.toArray(new IAttributeDefinition<?, ?, ?>[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPElement#getAttributes()
	 */
	public IAttribute<?, ?, ?>[] getAttributes() {
		final Set<IAttribute<?, ?, ?>> processAttributes = job.getProcessAttributes(jobRanks);
		return processAttributes.toArray(new IAttribute<?, ?, ?>[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPElement#getDisplayAttributes()
	 */
	public IAttribute<?, ?, ?>[] getDisplayAttributes() {
		final List<IAttribute<?, ?, ?>> attrs = Arrays.asList(getAttributes());
		Set<IAttribute<?, ?, ?>> attrSet = new HashSet<IAttribute<?, ?, ?>>(attrs);
		for (IAttribute<?, ?, ?> attr : attrSet) {
			if (!attr.getDefinition().getDisplay()) {
				attrs.remove(attr);
			}
		}
		return attrs.toArray(new IAttribute<?, ?, ?>[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPElement#getID()
	 */
	public String getID() {
		return job.getProcessName(jobRank);
	}

	/**
	 * @return
	 */
	public IPJob getJob() {
		return job;
	}

	public int getJobRank() {
		return jobRank;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPElement#getName()
	 */
	public String getName() {
		return job.getProcessName(jobRank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPElement#getParent()
	 */
	public IPElement getParent() {
		return job;
	}

	public int getPid() {
		// FIXME This changes when we implement Process Attribute queries. See
		// Bug 309343
		return 0;
	}

	/**
	 * @return the lines in the job's output prefixed by the process rank
	 */
	public String getSavedOutput() {
		return job.getSavedOutput(jobRank);
	}

	/**
	 * @return
	 */
	public State getState() {
		return job.getProcessState(jobRank);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((job == null) ? 0 : job.hashCode());
		result = prime * result + jobRank;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.elements.IPElement#removeAttribute(org.eclipse.ptp
	 * .core.attributes.IAttribute)
	 */
	public void removeAttribute(IAttribute<?, ?, ?> attrValue) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.IPElement#size()
	 */
	public int size() {
		return 0;
	}

}
