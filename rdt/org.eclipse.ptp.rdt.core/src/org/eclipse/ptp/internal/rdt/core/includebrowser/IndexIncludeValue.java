package org.eclipse.ptp.internal.rdt.core.includebrowser;

import java.io.Serializable;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteIndexFileLocation;

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
				includedByLocation = converter.fromInternalFormat(include.getIncludedByLocation().getFullPath());
			
			includedByLocation = new RemoteIndexFileLocation(include.getIncludedByLocation().getFullPath(), includedByLocation.getURI()); 
		}
		if (include.getIncludesLocation() != null)
		{
			
			if (converter != null)
				includesLocation = converter.fromInternalFormat(include.getIncludesLocation().getFullPath());
			
			includesLocation = new RemoteIndexFileLocation(include.getIncludesLocation().getFullPath(), includesLocation.getURI());
		}
		
		includedByTimestamp = include.getIncludedBy().getTimestamp();
		name = include.getName();
		fullName = include.getFullName();
		nameLength = include.getNameLength();
		nameOffset = include.getNameOffset();
		isActive = include.isActive();
		isResolved = include.isResolved();
		isSystemInclude = include.isSystemInclude();
		
	}
	
	//returns absolute url in the form of: schema://host/mypath...filename
	public IIndexFileLocation getIncludedByLocation() throws CoreException
	{
		return this.includedByLocation;
	}

	//returns absolute url in the form of: protocol://host/mypath...file.ext
	public IIndexFileLocation getIncludesLocation() throws CoreException
	{
		return this.includesLocation;
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

}
