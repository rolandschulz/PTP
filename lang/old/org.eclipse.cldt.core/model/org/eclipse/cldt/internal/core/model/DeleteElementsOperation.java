/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cldt.internal.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.IBuffer;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.ICModelStatusConstants;
import org.eclipse.cldt.core.model.IRegion;
import org.eclipse.cldt.core.model.ISourceRange;
import org.eclipse.cldt.core.model.ISourceReference;
import org.eclipse.cldt.core.model.ITranslationUnit;
import org.eclipse.cldt.internal.core.CharOperation;

/**
 * DeleteElementsOperation
 */
public class DeleteElementsOperation extends MultiOperation {
	/**
	 * The elements this operation processes grouped by compilation unit
	 * @see processElements(). Keys are compilation units,
	 * values are <code>IRegion</code>s of elements to be processed in each
	 * compilation unit.
	 */ 
	protected Map fChildrenToRemove;

	/**
	 * When executed, this operation will delete the given elements. The elements
	 * to delete cannot be <code>null</code> or empty, and must be contained within a
	 * compilation unit.
	 */
	public DeleteElementsOperation(ICElement[] elementsToDelete, boolean force) {
		super(elementsToDelete, force);
	}
	
	/**
	 * @see MultiOperation
	 */
	protected String getMainTaskName() {
		return "operation.deleteElementProgress"; //$NON-NLS-1$
	}

	/**
	 * Groups the elements to be processed by their compilation unit.
	 * If parent/child combinations are present, children are
	 * discarded (only the parents are processed). Removes any
	 * duplicates specified in elements to be processed.
	 */
	protected void groupElements() throws CModelException {
		fChildrenToRemove = new HashMap(1);
		int uniqueTUs = 0;
		for (int i = 0, length = fElementsToProcess.length; i < length; i++) {
			ICElement e = fElementsToProcess[i];
			ITranslationUnit tu = getTranslationUnitFor(e);
			if (tu == null) {
				throw new CModelException(new CModelStatus(ICModelStatusConstants.READ_ONLY, e));
			}
			IRegion region = (IRegion) fChildrenToRemove.get(tu);
			if (region == null) {
				region = new Region();
				fChildrenToRemove.put(tu, region);
				uniqueTUs++;
			}
			region.add(e);
		}
		fElementsToProcess = new ICElement[uniqueTUs];
		Iterator iter = fChildrenToRemove.keySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			fElementsToProcess[i++] = (ICElement) iter.next();
		}
	}
	/**
	 * Deletes this element from its compilation unit.
	 * @see MultiOperation
	 */
	protected void processElement(ICElement element) throws CModelException {
		ITranslationUnit tu = (ITranslationUnit) element;
	
		IBuffer buffer = tu.getBuffer();
		if (buffer == null) return;
		CElementDelta delta = new CElementDelta(tu);
		ICElement[] cuElements = ((IRegion) fChildrenToRemove.get(tu)).getElements();
		for (int i = 0, length = cuElements.length; i < length; i++) {
			ICElement e = cuElements[i];
			if (e.exists()) {
				char[] contents = buffer.getCharacters();
				if (contents == null) continue;
				String tuName = tu.getElementName();
				replaceElementInBuffer(buffer, e, tuName);
				delta.removed(e);
			}
		}
		if (delta.getAffectedChildren().length > 0) {
			tu.save(getSubProgressMonitor(1), fForce);
			if (!tu.isWorkingCopy()) { // if unit is working copy, then save will have already fired the delta
				addDelta(delta);
//				this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
			}
		}
	}

	/**
	 * @deprecated marked deprecated, future to use ASTRewrite
	 */
	private void replaceElementInBuffer(IBuffer buffer, ICElement elementToRemove, String cuName) throws CModelException {
		if (elementToRemove instanceof ISourceReference) {
			ISourceRange range = ((ISourceReference)elementToRemove).getSourceRange();
			int startPosition = range.getStartPos();
			int length = range.getLength();
			// Copy the extra spaces and newLines like it is part of
			// the element.  Note: the CopyElementAction is doing the same.
			boolean newLineFound = false;
			for (int offset = range.getStartPos() + range.getLength();;++offset) {
				try {
					char c = buffer.getChar(offset);
					// TODO:Bug in the Parser, it does not give the semicolon
					if (c == ';') {
						length++;
					} else if (c == '\r' || c == '\n') {
						newLineFound = true;
						length++;
					} else if (!newLineFound && c == ' ') { // Do not include the spaces after the newline
						length++ ;
					} else {
						break;
					}
				} catch (Exception e) {
					break;
				}
			}
			buffer.replace(startPosition, length, CharOperation.NO_CHAR);
		}
	}

	/**
	 * @see MultiOperation
	 * This method first group the elements by <code>ICompilationUnit</code>,
	 * and then processes the <code>ICompilationUnit</code>.
	 */
	protected void processElements() throws CModelException {
		groupElements();
		super.processElements();
	}
	/**
	 * @see MultiOperation
	 */
	protected void verify(ICElement element) throws CModelException {
		ICElement[] children = ((IRegion) fChildrenToRemove.get(element)).getElements();
		for (int i = 0; i < children.length; i++) {
			ICElement child = children[i];
			if (child.getResource() != null)
				error(ICModelStatusConstants.INVALID_ELEMENT_TYPES, child);
			if (child.isReadOnly())
				error(ICModelStatusConstants.READ_ONLY, child);
		}
	}

}
