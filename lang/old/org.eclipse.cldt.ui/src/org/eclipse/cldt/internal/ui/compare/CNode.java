package org.eclipse.cldt.internal.ui.compare;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cldt.internal.ui.viewsupport.FortranElementImageProvider;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
/**
 * 
 */

class CNode extends DocumentRangeNode implements ITypedElement {

	private CNode fParent;
		
	public CNode(CNode parent, int type, String id, IDocument doc, int start, int length) {
		super(type, id, doc, start, length);
		fParent= parent;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public CNode(CNode parent, int type, String id, int start, int length) {
		this(parent, type, id, parent.getDocument(), start, length);
	}

	/**
	 * @see ITypedInput#getName
	 */
	public String getName() {
		return getId();
	}

	/**
	 * @see ITypedInput#getType
	 */
	public String getType() {
		return "c2"; //$NON-NLS-1$
	}

	/**
	 * @see ITypedInput#getImage
	 */
	public Image getImage() {
		ImageDescriptor descriptor = FortranElementImageProvider.getImageDescriptor(getTypeCode());
		return FortranUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}
}
