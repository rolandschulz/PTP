/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.core.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.internal.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.internal.core.model.LayoutAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToInformation;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus;
import org.eclipse.ptp.rm.lml.internal.core.model.OverviewAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.TableHandler;

/**
 * Interface to manage the handling of an LguiType. It helps to work with LguiType without knowing the exact build of LguiType.
 */
public interface ILguiItem {
	/*
	 * Mandatory table columns
	 */
	public static String JOB_ID = "step"; //$NON-NLS-1$
	public static String JOB_OWNER = "owner"; //$NON-NLS-1$
	public static String JOB_STATUS = "status"; //$NON-NLS-1$
	public static String JOB_QUEUE_NAME = "queue"; //$NON-NLS-1$
	/*
	 * Other well-known table columns
	 */
	public static String JOB_WALL = "wall"; //$NON-NLS-1$
	public static String JOB_TOTAL_CORES = "totalcores"; //$NON-NLS-1$

	/*
	 * Table types
	 */
	public static String ACTIVE_JOB_TABLE = "joblistrun"; //$NON-NLS-1$
	public static String INACTIVE_JOB_TABLE = "joblistwait"; //$NON-NLS-1$

	/*
	 * Table content types
	 */
	public static String CONTENT_CLASSES = "classes"; //$NON-NLS-1$
	public static String CONTENT_GROUPS = "groups"; //$NON-NLS-1$
	public static String CONTENT_JOBS = "jobs"; //$NON-NLS-1$
	public static String CONTENT_NODES = "nodes"; //$NON-NLS-1$
	public static String CONTENT_OTHER = "other"; //$NON-NLS-1$
	public static String CONTENT_QUEUES = "queues"; //$NON-NLS-1$
	public static String CONTENT_USERS = "users"; //$NON-NLS-1$

	/**
	 * @param listener
	 */
	public void addListener(ILguiListener listener);

	/**
	 * Add a user job
	 * 
	 * @param jobId
	 *            ID of job to add
	 * @param status
	 *            job status information
	 * @param force
	 *            force the job to be added to the table
	 */
	public void addUserJob(String jobId, JobStatusData status, boolean force);

	public String[] getColumnTitlePattern(String gid);

	/**
	 * The meth
	 * 
	 * @param output
	 */
	public void getCurrentLayout(OutputStream output);

	/**
	 * @return
	 */
	public LayoutAccess getLayoutAccess();

	public String[] getMessageOfTheDay();

	/**
	 * @return
	 */
	public NodedisplayAccess getNodedisplayAccess();

	/**
	 * @return
	 */
	public ObjectStatus getObjectStatus();

	/**
	 * @return
	 */
	public OIDToInformation getOIDToInformation();

	/**
	 * @return
	 */
	public OIDToObject getOIDToObject();

	/**
	 * @return
	 */
	public OverviewAccess getOverviewAccess();

	public Map<String, List<IPattern>> getPattern();

	public List<IPattern> getPattern(String gid);

	/**
	 * @return
	 */
	public TableHandler getTableHandler();

	/**
	 * @param jobId
	 * @return
	 */
	public JobStatusData getUserJob(String jobId);

	/**
	 * @return
	 */
	public JobStatusData[] getUserJobs();

	public String getUsername();

	/**
	 * Getting the version of the LguiType:
	 * 
	 * @return version of the LguiType
	 */
	public String getVersion();

	/**
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * Checking if any layout is present.
	 * 
	 * @return
	 */
	public boolean isLayout();

	/**
	 * Inform all listeners, that something changed in the data-model. Handlers should use this event to update their
	 * model-references. Otherwise inconsistent return-values will be the result.
	 */
	public void notifyListeners();

	/**
	 * 
	 * @param layout
	 */
	public void reloadLastLayout(StringBuilder layout);

	/**
	 * @param name
	 */
	public void removeUserJob(String name);

	/**
	 * The Resource Manager is being closed. The current layout of the different monitoring parts should be saved during the
	 * closing.
	 * 
	 */
	public String saveCurrentLayout();

	public void setPattern(Map<String, List<IPattern>> pattern);

	public void setPattern(String gid, List<IPattern> filterValues);

	public void setRequest(RequestType request);

	/**
	 * Getting a string representing the ILguiItem.
	 * 
	 * @return string
	 */
	public String toString();

	/**
	 * 
	 * @param stream
	 * @throws JAXBException
	 */
	public void update(InputStream stream);

	/**
	 * @param name
	 * @param status
	 * @param detail
	 */
	public void updateUserJob(String name, String status, String detail);
}
