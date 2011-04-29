/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.sorters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;

/**
 * Sorts the viewer on the column passed in at initialization. Clicking a second
 * time on the column reverses the direction of the sort.
 * 
 * @author arossi
 * 
 */
public class JobListViewerSorter extends ViewerSorter {
	private int toggle = 1;
	private int column = 0;

	public JobListViewerSorter(int column) {
		this.column = column;
	}

	@Override
	public int compare(Viewer viewer, Object o1, Object o2) {
		JobStatusData st1 = (JobStatusData) o1;
		JobStatusData st2 = (JobStatusData) o2;

		String s1 = JobStatusData.ZEROSTR;
		String s2 = JobStatusData.ZEROSTR;

		switch (column) {
		case 0:
			s1 = st1.getJobId();
			s2 = st2.getJobId();
			break;
		case 1:
			s1 = st1.getState();
			s2 = st2.getState();
			break;
		case 2:
			s1 = st1.getStateDetail();
			s2 = st2.getStateDetail();
			break;
		case 4:
			s1 = JobStatusData.ZEROSTR + st1.getOutReady();
			s2 = JobStatusData.ZEROSTR + st2.getOutReady();
			break;
		case 5:
			s1 = JobStatusData.ZEROSTR + st1.getErrReady();
			s2 = JobStatusData.ZEROSTR + st2.getErrReady();
			break;
		default:
		}

		return s1.compareTo(s2) * toggle;
	}

	public void toggle() {
		toggle *= -1;
	}
}
