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

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.ptp.rm.core.utils.DebugUtil;

/**
 * 
 * @author Greg Watson
 * 
 */
public class MPICH2ListJobsParser {
	public enum JobListState {
		JOBLIST_INIT,
		JOBLIST_START,
		JOBLIST_END,
		JOBLIST_COMPLETE,
		JOBLIST_ERROR
	}

	private JobListState state;
	private final MPICH2JobMap map = new MPICH2JobMap();
	private String errorMsg;

	/**
	 * @param parser
	 * @param line
	 * @param job
	 */
	private void processJobInfo(String line, MPICH2JobMap.Job job) {
		String[] parts = line.split("="); //$NON-NLS-1$
		String key;
		String value;

		if (parts.length == 1) {
			key = parts[0].trim();
			value = ""; //$NON-NLS-1$
		} else if (parts.length == 2) {
			key = parts[0].trim();
			value = parts[1].trim();
		} else {
			state = JobListState.JOBLIST_ERROR;
			errorMsg = line;
			return;
		}

		if ("jobid".equals(key)) { //$NON-NLS-1$
			job.setJobID(value);
		} else if ("jobalias".equals(key)) { //$NON-NLS-1$
			job.setJobAlias(value);
		} else if ("username".equals(key)) { //$NON-NLS-1$
			job.setUsername(value);
		} else if ("host".equals(key)) { //$NON-NLS-1$
			job.setHost(value);
		} else if ("pid".equals(key)) { //$NON-NLS-1$
			job.setPid(Integer.parseInt(value));
		} else if ("sid".equals(key)) { //$NON-NLS-1$
			job.setSid(Integer.parseInt(value));
		} else if ("rank".equals(key)) { //$NON-NLS-1$
			job.setRank(Integer.parseInt(value));
		} else if ("pgm".equals(key)) { //$NON-NLS-1$
			job.setPgm(value);
		} else {
			state = JobListState.JOBLIST_ERROR;
			errorMsg = line;
			return;
		}

		if (job.isComplete()) {
			state = JobListState.JOBLIST_END;
		}
	}

	/**
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public MPICH2JobMap parse(BufferedReader reader) throws IOException {
		String line = null;
		MPICH2JobMap.Job job = null;

		state = JobListState.JOBLIST_INIT;

		while (state != JobListState.JOBLIST_COMPLETE) {
			switch (state) {
			case JOBLIST_INIT:
				if ((line = reader.readLine()) == null) {
					state = JobListState.JOBLIST_COMPLETE;
					break;
				}

				line = line.trim();

				if (line.length() == 0) {
					// Ignore empty line
					break;
				}

				state = JobListState.JOBLIST_START;

				job = map.new Job();
				processJobInfo(line, job);
				break;

			case JOBLIST_START:
				if ((line = reader.readLine()) == null) {
					state = JobListState.JOBLIST_END;
					break;
				}

				line = line.trim();

				if (line.length() == 0) {
					state = JobListState.JOBLIST_END;
					break;
				}

				processJobInfo(line, job);
				break;

			case JOBLIST_END:
				if (job != null) {
					if (job.isComplete()) {
						map.addJob(job);
					} else {
						DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "job is incomplete, skipping" + job.toString()); //$NON-NLS-1$
					}
				}

				if (line == null) {
					state = JobListState.JOBLIST_COMPLETE;
				} else {
					state = JobListState.JOBLIST_INIT;
				}
				if (job != null) {
					DebugUtil.trace(DebugUtil.RTS_DISCOVER_TRACING, "found job " + job.toString()); //$NON-NLS-1$
				}
				break;

			case JOBLIST_COMPLETE:
				break;

			case JOBLIST_ERROR:
				while ((line = reader.readLine()) != null) {
					errorMsg += "\n" + line; //$NON-NLS-1$
				}
				return null;

			default:
				return null;
			}
		}

		return map;
	}

	/**
	 * @return
	 */
	public String getErrorMessage() {
		return errorMsg;
	}
}
