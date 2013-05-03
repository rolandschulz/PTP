/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.core.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.elements.JobPartType;
import org.eclipse.ptp.rm.lml.core.elements.JobType;
import org.eclipse.ptp.rm.lml.core.model.IUsagebarInterpreter;

/**
 * Default implementation of the IUsagebarInterpreter.
 * Implements a standard mapping of node- to cpu-ID.
 * Uses cpupernode-attributes of the LML-usagebar for this mapping.
 * 
 */
public class UsagebarInterpreter implements IUsagebarInterpreter {

	/**
	 * Comparator for job-elements
	 * Jobs are compared by cpucount-attributes
	 * 
	 */
	public static class JobComparator implements Comparator<JobType> {

		/**
		 * String identifying the empty-job.
		 */
		private static final String emptyString = "empty"; //$NON-NLS-1$

		@Override
		public int compare(JobType o1, JobType o2) {
			// Catch null values
			if (o1 == null || o1.getOid() == null) {
				return 1;
			}
			if (o2 == null || o2.getOid() == null) {
				return -1;
			}
			// Notice: wrong ordering so that bigger jobs are shown first
			// Handle empty jobs
			if (o1.getOid().equals(emptyString))
				return 1;
			if (o2.getOid().equals(emptyString))
				return -1;
			return o2.getCpucount().intValue() - o1.getCpucount().intValue();
		}

	}

	/**
	 * LML-model for the interpret usageAdapter.
	 */
	private final UsageAdapter usageAdapter;

	/**
	 * Create an interpreter for the given usagebar.
	 * The jobs inside the usagebar will be sorted by the cpu-count
	 * by calling this constructor.
	 * 
	 * @param usageAdapter
	 *            the interpret usagebar.
	 */
	public UsagebarInterpreter(UsageAdapter usageAdapter) {
		this.usageAdapter = usageAdapter;
		// Sort jobs by the cpu-count
		final JobComparator comp = new JobComparator();
		Collections.sort(usageAdapter.getJob(), comp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.lml.core.model.IUsagebarInterpreter#getLastCPUinNode(int)
	 */
	@Override
	public int getLastCpuInNode(int node) {
		double nodes = 0;
		int cpu = 0;
		int jobId = 0;
		final List<JobType> jobList = usageAdapter.getJob();

		// Search job, in which searched node lies
		while (jobId < jobList.size()) {
			final JobType job = jobList.get(jobId);
			nodes += getNodesByJob(job, job.getCpucount().intValue());
			cpu += job.getCpucount().intValue();

			if (nodes < node) {
				jobId++;
			}
			else {
				nodes -= getNodesByJob(job, job.getCpucount().intValue());
				cpu -= job.getCpucount().intValue();
				// cpu is now at the beginning of the job with id jobid, in which the searched node can be found
				break;
			}
		}

		if (jobId == jobList.size())
			return cpu;// Return really last cpu within this usagebar
		// Now sjob contains searched node
		final JobType job = jobList.get(jobId);
		int cpuPerNode = usageAdapter.getCpuPerNode().intValue();
		if (job.getCpupernode() != null)
			cpuPerNode = job.getCpupernode().intValue();

		if (job.getJobpart() == null || job.getJobpart().size() == 0) {// Simple case first
			// No job-parts
			cpu += (node - nodes) * cpuPerNode;
			return cpu;
		}
		else {
			// Search for part, where end of node lies
			int jobPartId = 0;
			final List<JobPartType> jobPartList = job.getJobpart();
			while (jobPartId < jobPartList.size()) {

				int tmpCpuPerNode = cpuPerNode;

				final JobPartType jobPart = jobPartList.get(jobPartId);
				if (jobPart.getCpupernode() != null)
					tmpCpuPerNode = jobPart.getCpupernode().intValue();

				final double tmpNodes = jobPart.getCpucount().intValue() / (double) tmpCpuPerNode;
				nodes += tmpNodes;
				cpu += jobPart.getCpucount().intValue();

				if (nodes < node) {
					jobPartId++;
				}
				else {
					nodes -= tmpNodes;
					cpu -= jobPart.getCpucount().intValue();
					break;
				}
			}

			if (jobPartId == jobPartList.size()) {// Usually this should not happen
				jobPartId = jobPartList.size() - 1;
			}

			// apart contains end of node
			final JobPartType jobPart = jobPartList.get(jobPartId);
			if (jobPart.getCpupernode() != null)
				cpuPerNode = jobPart.getCpupernode().intValue();

			return (int) (cpu + (node - nodes) * cpuPerNode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.lml.core.model.IUsagebarInterpreter#getNodeCount()
	 */
	@Override
	public int getNodeCount() {
		final int cpuPerNode = usageAdapter.getCpuPerNode().intValue();

		double nodes = 0;

		final List<JobType> jobList = usageAdapter.getJob();
		for (int i = 0; i < jobList.size(); i++) {
			final JobType job = jobList.get(i);

			final int jobCpuPerNode = job.getCpupernode() == null ? cpuPerNode : job.getCpupernode().intValue();

			if (job.getJobpart().size() == 0) {// No job-parts?
				nodes += job.getCpucount().intValue() / (double) jobCpuPerNode;
			}
			else {
				final List<JobPartType> jobPartList = job.getJobpart();

				for (int j = 0; j < jobPartList.size(); j++) {

					final JobPartType jobPart = jobPartList.get(j);

					final int jobPartCpuPerNode = jobPart.getCpupernode() == null ? jobCpuPerNode : jobPart.getCpupernode()
							.intValue();

					nodes += jobPart.getCpucount().intValue() / (double) jobPartCpuPerNode;
				}
			}
		}

		return (int) Math.round(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.lml.core.model.IUsagebarInterpreter#getNodecountAtCpu(int)
	 */
	@Override
	public int getNodecountAtCpu(int cpuCount) {
		final List<JobType> jobList = usageAdapter.getJob();

		int i = 0;
		double nodes = 0;
		while (i < jobList.size() && cpuCount > 0) {
			nodes += getNodesByJob(jobList.get(i), cpuCount);
			cpuCount -= jobList.get(i).getCpucount().intValue();
			i++;
		}

		if (Math.abs(nodes - (int) nodes) < 1e-5) {
			return (int) nodes;
		}
		else
			return ((int) nodes + 1);
	}

	/**
	 * Helping function for retrieving the amount of nodes covered by one job.
	 * 
	 * @param job
	 *            a job within this usagebar
	 * @param tillCpu
	 *            is the index of cpu till which the number of nodes should be calculated;
	 *            tillcpu is in range [0 and job.getCpucount().intValue()]
	 *            call this function with tillcpu=job.getCpucount().intValue() and you will get all nodes covered by this job
	 * @return how many nodes are covered by this job till the cpu-id tillcpu
	 */
	protected double getNodesByJob(JobType job, int tillCpu) {
		final int cpuPerNode = usageAdapter.getCpuPerNode().intValue();

		double nodes = 0;

		final int jobCpuPerNode = job.getCpupernode() == null ? cpuPerNode : job.getCpupernode().intValue();

		if (tillCpu < 0 || tillCpu > job.getCpucount().intValue()) {
			tillCpu = job.getCpucount().intValue();
		}

		if (job.getJobpart().size() == 0) {// No job-parts?
			nodes = tillCpu / (double) jobCpuPerNode;
		}
		else {
			final List<JobPartType> jobPartList = job.getJobpart();

			int cpuSum = 0;

			for (int j = 0; j < jobPartList.size(); j++) {

				final JobPartType jobPart = jobPartList.get(j);

				final int partCpuPerNode = jobPart.getCpupernode() == null ? jobCpuPerNode : jobPart.getCpupernode().intValue();

				cpuSum += jobPart.getCpucount().intValue();

				if (cpuSum <= tillCpu) {
					nodes += jobPart.getCpucount().intValue() / (double) partCpuPerNode;
				}
				else {// Add the rest of this jobpart, last jobpart-start befor tillcount is reached
					nodes += (tillCpu - (cpuSum - jobPart.getCpucount().intValue())) / (double) partCpuPerNode;
					break;
				}
			}
		}

		return nodes;
	}

}