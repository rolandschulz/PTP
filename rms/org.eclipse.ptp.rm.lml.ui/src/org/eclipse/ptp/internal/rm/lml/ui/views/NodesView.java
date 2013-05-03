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

package org.eclipse.ptp.internal.rm.lml.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.INodedisplayZoomEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableFilterEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.listeners.INodedisplayZoomListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.ui.UIUtils;
import org.eclipse.ptp.rm.lml.ui.providers.AbstractNodedisplayView;
import org.eclipse.ptp.rm.lml.ui.providers.NodedisplayView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Based on original work by Greg Watson, Clement Chu and Daniel (JD) Barboza
 * 
 */
public class NodesView extends ViewPart {
	private final class LguiListener implements ILMLListener {
		@Override
		public void handleEvent(ILguiAddedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					fLguiItem = lmlManager.getSelectedLguiItem();
					if (!nodedisplayView.isDisposed()) {
						nodedisplayView.update(fLguiItem);
						nodedisplayView.setVisible(true);
						checkActionStates();
					}
					if (fLguiItem != null
							&& fLguiItem.getNodedisplayAccess() != null) {
						setPartName(fLguiItem.getNodedisplayAccess().getTitle(
								gid));
					}
				}
			});

		}

		@Override
		public void handleEvent(ILguiRemovedEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					if (!nodedisplayView.isDisposed()) {
						nodedisplayView.setVisible(false);
					}
					fLguiItem = null;
					setPartName("System Monitoring"); //$NON-NLS-1$
					checkActionStates();
				}
			});
		}

		@Override
		public void handleEvent(IMarkObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getObjectStatus() != null) {
				fLguiItem.getObjectStatus().mouseDown(event.getOid());
			}
		}

		@Override
		public void handleEvent(ISelectObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getObjectStatus() != null) {
				fLguiItem.getObjectStatus().mouseOver(event.getOid());
			}
		}

		@Override
		public void handleEvent(ITableFilterEvent event) {
			// TODO Auto-generated method stub

		}

		@Override
		public void handleEvent(ITableSortedEvent e) {
		}

		@Override
		public void handleEvent(IUnmarkObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getObjectStatus() != null) {
				fLguiItem.getObjectStatus().mouseUp(event.getOid());
			}
		}

		@Override
		public void handleEvent(IUnselectedObjectEvent event) {
			if (fLguiItem != null && fLguiItem.getOIDToObject() != null
					&& fLguiItem.getObjectStatus() != null) {
				final ObjectType object = fLguiItem.getOIDToObject()
						.getObjectById(event.getOid());
				fLguiItem.getObjectStatus().mouseexit(object);
			}

		}

		@Override
		public void handleEvent(IViewUpdateEvent event) {
			UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					fLguiItem = lmlManager.getSelectedLguiItem();
					if (!nodedisplayView.isDisposed()) {
						nodedisplayView.update(fLguiItem);
						nodedisplayView.setVisible(true);
						checkActionStates();
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
	private AbstractNodedisplayView nodedisplayView = null;
	public Viewer viewer;
	public ILguiItem fLguiItem = null;
	private final ILMLListener lguiListener = new LguiListener();
	private final LMLManager lmlManager = LMLManager.getInstance();
	private String gid = null;

	/**
	 * Actions for changing the level of detail shown by the nodedisplay.
	 * incAction = increases level of detail
	 * decAction = decreases level of detail
	 */
	private Action incAction;

	private Action decAction;

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

		// Check level of detail buttons on zoom event
		nodedisplayView.addZoomListener(new INodedisplayZoomListener() {
			@Override
			public void handleEvent(INodedisplayZoomEvent event) {
				UIUtils.safeRunSyncInUIThread(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						checkActionStates();
					}
				});
			}
		});

		createToolbar();

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

	/**
	 * Checks if the action buttons for changing the detail level
	 * have to be enabled or disabled.
	 */
	protected void checkActionStates() {
		// If Nodedisplay is not visible, because not created or currently removed, deactivate toolbarbuttons
		if (nodedisplayView == null || !nodedisplayView.isVisible()) {
			decAction.setEnabled(false);
			incAction.setEnabled(false);
		}
		else {
			decAction.setEnabled(nodedisplayView.getShownMaxLevel() > nodedisplayView.getMinimumLevelOfDetail());

			incAction.setEnabled(nodedisplayView.getShownMaxLevel() < nodedisplayView.getMaximumNodedisplayDepth());
		}
	}

	/**
	 * Creates additional buttons for changing the maximum shown depth
	 * in the nodedisplay.
	 */
	protected void createToolbar() {
		incAction = new Action(Messages.NodesView_0) {
			@Override
			public void run() {
				if (nodedisplayView != null) {
					nodedisplayView.setMaxLevel(nodedisplayView.getShownMaxLevel() + 1);
					nodedisplayView.update();
				}
				checkActionStates();
			}
		};

		decAction = new Action(Messages.NodesView_1) {
			@Override
			public void run() {
				if (nodedisplayView != null) {
					if (nodedisplayView.getShownMaxLevel() > nodedisplayView.getMinimumLevelOfDetail()) {
						nodedisplayView.setMaxLevel(nodedisplayView.getShownMaxLevel() - 1);
						nodedisplayView.update();
					}
				}
				checkActionStates();
			}
		};

		// Set icons
		final ImageDescriptor zoomIn = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD);
		final ImageDescriptor zoomOut = PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_BACK);
		incAction.setImageDescriptor(zoomIn);
		decAction.setImageDescriptor(zoomOut);

		incAction.setToolTipText(Messages.NodesView_2);
		decAction.setToolTipText(Messages.NodesView_3);

		final IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(decAction);
		toolbarManager.add(incAction);

		checkActionStates();
	}
}
