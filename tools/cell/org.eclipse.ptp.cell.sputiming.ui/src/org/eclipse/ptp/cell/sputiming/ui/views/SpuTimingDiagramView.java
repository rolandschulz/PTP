/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.sputiming.ui.views;

import org.eclipse.ptp.cell.sputiming.ui.Activator;
import org.eclipse.ptp.cell.sputiming.ui.debug.Debug;
import org.eclipse.ptp.cell.sputiming.ui.parse.ParsedTimingFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;


public class SpuTimingDiagramView extends ViewPart {

	TimeDiagram diagram;
	Composite parent;
	ScrolledComposite sc;
	
	public void createPartControl(Composite parent) {
		this.parent = parent;

		Debug.POLICY.trace(Debug.DEBUG_UI, "Start creating view"); //$NON-NLS-1$
		parent.setLayout(new FillLayout());
	    // Create the ScrolledComposite to scroll horizontally and vertically
	    sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
	    
	    Activator.getDefault().setView(this);
	    ParsedTimingFile parsedTiming = Activator.getDefault().getTiming();
		if(parsedTiming != null) {		    
		    // Creating the time diagram itself
			diagram = new TimeDiagram(sc, parsedTiming);
			
			// Putting the time diagram into a scroll pane
			sc.setContent(diagram);
			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);
			sc.setMinSize(diagram.getSize().x, diagram.getSize().y);
		}// if(!null)
		
		Debug.POLICY.trace(Debug.DEBUG_UI, "Finished creating view"); //$NON-NLS-1$
	}// private method

	public void setFocus() {
		if (diagram != null) {
			diagram.setFocus();
		}
	}// overriden method

	public void invalidateTiming() {
		Debug.POLICY.trace(Debug.DEBUG_UI, "Request to update view"); //$NON-NLS-1$
		Display display = parent.getDisplay();
		display.syncExec(
				new Runnable() {
					public void run(){
						try {
							Debug.POLICY.trace(Debug.DEBUG_UI, "Start view update thread"); //$NON-NLS-1$
							if (diagram != null) {
									Debug.POLICY.trace(Debug.DEBUG_UI, "Dispose old sputiming diagram"); //$NON-NLS-1$
								diagram.dispose();
							}
							ParsedTimingFile parsedTiming = Activator.getDefault().getTiming();
							if(parsedTiming != null) {
							    // Creating the time diagram itself
									Debug.POLICY.trace(Debug.DEBUG_UI, "Creating new sputiming diagram"); //$NON-NLS-1$
								diagram = new TimeDiagram(sc, parsedTiming);
								
								// Putting the time diagram into a scroll pane
								sc.setContent(diagram);
								sc.setExpandHorizontal(true);
								sc.setExpandVertical(true);
								sc.setMinSize(diagram.getSize().x, diagram.getSize().y);
							}// if(!null)
						Debug.POLICY.trace(Debug.DEBUG_UI, "Finished view update thread"); //$NON-NLS-1$
						} catch (Exception e) {
							Debug.POLICY.error(Debug.DEBUG_UI, e);
							Debug.POLICY.logError(e);
						}
					}
				}
		);
	}

	@Override
	public void dispose() {
		super.dispose();
		Activator.getDefault().setView(null);
	}
} // class
