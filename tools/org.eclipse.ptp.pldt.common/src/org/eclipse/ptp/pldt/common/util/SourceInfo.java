/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.common.util;

import org.eclipse.ptp.pldt.common.Artifact;

/**
 * Class encapsulates locational information for creating  markers representing Artifacts.
 * 
 * 
 */
public class SourceInfo
{
	/** starting line location within in the file */
    private int startingLine;
    /** line number within the file */
    private int start;
    /** end position (character position) relative to start of file */
    private int end;
    /** type of construct, e.g. Artifact.CONSTANT, etc. */
    private int constructType;

    /** explicit default contstructor with no info; assumed to
     * be filled in with set methods.
     *
     */
    public SourceInfo(){
    }
    
    /** ctor with info already filled in */
    public SourceInfo(int startLine, int start, int end, int construct){
    	this.startingLine=startLine;
    	this.start=start;
    	this.end=end;
    	this.constructType=construct;
    }
    /**
     * @return Returns the end position, relative to start of file
     */
    public int getEnd()
    {
        return end;
    }

    /**
     * @param end The end to set.
     */
    public void setEnd(int end)
    {
        this.end = end;
    }

    /**
     * @return Returns the start position, relative to start of file
     */
    public int getStart()
    {
        return start;
    }

    /**
     * @param start The start to set.
     */
    public void setStart(int start)
    {
        this.start = start;
    }

    /**
     * @return Returns the startingLine.
     */
    public int getStartingLine()
    {
        return startingLine;
    }

    /**
     * @param startingLine The startingLine to set.
     */
    public void setStartingLine(int startingLine)
    {
        this.startingLine = startingLine;
    }

    /**
     * @return Returns the constructType.
     */
    public int getConstructType()
    {
        return constructType;
    }

    /**
     * @param constructType The constructType to set.
     */
    public void setConstructType(int constructType)
    {
        this.constructType = constructType;
    }
    /**
     * A string representation of the object
     */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("SourceInfo line:").append(getStartingLine()); //$NON-NLS-1$
		s.append(" startPos:").append(getStart()); //$NON-NLS-1$
		s.append(" endPos:").append(getEnd()); //$NON-NLS-1$
		int type = getConstructType();
		s.append(" constructType:").append(type); //$NON-NLS-1$
		try {
			s.append(" ").append(Artifact.CONSTRUCT_TYPE_NAMES[type]);
		} catch (Exception e) {
		}
		return s.toString();
	}
}
