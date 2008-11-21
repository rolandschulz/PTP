/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Greg Watson
 *
 */
public class MPICH2JobMap {

	/**
	 * An MPICH2 job.
	 */
	public class Job {
		private static final int JOBID = 1 << 0;
		private static final int JOBALIAS = 1 << 1;
		private static final int USERNAME = 1 << 2;
		private static final int HOST = 1 << 3;
		private static final int PID = 1 << 4;
		private static final int SID = 1 << 5;
		private static final int RANK = 1 << 6;
		private static final int PGM = 1 << 7;
		
		private String jobID = null;
		private String jobAlias = null;
		private String username = null;
		private String host = null;
		private int pid = 0;
		private int sid = 0;
		private int rank = 0;
		private String pgm = null;
		private int complete;

		public Job() {
			this.complete = 0;
		}
		
		/**
		 * @return the host
		 */
		public String getHost() {
			return host;
		}
		
		/**
		 * @return the jobID
		 */
		public String getJobID() {
			return jobID;
		}
		
		/**
		 * @return the jobAlias
		 */
		public String getJobAlias() {
			return jobAlias;
		}
		
		/**
		 * @return the pgm
		 */
		public String getPgm() {
			return pgm;
		}
		
		/**
		 * @return the pid
		 */
		public int getPid() {
			return pid;
		}
		
		/**
		 * @return the rank
		 */
		public int getRank() {
			return rank;
		}
		
		/**
		 * @return the sid
		 */
		public int getSid() {
			return sid;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}
		
		/**
		 * @return the complete
		 */
		public boolean isComplete() {
			return complete == (JOBID | JOBALIAS | USERNAME | HOST | PID | SID | RANK | PGM);
		}
		
		/**
		 * @param host the host to set
		 */
		public void setHost(String host) {
			this.host = host;
			this.complete |= HOST;
		}
		
		/**
		 * @param jobID the jobID to set
		 */
		public void setJobID(String jobID) {
			this.jobID = jobID;
			this.complete |= JOBID;
		}
		
		/**
		 * @param jobAlias the jobAlias to set
		 */
		public void setJobAlias(String jobAlias) {
			this.jobAlias = jobAlias;
			this.complete |= JOBALIAS;
		}

		/**
		 * @param pgm the pgm to set
		 */
		public void setPgm(String pgm) {
			this.pgm = pgm;
			this.complete |= PGM;
		}
		
		/**
		 * @param pid the pid to set
		 */
		public void setPid(int pid) {
			this.pid = pid;
			this.complete |= PID;
		}
		
		/**
		 * @param rank the rank to set
		 */
		public void setRank(int rank) {
			this.rank = rank;
			this.complete |= RANK;
		}
		
		/**
		 * @param sid the sid to set
		 */
		public void setSid(int sid) {
			this.sid = sid;
			this.complete |= SID;
		}
		/**
		 * @param username the username to set
		 */
		public void setUsername(String username) {
			this.username = username;
			this.complete |= USERNAME;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			String str = "Job "; //$NON-NLS-1$
			if (!isComplete()) {
				str += "(incomplete) ["; //$NON-NLS-1$
			} else {
				str += "]"; //$NON-NLS-1$
			}
			if ((complete & JOBID) == JOBID) {
				str += " id: " + jobID; //$NON-NLS-1$
			}
			if ((complete & JOBALIAS) == JOBALIAS) {
				str += " alias: " + jobAlias; //$NON-NLS-1$
			}
			if ((complete & USERNAME) == USERNAME) {
				str += " username:" + username; //$NON-NLS-1$
			}
			if ((complete & HOST) == HOST) {
				str += " host:" + host; //$NON-NLS-1$
			}
			if ((complete & PID) == PID) {
				str += " pid:" + pid; //$NON-NLS-1$
			}
			if ((complete & SID) == SID) {
				str += " sid:" + sid; //$NON-NLS-1$
			}
			if ((complete & RANK) == RANK) {
				str += " rank:" + rank; //$NON-NLS-1$
			}
			if ((complete & PGM) == PGM) {
				str += " pgm:" + pgm; //$NON-NLS-1$
			}
			return str + "]"; //$NON-NLS-1$
		}

	}

	private final Map<String, List<Job>> jobsByID = new HashMap<String, List<Job>>();
	public boolean hasErrors = false;

	public MPICH2JobMap() {
		// Nothing.
	}

	public void addJob(Job job) {
		List<Job> jobs = jobsByID.get(job.getJobID());
		if (jobs == null) {
			jobs = new ArrayList<Job>();
			jobsByID.put(job.getJobID(), jobs);			
		}
		jobs.add(job);
	}
	
	public List<Job> getJob(String jobID) {
		return jobsByID.get(jobID);
	}

	public Collection<List<Job>> getJobs() {
		return jobsByID.values();
	}
}
