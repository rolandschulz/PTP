/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch
 */

package org.eclipse.ptp.rm.lml.ui.views;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.ui.UIUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Claudia Knobloch
 * 
 *         Based on original work by Greg Watson, Clement Chu and Daniel (JD)
 *         Barboza
 * 
 */
public class NodesView extends ViewPart {
	private final class LguiListener implements ILMLListener {
		public void handleEvent(IJobListSortedEvent e) {
		}

		public void handleEvent(ILguiAddedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					fLguiItem = lmlManager.getSelectedLguiItem();
					if (!nodedisplayView.isDisposed()) {
						nodedisplayView.update(fLguiItem);
						nodedisplayView.setVisible(true);
					}
				}
			});

		}

		public void handleEvent(ILguiRemovedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					if (!nodedisplayView.isDisposed()) {
						nodedisplayView.setVisible(false);
					}
					fLguiItem = null;
				}
			});
		}

		public void handleEvent(IMarkObjectEvent event) {
			if (fLguiItem != null) {
				fLguiItem.getObjectStatus().mouseDown(event.getOid());
			}
		}

		public void handleEvent(ISelectedObjectChangeEvent event) {
			if (fLguiItem != null) {
				fLguiItem.getObjectStatus().mouseOver(event.getOid());
			}
		}

		public void handleEvent(ITableColumnChangeEvent e) {

		}

		public void handleEvent(IUnmarkObjectEvent event) {
			if (fLguiItem != null) {
				fLguiItem.getObjectStatus().mouseUp(event.getOid());
			}
		}

		public void handleEvent(IUnselectedObjectEvent event) {
			if (fLguiItem != null) {
				final ObjectType object = fLguiItem.getOIDToObject().getObjectById(event.getOid());
				fLguiItem.getObjectStatus().mouseexit(object);
			}

		}

		public void handleEvent(IViewUpdateEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					fLguiItem = lmlManager.getSelectedLguiItem();
					if (!nodedisplayView.isDisposed()) {
						nodedisplayView.update(fLguiItem);
						nodedisplayView.setVisible(true);
					}
				}
			});
		}
	}

	private Composite composite = null;
	private NodedisplayView nodedisplayView = null;
	public Viewer viewer;
	public ILguiItem fLguiItem = null;
	private final ILMLListener lguiListener = new LguiListener();
	private final LMLManager lmlManager = LMLManager.getInstance();

	/**
	 * 
	 */
	public NodesView() {
		super();
	}

	private void createNodedisplayView() {

		if (!composite.isDisposed()) {
			nodedisplayView = new NodedisplayView(null, null, composite);
			if (fLguiItem != null) {
				nodedisplayView.update(fLguiItem);
			}
			composite.layout();
		}

	}

	@Override
	public void createPartControl(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		fLguiItem = lmlManager.getSelectedLguiItem();
		lmlManager.addListener(lguiListener, this.getClass().getName());
		fLguiItem = lmlManager.getSelectedLguiItem();
		createNodedisplayView();
	}

	public void generateNodesdisplay() {
		fLguiItem = lmlManager.getSelectedLguiItem();
		createNodedisplayView();
	}

	@Override
	public void dispose() {
		lmlManager.removeListener(lguiListener);
	}

	/*
	 * Method required so the class can extends ViewPart
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

}
