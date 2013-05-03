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

package org.eclipse.ptp.rm.lml.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.core.elements.InformationType;
import org.eclipse.ptp.rm.lml.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;

/**
 * This class provides access to information-objects within a lml-model. It can
 * be used for fast and simple access to information filtered by the
 * information-type or the object-id.
 */
public class OIDToInformation extends LguiHandler {

	// Hashmap, keys are oid-references, values are lists of InfoType
	private HashMap<String, List<InfoType>> oidToInfo;

	/**
	 * Create an information-handler with standard-parameters.
	 * 
	 * @param lguiItem
	 *            LML-data-handler, which groups this handler and others to a
	 *            set of LMLHandler. This instance is needed to notify all
	 *            LMLHandler, if any data of the LguiType-instance was changed.
	 */
	public OIDToInformation(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);

		updateData(lgui);

		lguiItem.addListener(new ILguiListener() {

			public void handleEvent(ILguiUpdatedEvent e) {
				updateData(e.getLgui());
			}
		});
	}

	/**
	 * 
	 * 
	 * @param oid
	 * @return
	 */
	public InfoType getInfoByOid(String oid) {
		final List<InformationType> listInformation = lguiItem.getOverviewAccess().getInformations();
		for (final InformationType information : listInformation) {
			final List<InfoType> listInfo = information.getInfo();
			for (final InfoType info : listInfo) {
				if (info.getOid().equals(oid)) {
					return info;
				}
			}
		}
		return null;
	}

	/**
	 * Get all info-objects for the object with passed id.
	 * 
	 * example
	 * 
	 * <pre>
	 * {@code
	 * <information>
	 * 		<info oid="empty" type="short" description="additional infos for this job">
	 * 			<data key="owner" value="noone" />
	 * 			<data key="cpus" value="19840" />
	 * 		</info>
	 * 		
	 * 		<info oid="job1" type="short" >
	 * 			<data key="owner" value="carsten" />
	 * 			<data key="cpus" value="1024" />
	 * 		</info>
	 * 		
	 * 		<info oid="job1" type="long" >
	 * 			<data key="owner" value="carsten" />
	 * 			<data key="cpus" value="1024" />
	 * 			<data key="starttime" value="05.04.2011" />
	 * 			<data key="endtime" value="06.04.2011" />
	 * 		</info>
	 * 		
	 * 		</information>
	 * 		
	 * }
	 * </pre>
	 * 
	 * getInfosById("job1") returns a list with the last two info-tags, which
	 * are associated with object "job1"
	 * 
	 * 
	 * @param id
	 *            ID-name of an object defined in objects-tag
	 * @return all info-elements for this object, null if no info exists for
	 *         this id
	 */
	public List<InfoType> getInfosById(String id) {
		return oidToInfo.get(id);
	}

	/**
	 * Get all information of passed type defined in the lml-instance. example:
	 * 
	 * <pre>
	 * {@code
	 * <information>
	 * 		<info oid="empty" type="short" description="additional infos for this job">
	 * 			<data key="owner" value="noone" />
	 * 			<data key="cpus" value="19840" />
	 * 		</info>
	 * 		
	 * 		<info oid="job1" type="short" >
	 * 			<data key="owner" value="carsten" />
	 * 			<data key="cpus" value="1024" />
	 * 		</info>
	 * 		
	 * 		<info oid="job1" type="long" >
	 * 			<data key="owner" value="carsten" />
	 * 			<data key="cpus" value="1024" />
	 * 			<data key="starttime" value="05.04.2011" />
	 * 			<data key="endtime" value="06.04.2011" />
	 * 		</info>
	 * 		
	 * 		</information>
	 * 		
	 * }
	 * </pre>
	 * 
	 * getInfosByType( "job1", "short") returns the second info-tag
	 * getInfosByType( "empty", "short") returns the first info-tag
	 * 
	 * 
	 * @param id
	 *            identification for an object
	 * @param type
	 *            type of information
	 * @return all infos of a type for object with given id, null if no infos
	 *         there, empty list if no infos with this type exist
	 */
	public List<InfoType> getInfosByType(String id, String type) {

		final List<InfoType> infoList = getInfosById(id);
		if (infoList == null) {
			return null;
		}

		final List<InfoType> result = new ArrayList<InfoType>();
		// Get only infos with specific type
		for (final InfoType info : infoList) {
			if (info.getType().equals(type)) {
				result.add(info);
			}
		}

		return result;
	}

	/**
	 * Call this method, if lml-model changed. The new model is passed to this
	 * handler. All getter-functions accessing the handler will then return
	 * data, which is collected from this new model
	 * 
	 * @param lgui
	 *            new lml-data-model
	 */
	public void updateData(LguiType lgui) {
		this.lgui = lgui;

		getInformationFromModel();
	}

	/**
	 * Extracts all info-tags from lml-model and saves them in oidtoinfo.
	 */
	private void getInformationFromModel() {

		oidToInfo = new HashMap<String, List<InfoType>>();

		for (final Object object : jaxbUtil.getObjects(lgui)) {

			if (!(object instanceof InformationType)) {
				continue;
			}

			for (final InfoType info : ((InformationType) object).getInfo()) {
				// over all info-tags (information/info)

				final String oid = info.getOid();

				if (oidToInfo.containsKey(oid)) {
					// Already list existent
					final List<InfoType> oldList = oidToInfo.get(oid);
					oldList.add(info);
				} else {
					// new list for oid
					final ArrayList<InfoType> newList = new ArrayList<InfoType>();
					newList.add(info);
					oidToInfo.put(oid, newList);
				}
			}

		}

	}

}
