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
	 * Create an MPI Artifact to keep track of an MPI function call (or ???) in a file.
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
	public SourceInfo getSourceInfo()
	{
		return sourceInfo_;
	}

	public int getNewline()
	{
		return newline_;
	}

	public Object getArtifactAssist()
	{
		return artifactAssist_;
	}

}
