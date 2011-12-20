/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A basic tree data structure. It was built for storing file paths to be excluded from synchronization, but there is nothing
 * specific to sync. Note, however, that it is designed to handle absolute paths (chains). Items are added, removed, and
 * searched as lists that always start at the root of the tree. Also, operations are defined in a way that makes sense for
 * a file filter but may not make sense for other uses. Specifically, it is implemented to take prefixes into account. See
 * comments for details.
 *
 * Note: This could be made generic easily enough by replacing "String" with a generic type.
 *
 */
public class BasicTree {
	private BasicTree parent = null;
	private ArrayList<BasicTree> children = new ArrayList<BasicTree>();
	private String value = null;
	
	/**
	 * Create an empty tree with only the sentinel node.
	 */
	public BasicTree() {
		// Nothing to do
	}
	
	private BasicTree(BasicTree p, List<String> v) {
		assert(p != null);
		assert(v.size() > 0);
		parent = p;
		this.addInternal(v);
	}

	/**
	 * Convenience method so users can add a single item without wrapping it in a list.
	 * @param item
	 */
	public void add(String item) {
		this.add(Arrays.asList(item));
	}

	/**
	 * Add a chain of items to the list.
	 * Note that existing chains with the same prefix are deleted, since it is assumed that we now want all chains with that
	 * prefix to be filtered.
	 *
	 * @param items
	 * 			must not contain null values or else no items are added.
	 */
	public void add(List<String> items) {
		LinkedList<String> itemsCopy = new LinkedList<String>();
		itemsCopy.addAll(items);
		this.addInternal(itemsCopy);
	}
	private void addInternal(List<String> items) {
		if (items.size() == 0 || items.contains(null)) {
			return;
		}

		// All nodes except the sentinel node should take values from the list
		if (parent != null) {
			value = items.remove(0);
			if (items.size() == 0) {
				children.clear();
				return;
			}
		}

		// Pass list to child with same next value
		for (BasicTree t : children) {
			if (t.value.equals(items.get(0))) {
				t.addInternal(items);
				return;
			}
		}

		// If no such child, start a new chain
		children.add(new BasicTree(this, items));
	}
	
	/**
	 * Remove a chain of items from the list. Note that all chains with the given chain as prefix are removed.
	 *
	 * @param items
	 * @return whether the items were found and removed
	 */
	public boolean remove(List<String> items) {
		LinkedList<String> itemsCopy = new LinkedList<String>();
		itemsCopy.addAll(items);
		return this.removeInternal(itemsCopy);
	}
	private boolean removeInternal(List<String> items) {
		if (items.size() == 0) {
			return false;
		}

		BasicTree matchingChild = null;
		for (BasicTree t : children) {
			if (t.value.equals(items.get(0))) {
				matchingChild = t;
			}
		}

		if (matchingChild == null) {
			return false;
		}
		
		if (items.size() == 1) {
			children.remove(matchingChild);
			if (parent != null && children.isEmpty()) {
				parent.children.remove(this);
			}
			return true;
		} else {
			items.remove(0);
			boolean found = matchingChild.removeInternal(items);
			if (found && parent != null && children.isEmpty()) {
				parent.children.remove(this);
			}
			return found;
		}
	}
	
	/**
	 * Return whether a given chain of items is in the list. Note that paths are considered to be contained if a prefix of
	 * the path is in the tree.
	 * 
	 * @param items
	 * @return whether the chain of items is in the tree
	 */
	public boolean contains(List<String> items) {
		LinkedList<String> itemsCopy = new LinkedList<String>();
		itemsCopy.addAll(items);
		return this.containsInternal(itemsCopy);
	}
	private boolean containsInternal(List<String> items) {
		// Found prefix (or entire chain), so it is contained
		if (this.children.size() == 0) {
			return true;
		}

		// Not filtered if there are filtered children.
		if (items.size() == 0) {
			return false;
		}
		
		for (BasicTree t : children) {
			if (t.value.equals(items.get(0))) {
				items.remove(0);
				return t.containsInternal(items);
			}
		}
		
		return false;
	}
	
	/**
	 * Returns all of the chains in the tree.
	 * @return list of chains (list of items)
	 */
	public List<List<String>> getItems() {
		List<List<String>> chains = new LinkedList<List<String>>();

		// Interior nodes
		for (BasicTree bt : this.children) {
			List<List<String>> childChains = bt.getItems();
			if (parent != null) {
				for (List<String> c : childChains) {
					c.add(0, value);
				}
			}
			chains.addAll(childChains);
		}
		
		// Leaf nodes
		if (chains.size() == 0 && parent != null) {
			List<String> newChain = new LinkedList<String>();
			newChain.add(value);
			chains.add(newChain);
		}

		return chains;
	}
}
