/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */
package org.eclipse.ptp.rm.lml.core.model;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.internal.core.model.LayoutAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToInformation;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus;
import org.eclipse.ptp.rm.lml.internal.core.model.OverviewAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.TableHandler;
import org.eclipse.ptp.rm.lml.internal.core.model.jobs.JobStatusData;

/**
 * Interface to manage the handling of an LguiType. It helps to work with LguiType without knowing the exact build of LguiType.
 */
public interface ILguiItem {
	public void addListener(ILguiListener listener);

	public void addUserJob(String name, JobStatusData status);

	public void getCurrentLayout(OutputStream output);

	public LayoutAccess getLayoutAccess();

	public NodedisplayAccess getNodedisplayAccess();

	public ObjectStatus getObjectStatus();

	public OIDToInformation getOIDToInformation();

	public OIDToObject getOIDToObject();

	public OverviewAccess getOverviewAccess();

	public void getRequestXml(FileOutputStream output);

	public TableHandler getTableHandler();

	public Map<String, String> getUserJobMap(String gid);

	/**
	 * Getting the version of the LguiType:
	 * 
	 * @return version of the LguiType
	 */
	public String getVersion();

	public boolean isEmpty();

	/**
	 * Checking if any layout is present.
	 * 
	 * @return
	 */
	public boolean isLayout();

	public void removeUserJob(String name);

	public void restoreUserJobs(Map<String, JobStatusData> map);

	//
	// /**
	// * Getting the source of the XML file from whcih the corresponding LguiType has been generated.
	// * @return the source of the XML file
	// */
	// public URI getXmlFile();
	//
	/**
	 * Getting a string representing the ILguiItem.
	 * 
	 * @return string
	 */
	public String toString();

	public void update(InputStream stream);

	public boolean update(String name, JobStatusData status);

	public void updateData();
}
