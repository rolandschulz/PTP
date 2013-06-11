/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common;

import java.util.Date;

import org.eclipse.ptp.pldt.common.messages.Messages;
import org.eclipse.ptp.pldt.common.util.SourceInfo;

/**
 * Artifacts contain information about a framework (e.g. MPI) function call or constant reference found in source code.
 * 
 * Note: if we have any parent/child relationships, this class should know about it.
 * 
 * @author Beth Tibbitts
 * 
 */
/**
 * @author beth
 *
 */
public class Artifact implements IArtifact
{
	/** Line number where the artifact occurs */
	private int line_;
	/** Column number where the artifact occurs (unused) */
	private int column_;
	/** in which file is the line */
	private String fileName_;
	/** Longer description of MPI artifact */
	private String description_;
	/** Short heading name of MPI artifact */
	private String shortName_;

	/** to help, this would likely be an AST node */
	private Object artifactAssist_;

	/**
	 * Which file-analysis-invocation gave rise to this artifact.<br>
	 * (Analyzing one file may produce artifacts in another file. This is the first one)
	 */
	private String primaryfileName_;
	/** New line number if changes are made to source (unused) */
	private int newline_;
	/** unique ID of artifact, using for lookups by ArtifactManager */
	private String uniqueID_;
	/** timestamp used for generating unique ids */
	private static long now_ = new Date().getTime();
	/** Object containing additional source information */
	private SourceInfo sourceInfo_;
	/** Constant used in marker to indicate type of artifact is unspecified */
	public static final int NONE = 0;
	/** Constant used in marker to indicate type of artifact is a function call */
	public static final int FUNCTION_CALL = 1;
	/** Constant used in marker to indicate type of artifact is a constant */
	public static final int CONSTANT = 2;
	/** Constant used in marker to indicate type of artifact is a pragma */
	public static final int PRAGMA = 3;
	/** List of construct names, used to index into for table headings */
	public static final String[] CONSTRUCT_TYPE_NAMES = { Messages.Artifact_none, Messages.Artifact_function_call,
			Messages.Artifact_constant, Messages.Artifact_pragma };

	/**
	 * Create an Artifact to keep track of an something found in a file.
	 * 
	 * @param fileName
	 * @param line
	 * @param column
	 * @param shortName
	 * @param ignore
	 * @param primaryFileName
	 * @since 4.0
	 */
	public Artifact(String fileName, int line, int column, String shortName, SourceInfo sourceInfo)
	{
		this.line_ = line;
		this.newline_ = line;
		this.column_ = column;
		this.fileName_ = fileName;
		this.shortName_ = shortName;
		this.sourceInfo_ = sourceInfo;
		setId();
	}

	/**
	 * Create an Artifact to keep track of an MPI function call (or similar item) in a file,
	 * including an object that may be an AST node for convenience.
	 * 
	 * @param fileName
	 * @param line
	 * @param column
	 * @param shortName
	 * @param desc
	 * @param sourceInfo
	 * @param artifactAssist
	 */
	public Artifact(String fileName, int line, int column, String shortName, String desc, SourceInfo sourceInfo,
			Object artifactAssist)
	{
		this.line_ = line;
		this.newline_ = line;
		this.column_ = column;
		this.fileName_ = fileName;
		this.shortName_ = shortName;
		this.sourceInfo_ = sourceInfo;
		this.artifactAssist_ = artifactAssist;
		setId();
	}

	/**
	 * Create an artifact to keep track of something in a file, including file name
	 * 
	 * @param fileName
	 * @param line
	 * @param column
	 * @param shortName
	 * @param desc
	 * @param ignore
	 * @param primaryFileName
	 * @param sourceInfo
	 */
	public Artifact(String fileName, int line, int column, String shortName, String desc, boolean ignore,
			String primaryFileName, SourceInfo sourceInfo)
	{
		this(fileName, line, column, shortName, sourceInfo);
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
	protected void setId()
	{
		uniqueID_ = Long.toString(now_++, 36);
	}

	/**
	 * Get unique ID
	 */
	public String getId()
	{
		return uniqueID_;
	}

	/**
	 * Hand representation of data, useful for debugging, etc.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer(Messages.Artifact_artifact);
		buf.append(" id=").append(uniqueID_); //$NON-NLS-1$
		buf.append(" ").append(shortName_); //$NON-NLS-1$
		buf.append(" line=").append(line_).append(" filename=").append(fileName_); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(" desc=").append(description_); //$NON-NLS-1$
		buf.append(" start=").append(getSourceInfo().getStart()); //$NON-NLS-1$
		buf.append(" end=").append(getSourceInfo().getEnd()); //$NON-NLS-1$
		return buf.toString();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#getColumn()
	 */
	public int getColumn()
	{
		return column_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#setColumn(int)
	 */
	public void setColumn(int column_)
	{
		this.column_ = column_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#getDescription()
	 */
	public String getDescription()
	{
		return description_;
	}

	/**
	 * @param description_
	 */
	public void setDescription(String description_)
	{
		this.description_ = description_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#getFileName()
	 */
	public String getFileName()
	{
		return fileName_;
	}

	/**
	 * @param fileName_
	 */
	public void setFileName(String fileName_)
	{
		this.fileName_ = fileName_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#getLine()
	 */
	public int getLine()
	{
		return line_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#setLine(int)
	 */
	public void setLine(int line_)
	{
		this.line_ = line_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#getPrimaryfileName()
	 */
	public String getPrimaryfileName()
	{
		return primaryfileName_;
	}
	/**
	 * Which file-analysis-invocation gave rise to this artifact.<br>
	 * (Analyzing one file may produce artifacts in another file. This is the first one)
	 * @param primaryfileName
	 */
	public void setPrimaryfileName(String primaryfileName)
	{
		this.primaryfileName_ = primaryfileName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#getShortName()
	 */
	public String getShortName()
	{
		return shortName_;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.IArtifact#setShortName(java.lang.String)
	 */
	public void setShortName(String shortName_)
	{
		this.shortName_ = shortName_;
	}

	/**
	 * Get detailed location info on where the artifact is located in the file (more than just line number, for example)
	 * @return Returns the sourceInfo_.
	 */
	public SourceInfo getSourceInfo()
	{
		return sourceInfo_;
	}

	/**
	 * Get new line (perhaps unused)
	 * @return
	 */
	public int getNewline()
	{
		return newline_;
	}

	/**
	 * Returns an object cached for assisting in artifact analysis, possibly an AST node
	 * @return
	 */
	public Object getArtifactAssist()
	{
		return artifactAssist_;
	}

}
