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

package org.eclipse.ptp.utils.core;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code DisjointBitSets} associates disjoint {@code BitSet}'s with keys. The
 * {@code BitSet}'s are enforced to be disjoint among distinct keys. <br>
 * <br> {@code DisjointBitSets} will make copies of any keys set within it. <br>
 * {@code DisjointBitSets} will not modify the key stored in it, nor allow
 * anyone else to.
 * 
 * @author Randy M. Roberts
 * 
 * @param <K>
 *            the key type
 * @since 2.0
 */
public class DisjointBitSets<K> implements Iterable<DisjointBitSets.Entry<K>> {

	public static class Entry<X> implements Map.Entry<X, BitSet> {

		private final BitSet bitSet;
		private final X x;

		public Entry(X x, BitSet bitSet) {
			this.x = x;
			this.bitSet = bitSet;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map.Entry#getKey()
		 */
		public X getKey() {
			return x;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map.Entry#getValue()
		 */
		public BitSet getValue() {
			return bitSet;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Map.Entry#setValue(java.lang.Object)
		 */
		public BitSet setValue(BitSet value) {
			throw new UnsupportedOperationException();
		}

	}

	private final Map<K, BitSet> bitSetMap = new HashMap<K, BitSet>();
	private final BitSet unionOfBitSets;
	private ICopier<K> keyCopier;

	public DisjointBitSets() {
		this((ICopier<K>) null);
	}

	/**
	 * Copy constructor
	 * 
	 * @param other
	 */
	public DisjointBitSets(DisjointBitSets<K> other) {
		this.keyCopier = other.keyCopier;
		this.unionOfBitSets = (BitSet) other.unionOfBitSets.clone();
		for (Map.Entry<K, BitSet> entry : other.bitSetMap.entrySet()) {
			// no need to clone the key, since DisjointBitSets promises
			// not to allow anyone to modify the key values
			K key = entry.getKey();

			// we do need to clone the BitSets, they could be modified
			// via "other"
			this.bitSetMap.put(key, (BitSet) entry.getValue().clone());
		}
	}

	/**
	 * @param keyCopier
	 *            the functor used to make copies of keys in order to prevent
	 *            unwanted external modification of keys
	 * 
	 */
	public DisjointBitSets(ICopier<K> keyCopier) {
		this.keyCopier = keyCopier;
		// an empty set of BitSets
		this.unionOfBitSets = new BitSet();
	}

	/**
	 * @param nBits
	 *            the initial size of {@code BitSet}'s
	 */
	public DisjointBitSets(int nBits) {
		this(nBits, null);
	}

	/**
	 * @param nBits
	 *            the initial size of {@code BitSet}'s
	 * @param keyCopier
	 *            the functor used to make copies of keys in order to prevent
	 *            unwanted external modification of keys
	 */
	public DisjointBitSets(int nBits, ICopier<K> keyCopier) {
		this.keyCopier = keyCopier;
		// an empty set of BitSets
		this.unionOfBitSets = new BitSet(nBits);
	}

	/**
	 * Clears the bits the given set from all of the key's BitSets
	 * 
	 * @param set
	 * @throws NullPointerException
	 *             if provided a null set
	 */
	public void andNot(BitSet set) {
		if (set == null) {
			throw new NullPointerException("set may not be null");
		}

		// remove these bitset from the total bitset
		unionOfBitSets.andNot(set);

		boolean foundEmpties = false;
		// remove these bitset from the BitSets for all values of A
		for (Map.Entry<K, BitSet> entry : bitSetMap.entrySet()) {
			final BitSet indicesForAttr = entry.getValue();
			indicesForAttr.andNot(set);
			foundEmpties = foundEmpties || indicesForAttr.isEmpty();
		}

		// we may have some attributes with empty bitsets
		if (foundEmpties) {
			removeEmptyBitSets();
		}
	}

	/**
	 * @param key
	 * @param set
	 */
	public void andNot(K key, BitSet set) {
		if (key == null) {
			throw new NullPointerException("key may not be null");
		}
		if (set == null) {
			throw new NullPointerException("set may not be null");
		}
		BitSet bitset = bitSetMap.get(key);
		if (bitset == null) {
			return;
		}
		unionOfBitSets.andNot(bitset);
		bitset.andNot(set);
		if (bitset.isEmpty()) {
			bitSetMap.remove(key);
		} else {
			unionOfBitSets.or(bitset);
		}
	}

	/**
	 * clear this {@code DisjointBitSets}
	 */
	public void clear() {
		this.unionOfBitSets.clear();
		this.bitSetMap.clear();
	}

	/**
	 * @return a copy of this {@code DisjointBitSets}
	 */
	public DisjointBitSets<K> copy() {
		return new DisjointBitSets<K>(this);
	}

	/**
	 * Retrieve the {@code BitSet} representing the set that contain this value
	 * for their key.
	 * 
	 * @param key
	 *            may not be null
	 * @return the {@code BitSet} of set containing this value for the key
	 * @throws NullPointerException
	 *             if provided a null key
	 */
	public BitSet getBitSet(K key) {
		if (key == null) {
			throw new NullPointerException("key may not be null");
		}

		BitSet bitSet = bitSetMap.get(key);
		if (bitSet == null) {
			return new BitSet();
		}
		// return a copy, so no one can modify it.
		return (BitSet) bitSet.clone();
	}

	/**
	 * @param bitIndex
	 * @return null if not found
	 */
	public K getKey(int bitIndex) {
		// see if we can find the bitIndex in the extant
		// BitSets
		for (Map.Entry<K, BitSet> entry : bitSetMap.entrySet()) {
			BitSet bitSetForEntry = entry.getValue();
			if (bitSetForEntry.get(bitIndex)) {
				// clone the value, so that no one can
				// modify our value
				K entryKey = entry.getKey();
				return copyKey(entryKey);
			}
		}
		// if not just return null
		return null;
	}

	public ICopier<K> getKeyCopier() {
		return keyCopier;
	}

	/**
	 * @return the set of all keys that are contained by at least one non-empty
	 *         {@code BitSet}
	 */
	public Set<K> getKeys() {
		Set<K> keySet = new HashSet<K>();
		for (K key : bitSetMap.keySet()) {
			// copy the key, so it cannot
			// be modified from outside
			keySet.add(copyKey(key));
		}
		return keySet;
	}

	/**
	 * Retrieve an {@code DisjointBitSets} for a subSet of its {@code BitSet}.
	 * 
	 * @param bitset
	 * @return the {@code DisjointBitSets} determined from the intersection of
	 *         the given bitset with the {@code BitSet}'s contained in this
	 *         {@code DisjointBitSets}
	 */
	public DisjointBitSets<K> getSubset(BitSet bitset) {
		if (bitset == null) {
			throw new NullPointerException("bitset may not be null");
		}

		DisjointBitSets<K> subSet = new DisjointBitSets<K>(bitset.size(), this.keyCopier);

		// perform the intersection on the subSet's total unions
		subSet.unionOfBitSets.or(this.unionOfBitSets);
		subSet.unionOfBitSets.and(bitset);

		// if the intersection is empty, then there is no
		// more to do
		if (subSet.unionOfBitSets.isEmpty()) {
			return subSet;
		}

		// build the subSet from the current bitSets
		for (Map.Entry<K, BitSet> entry : bitSetMap.entrySet()) {
			// do not modify the original BitSet, so use a clone
			BitSet bitSetForEntry = (BitSet) entry.getValue().clone();
			// perform the intersection on the BitSet
			bitSetForEntry.and(bitset);

			if (!bitSetForEntry.isEmpty()) {
				// no need to copy here, since DisjointBitSets promises
				// not to allow anyone to modify the key values
				K entryKey = entry.getKey();
				subSet.bitSetMap.put(entryKey, bitSetForEntry);
			}
		}
		return subSet;
	}

	/**
	 * @return the union of all {@code BitSet}'s that have keys
	 */
	public BitSet getUnion() {
		// clone, so we don't let anyone modify
		// our BitSet
		return (BitSet) unionOfBitSets.clone();
	}

	/**
	 * @param bitset
	 * @return whether there are bitset in common with those that contain an
	 *         key.
	 * @throws NullPointerException
	 *             for null bitset
	 */
	public boolean intersects(BitSet bitset) {
		if (bitset == null) {
			throw new NullPointerException("bitset may not be null");
		}
		return unionOfBitSets.intersects(bitset);
	}

	/**
	 * @return whether any bitset contain an key
	 */
	public boolean isEmpty() {
		return unionOfBitSets.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Entry<K>> iterator() {
		final List<Entry<K>> copyList = new ArrayList<Entry<K>>(bitSetMap.size());
		for (Map.Entry<K, BitSet> entry : bitSetMap.entrySet()) {
			// clone the key, so it cannot
			// be modified from outside
			K copyEntryKey = copyKey(entry.getKey());
			BitSet copyEntryBitSet = (BitSet) entry.getValue().clone();
			copyList.add(new Entry<K>(copyEntryKey, copyEntryBitSet));
		}
		return copyList.iterator();
	}

	/**
	 * Unions the {@code BitSet} with the current {@code BitSet} of the given
	 * key.
	 * 
	 * @param key
	 *            may not be null
	 * @param bitset
	 *            may not be null
	 * 
	 * @throws NullPointerException
	 *             if provided a null {@code BitSet} or key
	 */
	public void or(K key, BitSet bitset) {
		innerSetBitSet(key, bitset, false);
	}

	/**
	 * The {@code BitSet} for this key will be replaced with {@code bitset}.
	 * 
	 * @param key
	 *            may not be null
	 * @param bitset
	 *            may not be null
	 * 
	 * @throws NullPointerException
	 *             if provided a null {@code BitSet} or key
	 */
	public void put(K key, BitSet bitset) {
		innerSetBitSet(key, bitset, true);
	}

	/**
	 * Remove the {@code BitSet} associated with the given key
	 * 
	 * @param key
	 */
	public void remove(K key) {
		if (key == null) {
			throw new NullPointerException("key may not be null");
		}
		BitSet bitset = bitSetMap.get(key);
		if (bitset == null) {
			return;
		}
		unionOfBitSets.andNot(bitset);
		bitSetMap.remove(key);
	}

	public void setKeyCopier(ICopier<K> keyCopier) {
		this.keyCopier = keyCopier;
	}

	@Override
	public String toString() {
		return bitSetMap.toString();
	}

	private K copyKey(K key) {
		if (keyCopier == null) {
			return key;
		} else {
			return keyCopier.copy(key);
		}
	}

	/**
	 * @param key
	 * @param bitset
	 * @param setting
	 *            true means overwrite, false means union
	 */
	private void innerSetBitSet(K key, BitSet bitset, boolean setting) {
		if (key == null) {
			throw new NullPointerException("key may not be null");
		}
		if (bitset == null) {
			throw new NullPointerException("bitset may not be null");
		}

		if (setting) {
			// If we are setting then remove the old bitset for the
			// key from the total
			BitSet oldAttrIndices = bitSetMap.get(key);
			if (oldAttrIndices != null) {
				unionOfBitSets.andNot(oldAttrIndices);
			}

		}

		// union these bitset with the total bitset
		unionOfBitSets.or(bitset);

		// remove these bitset from the BitSets for all **other** values of A
		// to maintain disjoint bitset sets

		// If the incoming key is already in the map
		// it will be handled in the following for loop.
		// If not then it needs to be handled as a separate
		// case.
		boolean equalsCaseHandled = false;
		boolean foundEmpties = false;

		for (Map.Entry<K, BitSet> entry : bitSetMap.entrySet()) {
			K entryKey = entry.getKey();
			BitSet bitSetForEntry = entry.getValue();
			if (!key.equals(entryKey)) {
				// remove bitset from bitSetForEntry
				bitSetForEntry.andNot(bitset);
			} else {
				if (setting) {
					bitSetForEntry.clear();
				}
				bitSetForEntry.or(bitset);
				// if there were not entries for this key
				// then this code will not be executed.
				equalsCaseHandled = true;
			}
			foundEmpties = foundEmpties || bitSetForEntry.isEmpty();
		}

		// If we didn't handle the equals case in the above for-loop,
		// then we should add a copy of the incoming bitset
		// into the map.
		if (!equalsCaseHandled) {
			bitSetMap.put(key, (BitSet) bitset.clone());
		}

		// we may have some attributes with empty BitSets
		if (foundEmpties) {
			removeEmptyBitSets();
		}
	}

	/**
	 * remove entries from the map if the BitSet is empty
	 */
	private void removeEmptyBitSets() {
		// need to copy entry set to avoid concurrent modifications
		List<Map.Entry<K, BitSet>> entryList = new ArrayList<Map.Entry<K, BitSet>>(bitSetMap.entrySet());
		for (Map.Entry<K, BitSet> entry : entryList) {
			BitSet indicesForEntry = entry.getValue();
			// if indicesForEntry is empty, remove it completely
			if (indicesForEntry.isEmpty()) {
				K key = entry.getKey();
				bitSetMap.remove(key);
			}
		}
	}
}
