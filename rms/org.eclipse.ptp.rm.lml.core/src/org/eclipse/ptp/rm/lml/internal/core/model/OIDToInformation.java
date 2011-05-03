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

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InformationType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;

/**
 * This class provides access to information-objects within a lml-model. It can
 * be used for fast and simple access to information filtered by the
 * information-type or the object-id.
 * 
 * @author karbach
 * 
 */
public class OIDToInformation extends LguiHandler {

	// Hashmap, keys are oid-references, values are lists of InfoType
	private HashMap<String, List<InfoType>> oidtoinfo;

	/**
	 * Create an information-handler with standard-parameters.
	 * 
	 * @param psuperHandler
	 *            LML-data-handler, which groups this handler and others to a
	 *            set of LMLHandler. This instance is needed to notify all
	 *            LMLHandler, if any data of the LguiType-instance was changed.
	 */
	public OIDToInformation(ILguiItem psuperHandler, LguiType model) {
		super(psuperHandler, model);

		updateData(model);

		lguiItem.addListener(new ILguiListener() {

			public void handleEvent(ILguiUpdatedEvent e) {
				updateData(e.getLguiItem().getLguiType());
			}
		});
	}

	/**
	 * Extracts all info-tags from lml-model and saves them in oidtoinfo.
	 */
	private void getInformationFromModel() {

		oidtoinfo = new HashMap<String, List<InfoType>>();

		List<JAXBElement<?>> alltags = lgui.getObjectsAndRelationsAndInformation();

		for (JAXBElement<?> jaxbtag : alltags) {// over all information-tags

			Object tag = jaxbtag.getValue();

			if (!(tag instanceof InformationType)) {
				continue;
			}

			InformationType ainfos = (InformationType) tag;

			List<InfoType> realinfos = ainfos.getInfo();

			for (InfoType ainfo : realinfos) { // over all info-tags
												// (information/info)

				String oid = ainfo.getOid();

				if (oidtoinfo.containsKey(oid)) {// Already list existent
					List<InfoType> oldlist = oidtoinfo.get(oid);
					oldlist.add(ainfo);
				} else {// new list for oid
					ArrayList<InfoType> newlist = new ArrayList<InfoType>();
					newlist.add(ainfo);
					oidtoinfo.put(oid, newlist);
				}
			}

		}

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

		List<InfoType> allinfos = getInfosById(id);
		if (allinfos == null) {
			return null;
		}

		List<InfoType> res = new ArrayList<InfoType>();
		// Get only infos with specific type
		for (InfoType ainfo : allinfos) {
			if (ainfo.getType().equals(type)) {
				res.add(ainfo);
			}
		}

		return res;
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
		return oidtoinfo.get(id);
	}

	/**
	 * Call this method, if lml-model changed. The new model is passed to this
	 * handler. All getter-functions accessing the handler will then return
	 * data, which is collected from this new model
	 * 
	 * @param lgui
	 *            new lml-data-model
	 */
	public void updateData(LguiType pmodel) {
		lgui = pmodel;

		getInformationFromModel();
	}
}
