package org.eclipse.fdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ICModelStatus;
import org.eclipse.fdt.core.model.ICModelStatusConstants;
import org.eclipse.fdt.core.model.IStructure;
import org.eclipse.fdt.core.model.ITranslationUnit;

/**
 * <p>This operation creates an instance method. 
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing type
 *  <li>The source code for the method. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateMethodOperation extends CreateMemberOperation {
	/**
	 * Parameter types of the element.
	 */
	protected String[] fParameterTypes;

	/**
	 * The source code for the new member.
	 */
	protected String fSource;


	/**
	 * When executed, this operation will create a method
	 * in the given type with the specified source.
	 */
	public CreateMethodOperation(IStructure parentElement, String name, String returnType, String source, String[] parameters, boolean force) {
		super(parentElement, name, returnType, force);
		fParameterTypes = parameters;
		fSource = source;
	}

	/**
	 * @see CreateElementInCUOperation#generateResultHandle
	 */
	protected ICElement generateResultHandle() {
		//TODO: what about collisions, we need the signature here.
		return getStructure().getMethod(fName);
	}

	/**
	 * @see CreateElementInCUOperation#getMainTaskName
	 */
	public String getMainTaskName(){
		return "operation.createMethodProgress"; //$NON-NLS-1$
	}

	/**
	 * @see CreateTypeMemberOperation#verifyNameCollision
	 */
	protected ICModelStatus verifyNameCollision() {
		ICModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		if (fSource == null) {
			return new CModelStatus(ICModelStatusConstants.INVALID_CONTENTS);
		}
		if (!fForce) {
			//check for name collisions
			//if (node == null) {
			//	return new CModelStatus(ICModelStatusConstants.INVALID_CONTENTS);
			//	}
			//} catch (CModelException cme) {
			//}
		}

		return CModelStatus.VERIFIED_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.model.CreateElementInTUOperation#generateElement(org.eclipse.fdt.core.model.ITranslationUnit)
	 */
	protected String generateElement(ITranslationUnit unit) throws CModelException {
		StringBuffer sb = new StringBuffer();
		sb.append(fReturnType);
		sb.append(' ');
		sb.append(fName);
		sb.append('(');
		if (fParameterTypes != null) {
			for (int i = 0; i < fParameterTypes.length; ++i) {
				if (i != 0) {
					sb.append(',').append(' ');
				}
				sb.append(fParameterTypes[i]);
			}
		}
		sb.append(')').append(' ').append('{').append(Util.LINE_SEPARATOR);
		sb.append(fSource);
		sb.append(Util.LINE_SEPARATOR).append('}').append(Util.LINE_SEPARATOR);
		return sb.toString();
	}

}
