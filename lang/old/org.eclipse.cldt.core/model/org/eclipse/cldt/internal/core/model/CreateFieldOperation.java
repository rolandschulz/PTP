package org.eclipse.cldt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.ICModelStatus;
import org.eclipse.cldt.core.model.IStructure;
import org.eclipse.cldt.core.model.ITranslationUnit;
/**
 * <p>This operation creates a field declaration in a type.
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing Type
 *  <li>The source code for the declaration. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateFieldOperation extends CreateMemberOperation {
	/**
	 * Initializer for Element
	 */
	String fInitializer;

	/**
	 * When executed, this operation will create a field with the given name
	 * in the given type with the specified source.
	 *
	 * <p>By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	public CreateFieldOperation(IStructure parentElement, String name, String returnType, String initializer, boolean force) {
		super(parentElement, name, returnType, force);
		fInitializer = initializer;
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName(){
		return "operation.createFieldProgress"; //$NON-NLS-1$
	}

	/**
	 * By default the new field is positioned after the last existing field
	 * declaration, or as the first member in the type if there are no
	 * field declarations.
	 */
	protected void initializeDefaultPosition() {
		IStructure parentElement = getStructure();
		try {
			ICElement[] elements = parentElement.getFields();
			if (elements != null && elements.length > 0) {
				createAfter(elements[elements.length - 1]);
			} else {
				elements = parentElement.getChildren();
				if (elements != null && elements.length > 0) {
					createBefore(elements[0]);
				}
			}
		} catch (CModelException e) {
		}
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		return getStructure().getField(fName);
	}

	/**
	 * @see CreateTypeMemberOperation#verifyNameCollision
	 */
	protected ICModelStatus verifyNameCollision() {
		return super.verifyNameCollision();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CreateElementInTUOperation#generateElement(org.eclipse.cdt.core.model.ITranslationUnit)
	 */
	protected String generateElement(ITranslationUnit unit) throws CModelException {
		StringBuffer sb = new StringBuffer();
		sb.append(fReturnType).append(' ');
		sb.append(fName);
		if (fInitializer != null && fInitializer.length() > 0) {
			sb.append(' ').append('=').append(' ');
			sb.append(fInitializer);
		}
		sb.append(';');
		return sb.toString();
	}
}
