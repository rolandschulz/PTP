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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.cell.sputiming.ui.parse.InstructionLine;
import org.eclipse.ptp.cell.sputiming.ui.parse.ParsedTimingFile;
import org.eclipse.ptp.cell.sputiming.ui.parse.TimingFileLine;
import org.eclipse.ptp.cell.sputiming.ui.views.DiagramBar;
import org.eclipse.ptp.cell.sputiming.ui.views.DiagramVector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


public class TimeDiagram extends Composite {

	private static int NOPE  = 0;
	private static int BAR   = 1;
	private static int TEXT  = 2;
	
	private Color LINE_NUMBER_COLOR = new Color(null, 70, 70, 70);
	
	private GridLayout gridLayout;
	
	private int componentWidth  = 0;
	private int componentHeight = 0;
	
	private int stepSize  = 10;
	private int maxTime   = 0;
	private int barHeight = 23;
	
	private List lineNumberLabels       = new ArrayList();
	private List instructionLabels      = new ArrayList();
	private int  widestLineNumberLabel  = 0;
	private int  widestInstructionLabel = 0;
	
	public TimeDiagram(Composite parent, ParsedTimingFile timingFile) {
		super(parent, SWT.NONE);
		this.maxTime = timingFile.getMaxTime();
		init(timingFile);
	}//constructor

	private void init(ParsedTimingFile timingFile) {
		
		gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth       = 0;
		gridLayout.marginHeight      = 0;
		gridLayout.verticalSpacing   = 0;
		gridLayout.horizontalSpacing = 0;
		this.setLayout(gridLayout);
		
		buildBars(timingFile);
	
	}//init
	
	private void buildBars(ParsedTimingFile timingFile) {

		int lineCount = 0;
		Iterator it = timingFile.getLineVector().iterator();
		while(it.hasNext()) {
		
			TimingFileLine line = (TimingFileLine) it.next();
			
			if(line instanceof InstructionLine) {
				
				lineCount++;
				
				InstructionLine instructionLine = (InstructionLine) line;
				
				buildInstructionBar(
						lineCount,
						instructionLine.getLineNumber(),
						instructionLine.getInstruction(),
						instructionLine.getParallelInfo(),
						instructionLine.getTimeInfo().getPosition(),
						instructionLine.getTimeInfo().getNopesCount(),
						instructionLine.getTimeInfo().getInstructionSize(),
						instructionLine.getPipelineNumber());
				
			}
			/*
			else
			if (line instanceof NonInstructionLine) {
				NonInstructionLine nonInstructionLine = (NonInstructionLine) line;
				
				buildNonInstructionBlock(
						lineCount,
						nonInstructionLine.getLineNumber(),
						nonInstructionLine.getLine());
			}
			//if(..instanceof)-chain
			*/
			
		}//while
		
		this.pack();
		processLabels();

	}//private method

	private void buildNonInstructionBlock(int lineCount, int lineNumber, String line) {
		
		Color lineBackground = getBackgroundColor(lineCount);
		createLineLabel(lineNumber, lineBackground);

		CLabel nonInstructionLabel = new CLabel(this, SWT.RIGHT);
		nonInstructionLabel.setText(line);
		nonInstructionLabel.setForeground(LINE_NUMBER_COLOR);
		nonInstructionLabel.setBackground(lineBackground);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.horizontalSpan = 2;
		nonInstructionLabel.setLayoutData(gridData);
		
		this.componentHeight += this.barHeight;
		
	}

	private void processLabels() {
		this.widestLineNumberLabel  = getTheWidestCLabel(lineNumberLabels);
		this.widestInstructionLabel = getTheWidestCLabel(instructionLabels);
	}//private method

	private int getTheWidestCLabel(List labelsList) {
		int      widestLabel = 0;
		Iterator it          = labelsList.iterator();
		while(it.hasNext()) {
			CLabel currentLabel = (CLabel) it.next();
			int    currentWidth = currentLabel.getSize().x;
			if(currentWidth > widestLabel) {
				widestLabel = currentWidth;
			}//if
		}//while
		return widestLabel;
	}//private method
	
	private void buildInstructionBar(
			int lineCount, int lineNumber, String instruction, int parallelInfo,
			int initial, int nopeSize, int size, int colorIndex) {
		
		Color lineBackground = getBackgroundColor(lineCount);
		createLineLabel(lineNumber, lineBackground);
		
		CLabel instructionLabel = new CLabel(this, SWT.LEFT);
		instructionLabel.setText(instruction);
		
		//Getting current system font
		Font currentFont = new Font(null, "arial", 10, SWT.NORMAL); //$NON-NLS-1$
		if(parallelInfo == InstructionLine.COULDBE) {
			currentFont = new Font(null, "arial", 10, SWT.ITALIC); //$NON-NLS-1$
		}
		else
		if(parallelInfo == InstructionLine.YES) {
			currentFont = new Font(null, "arial", 10, SWT.BOLD); //$NON-NLS-1$
		}//if-else
		instructionLabel.setFont(currentFont);
		
		//Setting others instruction label attributes
		instructionLabel.setForeground(getColorByIndex(colorIndex, TEXT));
		instructionLabel.setBackground(lineBackground);
		instructionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		instructionLabels.add(instructionLabel);
		
		DiagramVector dVector = new DiagramVector();
		
		if(nopeSize > 0) {
			dVector.addBlock(initial, nopeSize, getColorByIndex(colorIndex, NOPE));
			dVector.addBlock(initial+nopeSize, size, getColorByIndex(colorIndex, BAR));
		}
		else {
			dVector.addBlock(initial, size, getColorByIndex(colorIndex, BAR));			
		}//if(..)-else
		
		DiagramBar bar = new DiagramBar(this, lineBackground, this.maxTime, this.barHeight, this.maxTime*this.stepSize);
		bar.setDiagramVector(dVector);
		
		if(this.componentWidth < bar.getSize().x) {
			this.componentWidth = bar.getSize().x;
		}//if(..)
		
		this.componentHeight += bar.getSize().y;
		
	}//private method

	private void createLineLabel(int lineNumber, Color lineBackground) {
		CLabel lineLabel = new CLabel(this, SWT.RIGHT);
		lineLabel.setForeground(LINE_NUMBER_COLOR);
		lineLabel.setBackground(lineBackground);
		lineLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		lineLabel.setText(Integer.toString(lineNumber));		
		lineNumberLabels.add(lineLabel);
	}
	
	private Color getBackgroundColor(int lineCount) {
		if((lineCount % 2) == 0) {
			return new Color(null, 175, 175, 175);
		}
		else {
			return new Color(null, 185, 185, 185);
		}//if-else
	}

	private Color getColorByIndex(int colorIndex, int type) {
		if(type == NOPE) {
			if(colorIndex == 0)
				return new Color(null, 150, 150, 200);
			else
			if(colorIndex == 1)
				return new Color(null, 150, 200, 150);
		}	
		else
		if(type == BAR) {
			if(colorIndex == 0)
				return new Color(null, 0, 0, 200);
			else
			if(colorIndex == 1)
				return new Color(null, 0, 200, 0);
		}
		else
		if(type == TEXT) {
			if(colorIndex == 0)
				return new Color(null, 0, 0, 130);
			else
			if(colorIndex == 1)
				return new Color(null, 0, 130, 0);
		}

		return new Color(null, 255, 10, 10);

	}//private method
	
	public Point getSize() {
		return new Point(
				this.widestLineNumberLabel + this.widestInstructionLabel + this.componentWidth,
				this.componentHeight);
	}
	
}//class
