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

import java.util.HashMap;
import java.util.List;

import org.eclipse.ptp.rm.lml.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectName;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectsType;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;

/**
 * This class provides an index for fast access to objects within the objects
 * tag. You can pass the id of the objects. This class then returns the
 * corresponding objects.
 */
public class OIDToObject extends LguiHandler {

	/*
	 * Color which is returned, if an id as parameter is not connected
	 */
	private static final LMLColor notConnectedColor = LMLColor.lightGray;

	private HashMap<String, ObjectType> oidToObject;
	private HashMap<String, LMLColor> oidToColor;

	private String systemId = null;// Id of an object with type system, there
									// should only be one

	/**
	 * Create OIDToObject with standard parameters
	 * 
	 * @param lguiItem
	 *            LML-data-handler, which groups this handler and others to a
	 *            set of LMLHandler. This instance is needed to notify all
	 *            LMLHandler, if any data of the LguiType-instance was changed.
	 */
	public OIDToObject(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);

		updateData();

		this.lguiItem.addListener(new ILguiListener() {

			public void handleEvent(ILguiUpdatedEvent e) {
				update(e.getLgui());
				updateData();
			}
		});
	}

	/**
	 * Get a color-object for an objects-id
	 * 
	 * * example:
	 * 
	 * <pre>
	 * {@code
	 * <objects>
	 * 		<object id="job1" type="job" color="#F00" />
	 * 		<object id="job2" type="job" color="#0F0" />
	 * 		<object id="job3" type="job" color="#00F" />
	 * 	   </objects>
	 * }
	 * </pre>
	 * 
	 * getColorById("job2") returns LMLColor with color intensities: red=0,
	 * green=255, blue=0
	 * 
	 * 
	 * @param id
	 *            id of an object
	 * @return LMLColor for this object
	 */
	public LMLColor getColorById(String id) {
		if (id == null) {
			return notConnectedColor;
		}
		final LMLColor result = oidToColor.get(id);

		if (result == null) {
			return notConnectedColor;
		} else {
			return result;
		}
	}

	/**
	 * get an object by an Id of this object
	 * 
	 * example:
	 * 
	 * <pre>
	 * {@code
	 * <objects>
	 * 		<object id="job1" type="job" color="#F00" />
	 * 		<object id="job2" type="job" color="#0F0" />
	 * 		<object id="job3" type="job" color="#00F" />
	 * 	   </objects>
	 * }
	 * </pre>
	 * 
	 * getObjectById("job2") returns JAXB-instance of ObjectType with content {@code <object id="job2" type="job" color="#0F0" />}
	 * 
	 * @param id
	 * @return object with given id
	 */
	public ObjectType getObjectById(String id) {
		return oidToObject.get(id);
	}

	/**
	 * Save method to get the connected LML-object for
	 * a nodeData. Checks for null-pointers and then calls
	 * getObjectById with the String-parameter.
	 * 
	 * @param nodeData
	 *            instance with information about one physical element of a computing resource
	 * @return corresponding object from LML-file or null, if nothing is found
	 */
	public ObjectType getObjectByLMLNode(LMLNodeData nodeData) {
		if (nodeData == null || nodeData.getDataElement() == null) {
			return null;
		}

		return getObjectById(nodeData.getDataElement().getOid());
	}

	/**
	 * search for an object of type "system", return its id Searching will only
	 * be done once for every model
	 * 
	 * example
	 * 
	 * *
	 * 
	 * <pre>
	 * {@code
	 * <objects>
	 * 		<object id="job1" type="job" color="#F00" />
	 * 		<object id="job2" type="job" color="#0F0" />
	 * 		<object id="job3" type="job" color="#00F" />
	 * 		<object id="pc" type="system"/>
	 * 	   </objects>
	 * }
	 * </pre>
	 * 
	 * getSystemObjectId() will return id "pc". If there is no object with
	 * type="system" this function returns null.
	 * 
	 * @return id of system-object
	 */
	public String getSystemObjectId() {

		return systemId;
	}

	/**
	 * Call this method, if lml-model changed. All getter-functions accessing
	 * the handler will then return data, which is collected from this new model
	 */
	private void updateData() {

		final List<ObjectsType> objectsList = lguiItem.getOverviewAccess().getObjects();

		for (final ObjectsType objects : objectsList) {

			final List<ObjectType> objectList = objects.getObject();

			oidToObject = new HashMap<String, ObjectType>();

			oidToColor = new HashMap<String, LMLColor>();

			for (final ObjectType object : objectList) {
				oidToObject.put(object.getId(), object);
				oidToColor.put(object.getId(), LMLColor.stringToColor(object.getColor()));

				if (object.getType() == ObjectName.SYSTEM) {
					systemId = object.getId();
				}
			}

		}
	}

}