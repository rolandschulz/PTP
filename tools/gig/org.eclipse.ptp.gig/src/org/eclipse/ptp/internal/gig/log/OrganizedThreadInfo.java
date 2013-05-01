/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.internal.gig.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.gig.GIGPlugin;
import org.eclipse.ptp.internal.gig.messages.Messages;
import org.eclipse.ptp.internal.gig.util.GIGUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.statushandlers.StatusManager;

/*
 * This organizes a list of ThreadInfo objects by block then by thread
 */
public class OrganizedThreadInfo {

	// this is currently 32, but NVIDIA has warned that this may change
	private static int threadsPerWarp;

	public static void setThreadsPerWarp(int threadsPerWarp) {
		OrganizedThreadInfo.threadsPerWarp = threadsPerWarp;
	}

	/*
	 * block number maps to info about that block, which has warp number map to info about that warp,
	 * which has thread number map to info about that thread, which is a simple list of info.
	 * This is uninitialized prior to calling the organize method.
	 */
	private final Map<Integer, Map<Integer, Map<Integer, List<TwoThreadInfo>>>> blocks;

	// this contains all the data prior to the organize method
	private final List<TwoThreadInfo> tempList = new ArrayList<TwoThreadInfo>();

	public OrganizedThreadInfo() {
		blocks = new HashMap<Integer, Map<Integer, Map<Integer, List<TwoThreadInfo>>>>();
	}

	/*
	 * adds the element to the unorganized list, be sure to organize before using
	 */
	public void add(TwoThreadInfo twoThreadInfo) {
		tempList.add(twoThreadInfo);
	}

	/*
	 * tells if the organized data structure is empty
	 */
	public boolean isEmpty() {
		return blocks.isEmpty();
	}

	/*
	 * This method is meant to be called only one time, and only after all elements have been added to the temp list
	 */
	public void organize() {
		for (final TwoThreadInfo twoThreadInfo : tempList) {
			organize(twoThreadInfo, 0);
			organize(twoThreadInfo, 1);
		}
	}

	/*
	 * puts the twoThreadInfo object into its appropriate spot based on which thread is specified (O or 1)
	 */
	private void organize(TwoThreadInfo twoThreadInfo, int i) {
		ThreadInfo threadInfo;
		if (i == 0) {
			threadInfo = twoThreadInfo.getThreadInfo0();
		}
		else {
			threadInfo = twoThreadInfo.getThreadInfo1();
		}
		try {
			// we find out where in the data structure to put it, and initialize that element if needed, then put it in there.
			final int block = threadInfo.getBlock();
			final int thread = threadInfo.getThread();
			final int warp = thread / threadsPerWarp;
			if (blocks.containsKey(block)) {
				final Map<Integer, Map<Integer, List<TwoThreadInfo>>> warps = blocks.get(block);
				if (warps.containsKey(warp)) {
					final Map<Integer, List<TwoThreadInfo>> threads = warps.get(warp);
					if (threads.containsKey(thread)) {
						final List<TwoThreadInfo> threadInfos = threads.get(thread);
						threadInfos.add(twoThreadInfo);
					}
					else {
						final List<TwoThreadInfo> threadInfos = new ArrayList<TwoThreadInfo>();
						threadInfos.add(twoThreadInfo);
						threads.put(thread, threadInfos);
					}
				}
				else {
					final Map<Integer, List<TwoThreadInfo>> threads = new HashMap<Integer, List<TwoThreadInfo>>();
					final List<TwoThreadInfo> threadInfos = new ArrayList<TwoThreadInfo>();
					threadInfos.add(twoThreadInfo);
					threads.put(thread, threadInfos);
					warps.put(warp, threads);
				}
			}
			else {
				final Map<Integer, Map<Integer, List<TwoThreadInfo>>> warps = new HashMap<Integer, Map<Integer, List<TwoThreadInfo>>>();
				final Map<Integer, List<TwoThreadInfo>> threads = new HashMap<Integer, List<TwoThreadInfo>>();
				final List<TwoThreadInfo> threadInfos = new ArrayList<TwoThreadInfo>();
				threadInfos.add(twoThreadInfo);
				threads.put(thread, threadInfos);
				warps.put(warp, threads);
				blocks.put(block, warps);
			}
		} catch (final NumberFormatException nfe) {
			/*
			 * Just ignore this improperly formatted ThreadInfo object (not all of them are "properly" formatted after all--example:
			 * deadlock doesn't specify two threads)
			 */
		}
	}

	/*
	 * Sets up in the GUI the subtree that stems from a block index
	 */
	private void setupBlockTree(TreeItem topLevel, Map<Integer, Map<Integer, List<TwoThreadInfo>>> warps, int block,
			String lowLevelLabel, IProject project) {
		final TreeItem blockTree = new TreeItem(topLevel, SWT.NONE);
		blockTree.setText(Messages.BLOCK + block);

		// sort the warps, as a map is not guaranteed to be sorted
		final Set<Integer> keys = warps.keySet();
		final int[] keysArray = new int[keys.size()];
		final Iterator<Integer> iter = keys.iterator();
		int j;
		for (int i = 0; i < keysArray.length; i++) {
			j = iter.next();
			keysArray[i] = j;
		}
		Arrays.sort(keysArray);

		// setup the each warp
		for (final int warp : keysArray) {
			setupWarpTree(blockTree, warps.get(warp), warp, lowLevelLabel, project);
		}
	}

	/*
	 * Sets up the node for a threadTree item
	 */
	private void setupThreadInfoLeaf(TreeItem twoThreadTree, ThreadInfo threadInfo, IProject project) {
		try {
			final String label = threadInfo.getLabel(project);
			final TreeItem leaf = new TreeItem(twoThreadTree, SWT.NONE);
			leaf.setText(label);
			// connect the leaf with the data so that it can be accessed on double click
			leaf.setData(threadInfo);
		} catch (final NumberFormatException nfe) {
			// the getLabel may throw this exception for items that don't have info for both threads
		}
	}

	/*
	 * sets up the subtree for the thread
	 */
	private void setupThreadTree(TreeItem warpTree, List<TwoThreadInfo> list, int thread, String lowLevelLabel, IProject project) {
		final TreeItem threadTree = new TreeItem(warpTree, SWT.NONE);
		threadTree.setText(Messages.THREAD_NUMBER + thread);

		// No sorting required, since errors don't have an obvious ordering

		for (final TwoThreadInfo twoThreadInfo : list) {
			setupTwoThreadInfoTree(threadTree, twoThreadInfo, lowLevelLabel, project);
		}
	}

	/*
	 * This will set up the tree in the GUI
	 */
	public void setupTree(Tree tree, String topLevelLabel, String emptyTopLevelLabel, String lowLevelLabel, IProject project) {
		// add a double click listener for this tree to interact with source file
		tree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// retrieve the data that was double clicked
				final Widget widget = event.widget;
				if (widget instanceof Tree) {
					final Tree tree = (Tree) widget;
					final TreeItem item = tree.getItem(new Point(event.x, event.y));
					final Object data = item.getData();
					if (data instanceof ThreadInfo) {
						// now jump to the line in the source code that we want
						final ThreadInfo info = (ThreadInfo) data;
						final IFile file = info.getFile();
						final int line = info.getLine();
						try {
							GIGUtilities.jumpToLine(file, line);
						} catch (final CoreException e) {
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.CORE_EXCEPTION, e));
						}
					}
					else if (data instanceof TwoThreadInfo) {
						// now jump to the line in the log file that we want
						final TwoThreadInfo twoInfo = (TwoThreadInfo) data;
						final IFile file = twoInfo.getFile();
						final int line = twoInfo.getLine();
						try {
							GIGUtilities.jumpToLine(file, line);
						} catch (final CoreException e) {
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.CORE_EXCEPTION, e));
						}
					}
				}
			}

		});
		// a lot of the time, there is nothing to report
		if (blocks.size() == 0) {
			// report there is nothing by creating a label that says as much
			if (emptyTopLevelLabel != null) {
				final TreeItem topLevel = new TreeItem(tree, SWT.NONE);
				topLevel.setText(emptyTopLevelLabel);
			}
			else {
				// not even worth reporting the non-existence of this
				return;
			}
		}
		else {
			// we do have something to report, set it up
			final TreeItem topLevel = new TreeItem(tree, SWT.NONE);
			topLevel.setText(topLevelLabel);

			// sort the blocks, as a map doesn't guarantee sorted
			final Set<Integer> keys = blocks.keySet();
			final int[] keysArray = new int[keys.size()];
			final Iterator<Integer> iter = keys.iterator();
			int j;
			for (int i = 0; i < keysArray.length; i++) {
				j = iter.next();
				keysArray[i] = j;
			}
			Arrays.sort(keysArray);

			for (final int block : keysArray) {
				setupBlockTree(topLevel, blocks.get(block), block, lowLevelLabel, project);
			}
		}
	}

	/*
	 * Sets up the TwoThreadInfo subtree with a node and up to two leaves
	 */
	private void setupTwoThreadInfoTree(TreeItem threadTree, TwoThreadInfo twoThreadInfo, String lowLevelLabel, IProject project) {
		final TreeItem twoThreadTree = new TreeItem(threadTree, SWT.NONE);
		twoThreadTree.setText(lowLevelLabel);
		// set data for double click retrieval
		twoThreadTree.setData(twoThreadInfo);

		setupThreadInfoLeaf(twoThreadTree, twoThreadInfo.getThreadInfo0(), project);
		setupThreadInfoLeaf(twoThreadTree, twoThreadInfo.getThreadInfo1(), project);
	}

	/*
	 * sets up a subtree with the root node being a warp
	 */
	private void setupWarpTree(TreeItem blockTree, Map<Integer, List<TwoThreadInfo>> threads, int warp, String lowLevelLabel,
			IProject project) {
		final TreeItem warpTree = new TreeItem(blockTree, SWT.NONE);
		warpTree.setText(Messages.WARP_NUMBER + warp);

		// sort the threads, since map won't guarantee sorted
		final Set<Integer> keys = threads.keySet();
		final int[] keysArray = new int[keys.size()];
		final Iterator<Integer> iter = keys.iterator();
		int j;
		for (int i = 0; i < keysArray.length; i++) {
			j = iter.next();
			keysArray[i] = j;
		}
		Arrays.sort(keysArray);

		for (final int thread : keysArray) {
			setupThreadTree(warpTree, threads.get(thread), thread, lowLevelLabel, project);
		}
	}
}
