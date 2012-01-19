/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.ui.views;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableFilterEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.ui.UIUtils;
import org.eclipse.ptp.rm.lml.ui.providers.NodedisplayView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * Based on original work by Greg Watson, Clement Chu and Daniel (JD) Barboza
 * 
 */
public class NodesView extends ViewPart {
	private final class LguiListener implements ILMLListener {
		public void handleEvent(ILguiAddedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				public void run() throws Exception {
					fLguiItem = lmlManager.getSelectedLguiItem();
					if (!nodedisplayView.isDisposed()) {
						nodedisplayView.update(fLguiItem);
						nodedisplayView.setVisible(true);
					}
					if (fLguiItem != null
							&& fLguiItem.getNodedisplayAccess() != null) {
						setPartName(fLguiItem.getNodedisplayAccess().getTitle(
								gid));
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
					setPartName("System Monitoring"); //$NON-NLS-1$
				}
			});
		}

		public void handleEvent(IMarkObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getObjectStatus() != null) {
				fLguiItem.getObjectStatus().mouseDown(event.getOid());
			}
		}

		public void handleEvent(ISelectObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getObjectStatus() != null) {
				fLguiItem.getObjectStatus().mouseOver(event.getOid());
			}
		}

		public void handleEvent(ITableFilterEvent event) {
			// TODO Auto-generated method stub

		}

		public void handleEvent(ITableSortedEvent e) {
		}

		public void handleEvent(IUnmarkObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getObjectStatus() != null) {
				fLguiItem.getObjectStatus().mouseUp(event.getOid());
			}
		}

		public void handleEvent(IUnselectedObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getOIDToObject() != null
					&& fLguiItem.getObjectStatus() != null) {
				final ObjectType object = fLguiItem.getOIDToObject()
						.getObjectById(event.getOid());
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
					if (fLguiItem != null
							&& fLguiItem.getNodedisplayAccess() != null) {
						setPartName(fLguiItem.getNodedisplayAccess().getTitle(
								gid));
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
	private String gid = null;

	/**
	 * 
	 */
	public NodesView() {
		super();
	}

	@Override
	public void createPartControl(final Composite parent) {
		gid = getViewSite().getId();
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setBackground(composite.getDisplay().getSystemColor(
				SWT.COLOR_WHITE));

		fLguiItem = lmlManager.getSelectedLguiItem();
		lmlManager.addListener(lguiListener, this.getClass().getName());

		nodedisplayView = new NodedisplayView(null, null, composite);
		if (fLguiItem != null) {
			nodedisplayView.update(fLguiItem);
		}
		composite.layout();
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
