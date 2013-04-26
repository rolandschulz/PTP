/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.includebrowser;

import java.io.Serializable;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.core.runtime.CoreException;

public class IndexIncludeValue implements IIndexIncludeValue, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6440736393666515385L;

	private transient IIndexInclude include;
	private transient IIndexFile includedBy;
	private transient IIndexLocationConverter converter;

	private IIndexFileLocation includedByLocation;
	private IIndexFileLocation includesLocation;
	
	private long includedByTimestamp;
	private String name, fullName;
	private int nameLength;
	private int nameOffset;
	private boolean isActive;
	private boolean isResolved;
	private boolean isSystemInclude;
	private boolean fIsIncludedFileExported;
	

	public IndexIncludeValue(IIndexInclude include) throws CoreException
	{
		this(include, null);
	}

	public IndexIncludeValue(IIndexInclude include, IIndexLocationConverter converter) throws CoreException
	{
		if (include == null)
			throw new IllegalArgumentException();
		
		this.converter = converter;
		this.include = include;
		includedBy = include.getIncludedBy();

		//converting non-serializable IndexFileLocation to RemoteIndexFileLocation
		if (include.getIncludedByLocation() != null)
		{
			
			if (converter != null)
				includedByLocation = converter.fromInternalFormat(include.getIncludedByLocation().getURI().getPath());
			
			//includedByLocation = new RemoteIndexFileLocation(include.getIncludedByLocation()); 
		}
		if (include.getIncludesLocation() != null)
		{
			
			if (converter != null)
				includesLocation = converter.fromInternalFormat(include.getIncludesLocation().getURI().getPath());
			
			//includesLocation = new RemoteIndexFileLocation(include.getIncludesLocation());
		}
		
		includedByTimestamp = include.getIncludedBy().getTimestamp();
		name = include.getName();
		fullName = include.getFullName();
		nameLength = include.getNameLength();
		nameOffset = include.getNameOffset();
		isActive = include.isActive();
		isResolved = include.isResolved();
		isSystemInclude = include.isSystemInclude();
		fIsIncludedFileExported = include.isIncludedFileExported();
		
	}
	
	//returns absolute url in the form of: schema://host/mypath...filename
	public IIndexFileLocation getIncludedByLocation() throws CoreException
	{
		IIndexFileLocation location = this.includedByLocation;
		
		if (location == null && converter == null) {		
			if (this.getIncludedBy() != null && this.getIncludedBy().getLocation() != null)
				location = this.getIncludedBy().getLocation();
		}
		
		return location;
	}

	//returns absolute url in the form of: protocol://host/mypath...file.ext
	public IIndexFileLocation getIncludesLocation() throws CoreException
	{
		IIndexFileLocation location = this.includesLocation;
		
		if (location == null && converter == null && this.include!=null &&  this.include.getIncludesLocation() != null) {
			location = this.include.getIncludesLocation();
		}
	
		return location;
	}

	public String getName() throws CoreException
	{
		return this.name;
	}

	public int getNameLength() throws CoreException
	{
		return this.nameLength;
	}

	public int getNameOffset() throws CoreException
	{
		return this.nameOffset;
	}

	public boolean isActive() throws CoreException
	{
		return this.isActive;
	}

	public boolean isResolved() throws CoreException
	{
		return this.isResolved;
	}

	public boolean isSystemInclude() throws CoreException
	{
		return this.isSystemInclude;
	}

	public long getIncludedByTimestamp()
	{
		return this.includedByTimestamp;
	}
	
	//NOTE: after the transport getter below will return null
	public IIndexFile getIncludedBy() throws CoreException
	{
		return this.includedBy;
	}
	
	public IIndexInclude getIndexInclude()
	{
		return this.include;
	}
	
	public IIndexLocationConverter getLocationConverter()
	{
		return this.converter;
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer(100);
		buffer.append("\nsuper = ").append(super.toString()); //$NON-NLS-1$
		buffer.append("\nincludedByLocation = ").append(this.includedByLocation); //$NON-NLS-1$
		buffer.append("\nincludesLocation = ").append(this.includesLocation); //$NON-NLS-1$
		buffer.append("\nname = ").append(this.name); //$NON-NLS-1$
		buffer.append("\nnameLength = ").append(this.nameLength); //$NON-NLS-1$
		buffer.append("\nnameOffset = ").append(this.nameOffset); //$NON-NLS-1$
		buffer.append("\nisActive = ").append(this.isActive); //$NON-NLS-1$
		buffer.append("\nisResolved = ").append(this.isResolved); //$NON-NLS-1$
		buffer.append("\nisSystemInclude = ").append(this.isSystemInclude); //$NON-NLS-1$
		buffer.append("\nincludedByTimestamp = ").append(this.includedByTimestamp); //$NON-NLS-1$
		
		return buffer.toString();
	}

	public boolean isResolvedByHeuristics() throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	public String getFullName() throws CoreException {
		return fullName;
	}

	public boolean isIncludedFileExported() throws CoreException {
		return fIsIncludedFileExported;
	}

}
