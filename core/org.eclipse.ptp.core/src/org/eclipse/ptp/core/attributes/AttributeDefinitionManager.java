/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core.attributes;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class AttributeDefinitionManager {

	/*
	 * Predefine attributes. These are attributes that
	 * the UI knows about.
	 */
	private final static String ATTR_ID = "id";
	private final static String ATTR_NAME = "name";
	
	private final static IIntegerAttributeDefinition idAttributeDefinition = new IntegerAttributeDefinition(ATTR_ID, "id", "ID of element", 0);
	private final static IStringAttributeDefinition nameAttributeDefinition = new StringAttributeDefinition(ATTR_NAME, "name", "Name of element", "");
	
	private final HashMap<String, IAttributeDefinition> attributeDefs = new HashMap<String, IAttributeDefinition>();
	
	public AttributeDefinitionManager() {
		setAttributeDefinition(getIdAttributeDefinition());
		setAttributeDefinition(getNameAttributeDefinition());
	}
	
	/**
	 * Create an attribute definition
	 * 
	 * @param attr
	 */
	public void setAttributeDefinition(IAttributeDefinition attrDef) {
		if (!attributeDefs.containsKey(attrDef.getId())) {
			attributeDefs.put(attrDef.getId(), attrDef);
		}
	}
	
	public void setAttributeDefinitions(IAttributeDefinition[] attrDefs) {
		for (IAttributeDefinition attrDef : attrDefs) {
			setAttributeDefinition(attrDef);
		}
	}
	
	/**
	 * Lookup an attribute definition
	 * 
	 * @param attrId
	 * @return attribute definition
	 */
	public IAttributeDefinition getAttributeDefinition(String attrId) {
		return attributeDefs.get(attrId);
	}
	
	public IBooleanAttributeDefinition createBooleanAttributeDefinition(final String uniqueId, final String name, final String description, final Boolean defaultValue) {
		IBooleanAttributeDefinition def = new BooleanAttributeDefinition(uniqueId, name, description, defaultValue);
		setAttributeDefinition(def);
		return def;
	}
	
	public IDateAttributeDefinition createDateAttributeDefinition(final String uniqueId, final String name, final String description, final Date defaultValue, final DateFormat outputDateFormat) {
		IDateAttributeDefinition def = new DateAttributeDefinition(uniqueId, name, description, defaultValue, outputDateFormat);
		setAttributeDefinition(def);
		return def;
	}
	
	public IDateAttributeDefinition createDateAttributeDefinition(final String uniqueId, final String name, final String description, final Date defaultValue, final DateFormat outputDateFormat, final Date min, final Date max) throws IllegalValueException {
		IDateAttributeDefinition def = new DateAttributeDefinition(uniqueId, name, description, defaultValue, outputDateFormat, min, max);
		setAttributeDefinition(def);
		return def;
	}

	public IDoubleAttributeDefinition createDoubleAttributeDefinition(final String uniqueId, final String name, final String description, final Double defaultValue) {
		IDoubleAttributeDefinition def = new DoubleAttributeDefinition(uniqueId, name, description, defaultValue);
		setAttributeDefinition(def);
		return def;
	}
	
	public IDoubleAttributeDefinition createDoubleAttributeDefinition(final String uniqueId, final String name, final String description, final Double defaultValue, final Double min, final Double max) throws IllegalValueException {
		IDoubleAttributeDefinition def = new DoubleAttributeDefinition(uniqueId, name, description, defaultValue, min, max);
		setAttributeDefinition(def);
		return def;
	}

	public IEnumeratedAttributeDefinition createEnumeratedAttributeDefinition(
			final String uniqueId, final String name, final String description,
			final String defaultValue, final String[] values) throws IllegalValueException {
		IEnumeratedAttributeDefinition def = new EnumeratedAttributeDefinition(uniqueId, name, description, defaultValue, values);
		setAttributeDefinition(def);
		return def;
	}
	
	public IIntegerAttributeDefinition createIntegerAttributeDefinition(final String uniqueId, final String name, final String description, final Integer defaultValue) {
		IIntegerAttributeDefinition def = new IntegerAttributeDefinition(uniqueId, name, description, defaultValue);
		setAttributeDefinition(def);
		return def;
	}

	public IIntegerAttributeDefinition createIntegerAttributeDefinition(final String uniqueId, final String name, final String description, final Integer defaultValue, final Integer min, final Integer max) throws IllegalValueException {
		IIntegerAttributeDefinition def = new IntegerAttributeDefinition(uniqueId, name, description, defaultValue, min, max);
		setAttributeDefinition(def);
		return def;
	}

	public IStringAttributeDefinition createStringAttributeDefinition(final String uniqueId, final String name, final String description, final String defaultValue) {
		IStringAttributeDefinition def = new StringAttributeDefinition(uniqueId, name, description, defaultValue);
		setAttributeDefinition(def);
		return def;
	}
	
	public IArrayAttributeDefinition createArrayAttributeDefinition(final String uniqueId, final String name, final String description, final Object[] defaultValue) {
		IArrayAttributeDefinition def = new ArrayAttributeDefinition(uniqueId, name, description, defaultValue);
		setAttributeDefinition(def);
		return def;
	}

	public static IIntegerAttributeDefinition getIdAttributeDefinition() {
		return idAttributeDefinition;
	}
	
	public static IStringAttributeDefinition getNameAttributeDefinition() {
		return nameAttributeDefinition;
	}
}
