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

import java.io.InputStream;
import java.net.URI;

import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.internal.core.model.LayoutAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.NodedisplayAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToInformation;
import org.eclipse.ptp.rm.lml.internal.core.model.OIDToObject;
import org.eclipse.ptp.rm.lml.internal.core.model.ObjectStatus;
import org.eclipse.ptp.rm.lml.internal.core.model.OverviewAccess;
import org.eclipse.ptp.rm.lml.internal.core.model.TableHandler;


/**
 * Interface to manage the handling of an LguiType. It helps to work with LguiType without knowing the exact build of LguiType. 
 * @author Claudia Knobloch
 */
public interface ILguiItem  {
	
	/**
	 * Getting the source of the XML file from whcih the corresponding LguiType has been generated. 
	 * @return the source of the XML file
	 */
	public URI getXmlFile();
	
	/**
	 * Getting a string representing the ILguiItem.
	 * @return string 
	 */
	public String toString();
	
	/**
	 * Getting the version of the LguiType:
	 * @return version of the LguiType
	 */
	public String getVersion();
	
	/**
	 * Checking if any layout is present.
	 * @return 
	 */
	public boolean isLayout();
	
	public TableHandler getTableHandler();
	
	public OverviewAccess getOverviewAccess();
	
	public  void addListener(ILguiListener listener);
	
	public OIDToObject getOIDToObject();
	
	public ObjectStatus getObjectStatus();
	
	public OIDToInformation getOIDToInformation();
	
	public NodedisplayAccess getNodedisplayAccess();
	
	public LayoutAccess getLayoutAccess();
	
	/**
	 * Inform all listeners, that something changed in the data-model.
	 * Handlers should use this event to update their model-references.
	 * Otherwise inconsistent return-values will be the result.
	 */
	public void updateData();
	
	public void updateXML();
	
	public void update(InputStream stream);
	
	public void addJob();
	
}
