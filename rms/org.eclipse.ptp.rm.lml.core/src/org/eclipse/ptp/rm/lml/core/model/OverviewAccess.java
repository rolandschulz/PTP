/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.core.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.elements.AbslayoutType;
import org.eclipse.ptp.rm.lml.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.core.elements.InfodataType;
import org.eclipse.ptp.rm.lml.core.elements.InformationType;
import org.eclipse.ptp.rm.lml.core.elements.LayoutType;
import org.eclipse.ptp.rm.lml.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectsType;
import org.eclipse.ptp.rm.lml.core.elements.SplitlayoutType;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;

public class OverviewAccess extends LguiHandler {

	public OverviewAccess(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);

		this.lguiItem.addListener(new ILguiListener() {
			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLgui());
			}
		});
	}

	/**
	 * Getting a list of all elements of type AbsLayout.
	 * 
	 * @return list of elements(AbsLayout)
	 */
	public List<AbslayoutType> getAbslayouts() {
		final List<AbslayoutType> layouts = new LinkedList<AbslayoutType>();
		for (final LayoutType tag : getLayouts()) {
			if (tag instanceof AbslayoutType) {
				layouts.add((AbslayoutType) tag);
			}
		}
		return layouts;
	}

	/**
	 * Getting a list of all elements of type GobjectType from LguiType.
	 * 
	 * @return list of elements(GobjectsType)
	 */
	public List<GobjectType> getGraphicalObjects() {
		final List<GobjectType> objects = new LinkedList<GobjectType>();
		for (final Object object : jaxbUtil.getObjects(lgui)) {
			if (object instanceof GobjectType) {
				objects.add((GobjectType) object);
			}
		}
		return objects;
	}

	public String getInfodataValue(InfoType info, String key) {
		for (final InfodataType data : info.getData()) {
			if (data.getKey().equals(key)) {
				return data.getValue();
			}
		}
		return null;
	}

	public InfoType getInformation(String oid) {
		for (final InformationType information : getInformations()) {
			for (final InfoType info : information.getInfo()) {
				if (info.getOid().equals(oid)) {
					return info;
				}
			}
		}
		return null;
	}

	/**
	 * Getting a list of all elements of type InformationsType from LguiType.
	 * 
	 * @return list of elements(InfomationsType)
	 */
	public List<InformationType> getInformations() {
		final List<InformationType> informations = new LinkedList<InformationType>();
		for (final Object object : jaxbUtil.getObjects(lgui)) {
			if (object instanceof InformationType) {
				informations.add((InformationType) object);
			}
		}
		return informations;
	}

	/**
	 * Getting a list of all elements of type LayoutType.
	 * 
	 * @return list of elements(LayoutType)
	 */
	public List<LayoutType> getLayouts() {
		final List<LayoutType> layouts = new LinkedList<LayoutType>();
		for (final Object object : jaxbUtil.getObjects(lgui)) {
			if (object instanceof LayoutType) {
				layouts.add((LayoutType) object);
			}
		}
		return layouts;
	}

	/**
	 * Getting a list with all elements of type ObjectsType from LguiType.
	 * 
	 * @return list of elements(ObjectsType)
	 */
	public List<ObjectsType> getObjects() {
		final List<ObjectsType> objects = new LinkedList<ObjectsType>();
		for (final Object object : jaxbUtil.getObjects(lgui)) {
			if (object instanceof ObjectsType) {
				objects.add((ObjectsType) object);
			}
		}
		return objects;
	}

	/**
	 * Get the oid corresponding to the job ID
	 * 
	 * @param jobId
	 *            ID of the job (sometimes known as the step)
	 * @return Corresponding OID or null if none
	 */
	public String getOIDByJobId(String jobId) {
		final List<InformationType> listInformation = getInformations();
		for (final InformationType information : listInformation) {
			final List<InfoType> listInfo = information.getInfo();
			for (final InfoType info : listInfo) {
				final List<InfodataType> listData = info.getData();
				for (final InfodataType data : listData) {
					if (data.getKey().equals("step") && data.getValue().indexOf(jobId) != -1) { //$NON-NLS-1$
						return info.getOid();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Getting a list of all elements of type Splitlayout.
	 * 
	 * @return list of elements(Splitlayout)
	 */
	public List<SplitlayoutType> getSplitlayouts() {
		final List<SplitlayoutType> tables = new LinkedList<SplitlayoutType>();
		for (final LayoutType tag : getLayouts()) {
			if (tag instanceof SplitlayoutType) {
				tables.add((SplitlayoutType) tag);
			}
		}
		return tables;
	}

}
