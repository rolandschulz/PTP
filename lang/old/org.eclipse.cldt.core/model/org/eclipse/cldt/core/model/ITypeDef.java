package org.eclipse.cldt.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Represents a field declared in a type.
 */
public interface ITypeDef extends ICElement, ISourceManipulation, ISourceReference {
	/**
	 * Returns the type of the typedef item
	 * @return String
	 */
	String getTypeName();
}
