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

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class DiagramBar extends Canvas implements PaintListener {

	private static final int VERTICAL_BORDER = 2;

	private int gridSize = 10;
	
	private int height;
	private int width;
	private int arcSize = 7;
	
	private int totalValue;
	
	private Color         backgroundColor;
	private DiagramVector diagramVector;
	
	public DiagramBar(Composite parent, Color backgroundColor, int totalValue, int heightPixels, int maxWidthPixels) {
		super(parent, SWT.NONE);
		this.height          = heightPixels;
		this.width           = maxWidthPixels + 2;
		this.totalValue      = totalValue;
		this.backgroundColor = backgroundColor;
		this.addPaintListener(this);	
	}//constructor

	public Point getSize() {
		return new Point(width, height);
	}
	
	public Point computeSize (int wHint, int hHint)  {
		return new Point(width, height);
	}

	public Point computeSize (int wHint, int hHint, boolean changed)  {
		return this.computeSize(wHint, hHint);
	}
	
	public void setDiagramVector(DiagramVector diagramVector) {
		this.diagramVector = diagramVector;
	}//public method
	
	public void paintControl(PaintEvent e) {

		//Filling the background
		e.gc.setBackground(backgroundColor);		
		e.gc.fillRectangle(0, 0, this.width, this.height);
		
		//Printing the grid
		for(int i = gridSize; i < this.width; i += gridSize) {
			e.gc.setForeground(new Color(null, 150, 150,  150));
			e.gc.setLineStyle(SWT.LINE_DOT);
			e.gc.drawLine(i, 0, i, height);
		}//for i

		//Making the bars itself
		if(diagramVector != null) {
			
			e.gc.setLineStyle(SWT.LINE_SOLID);
			
			Iterator it = diagramVector.iterator();
			while(it.hasNext()) {
				DiagramBlock diagramBlock = (DiagramBlock) it.next();
				int pos = (diagramBlock.getPosition() * width) / totalValue;
				int size = (diagramBlock.getSize() * width) / totalValue;
				e.gc.setAntialias(1);
				e.gc.setForeground(new Color(null, 10, 10,  10));
				e.gc.setBackground(diagramBlock.getColor());
				e.gc.fillRoundRectangle(
						pos, DiagramBar.VERTICAL_BORDER,
						size, height-(2*DiagramBar.VERTICAL_BORDER), arcSize, arcSize);
				e.gc.drawRoundRectangle(
						pos, DiagramBar.VERTICAL_BORDER,
						size, height-(2*DiagramBar.VERTICAL_BORDER), arcSize, arcSize);
			}//while
			
		}//if(!null)
		
	}//interface method

}//class
