/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland Schulz - initial implementation

 *******************************************************************************/
package org.eclipse.ptp.rm.proxy.core.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.ptp.proxy.runtime.server.ElementIDGenerator;
import org.eclipse.ptp.utils.core.RangeSet;

// TODO: Auto-generated Javadoc
/**
 * The Class ElementManager.
 */
public class ElementManager {

	/** The key to element. */
	private Map<String, IElement> keyToElement;
	// private Map<String, T> elementIDToElement; //only required if requests to
	// getElementByElementID are common
	// public ElementManager() {
	// keyToElement = new HashMap<String, Element>();
	// // elementIDToElement = new HashMap<String, T>();
	// }
	private ElementManager addedElements;
	private ElementManager removedElements;
	private ElementManager changedElements;

	/**
	 * Instantiates a new element manager.
	 */
	public ElementManager() {
		keyToElement = new HashMap<String, IElement>();
	}

	/**
	 * Instantiates a new element manager.
	 * 
	 * @param elements
	 *            the elements
	 */
	private ElementManager(Collection<IElement> elements) {
		keyToElement = new HashMap<String, IElement>();
		for (IElement element : elements) {
			keyToElement.put(element.getKey(), element);
		}
	}

	/**
	 * Adds the content of other ElementManager to this one
	 * 
	 * @param other
	 *            ElementManager to be added
	 */
	private void add(ElementManager other) {
		keyToElement.putAll(other.keyToElement);
	}

	/**
	 * Adds the element.
	 * 
	 * @param element
	 *            the element
	 */
	void addElement(IElement element) {
		// should this check for complete element and throw exception otherwise?
		keyToElement.put(element.getKey(), element);
		// elementIDToElement.put(element.getElementID(), element);
	}

	/**
	 * @return added elements by last update
	 */
	public ElementManager getAddedElements() {
		return addedElements;
	}

	/**
	 * @return changed elements by last update
	 */
	public ElementManager getChangedElements() {
		return changedElements;
	}

	/**
	 * Returns new ElementManager with the elements in this ElementManager which
	 * are also (by key) in list
	 * 
	 * @param other
	 *            ResourceManager to compare to
	 * @return a ElementManager with the common elements
	 */
	public ElementManager getCommon(ElementManager other) {
		// get common Keys
		Set<String> keys = new HashSet<String>(getElementIDs());
		keys.retainAll(other.getElementIDs());
		// get Elements with common Key
		return getElementsByKeys(keys);

	}

	/**
	 * Gets the element by element id.
	 * 
	 * @param elementID
	 *            the element id
	 * @return the element by element id
	 */
	public IElement getElementByElementID(int elementID) {
		for (IElement element : getElements()) {
			if (element.getElementID() == elementID) {
				return element;
			}
		}
		return null;
		// return elementIDToElement.get(elementID);
	}

	// private Collection<String> getParentKeys() {
	// Set<String> parentKeys = new HashSet<String>();
	// for (IElement element: keyToElement.values()) {
	// parentKeys.add(element.getParentKey());
	// }
	// return parentKeys;
	// }

	/**
	 * Gets the element by key.
	 * 
	 * @param key
	 *            the key
	 * @return the element by key
	 */
	IElement getElementByKey(String key) {
		return keyToElement.get(key);
	}

	/**
	 * Gets the element id by key.
	 * 
	 * @param key
	 *            the key
	 * @return the element id by key
	 */
	public int getElementIDByKey(String key) {
		return getElementByKey(key).getElementID();
	}

	/**
	 * @return
	 */
	private Set<String> getElementIDs() {
		return keyToElement.keySet();
	}

	/**
	 * Gets the element IDs as RangeSet.
	 * 
	 * @return RangeSet of the element IDs
	 */
	public RangeSet getElementIDsAsRange() {
		RangeSet range = new RangeSet();
		for (IElement element : getElements()) {
			range.add(element.getElementID());
		}
		return range;
	}

	/**
	 * @return
	 */
	private Collection<IElement> getElements() {
		return keyToElement.values();
	}

	/**
	 * Gets the elements by keys.
	 * 
	 * @param keys
	 *            the keys
	 * @return the elements by keys
	 */
	private ElementManager getElementsByKeys(Set<String> keys) {
		ElementManager ret = new ElementManager();
		for (String key : keys) {
			ret.addElement(getElementByKey(key));
		}
		return ret;
	}

	/**
	 * Returns a new ElementManager with those elements which are common to both
	 * lists (by ID) but are different in their values
	 * 
	 * @param other
	 *            ElementManager to compare to
	 * @return the ElementManager with the modified elements (those elements
	 *         from this ElementManager)
	 * 
	 */
	private ElementManager getModified(ElementManager other) {
		Collection<IElement> commonElements = getCommon(other).getElements();
		commonElements.removeAll(other.getElements());
		return new ElementManager(commonElements);
	}

	/**
	 * @return removed elements by last update
	 */
	public ElementManager getRemovedElements() {
		return removedElements;
	}

	// returns those elements (considering only key) which are in this list but
	// not in the passed list
	/**
	 * Minus.
	 * 
	 * @param other
	 *            the list
	 * @return the element manager
	 */
	private ElementManager minus(ElementManager other) {
		// get the matching keys first
		Set<String> keys = new HashSet<String>(getElementIDs());
		keys.removeAll(other.getElementIDs());
		// get the elements for these keys
		return getElementsByKeys(keys);
	}

	/**

	 */
	public List<String> serialize() {
		List<String> eventArgs = new ArrayList<String>();
		;
		// create the Args for one sendEvent (all Element Arguments with the
		// same parent)
		eventArgs.add(new Integer(size()).toString());// Number of Elements
		for (IElement element : getElements()) {
			eventArgs.addAll(element.toStringArray()); // Convert Element
		}
		return eventArgs;
	}

	/*
	 * Serialize into List of List of Strings. The inner List contains all
	 * Elements with the same parent In the Format: ParentKey, NumberOfElements,
	 * Elements The outer List are these Lists for all different parents
	 */
	public List<List<String>> serializeSplittedByParent() {
		List<List<String>> result = new ArrayList<List<String>>();

		// first split by Parent
		Map<String, ElementManager> parentKey_addedElements_Map = splitByParent();

		// loop over all sets of elements with the same parent
		for (Entry<String, ElementManager> pKey_elements : parentKey_addedElements_Map
				.entrySet()) {
			String parentKey = pKey_elements.getKey();
			ElementManager elements = pKey_elements.getValue();
			List<String> eventArgs = new ArrayList<String>();
			eventArgs.add(parentKey); // This is the key not the ID (has to be
										// changed before sending)
			eventArgs.addAll(elements.serialize());
			result.add(eventArgs);
		}
		return result;
	}

	/**
	 * Assign element ids Because the ElmentManger is not sorted the assignment
	 * is random.
	 * 
	 * @param rangeSet
	 *            the range of ids
	 */
	private void setElementIDs(RangeSet rangeSet) {
		Iterator<String> i = rangeSet.iterator();
		for (IElement e : getElements()) {
			e.setElementID(Integer.parseInt((i.next())));
		}
	}

	public int size() {
		return keyToElement.size();
	}

	/**
	 * Returns a list of Elements split by Parent
	 * 
	 * @return the map
	 */
	public Map<String, ElementManager> splitByParent() {
		Map<String, ElementManager> ret = new HashMap<String, ElementManager>();

		for (IElement element : getElements()) {
			String parentKey = element.getParentKey();
			// System.err.println("splitByParent: "+element.getKey()+","+parentKey);
			if (!ret.containsKey(parentKey)) {
				ret.put(parentKey, new ElementManager());
			}
			ret.get(parentKey).addElement(element);
		}
		return ret;
	}

	@Override
	public String toString() {
		return keyToElement.toString();
	}

	public void update(Collection<IElement> eList) {
		ElementManager newElements = new ElementManager(eList);
		// ElementManager common = getCommon(newElements);

		addedElements = newElements.minus(this);
		addedElements.setElementIDs(ElementIDGenerator.getInstance()
				.getUniqueIDs(addedElements.size()));
		removedElements = this.minus(newElements);
		changedElements = newElements.getModified(this);

		add(addedElements);
		getElementIDs().removeAll(removedElements.getElementIDs());

		// We need to fill copy elementID to addedElements and attributes to
		// this
		// Because we both need the subsection and need to keep the main
		// ElementManager up to date
		for (IElement element : changedElements.getElements()) {
			// System.err.println("Changed: "+element.getKey()+","+element+","+
			// getElementByKey(element.getKey()));
			IElement ownElement = getElementByKey(element.getKey());
			element.setElementID(ownElement.getElementID()); // copy elementID
																// to
																// addedElements
			ownElement.setAttributes(element); // copy attributes to this
		}
	}

}
