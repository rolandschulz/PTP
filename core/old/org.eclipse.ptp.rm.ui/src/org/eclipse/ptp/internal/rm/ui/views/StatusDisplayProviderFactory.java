/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.internal.rm.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ptp.rm.core.RMJobStatus;
import org.eclipse.ptp.rm.core.RMStatus;
import org.eclipse.ptp.rm.ui.RMUiPlugin;
import org.eclipse.swt.graphics.Image;

public class StatusDisplayProviderFactory {

	private static class RMJobStatusDP implements IStatusDisplayProvider {
		private static Map ordering;

		static {
			ordering = new HashMap();
			ordering.put(RMJobStatus.PENDING, new Integer(0));
			ordering.put(RMJobStatus.RUNNING, new Integer(1));
			ordering.put(RMJobStatus.SUSPENDED, new Integer(2));
			ordering.put(RMJobStatus.DONE, new Integer(3));
			ordering.put(RMJobStatus.EXIT, new Integer(4));
			ordering.put(RMJobStatus.UNKNOWN, new Integer(5));
		}

		public static IStatusDisplayProvider[] getAll() {
			final Collection allStatuses = ordering.keySet();
			final ArrayList sdps = new ArrayList(
					allStatuses.size());
			for (Iterator ait = allStatuses.iterator(); ait.hasNext();) {
				sdps.add(make((RMJobStatus) ait.next()));
			}
			return (IStatusDisplayProvider[]) sdps.toArray(new IStatusDisplayProvider[0]);
		}

		private final RMJobStatus status;

		public RMJobStatusDP(RMJobStatus status) {
			this.status = status;
		}

		public int compareTo(Object arg0) {
			final RMJobStatusDP other = (RMJobStatusDP) arg0;
			Comparable orderThis = (Comparable) ordering.get(status);
			Comparable orderOther = (Comparable) ordering.get(other.status);
			return orderThis.compareTo(orderOther);
		}

		public Image getImage() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getText() {
			return status.toString();
		}
	}

	private static class RMStatusDP implements IStatusDisplayProvider {
		private static Map ordering;
		private static Map iconNames;

		static {
			ordering = new HashMap();
			ordering.put(RMStatus.OK, new Integer(0));
			ordering.put(RMStatus.ALLOCATED_OTHER, new Integer(1));
			ordering.put(RMStatus.UNAVAILABLE, new Integer(2));
			ordering.put(RMStatus.DOWN, new Integer(3));
			ordering.put(RMStatus.UNKNOWN, new Integer(4));
			iconNames = new HashMap();
			iconNames.put(RMStatus.OK, createImage("node_up.gif"));
			iconNames.put(RMStatus.ALLOCATED_OTHER, createImage("node_alloc_other.gif"));
			iconNames.put(RMStatus.UNAVAILABLE, createImage("node_unavailable.gif"));
			iconNames.put(RMStatus.DOWN, createImage("node_down.gif"));
			iconNames.put(RMStatus.UNKNOWN, createImage("node_unknown.gif"));
		}

		public static IStatusDisplayProvider[] getAll() {
			final Collection allStatuses = ordering.keySet();
			final ArrayList sdps = new ArrayList(
					allStatuses.size());
			for (Iterator ait = allStatuses.iterator(); ait.hasNext();) {
				sdps.add(make((RMStatus) ait.next()));
			}
			return (IStatusDisplayProvider[]) sdps.toArray(new IStatusDisplayProvider[0]);
		}

		private final RMStatus status;

		public RMStatusDP(RMStatus status) {
			this.status = status;
		}

		public int compareTo(Object arg0) {
			final RMStatusDP other = (RMStatusDP) arg0;
			Comparable orderThis = (Comparable) ordering.get(status);
			Comparable orderOther = (Comparable) ordering.get(other.status);
			return orderThis.compareTo(orderOther);
		}

		public Image getImage() {
			return (Image) iconNames.get(status); 
		}

		public String getText() {
			return status.toString();
		}
	}

	private static final Map statuses = new HashMap();

	public static IStatusDisplayProvider[] getAll(RMJobStatus status) {
		return RMJobStatusDP.getAll();
	}

	public static Image createImage(String path) {
		return RMUiPlugin.getImageDescriptor("icons/statuses/" + path).createImage();
	}

	public static IStatusDisplayProvider[] getAll(RMStatus status) {
		return RMStatusDP.getAll();
	}

	public static IStatusDisplayProvider make(RMJobStatus status) {
		if (statuses.containsKey(status)) {
			return (IStatusDisplayProvider) statuses.get(status);
		} else {
			final RMJobStatusDP statusDisplay = new RMJobStatusDP(status);
			statuses.put(status, statusDisplay);
			return statusDisplay;
		}
	}

	public static IStatusDisplayProvider make(RMStatus status) {
		if (statuses.containsKey(status)) {
			return (IStatusDisplayProvider) statuses.get(status);
		} else {
			final RMStatusDP statusDisplay = new RMStatusDP(status);
			statuses.put(status, statusDisplay);
			return statusDisplay;
		}
	}
}
