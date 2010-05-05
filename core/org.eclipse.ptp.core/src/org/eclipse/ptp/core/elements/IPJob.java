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
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
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
	 * Get the launch configuration that was used to launch this job
	 * 
	 * @return launch configuration
	 */
	public ILaunchConfiguration getLaunchConfiguration();

	/**
	 * @param attributeDefinition
	 * @param processJobRank the process in question
	 * @return
	 */
	public <T, A extends IAttribute<T,A,D>, D extends IAttributeDefinition<T,A,D>>
	A getProcessAttribute(D attributeDefinition, int processJobRank);

	/**
	 * @param attrDefId
	 * @param processJobRank the process in question
	 * @return
	 */
	public IAttribute<?,?,?> getProcessAttribute(String attrDefId, int processJobRank);
	
	/**
	 * @param processJobRanks
	 * @return
	 */
	public Set<IAttributeDefinition<?, ?, ?>> getProcessAttributeKeys(
			BitSet processJobRanks);
	
	/**
	 * @param processJobRanks limit the returned attributes
	 *  to this subset of process job ranks
	 * @return
	 */
	public Set<IAttribute<?, ?, ?>> getProcessAttributes(BitSet processJobRanks);
	
	/**
	 * @param attributeDefinition
	 * @return
	 */
	public <T, A extends IAttribute<T,A,D>, D extends IAttributeDefinition<T,A,D>>
	Set<A> getProcessAttributes(D attributeDefinition);
	
	/**
	 * @param attributeDefinition
	 * @param processJobRanks limit the returned attributes
	 *  to this subset of process job ranks
	 * @return
	 */
	public <T, A extends IAttribute<T,A,D>, D extends IAttributeDefinition<T,A,D>>
	Set<A> getProcessAttributes(D attributeDefinition, BitSet processJobRanks);

	
	/**
	 * @param attrDefId
	 * @param processJobRanks limit the returned attributes
	 *  to this subset of process job ranks
	 * @return
	 */
	public Set<IAttribute<?,?,?>> getProcessAttributes(String attrDefId, BitSet processJobRanks);

	
	/**
	 * Get the job ranks for all the processes known by this job
	 * 
	 * @return
	 */
	public BitSet getProcessJobRanks();
	
	/**
	 * Get the jobRanks for all the processes known by this job with
	 * attribute value equal to {@code attribute}
	 *
	 * @param attribute
	 * @return
	 */
	public <T, A extends IAttribute<T,A,D>, D extends IAttributeDefinition<T,A,D>>
	BitSet getProcessJobRanks(A attribute);

	/**
	 * @param processJobRank
	 * @return
	 */
	public String getProcessName(int processJobRank);
	
	/**
	 * @param processJobRank
	 * @return
	 */
	public String getProcessNodeId(int processJobRank);

	/**
	 * @param processJobRank
	 * @return
	 */
	public ProcessAttributes.State getProcessState(int processJobRank);

	/**
	 * Returns parent queue for this job.
	 * 
	 * @return IPQueue
	 */
	public IPQueue getQueue();
	
	/**
	 * @param processJobRank
	 * @return the output saved for this processJobRank
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
	 */
	public boolean hasProcessByJobRank(int processJobRank);

	/**
	 * @param processJobRanks
	 * @return true if all of these processes are contained by this job
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
	 * Sets this job to be a debug job
	 */
	public void setDebug();
}
