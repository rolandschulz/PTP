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
 * AttributeIndexSet associates sets of indices
 * with attribute values.
 * The index sets are enforced to be
 * disjoint among distinct attribute
 * values.
 * <br><br>
 * AttributeIndexSet will make copies
 * of any attributes set within it.
 * <br>
 * AttributeIndexSet will not modify
 * the attributes stored in it, nor allow anyone else
 * to.
 *
 * @author Randy M. Roberts
 *
 * @param <A> the attribute type that implements IAttribute
 */
public class AttributeIndexSet<A extends IAttribute<?, A, ?>> 
	implements Cloneable {
	
	private final Map<A, BitSet> indexSetMap = new HashMap<A, BitSet>();
	private final BitSet totalIndexSet;
	
	public AttributeIndexSet() {
		// an empty index set
		this.totalIndexSet = new BitSet();
	}

	/**
	 * Copy constructor
	 * @param other
	 */
	public AttributeIndexSet(AttributeIndexSet<A> other) {
		this.totalIndexSet = (BitSet) other.totalIndexSet.clone();
		for (Map.Entry<A, BitSet> entry : other.indexSetMap.entrySet()) {
			// no need to clone the attribute, since AttributeIndexSet promises
			// not to allow anyone to modify the attribute values
			A attr = entry.getKey();

			// we do need to clone the index bitsets, they could be modified
			// externally
			this.indexSetMap.put(attr, (BitSet) entry.getValue().clone());
		}
	}

	/**
	 * @param nIndices the initial number of indices
	 */
	public AttributeIndexSet(int nIndices) {
		// an empty index set
		this.totalIndexSet = new BitSet(nIndices);
	}

	/**
	 * clear this AttributeIndexSet 
	 */
	public void clear() {
		this.totalIndexSet.clear();
		this.indexSetMap.clear();
	}
	
	/**
	 * Clear the attributes for the given indices
	 * 
	 * @param indices
	 * @throws NullPointerException if provided a null indices
	 */
	public void clearAttributes(BitSet indices) {
		if (indices == null) {
			throw new NullPointerException("indices may not be null");
		}
	
		// remove these indices from the total indices
		totalIndexSet.andNot(indices);
		
		// remove these indices from the bitsets for all values of A
		// need to copy entry set to avoid concurrent modifications
		List<Entry<A, BitSet>> entrySet = new ArrayList<Entry<A,BitSet>>(indexSetMap.entrySet());
		for (Map.Entry<A, BitSet> entry : entrySet) {
			final BitSet indicesForAttr = entry.getValue();
			indicesForAttr.andNot(indices);
			// if indicesForAttr is empty, remove it completely
			if (indicesForAttr.isEmpty()) {
				final A attr = entry.getKey();
				indexSetMap.remove(attr);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public AttributeIndexSet<A> clone() {
		return new AttributeIndexSet<A>(this);
	}
	
	/**
	 * @param index
	 * @return null if not found
	 */
	public A getAttribute(int index) {
		// see if we can find the index in the extant
		// index sets
		for (Map.Entry<A, BitSet> entry : indexSetMap.entrySet()) {
			BitSet indicesForATmp = entry.getValue();
			if (indicesForATmp.get(index)) {
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
	 * one index
	 */
	public Set<A> getAttributes() {
		Set<A> valueSet = new HashSet<A>();
		for (A value : indexSetMap.keySet()) {
			// clone the attribute, so it cannot
			// be modified from outside
			valueSet.add(value.copy());
		}
		return valueSet;
	}
	
	/**
	 * @return the set of all indices that have attributes set
	 */
	public BitSet getIndexSet() {
		// clone, so we don't let anyone modify
		// our index bitset
		return (BitSet) totalIndexSet.clone();
	}
	
	/**
	 * Retrieve the BitSet representing the indices that
	 * contain this value for their attribute.
	 * 
	 * @param attribute may not be null
	 * @return the bitset of all indices containing that value
	 * for the attribute
	 * @throws NullPointerException if provided a null attribute
	 */
	public BitSet getIndexSet(A attribute) {
		if (attribute == null) {
			throw new NullPointerException("attribute may not be null");
		}

		BitSet bitSet = indexSetMap.get(attribute);
		if (bitSet == null) {
			return new BitSet();
		}
		// return a copy, so no one can modify it.
		return (BitSet) bitSet.clone();
	}

	/**
	 * Retrieve an AttributeIndexSet for a subSet of its indices.
	 * 
	 * @param indices
	 * @return the AttributeIndexSet determined from the intersection of the
	 * given indices with the indices contained in this AttributeIndexSet
	 */
	public AttributeIndexSet<A> getSubset(BitSet indices) {
		
		AttributeIndexSet<A> subSet = new AttributeIndexSet<A>();

		// perform the intersection on the subSet's totalIndices
		subSet.totalIndexSet.or(this.totalIndexSet);
		subSet.totalIndexSet.and(indices);
		
		// if the intersection is empty, then there is no
		// more to do
		if (subSet.totalIndexSet.isEmpty()) {
			return subSet;
		}
		
		// build the subSet from the current index sets
		for (Map.Entry<A, BitSet> entry : indexSetMap.entrySet()) {
			// do not modify the original bitset, so use a clone
			BitSet indicesForATmp = (BitSet) entry.getValue().clone();
			// perform the intersection on the bitset
			indicesForATmp.and(indices);

			if (!indicesForATmp.isEmpty()) {
				// no need to clone here, since AttributeIndexSet promises
				// not to allow anyone to modify the attribute values
				A aTmp = entry.getKey();
				subSet.indexSetMap.put(aTmp, indicesForATmp);
			}
		}
		return subSet;
	}
	
	/**
	 * @param indices
	 * @return whether there are indices in common with
	 * those that contain an attribute.
	 */
	public boolean intersects(BitSet indices) {
		return totalIndexSet.intersects(indices);
	}
	
	/**
	 * @return whether any indices contain an attribute
	 */
	public boolean isEmpty() {
		return totalIndexSet.isEmpty();
	}
	
	/**
	 * Sets the given attribute for the given indices.
	 * These indices will have their previous attributes
	 * cleared, and set to this value.
	 * @param attribute may not be null
	 * @param indices may not be null
	 * 
	 * @throws NullPointerException if provided a null indices or attribute
	 */
	public void setAttribute(A attribute, BitSet indices) {
		if (attribute == null) {
			throw new NullPointerException("attribute may not be null");
		}
		if (indices == null) {
			throw new NullPointerException("indices may not be null");
		}
		
		// union these indices with the total indices
		totalIndexSet.or(indices);

		// remove these indices from the bitsets for all **other** values of A
		// to maintain disjoint indices sets
		
		// need to copy entry set to avoid concurrent modifications
		List<Entry<A, BitSet>> entrySet = new ArrayList<Entry<A,BitSet>>(indexSetMap.entrySet());
		for (Map.Entry<A, BitSet> entry : entrySet) {
			A aTmp = entry.getKey();
			if (!attribute.equals(aTmp)) {
				BitSet indicesForATmp = entry.getValue();
				
				// remove indices from indicesForATmp
				indicesForATmp.andNot(indices);
				// if indicesForATmp is empty, remove it completely
				if (indicesForATmp.isEmpty()) {
					indexSetMap.remove(aTmp);
				}
			}
		}
		
		// add these indices to the value of attribute
		BitSet indicesForAttribute = indexSetMap.get(attribute);
		if (indicesForAttribute == null) {
			indicesForAttribute = new BitSet(indices.cardinality());
			indexSetMap.put(attribute, indicesForAttribute);
		}
		// perform a union
		indicesForAttribute.or(indices);
	}
}
