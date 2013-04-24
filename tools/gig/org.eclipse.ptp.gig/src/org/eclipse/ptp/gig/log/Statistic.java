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
package org.eclipse.ptp.gig.log;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.ptp.gig.util.GIGUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.statushandlers.StatusManager;

/*
 * represents a statistic reported near the end of the gklee kernel
 */
public class Statistic {

	// the ave refers to a percentage, and should equal num*100/total
	private final int aveWarp, numWarps, totalWarps, aveBI, numBIs, totalBIs;
	private final IFile logFile;
	private final int line;

	public Statistic(int aveWarp, int numWarps, int totalWarps, int aveBI, int numBIs, int totalBIs, IFile logFile, int line) {
		this.aveWarp = aveWarp;
		this.numWarps = numWarps;
		this.totalWarps = totalWarps;
		this.aveBI = aveBI;
		this.numBIs = numBIs;
		this.totalBIs = totalBIs;
		this.logFile = logFile;
		this.line = line;
	}

	public int getAverageBank() {
		return this.aveBI;
	}

	public int getAverageWarp() {
		return aveWarp;
	}

	/*
	 * Sets up the Statistic in the tree
	 */
	public void setupTree(Tree tree, Integer optionalLine) {
		tree.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				final Widget widget = event.widget;
				if (widget instanceof Tree) {
					final Tree tree = (Tree) widget;
					final TreeItem item = tree.getItem(new Point(event.x, event.y));
					final Object o = item.getData();
					if (o instanceof Integer) {
						final Integer line = (Integer) o;
						try {
							GIGUtilities.jumpToLine(logFile, line);
						} catch (final CoreException e) {
							StatusManager.getManager().handle(
									new Status(IStatus.ERROR, GIGPlugin.PLUGIN_ID, Messages.CORE_EXCEPTION, e));
						}
					}
				}
			}

		});
		TreeItem item, subItem;
		item = new TreeItem(tree, SWT.NONE);
		item.setText(Messages.STATISTICS);
		item.setData(optionalLine);

		subItem = new TreeItem(item, SWT.NONE);
		subItem.setText(String.format(Messages.WARP_STATISTIC, aveWarp, numWarps, totalWarps));
		subItem.setData(line);

		subItem = new TreeItem(item, SWT.NONE);
		subItem.setText(String.format(Messages.BLOCK_STATISTIC, aveBI, numBIs, totalBIs));
		subItem.setData(line);
	}

}
