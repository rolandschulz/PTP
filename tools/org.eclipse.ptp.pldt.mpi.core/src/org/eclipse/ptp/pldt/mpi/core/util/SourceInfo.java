/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.util;

/**
 * Class encapsulates locational information for creating MPI markers.
 * 
 * 
 */
public class SourceInfo
{
    private int startingLine;
    private int start;
    private int end;
    private int constructType;

    /**
     * @return Returns the end.
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
     * @return Returns the start.
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
}
