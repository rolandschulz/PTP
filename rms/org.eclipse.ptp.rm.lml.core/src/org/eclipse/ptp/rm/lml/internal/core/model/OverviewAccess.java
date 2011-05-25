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

package org.eclipse.ptp.rm.lml.internal.core.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.AbslayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.GobjectType;
import org.eclipse.ptp.rm.lml.internal.core.elements.InformationType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LayoutType;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectsType;
import org.eclipse.ptp.rm.lml.internal.core.elements.SplitlayoutType;

public class OverviewAccess extends LguiHandler {

	public OverviewAccess(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);
		
		this.lguiItem.addListener(new ILguiListener() {
			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLguiItem().getLguiType());
			}
		});
	}

	/**
	 * Getting a list of all elements of type AbsLayout.
	 * 
	 * @return list of elements(AbsLayout)
	 */
	public List<AbslayoutType> getAbslayouts() {
		List<AbslayoutType> layouts = new LinkedList<AbslayoutType>();
		for (LayoutType tag : getLayouts()) {
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
		List<GobjectType> objects = new LinkedList<GobjectType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof GobjectType) {
				objects.add((GobjectType) tag.getValue());
			}
		}
		return objects;
	}

	/**
	 * Getting a list of all elements of type InformationsType from LguiType.
	 * 
	 * @return list of elements(InfomationsType)
	 */
	public List<InformationType> getInformations() {
		List<InformationType> informations = new LinkedList<InformationType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof InformationType) {
				informations.add((InformationType) tag.getValue());
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
		List<LayoutType> layouts = new LinkedList<LayoutType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof LayoutType) {
				layouts.add((LayoutType) tag.getValue());
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
		List<ObjectsType> objects = new LinkedList<ObjectsType>();
		for (JAXBElement<?> tag : lgui.getObjectsAndRelationsAndInformation()) {
			if (tag.getValue() instanceof ObjectsType) {
				objects.add((ObjectsType) tag.getValue());
			}
		}
		return objects;
	}

	/**
	 * Getting a list of all elements of type Splitlayout.
	 * 
	 * @return list of elements(Splitlayout)
	 */
	public List<SplitlayoutType> getSplitlayouts() {
		List<SplitlayoutType> tables = new LinkedList<SplitlayoutType>();
		for (LayoutType tag : getLayouts()) {
			if (tag instanceof SplitlayoutType) {
				tables.add((SplitlayoutType) tag);
			}
		}
		return tables;
	}

}
