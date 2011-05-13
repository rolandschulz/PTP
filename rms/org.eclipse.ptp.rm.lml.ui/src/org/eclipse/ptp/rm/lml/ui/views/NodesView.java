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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectType;
import org.eclipse.ptp.rm.lml.ui.providers.LMLViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Claudia Knobloch
 * 
 *         Based on original work by Greg Watson, Clement Chu and Daniel (JD)
 *         Barboza
 * 
 */
public class NodesView extends LMLViewPart {
	private final class LguiListener implements ILMLListener {
		public void handleEvent(IJobListSortedEvent e) {
		}

		public void handleEvent(ITableColumnChangeEvent e) {

		}

		public void handleEvent(ISelectedObjectChangeEvent event) {
			fLguiItem.getObjectStatus().mouseOver(event.getOid());
		}

		public void handleEvent(IMarkObjectEvent event) {
			fLguiItem.getObjectStatus().mouseDown(event.getOid());
		}

		public void handleEvent(IUnmarkObjectEvent event) {
			fLguiItem.getObjectStatus().mouseUp(event.getOid());
		}

		public void handleEvent(IUnselectedObjectEvent event) {
			ObjectType object = fLguiItem.getOIDToObject().getObjectById(event.getOid());
			fLguiItem.getObjectStatus().mouseexit(object);
		}
	}

	private Composite composite = null;
	private Composite nodedisplayView = null;
	public Viewer viewer;
	public ILguiItem fLguiItem = null;
	private String gid = null;
	private final ILMLListener lguiListener = new LguiListener();
	private final ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();

	/**
	 * 
	 */
	public NodesView() {
		super();
	}

	@Override
	public void createPartControl(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		fLguiItem = lmlManager.getSelectedLguiItem();
		lmlManager.addListener(lguiListener, this.getClass().getName());
	}

	/*
	 * Method required so the class can extends ViewPart
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
	}

	public void generateNodesdisplay(String gid) {
		this.gid = gid;
		fLguiItem = lmlManager.getSelectedLguiItem();
		createNodedisplayView();
	}

	private void createNodedisplayView() {

		if (!composite.isDisposed()) {
			if (fLguiItem != null) {
				this.setPartName(fLguiItem.getNodedisplayAccess().toString());
				nodedisplayView = new NodedisplayView(fLguiItem, fLguiItem.getNodedisplayAccess().getNodedisplays().get(0),
						composite);
				composite.layout();
			} else {
				setPartName("NodedisplayView");
			}
			composite.addDisposeListener(new DisposeListener() {

				public void widgetDisposed(DisposeEvent e) {
					lmlManager.removeView(gid);

				}

			});
		}

	}

	@Override
	public void prepareDispose() {
		lmlManager.removeListener(lguiListener);
	}

}
