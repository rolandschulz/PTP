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

import org.eclipse.ptp.rm.lml.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.events.ILguiUpdatedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;

/**
 * Saves which object have to be highlighted.
 * 
 * Components, which want to be informed for user interactions can call
 * "addComponent" to add a listening component to this instance. Every call for
 * "mouseover" or "mousedown" and so on will be forwarded to all listening
 * Updatable-instances.
 */
public class ObjectStatus extends LguiHandler {

	/**
	 * 
	 * All graphical components of the applet have to implement this interface.
	 * It contains a callback-function, which is called for every registered
	 * component. So every component is informed, if the status of highlighted
	 * objects has changed.
	 */
	public static interface Updatable {
		/**
		 * @param j
		 *            job, which was changed
		 * @param mouseover
		 *            true= mouse is over this object
		 * @param mousedown
		 *            true= mouse is pressed on this object
		 */
		public void updateStatus(ObjectType j, boolean mouseover, boolean mousedown);
	}

	/**
	 * Saves all state-attributes, which can be changed by the user via
	 * mouse-interaction.
	 */
	private static class State {

		public boolean mouseDown, mouseOver;

		public State() {
			mouseDown = false;
			mouseOver = false;
		}
	}

	// lastMouseOver and lastMouseDown might be null at startup
	private ObjectType lastMouseOver;// saves the last object, which was touched
										// by the mouse-cursor
	private ObjectType lastMouseDown;// saves the last object, on which the
										// mouse clicked

	private HashMap<ObjectType, State> mapping;

	private final ArrayList<Updatable> components;// registered components,
													// whose call-back-functions
													// are called

	/**
	 * Initialize object-status attributes.
	 * 
	 * @param lguiItem
	 *            LML-data-handler, which groups this handler and others to a
	 *            set of LMLHandler. This instance is needed to notify all
	 *            LMLHandler, if any data of the LguiType-instance was changed.
	 */
	public ObjectStatus(ILguiItem lguiItem, LguiType lgui) {
		super(lguiItem, lgui);

		mapping = new HashMap<ObjectType, State>();
		components = new ArrayList<Updatable>();

		lguiItem.addListener(new ILguiListener() {

			public void handleEvent(ILguiUpdatedEvent e) {
				updateData(e.getLgui());
			}
		});
	}

	/**
	 * 
	 * Add component to internal memory. The component is then registered for
	 * call-back.
	 * 
	 * @param up
	 */
	public void addComponent(Updatable up) {
		if (!components.contains(up)) {
			components.add(up);
		}
	}

	/**
	 * @return last and current object, on which the user is pressing the mouse
	 */
	public ObjectType getLastMouseDown() {
		return lastMouseDown;
	}

	/**
	 * @return last and current object, which was touched by the mouse-cursor or
	 *         null
	 */
	public ObjectType getLastMouseOver() {
		return lastMouseOver;
	}

	/**
	 * @return true {@code <=>} at least one job has mousedown=true
	 */
	public boolean isAnyMouseDown() {
		return lastMouseDown != null;
	}

	/**
	 * @return true {@code <=>} at least one job has mouseover=true
	 */
	public boolean isAnyMouseover() {
		return lastMouseOver != null;
	}

	/**
	 * @param obj
	 * @return true, if mouse is pressed on this object
	 */
	public boolean isMouseDown(ObjectType obj) {
		if (obj == null) {
			return false;
		}

		if (mapping.containsKey(obj)) {
			return mapping.get(obj).mouseDown;
		} else {// Otherwise insert new pair into mapping-instance
			mapping.put(obj, new State());
			return false;
		}
	}

	/**
	 * @param obj
	 * @return true, if the connected object is currently touched by the cursor
	 */
	public boolean isMouseOver(ObjectType obj) {
		if (obj == null) {
			return false;
		}

		if (mapping.containsKey(obj)) {
			return mapping.get(obj).mouseOver;
		} else {// Otherwise insert new pair into mapping-instance
			mapping.put(obj, new State());
			return false;
		}
	}

	/**
	 * Sets mousedown-property for object obj. Should be called, if mouse is
	 * pressed on the obj object.
	 * 
	 * @param obj
	 */
	public void mouseDown(ObjectType obj) {
		if (obj == null) {
			return;
		}

		if (!mapping.containsKey(obj)) {
			mapping.put(obj, new State());
		}

		final State az = mapping.get(obj);

		if (!az.mouseDown) {
			az.mouseDown = true;
			lastMouseDown = obj;
			informAll(obj, az.mouseOver, true);
		}
	}

	/**
	 * Sets mousedown-property for object with id oid. Should be called, if
	 * mouse is pressed on the object.
	 * 
	 * @param oid
	 *            id of ObjectType-instance
	 */
	public void mouseDown(String oid) {
		final ObjectType object = lguiItem.getOIDToObject().getObjectById(oid);
		mouseDown(object);
	}

	/**
	 * Sets mouse-over-property for obj to false. This method is used, if the
	 * mouse exits the object obj.
	 * 
	 * @param obj
	 */
	public void mouseexit(ObjectType obj) {
		if (obj == null) {
			return;
		}

		if (!mapping.containsKey(obj)) {
			mapping.put(obj, new State());
		}

		final State az = mapping.get(obj);

		if (az.mouseOver) {
			az.mouseOver = false;
			lastMouseOver = null;
			informAll(obj, false, az.mouseDown);
		}
	}

	/**
	 * Sets mouse-over-property for object with given oid to false. This method
	 * is used, if the mouse exits the object.
	 * 
	 * @param oid
	 *            id of ObjectType-instance
	 */
	public void mouseExit(String oid) {
		final ObjectType object = lguiItem.getOIDToObject().getObjectById(oid);
		mouseexit(object);
	}

	/**
	 * If it is not known, which was the last focused job, call this function.
	 * Last focused objects are set to unfocused.
	 */
	public void mouseExitLast() {
		mouseexit(lastMouseOver);
		mouseUp(lastMouseDown);
	}

	/**
	 * Call this function, if the object obj is focussed by mouse-over. Does
	 * Mouseexit for jobs, which were focussed before, does normal
	 * mouse-over-action. Finally calls a mouseup for the last pressed job,
	 * except the last job is equal to obj.
	 * 
	 * @param obj
	 */
	public void mouseOver(ObjectType obj) {
		if (obj == null) {
			return;
		}

		if (obj == lastMouseOver) {
			return;
		}

		if (obj != lastMouseOver) {
			mouseExitLast();
		}
		if (obj != lastMouseDown) {
			mouseUp(lastMouseDown);
		}

		lastMouseOver = obj;
		justMouseover(obj);
	}

	/**
	 * Call this function, if the object with id oid is focussed by mouse-over.
	 * Does Mouseexit for jobs, which were focussed before, does normal
	 * mouse-over-action. Finally calls a mouseup for the last pressed job,
	 * except the last job is equal to obj.
	 * 
	 * @param oid
	 *            id of ObjectType-instance
	 */
	public void mouseOver(String oid) {
		final ObjectType object = lguiItem.getOIDToObject().getObjectById(oid);
		mouseOver(object);
	}

	/**
	 * Call this function, if mouse releases on this object.
	 * 
	 * @param obj
	 */
	public void mouseUp(ObjectType obj) {
		if (obj == null) {
			return;
		}

		// Release the last object, on which the mouse pressed
		if (lastMouseDown != obj) {
			mouseUp(lastMouseDown);
			mouseExitLast();
			mouseOver(obj);
		}

		if (!mapping.containsKey(obj)) {
			mapping.put(obj, new State());
		}
		final State az = mapping.get(obj);

		if (az.mouseDown) {
			az.mouseDown = false;
			lastMouseDown = null;
			informAll(obj, az.mouseOver, false);
		}
	}

	/**
	 * Call this function, if mouse releases on this object.
	 * 
	 * @param oid
	 *            id of ObjectType-instance
	 */
	public void mouseUp(String oid) {
		final ObjectType object = lguiItem.getOIDToObject().getObjectById(oid);
		mouseUp(object);
	}

	/**
	 * Remove a component from updater-list. This component's update-function
	 * wont be called after calling this function.
	 * 
	 * @param up
	 *            updatable Component, which should not be updated any more
	 */
	public void removeComponent(Updatable up) {
		if (components.contains(up)) {
			components.remove(up);
		}
	}

	/**
	 * Call this method, if lml-model changed. The new model is passed to this
	 * handler. All getter-functions accessing the handler will then return
	 * data, which is collected from this new model
	 * 
	 * @param model
	 *            new lml-data-model
	 */
	public void updateData(LguiType model) {
		reset();
	}

	/**
	 * Inform all components when an object-state updates
	 */
	private void informAll(ObjectType obj, boolean mouseover, boolean mousedown) {
		for (int i = 0; i < components.size(); i++) {
			components.get(i).updateStatus(obj, mouseover, mousedown);
		}
	}

	/**
	 * Set the mouseover-property for object obj to true Use this method, if the
	 * mouse moves over an object.
	 * 
	 * @param obj
	 */
	private void justMouseover(ObjectType obj) {

		if (obj == null) {
			return;
		}

		if (!mapping.containsKey(obj)) {
			mapping.put(obj, new State());
		}

		final State az = mapping.get(obj);

		if (!az.mouseOver) {
			az.mouseOver = true;
			informAll(obj, true, az.mouseDown);
		}
	}

	/**
	 * Reset all object states.
	 */
	private void reset() {
		mapping = new HashMap<ObjectType, State>();
		lastMouseOver = null;
		lastMouseDown = null;
	}

}
