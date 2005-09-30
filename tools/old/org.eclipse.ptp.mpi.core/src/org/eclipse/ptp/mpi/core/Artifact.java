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

package org.eclipse.ptp.mpi.core;

import java.util.Date;

import org.eclipse.ptp.mpi.core.util.SourceInfo;

/**
 * Artifacts contain information about a framework (e.g. MPI) function call or constant reference found in source code.
 * 
 * Note: if we have any parent/child relationships, this class should know about it.
 * 
 * @author Beth Tibbitts
 * 
 */
public class Artifact
{
    /** Line number where the artifact occurs */
    private int                  line_;
    /** Column number where the artifact occurs (unused) */
    private int                  column_;
    /** in which file is the line */
    private String               fileName_;
    /** Longer description of MPI artifact */
    private String               description_;
    /** Short heading name of MPI artifact */
    private String               shortName_;
    /**
     * Which file-analysis-invocation gave rise to this artifact.<br>
     * (Analyzing one file may produce artifacts in another file. This is the first one)
     */
    private String               primaryfileName_;
    /** New line number if changes are made to source (unused) */
    private int                  newline_;
    /** unique ID of artifact, using for lookups */
    private String               id_;
    /** BRT ?? id vs uniqueID_? */
    private String               uniqueID_;
    /** timestamp used for generating unique ids */
    private static long          now_                 = new Date().getTime();
    /** Object containing additional source information */
    private SourceInfo           sourceInfo_;
    public static final int      NONE                 = 0;
    public static final int      FUNCTION_CALL        = 1;
    public static final int      CONSTANT             = 2;

    public static final String[] CONSTRUCT_TYPE_NAMES = { "None", "Function Call", "Constant" };

    /**
     * Create an MPI Artifact to keep track of an MPI function call (or ???) in a file.
     * 
     * @param fileName
     * @param line
     * @param column
     * @param shortName
     * @param desc
     * @param ignore
     * @param primaryFileName
     */
    public Artifact(String fileName, int line, int column, String shortName, String desc, SourceInfo sourceInfo)
    {
        this.line_ = line;
        this.newline_ = line;
        this.column_ = column;
        this.fileName_ = fileName;
        this.shortName_ = shortName;
        this.sourceInfo_ = sourceInfo;
        setUniqueID();
        MpiArtifactManager.addMpiArtifactToHash(this);
    }

    public Artifact(String fileName, int line, int column, String shortName, String desc, boolean ignore,
            String primaryFileName, SourceInfo sourceInfo)
    {
        this(fileName, line, column, shortName, desc, sourceInfo);
        this.primaryfileName_ = primaryFileName;
    }

    // How to create an Eclipse IFile object from its fully-qualified name
    // import org.eclipse.core.resources.IFile;
    // import org.eclipse.core.runtime.Path;
    // import com.ibm.ngp.core.NGPPlugin;
    // import com.ibm.ngp.core.common.Log;
    /*
     * IFile f = NGPPlugin.getWorkspace().getRoot().getFileForLocation(new Path(filename)); if (f != null && f.exists()) { //
     * after checking that a file with this name actually exists, // here's how to get the fully-qualified name back
     * from the IFile object: Log.printit("PI ctor file is: "+f.getRawLocation()); }
     */

    /**
     * Set unique ID for this MPI artifact. Base 36 values use 0..9a...z and are at most 8 characters long. Will not be
     * repeated (u
     * 
     * @return string of unique ID.
     */
    private void setUniqueID()
    {
        uniqueID_ = Long.toString(now_++, 36);
    }

    public String getUniqueID()
    {
        return uniqueID_;
    }

    /**
     * Hand representation of data, useful for debugging, etc.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("MpiArtifact");
        buf.append(" ");
        buf.append(" id=").append(id_);
        buf.append(" ").append(shortName_);
        buf.append(" line=").append(line_).append(" filename=").append(fileName_);
        buf.append(" desc=").append(description_);
        buf.append(" start=").append(getSourceInfo_().getStart());
        buf.append(" end=").append(getSourceInfo_().getEnd());
        return buf.toString();
    }

    public int getColumn()
    {
        return column_;
    }

    public void setColumn(int column_)
    {
        this.column_ = column_;
    }

    public String getDescription()
    {
        return description_;
    }

    public void setDescription(String description_)
    {
        this.description_ = description_;
    }

    public String getFileName()
    {
        return fileName_;
    }

    public void setFileName(String fileName_)
    {
        this.fileName_ = fileName_;
    }

    public String getId()
    {
        return id_;
    }

    public void setId(String id_)
    {
        this.id_ = id_;
    }

    public int getLine()
    {
        return line_;
    }

    public void setLine(int line_)
    {
        this.line_ = line_;
    }

    public String getPrimaryfileName()
    {
        return primaryfileName_;
    }

    public void setPrimaryfileName(String primaryfileName_)
    {
        this.primaryfileName_ = primaryfileName_;
    }

    public String getShortName()
    {
        return shortName_;
    }

    public void setShortName(String shortName_)
    {
        this.shortName_ = shortName_;
    }

    /**
     * @return Returns the sourceInfo_.
     */
    public SourceInfo getSourceInfo_()
    {
        return sourceInfo_;
    }

    public int getNewline_()
    {
        return newline_;
    }

}
