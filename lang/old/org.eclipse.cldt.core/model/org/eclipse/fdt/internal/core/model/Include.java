package org.eclipse.fdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.IInclude;

public class Include extends SourceManipulation implements IInclude {
	
	private final boolean standard;
	private String fullPath;
	
	public Include(ICElement parent, String name, boolean isStandard) {
		super(parent, name, ICElement.C_INCLUDE);
		standard = isStandard;
	}

	public String getIncludeName() {
		return getElementName();
	}

	public boolean isStandard() {
		return standard;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IInclude#getFullFileName()
	 */
	public String getFullFileName() {
		return fullPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.IInclude#isLocal()
	 */
	public boolean isLocal() {
		return !isStandard();
	}

	/*
	 * This is not yet populated properly by the parse;
	 * however, it might be in the near future.
	 */
	public void setFullPathName(String fullPath) {
		this.fullPath = fullPath;
	}

}
