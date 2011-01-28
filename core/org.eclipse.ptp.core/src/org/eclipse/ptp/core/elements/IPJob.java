/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core.elements;

import java.util.BitSet;
import java.util.Set;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ProcessAttributes;
import org.eclipse.ptp.core.elements.listeners.IJobChildListener;
import org.eclipse.ptp.core.elements.listeners.IJobListener;

/**
 * A Job can be a parallel or sequential job, consisting of one or more
 * processes which are residing on Nodes. Jobs may span Machines, though this
 * may not be very common in reality, but the model allows for this. Finally, a
 * Job may or may not belong to the current user, it just means that if it is
 * visible then this user can see the Job in his or her Universe. This may have
 * ramifications on what the user can do to the Job if it is not owned by them.
 * 
 * @author Nathan DeBardeleben
 */
public interface IPJob extends IPElement {
	/**
	 * Add a listener for child events relating to this job.
	 * 
	 * @param listener
	 */
	public void addChildListener(IJobChildListener listener);

	/**
	 * Add a listener for events related to this job.
	 * 
	 * @param listener
	 */
	public void addElementListener(IJobListener listener);

	/**
	 * Add attributes to a collection of processes.
	 * 
	 * @param jobRanks
	 *            job ranks of processes to be modified
	 * @param attributes
	 *            AttributeManager for the attributes to add to each process
	 * @since 5.0
	 */
	public void addProcessAttributes(BitSet jobRanks, AttributeManager attributes);

	/**
	 * @param jobRanks
	 * @param attrs
	 * @since 5.0
	 */
	public void addProcessesByJobRanks(BitSet jobRanks, AttributeManager attrs);

	/**
	 * Get the launch configuration that was used to launch this job
	 * 
	 * @return launch configuration
	 */
	public ILaunchConfiguration getLaunchConfiguration();

	/**
	 * Retrieve the attribute associated with the given attribute definition for
	 * one of the job's processes.
	 * 
	 * @param attributeDefinition
	 *            the attribute returned is defined for this definition
	 * @param processJobRank
	 *            the process in question
	 * @return the process' attribute associated with this attribute definition
	 * @since 4.0
	 */
	public <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> A getProcessAttribute(D attributeDefinition,
			int processJobRank);

	/**
	 * Retrieve the attribute associated with the id for an attribute definition
	 * for one of the job's processes.
	 * 
	 * @param attrDefId
	 *            the id associated with the attribute definition used to
	 *            determine which attribute is returned
	 * @param processJobRank
	 *            the process in question
	 * @return the process' attribute associated with this attribute definition
	 *         id
	 * @since 4.0
	 */
	public IAttribute<?, ?, ?> getProcessAttribute(String attrDefId, int processJobRank);

	/**
	 * Return all the attribute definitions that are known by the specified
	 * processes.
	 * 
	 * @param processJobRanks
	 *            the set of processes from which the attribute definitions are
	 *            returned
	 * @return set of IAttributeDefinition keys
	 * @since 4.0
	 */
	public Set<IAttributeDefinition<?, ?, ?>> getProcessAttributeKeys(BitSet processJobRanks);

	/**
	 * Return all of the attributes from all of the specified processes owned by
	 * this job.
	 * 
	 * @param processJobRanks
	 *            limit the returned attributes to this subset of process job
	 *            ranks
	 * @return the set of all attributes possessed by the specified processes
	 * @since 4.0
	 */
	public Set<IAttribute<?, ?, ?>> getProcessAttributes(BitSet processJobRanks);

	/**
	 * Return the set of all attributes for this attribute definition from the
	 * specified set of this job's child processes
	 * 
	 * @param attributeDefinition
	 *            the attributes returned are all defined for this definition
	 * @param processJobRanks
	 *            limit the returned attributes to this subset of process job
	 *            ranks
	 * @return
	 * @since 4.0
	 */
	public <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> Set<A> getProcessAttributes(
			D attributeDefinition, BitSet processJobRanks);

	/**
	 * Return the set of all attributes specified by this attribute definition
	 * id from the specified set of this job's child processes
	 * 
	 * @param attrDefId
	 *            the attributes returned are all defined for the definition
	 *            with this id
	 * @param processJobRanks
	 *            limit the returned attributes to this subset of process job
	 *            ranks
	 * @return
	 * @since 4.0
	 */
	public Set<IAttribute<?, ?, ?>> getProcessAttributes(String attrDefId, BitSet processJobRanks);

	/**
	 * Get the job ranks for all the processes known by this job
	 * 
	 * @return
	 * @since 4.0
	 */
	public BitSet getProcessJobRanks();

	/**
	 * Get the jobRanks for all the processes known by this job with attribute
	 * value equal to {@code attribute}
	 * 
	 * @param attribute
	 * @return
	 * @since 4.0
	 */
	public <T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> BitSet getProcessJobRanks(A attribute);

	/**
	 * @param processJobRank
	 * @return
	 * @since 4.0
	 */
	public String getProcessName(int processJobRank);

	/**
	 * @param processJobRank
	 * @return
	 * @since 4.0
	 */
	public String getProcessNodeId(int processJobRank);

	/**
	 * @param processJobRank
	 * @return
	 * @since 4.0
	 */
	public ProcessAttributes.State getProcessState(int processJobRank);

	/**
	 * Get the resource manager controlling this job
	 * 
	 * @since 5.0
	 */
	public IResourceManagerControl getResourceManager();

	/**
	 * @param processJobRank
	 * @return the output saved for this processJobRank
	 * @since 4.0
	 */
	public String getSavedOutput(int processJobRank);

	/**
	 * Returns the state of the job
	 * 
	 * @return job state
	 */
	public JobAttributes.State getState();

	/**
	 * @param processJobRank
	 * @return true if process is contained by this job
	 * @since 4.0
	 */
	public boolean hasProcessByJobRank(int processJobRank);

	/**
	 * @param processJobRanks
	 * @return true if all of these processes are contained by this job
	 * @since 4.0
	 */
	public boolean hasProcessesByJobRanks(BitSet processJobRanks);

	/**
	 * Returns true/false regarding whether this Job is a debug job
	 * 
	 * @return True if this job is a debug job
	 */
	public boolean isDebug();

	/**
	 * Remove a listener for events relating to children of this job.
	 * 
	 * @param listener
	 */
	public void removeChildListener(IJobChildListener listener);

	/**
	 * Remove a listener for events relating to this job.
	 * 
	 * @param listener
	 */
	public void removeElementListener(IJobListener listener);

	/**
	 * Remove processes from the job
	 * 
	 * @param jobRanks
	 * @since 5.0
	 */
	public void removeProcessesByJobRanks(BitSet jobRanks);

	/**
	 * Sets this job to be a debug job
	 */
	public void setDebug();

	/**
	 * Set the launch configuration that was used to launch this job.
	 * 
	 * @param configuration
	 * @since 5.0
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration);
}
