package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.core.runtime.IPath;

/**
 * The Element Info of a Translation Unit.
 */
public class TranslationUnitInfo extends OpenableInfo {

	/** 
	 * Timestamp of original resource at the time this element
	 * was opened or last updated.
	 */
	public long fTimestamp;

	public TranslationUnitInfo (CElement element) {
		super(element);
	}

	/* Overide the SourceManipulation for the range.  */
	protected ISourceRange getSourceRange() {
		IPath location = ((TranslationUnit)getElement()).getLocation(); 		
		return new SourceRange(0, (int)location.toFile().length());
	}
}
