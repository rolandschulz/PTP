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

import javax.xml.bind.JAXBException;

import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;

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

	/**
	 * @param gid
	 * @return
	 */
	public String[] getColumnTitlePattern(String gid);

	/**
	 * The meth
	 * 
	 * @param output
	 */
	public void getCurrentLayout(OutputStream output);

	/**
	 * @return object to map component-ids to corresponding layout definitions
	 */
	public LayoutAccess getLayoutAccess();

	/**
	 * @return
	 */
	public String[] getMessageOfTheDay();

	public String getName();

	/**
	 * @return NodedisplayAccess-instance for accessing layouts of nodedisplays
	 */
	public NodedisplayAccess getNodedisplayAccess();

	/**
	 * @return a object, which saves which object has to be highlighted. All user interactions are saved globally for all components
	 *         in this object.
	 */
	public ObjectStatus getObjectStatus();

	/**
	 * @return object for getting infos for objects
	 */
	public OIDToInformation getOIDToInformation();

	/**
	 * @return a class, which provides an index for fast access to objects within the objects tag of LML. You can pass the id of the
	 *         objects to the returned object. It then returns the corresponding objects.
	 */
	public OIDToObject getOIDToObject();

	/**
	 * @return
	 */
	public OverviewAccess getOverviewAccess();

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

	public boolean isFilterOwnJobActive(String gid);

	/**
	 * Checking if any layout is present.
	 * 
	 * @return
	 */
	public boolean isLayout();

	public void lockPattern();

	/**
	 * Inform all listeners, that something changed in the data-model. Handlers should use this event to update their
	 * model-references. Otherwise inconsistent return-values will be the result.
	 */
	public void notifyListeners();

	/**
	 * 
	 * @param layout
	 */
	public void reloadLastLayout(String layout);

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

	public void setRequest(RequestType request);

	public void unlockPattern();

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
