/****************************
 * IBM Confidential
 * Licensed Materials - Property of IBM
 *
 * IBM Rational Developer for Power Systems Software
 * IBM Rational Team Concert for Power Systems Software
 *
 * (C) Copyright IBM Corporation 2011.
 *
 * The source code for this program is not published or otherwise divested of its trade secrets, 
 * irrespective of what has been deposited with the U.S. Copyright Office.
 */
package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.core.index.IIndexLocationConverter;

/**
 * Different converters are needed for the local and remote indexes.
 */
public interface IIndexLocationConverterFactory {

	public IIndexLocationConverter getConverter(ICProject project);
}
