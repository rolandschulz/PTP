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
package org.eclipse.ptp.cell.sputiming.ui.parse;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;



/**
 * @author richardm
 *
 */
public class TimingFileParser 
{
	private String timingFileName;
	
	
	/**
	 * Sets the filename attribute
	 * 
	 * @param filename
	 */
	public TimingFileParser(String filename)
	{
		timingFileName = filename;
	}
	
	public ParsedTimingFile parseTimingFile() throws IOException
	{	
		// Open file
		LineNumberReader linereader = 
			new LineNumberReader(
					new FileReader(timingFileName));
		
		// Creates a new ParsedTimingFile object
		ParsedTimingFile parsedTiming = 
			new ParsedTimingFile(timingFileName);
		
		// Loops over the timing file extracting information
		String line;
		int grpnum = 1;
		Vector linevector = 
			new Vector(50, 50);
		while((line = linereader.readLine()) != null)
		{
			if(line.charAt(0) == '0' || 
					line.charAt(0) == '1')
			{
				// Instruction line
				InstructionLine il = new InstructionLine(
						linereader.getLineNumber(), 
						grpnum);
				
				il.setPipelineNumber(Integer.parseInt(
					line.substring(0,1)));
				
//				 Pipeline info
				switch(line.charAt(1))
				{
				case ' ':
					il.setParallelInfo(
							InstructionLine.NO);
					break;
				case 'D':
					il.setParallelInfo(
							InstructionLine.YES);
					break;
				case 'd':
					il.setParallelInfo(
							InstructionLine.COULDBE);
					break;
				default:
					// TODO: Not valid! Generate a ProfileException
				}
				
				// Pipe Timing Info - column 4 to 53
				il.setPipeTimingInfo(new String(
						line.substring(3, 54)));
				
				// Instruction
				il.setInstruction(line.substring(54));
				
				// Insert line in the vector
				linevector.addElement(il);
			}	
			else	
			{
				// Non-instruction line
				NonInstructionLine nil = new NonInstructionLine(
						linereader.getLineNumber(), grpnum, 
						line);
				
				// Insert line in the vector
				linevector.addElement(nil);
			}
		}
		// Insert vector in a ParsedTimingFile object 
		parsedTiming.setLineVector(linevector);
		
		Map lastStartByPipeline = new HashMap();
		Iterator it = parsedTiming.getLineVector().iterator();
		int maxTime = 0;
	
		while(it.hasNext()) {
			TimingFileLine line1 = (TimingFileLine) it.next();
			
			if(line1 instanceof InstructionLine) {
				InstructionLine instructionLine = (InstructionLine) line1;
				Integer         currentPipeline = new Integer(instructionLine.getPipelineNumber());

				ProcessingInfo info = (ProcessingInfo) lastStartByPipeline.get(currentPipeline);

				if(info == null) {
					info = new ProcessingInfo();
					lastStartByPipeline.put(currentPipeline, info);
				}
				
				//Make the time info information be merged and cycles removed
				processTimeInfo(instructionLine);
				
				//Parse the time information into a structure
				String   timeInfoString = instructionLine.getPipeTimingInfo();
				TimeInfo timeInfo       = getInitialPosition(timeInfoString, info);	
				
				instructionLine.setTimeInfo(timeInfo);
				
				int currentTime =
					timeInfo.getPosition() + timeInfo.getNopesCount() + timeInfo.getInstructionSize();
				
				if(currentTime > maxTime) {
					maxTime = currentTime;
				}
			}
		}
		parsedTiming.setMaxTime(maxTime);

		return parsedTiming;
	}
	
	private void processTimeInfo(InstructionLine instructionLine) {
		String[] timeInfoSplited = instructionLine.getPipeTimingInfo().split(" "); //$NON-NLS-1$
		String firstPart = timeInfoSplited[0];
		if(timeInfoSplited.length > 1) {
			String lastPart = timeInfoSplited[timeInfoSplited.length-1];
			String mergedCycles = lastPart+firstPart;
			instructionLine.setPipeTimingInfo(mergedCycles.trim());
		}//if(..)
	}//private method
	
	private TimeInfo getInitialPosition(String timeInfoString, ProcessingInfo info) {		
		int     nopesCount       = 0;
		int     firstNumber      = -1;
		int     instructionSize  = 0;
		boolean foundFirstNumber = false;
		
		for(int i = 0; i < timeInfoString.length(); i++) {
			char currentChar = timeInfoString.charAt(i);
			
			switch (currentChar) {
			    case '-':
			    	nopesCount++;
			    	break;
			    	
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case '0':
					foundFirstNumber = true;
					instructionSize  = timeInfoString.length() - i;
					firstNumber = Integer.parseInt(Character.toString(currentChar));
					break;
	
				default:
					break;
			}//switch
			
			if(foundFirstNumber) {
				break;
			}//if(..)
			
		}//for i
		
		int nopeStart = firstNumber - nopesCount;
		if(nopeStart < 0) {
			nopeStart += 10;
		}//if(..)
		
		if(nopeStart < info.lastStart) {
			info.incCurrentStep();
		}//if(..)
		
		int      realStart = nopeStart + info.getCurrentStep();
		TimeInfo timeInfo  = new TimeInfo(realStart, nopesCount, instructionSize);
				
		//Setting the last start value of an instruction in the current pipeline
		info.lastStart = nopeStart;
		
		return timeInfo;
	}//private method

	private class ProcessingInfo {
		
		int lastStart   = 0;
		private int currentStep = 0;
		
		public int getCurrentStep() {
			return this.currentStep;
		}//getter
		
		public void incCurrentStep() {
			this.currentStep += 10;
		}//setter
		
	}//inner class

}
