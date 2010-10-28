 /*******************************************************************************
  * Copyright (c) 2010 The University of Tennessee,
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Benjamin Lindner (ben@benlabs.net) - initial implementation (bug 316671)

  *******************************************************************************/

package org.eclipse.ptp.rm.proxy.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.core.attributes.*;

/**
 * @since 2.0
 */
public class AttributeDefinitionSerializer {

	private List<String> attribute_elements;

	public AttributeDefinitionSerializer(IAttributeDefinition<?,?,?> attr) {
		if (attr instanceof DateAttributeDefinition) {
			create_from_date((DateAttributeDefinition)attr);
		}
		if (attr instanceof BooleanAttributeDefinition) {
			create_from_boolean((BooleanAttributeDefinition)attr);
		}
		if (attr instanceof StringAttributeDefinition) {
			create_from_string((StringAttributeDefinition)attr);
		}
		if (attr instanceof IntegerAttributeDefinition) {
			create_from_integer((IntegerAttributeDefinition)attr);
		}
		if (attr instanceof DoubleAttributeDefinition) {
			create_from_double((DoubleAttributeDefinition)attr);
		}
		
		
	}
	
	private void create_from_date(DateAttributeDefinition attr) {
		attribute_elements = new ArrayList<String>();
			
		// set default attribute elements common to all
		attribute_elements.add(attr.getId());
		attribute_elements.add("DATE");
		attribute_elements.add(attr.getName());
		attribute_elements.add(attr.getDescription());
		if (attr.getDisplay()) {
			attribute_elements.add("TRUE");			
		} else {
			attribute_elements.add("FALSE");			
		}
		
		// we need to use a "trick" to get the default value for the attribute definition:
		DateAttribute tmpdate = null;
		try {
			tmpdate = attr.create();
		} catch (IllegalValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		attribute_elements.add(tmpdate.toString());
		
		// extra element for the given data type:
		
		// broken: have to find out how date_style and time_style and locale are determined
		// 
		attribute_elements.add("0");
		attribute_elements.add("0");
		attribute_elements.add("0");

		attribute_elements.add(attr.getMinDate().toString());
		attribute_elements.add(attr.getMaxDate().toString());
		
	}


	private void create_from_string(StringAttributeDefinition attr) {
		attribute_elements = new ArrayList<String>();
		
		// set default attribute elements common to all
		attribute_elements.add(attr.getId());
		attribute_elements.add("STRING");
		attribute_elements.add(attr.getName());
		attribute_elements.add(attr.getDescription());
		if (attr.getDisplay()) {
			attribute_elements.add("TRUE");			
		} else {
			attribute_elements.add("FALSE");			
		}

		// we need to use a "trick" to get the default value for the attribute definition:
		StringAttribute tmpattr;
		tmpattr = attr.create();
		attribute_elements.add(tmpattr.toString());		
	}
	
	private void create_from_boolean(BooleanAttributeDefinition attr) {
		attribute_elements = new ArrayList<String>();
		
		// set default attribute elements common to all
		attribute_elements.add(attr.getId());
		attribute_elements.add("BOOLEAN");
		attribute_elements.add(attr.getName());
		attribute_elements.add(attr.getDescription());
		if (attr.getDisplay()) {
			attribute_elements.add("TRUE");			
		} else {
			attribute_elements.add("FALSE");			
		}

		// we need to use a "trick" to get the default value for the attribute definition:
		BooleanAttribute tmpattr;
		tmpattr = attr.create();
		attribute_elements.add(tmpattr.toString());		
	}	

	private void create_from_integer(IntegerAttributeDefinition attr) {
		attribute_elements = new ArrayList<String>();
		
		// set default attribute elements common to all
		attribute_elements.add(attr.getId());
		attribute_elements.add("INTEGER");
		attribute_elements.add(attr.getName());
		attribute_elements.add(attr.getDescription());
		if (attr.getDisplay()) {
			attribute_elements.add("TRUE");			
		} else {
			attribute_elements.add("FALSE");			
		}

		// we need to use a "trick" to get the default value for the attribute definition:
		IntegerAttribute tmpattr = null;
		try {
			tmpattr = attr.create();
		} catch (IllegalValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		attribute_elements.add(tmpattr.toString());	
		
		attribute_elements.add(attr.getMinValue().toString());
		attribute_elements.add(attr.getMaxValue().toString());
				
	}

	private void create_from_double(DoubleAttributeDefinition attr) {
		attribute_elements = new ArrayList<String>();
		
		// set default attribute elements common to all
		attribute_elements.add(attr.getId());
		attribute_elements.add("DOUBLE");
		attribute_elements.add(attr.getName());
		attribute_elements.add(attr.getDescription());
		if (attr.getDisplay()) {
			attribute_elements.add("TRUE");			
		} else {
			attribute_elements.add("FALSE");			
		}

		// we need to use a "trick" to get the default value for the attribute definition:
		DoubleAttribute tmpattr = null;
		try {
			tmpattr = attr.create();
		} catch (IllegalValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		attribute_elements.add(tmpattr.toString());		

		attribute_elements.add(attr.getMinValue().toString());
		attribute_elements.add(attr.getMaxValue().toString());
	}

	// yet to implement: arrays and enums
	
	public String str() {

		String totalstring = " ";
		
		Integer size = attribute_elements.size();
		totalstring += size.toString();
		
		for (String ae : attribute_elements) {
			totalstring += " \" \" " + ae;
		}
		
		return totalstring;
	}

	public String[] strList() {

		List<String> tmplist = new ArrayList<String>();
		
		// number of attribute definitions:
		tmplist.add("1");

		Integer size = attribute_elements.size();
		tmplist.add(size.toString());

		for (String ae : attribute_elements) {
			tmplist.add(ae);
		}
		
		return tmplist.toArray(new String[tmplist.size()]);
	}

}