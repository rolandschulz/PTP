package org.eclipse.cldt.internal.core.model;

import org.eclipse.cldt.core.model.ICElement;


/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 */
public class ArchiveContainerInfo extends OpenableInfo {

	/**
	 * Constructs a new C Model Info 
	 */
	protected ArchiveContainerInfo(CElement element) {
		super(element);
	}

	synchronized void sync() {
		BinaryRunner runner = CModelManager.getDefault().getBinaryRunner(getElement().getCProject(), true);
		if (runner != null) {
			runner.waitIfRunning();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElementInfo#addChild(org.eclipse.cdt.core.model.ICElement)
	 */
	protected void addChild(ICElement child) {
		if (!includesChild(child)) {
			super.addChild(child);
		}
	}
}
