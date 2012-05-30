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

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.utils.core.DisjointBitSets;
import org.eclipse.ptp.utils.core.ICopier;

/**
 * {@code AttributeIndexSet} associates sets of indices with attribute values. The index sets are enforced to be disjoint among
 * distinct attribute values. <br>
 * <br>
 * {@code AttributeIndexSet} will make copies of any attributes set within it. <br>
 * {@code AttributeIndexSet} will not modify the attributes stored in it, nor allow anyone else to.
 * 
 * @author Randy M. Roberts
 * 
 * @param <A>
 *            the attribute type that implements {@code IAttribute}
 * @since 4.0
 */
@Deprecated
public class AttributeIndexSet<A extends IAttribute<?, A, ?>> implements Iterable<DisjointBitSets.Entry<A>> {

	private final DisjointBitSets<A> disjointBitSets;

	public AttributeIndexSet() {
		disjointBitSets = new DisjointBitSets<A>(new ICopier<A>() {
			public A copy(A attr) {
				return attr.copy();
			}
		});
	}

	/**
	 * Copy constructor
	 * 
	 * @param other
	 */
	public AttributeIndexSet(AttributeIndexSet<A> other) {
		this.disjointBitSets = other.disjointBitSets.copy();
	}

	/**
	 * @param subSet
	 */
	private AttributeIndexSet(DisjointBitSets<A> subSet) {
		// no need to copy the subSet since it is derived
		// from a copy
		this.disjointBitSets = subSet;
	}

	/**
	 * @param nIndices
	 *            the initial number of indices
	 */
	public AttributeIndexSet(int nIndices) {
		// an empty index set
		this.disjointBitSets = new DisjointBitSets<A>(nIndices, new ICopier<A>() {
			public A copy(A attr) {
				return attr.copy();
			}
		});
	}

	/**
	 * Unions the indices with the indices of the given attribute.
	 * 
	 * @param attribute
	 *            may not be null
	 * @param indices
	 *            may not be null
	 * 
	 * @throws NullPointerException
	 *             if provided a null indices or attribute
	 */
	public void addIndicesToAttribute(A attribute, BitSet indices) {
		disjointBitSets.or(attribute, indices);
	}

	/**
	 * clear this {@code AttributeIndexSet}
	 */
	public void clear() {
		disjointBitSets.clear();
	}

	/**
	 * Clear the indices associated with the given attribute
	 * 
	 * @param attribute
	 */
	public void clearAttribute(A attribute) {
		disjointBitSets.remove(attribute);
	}

	/**
	 * Remove the given indices from all of the attribute's BitSets
	 * 
	 * @param clearedIndices
	 * @throws NullPointerException
	 *             if provided a null indices
	 */
	public void clearIndices(BitSet clearedIndices) {
		disjointBitSets.andNot(clearedIndices);
	}

	/**
	 * @param attribute
	 * @param clearedIndices
	 */
	public void clearIndicesForAttribute(A attribute, BitSet clearedIndices) {
		disjointBitSets.andNot(attribute, clearedIndices);
	}

	/**
	 * @return a copy of this {@code AttributeIndexSet}
	 */
	public AttributeIndexSet<A> copy() {
		return new AttributeIndexSet<A>(this);
	}

	/**
	 * @param index
	 * @return null if not found
	 */
	public A getAttribute(int index) {
		return disjointBitSets.getKey(index);
	}

	/**
	 * @return the set of all attributes that are contained by at least one index
	 */
	public Set<A> getAttributes() {
		return disjointBitSets.getKeys();
	}

	/**
	 * @return the set of all indices that have attributes set
	 */
	public BitSet getIndexSet() {
		return disjointBitSets.getUnion();
	}

	/**
	 * Retrieve the {@code BitSet} representing the indices that contain this value for their attribute.
	 * 
	 * @param attribute
	 *            may not be null
	 * @return the {@code BitSet} of indices containing this value for the attribute
	 * @throws NullPointerException
	 *             if provided a null attribute
	 */
	public BitSet getIndexSet(A attribute) {
		return disjointBitSets.getBitSet(attribute);
	}

	/**
	 * Retrieve an {@code AttributeIndexSet} for a subSet of its indices.
	 * 
	 * @param indices
	 * @return the {@code AttributeIndexSet} determined from the intersection of the given indices with the indices contained in
	 *         this {@code AttributeIndexSet}
	 */
	public AttributeIndexSet<A> getSubset(BitSet indices) {
		return new AttributeIndexSet<A>(disjointBitSets.getSubset(indices));
	}

	/**
	 * @param indices
	 * @return whether there are indices in common with those that contain an attribute.
	 */
	public boolean intersects(BitSet indices) {
		return disjointBitSets.intersects(indices);
	}

	/**
	 * @return whether any indices contain an attribute
	 */
	public boolean isEmpty() {
		return disjointBitSets.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<DisjointBitSets.Entry<A>> iterator() {
		return disjointBitSets.iterator();
	}

	/**
	 * Sets the given attribute for the given indices. These indices will have their previous attributes cleared, and set to this
	 * value.
	 * 
	 * @param attribute
	 *            may not be null
	 * @param indices
	 *            may not be null
	 * 
	 * @throws NullPointerException
	 *             if provided a null indices or attribute
	 */
	public void setIndicesOfAttribute(A attribute, BitSet indices) {
		disjointBitSets.put(attribute, indices);
	}

	@Override
	public String toString() {
		return disjointBitSets.toString();
	}
}
