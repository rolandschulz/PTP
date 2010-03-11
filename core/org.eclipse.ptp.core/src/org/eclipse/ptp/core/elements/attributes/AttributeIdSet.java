/*******************************************************************************
* Copyright (c) 2010 Los Alamos National Laboratory and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* 	LANL - Initial API and implementation
*******************************************************************************/

package org.eclipse.ptp.core.elements.attributes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.ptp.core.attributes.IAttribute;

/**
 * AttributeIdSet associates sets of Ids
 * with attribute values.
 * The id sets are enforced to be
 * disjoint among distinct attribute
 * values.
 * <br><br>
 * AttributeIdSet will make copies
 * of any attributes set within it.
 * <br>
 * AttributeIdSet will not modify
 * the attributes stored in it, nor allow anyone else
 * to.
 *
 * @author Randy M. Roberts
 *
 * @param <A> the attribute type that implements IAttribute
 */
public class AttributeIdSet<A extends IAttribute<?, A, ?>> 
	implements Cloneable {
	
	private final Map<A, BitSet> idSetMap = new HashMap<A, BitSet>();
	private final BitSet totalIdSet;
	
	public AttributeIdSet() {
		// an empty id set
		this.totalIdSet = new BitSet();
	}

	/**
	 * Copy constructor
	 * @param other
	 */
	public AttributeIdSet(AttributeIdSet<A> other) {
		this.totalIdSet = (BitSet) other.totalIdSet.clone();
		for (Map.Entry<A, BitSet> entry : other.idSetMap.entrySet()) {
			// no need to clone the attribute, since AttributeIdSet promises
			// not to allow anyone to modify the attribute values
			A attr = entry.getKey();

			// we do need to clone the id bitsets, they could be modified
			// externally
			this.idSetMap.put(attr, (BitSet) entry.getValue().clone());
		}
	}

	/**
	 * @param nIds the initial number of Ids
	 */
	public AttributeIdSet(int nIds) {
		// an empty id set
		this.totalIdSet = new BitSet(nIds);
	}

	/**
	 * clear this AttributeIdSet 
	 */
	public void clear() {
		this.totalIdSet.clear();
		this.idSetMap.clear();
	}
	
	/**
	 * Clear the attributes for the given Ids
	 * 
	 * @param ids
	 * @throws NullPointerException if provided a null Ids
	 */
	public void clearAttributes(BitSet ids) {
		if (ids == null) {
			throw new NullPointerException("ids may not be null");
		}
	
		// remove these Ids from the total Ids
		totalIdSet.andNot(ids);
		
		// remove these Ids from the bitsets for all values of A
		// need to copy entry set to avoid concurrent modifications
		List<Entry<A, BitSet>> entrySet = new ArrayList<Entry<A,BitSet>>(idSetMap.entrySet());
		for (Map.Entry<A, BitSet> entry : entrySet) {
			final BitSet idsForAttr = entry.getValue();
			idsForAttr.andNot(ids);
			// if idsForAttr is empty, remove it completely
			if (idsForAttr.isEmpty()) {
				final A attr = entry.getKey();
				idSetMap.remove(attr);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public AttributeIdSet<A> clone() {
		return new AttributeIdSet<A>(this);
	}
	
	/**
	 * @param id
	 * @return null if not found
	 */
	public A getAttribute(int id) {
		// see if we can find the id in the extant
		// id sets
		for (Map.Entry<A, BitSet> entry : idSetMap.entrySet()) {
			BitSet idsForATmp = entry.getValue();
			if (idsForATmp.get(id)) {
				// clone the value, so that no one can
				// modify our value
				A aTmp = entry.getKey();
				return aTmp.copy();
			}
		}
		// if not just return null
		return null;
	}
	
	/**
	 * @return the set of all attributes that are contained by at least
	 * one id
	 */
	public Set<A> getAttributes() {
		Set<A> valueSet = new HashSet<A>();
		for (A value : idSetMap.keySet()) {
			// clone the attribute, so it cannot
			// be modified from outside
			valueSet.add(value.copy());
		}
		return valueSet;
	}
	
	/**
	 * @return the set of all Ids that have attributes set
	 */
	public BitSet getIdSet() {
		// clone, so we don't let anyone modify
		// our id bitset
		return (BitSet) totalIdSet.clone();
	}
	
	/**
	 * Retrieve the BitSet representing the Ids that
	 * contain this value for their attribute.
	 * 
	 * @param attribute may not be null
	 * @return the bitset of all Ids containing that value
	 * for the attribute
	 * @throws NullPointerException if provided a null attribute
	 */
	public BitSet getIdSet(A attribute) {
		if (attribute == null) {
			throw new NullPointerException("attribute may not be null");
		}

		BitSet bitSet = idSetMap.get(attribute);
		if (bitSet == null) {
			return new BitSet();
		}
		// return a copy, so no one can modify it.
		return (BitSet) bitSet.clone();
	}

	/**
	 * Retrieve an AttributeIdSet for a subSet of its Ids.
	 * 
	 * @param ids
	 * @return the AttributeIdSet determined from the intersection of the
	 * given Ids with the Ids contained in this AttributeIdSet
	 */
	public AttributeIdSet<A> getSubset(BitSet ids) {
		
		AttributeIdSet<A> subSet = new AttributeIdSet<A>();

		// perform the intersection on the subSet's totalIds
		subSet.totalIdSet.or(this.totalIdSet);
		subSet.totalIdSet.and(ids);
		
		// if the intersection is empty, then there is no
		// more to do
		if (subSet.totalIdSet.isEmpty()) {
			return subSet;
		}
		
		// build the subSet from the current id sets
		for (Map.Entry<A, BitSet> entry : idSetMap.entrySet()) {
			// do not modify the original bitset, so use a clone
			BitSet idsForATmp = (BitSet) entry.getValue().clone();
			// perform the intersection on the bitset
			idsForATmp.and(ids);

			if (!idsForATmp.isEmpty()) {
				// no need to clone here, since AttributeIdSet promises
				// not to allow anyone to modify the attribute values
				A aTmp = entry.getKey();
				subSet.idSetMap.put(aTmp, idsForATmp);
			}
		}
		return subSet;
	}
	
	/**
	 * @param ids
	 * @return whether there are Ids in common with
	 * those that contain an attribute.
	 */
	public boolean intersects(BitSet ids) {
		return totalIdSet.intersects(ids);
	}
	
	/**
	 * @return whether any Ids contain an attribute
	 */
	public boolean isEmpty() {
		return totalIdSet.isEmpty();
	}
	
	/**
	 * Sets the given attribute for the given Ids.
	 * These Ids will have their previous attributes
	 * cleared, and set to this value.
	 * @param attribute may not be null
	 * @param ids may not be null
	 * 
	 * @throws NullPointerException if provided a null Ids or attribute
	 */
	public void setAttribute(A attribute, BitSet ids) {
		if (attribute == null) {
			throw new NullPointerException("attribute may not be null");
		}
		if (ids == null) {
			throw new NullPointerException("ids may not be null");
		}
		
		// union these Ids with the total Ids
		totalIdSet.or(ids);

		// remove these Ids from the bitsets for all **other** values of A
		// to maintain disjoint id sets
		
		// need to copy entry set to avoid concurrent modifications
		List<Entry<A, BitSet>> entrySet = new ArrayList<Entry<A,BitSet>>(idSetMap.entrySet());
		for (Map.Entry<A, BitSet> entry : entrySet) {
			A aTmp = entry.getKey();
			if (!attribute.equals(aTmp)) {
				BitSet idsForATmp = entry.getValue();
				
				// remove Ids from idsForATmp
				idsForATmp.andNot(ids);
				// if idsForATmp is empty, remove it completely
				if (idsForATmp.isEmpty()) {
					idSetMap.remove(aTmp);
				}
			}
		}
		
		// add these Ids to the value of attribute
		BitSet idsForAttribute = idSetMap.get(attribute);
		if (idsForAttribute == null) {
			idsForAttribute = new BitSet(ids.cardinality());
			idSetMap.put(attribute, idsForAttribute);
		}
		// perform a union
		idsForAttribute.or(ids);
	}
}
