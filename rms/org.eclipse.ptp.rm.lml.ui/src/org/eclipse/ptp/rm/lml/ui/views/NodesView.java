/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, Carsten Karbach, FZ Juelich
 */

package org.eclipse.ptp.rm.lml.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.ui.providers.LMLViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Claudia Knobloch, Carsten Karbach
 * 
 *         Based on original work by Greg Watson, Clement Chu and Daniel (JD) Barboza
 * 
 */
public class NodesView extends LMLViewPart {
	private final class LguiListener implements ILMLListener {
		public void handleEvent(IJobListSortedEvent e) {
		}
	}
	
	private Composite composite = null;
	private Composite nodedisplayView = null;
	public Viewer viewer;
	public ILguiItem fLguiItem = null;
	private final ILMLListener lguiListener = new LguiListener();
	private final ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager(); 
	
	/**
	 * 
	 */
	public NodesView() {
		super();
	}

	public void createPartControl(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));	
		
		ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
		fLguiItem = lmlManager.getSelectedLguiItem();
		createNodedisplayView();
		lmlManager.addListener(lguiListener, this.getClass().getName());
	}

	/*
	 * Method required so the class can extends ViewPart
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

	
	private void createNodedisplayView() {
	
		if (fLguiItem != null) {
			this.setPartName(fLguiItem.getNodedisplayAccess().toString());
			nodedisplayView = new NodedisplayView(fLguiItem, fLguiItem.getNodedisplayAccess().getNodedisplays().get(0), composite);
			composite.layout();
		} else {
			setPartName("NodedisplayView");
		}

	}

	public void prepareDispose() {
		lmlManager.removeListener(lguiListener);
	}
	
}
