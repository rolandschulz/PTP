package org.eclipse.ptp.core.elements.attributes;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Job attributes
 */
public class ElementAttributes {
	/*
	 * Predefine attributes. These are attributes that
	 * the UI knows about.
	 */
	private final static String ATTR_ID = "id";
	private final static String ATTR_NAME = "name";
	
	private final static IntegerAttributeDefinition idAttributeDefinition = 
		new IntegerAttributeDefinition(ATTR_ID, "ID", "Unique ID of element", 0);

	private final static StringAttributeDefinition nameAttributeDefinition = 
		new StringAttributeDefinition(ATTR_NAME, "Name", "Name of element", "");

	public static IntegerAttributeDefinition getIdAttributeDefinition() {
		return idAttributeDefinition;
	}
	
	public static StringAttributeDefinition getNameAttributeDefinition() {
		return nameAttributeDefinition;
	}

	public static IAttributeDefinition[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[]{
				idAttributeDefinition, 
				nameAttributeDefinition
			};
	}
}
