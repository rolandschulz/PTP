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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.core.elements.ILguiItem;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILguiListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Claudia Knobloch
 * 
 *         Based on original work by Greg Watson, Clement Chu and Daniel (JD) Barboza
 * 
 */
public class NodesView extends ViewPart {
	 /**
	  * 
	  * @author Claudia Knobloch
	  *
	  */
	private final class LguiListener implements ILguiListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.lml.core.listeners.ILguiListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.events.ILguiAddedEvent)
		 */
		public void handleEvent(ILguiAddedEvent e) {
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.lml.core.listeners.ILguiListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.events.ILguiAddedEvent)
		 */
		public void handleEvent(ILguiRemovedEvent e) {
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.rm.lml.core.listeners.ILguiListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.events.IJobListSortEvent)
		 */
		public void handleEvent(IJobListSortedEvent e) {
		}
	}
	
	private Canvas canvas = null;

	public NodesView() {
		super();
	}

	@Override
	public void createPartControl(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		canvas = new Canvas(composite, SWT.NONE);
		Composite computer = createComputer(canvas);
		ILMLManager mm = LMLCorePlugin.getDefault().getLMLManager();

		synchronized (mm) {
		}
		refreshView();
	}

	/*
	 * Method required so the class can extends ViewPart
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

	private void refreshView() {
	}
	
	private int rowcount = 9;
	private int nodecount = 8;
	private int cpucount = 8;
	
	private Composite createComputer(Composite parent){
		GridLayout grid = new GridLayout(1, true);
		grid.horizontalSpacing = 0;
		grid.verticalSpacing = 16;
		grid.marginHeight = 4;
		grid.marginWidth = 4;
		parent.setLayout(grid);
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		for (int i = 0; i < rowcount; i++) {
			Composite in = createRow(parent);
			in.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE ));
			in.setLayoutData(data);
		}
		
		return parent;
	}
	
	
	private Composite createRow(Composite parent){
		
		Composite frame = new Composite(parent, SWT.None);
		
		GridLayout grid = new GridLayout(nodecount/2, true);
		grid.horizontalSpacing = 4;
		grid.verticalSpacing = 4;
		grid.marginHeight = 4;
		grid.marginWidth = 4;
		frame.setLayout(grid);
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		for (int i = 0; i < nodecount; i++) {
			Composite in = createNode(frame);
			in.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY ));
			in.setLayoutData(data);
		}
		
		return frame;
		
	}
	
	
	private Composite createNode(Composite parent) {
	    final Composite frame = new Composite(parent, SWT.None);
	    frame.addListener(SWT.Paint, new Listener() {
	        public void handleEvent(Event event) {
	            final int MARGIN = 2;
	            Point size = frame.getSize();
	            int drawItemHeight = (size.y - MARGIN * (cpucount + 1)) / cpucount;
	            int drawItemWidth = (size.x - MARGIN * (cpucount + 1)) / cpucount;
	            int x = MARGIN;
	            event.gc.setBackground(event.display.getSystemColor(SWT.COLOR_RED));
	            for (int i = 0; i < cpucount; i++) {
	                int y = MARGIN;
	                for (int j = 0; j < cpucount; j++) {
	                    event.gc.fillRectangle(x, y, drawItemWidth, drawItemHeight);
	                    y += drawItemHeight + MARGIN;
	                }
	                x += drawItemWidth + MARGIN;
	            }
	        }
	    });
	    return frame;
	}
}