package org.eclipse.cldt.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class FortranElementPropertySource implements IPropertySource {
	
	private final static String LABEL= "CElementProperties.name"; //$NON-NLS-1$
	
	private ICElement fCElement;
	
	// Property Descriptors
	static private IPropertyDescriptor[] fgPropertyDescriptors;
	
	static {
		// resource name
		String displayName= FortranUIPlugin.getResourceString(LABEL);
		PropertyDescriptor descriptor= new PropertyDescriptor(IBasicPropertyConstants.P_TEXT, displayName);
		descriptor.setAlwaysIncompatible(true);
		
		fgPropertyDescriptors= new IPropertyDescriptor[] { descriptor };
	}
	
	public FortranElementPropertySource(ICElement elem) {
		fCElement= elem;
	}

	/**
	 * @see IPropertySource#getPropertyDescriptors
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return fgPropertyDescriptors;
	}

	/**
	 * @see IPropertySource#getPropertyValue
	 */	
	public Object getPropertyValue(Object name) {
		if (name.equals(IBasicPropertyConstants.P_TEXT)) {
			return fCElement.getElementName();
		}
		return null;
	}

	/**
	 * @see IPropertySource#setPropertyValue
	 */	
	public void setPropertyValue(Object name, Object value) {
	}

	/**
	 * @see IPropertySource#getEditableValue
	 */	
	public Object getEditableValue() {
		return null;
	}

	/**
	 * @see IPropertySource#isPropertySet
	 */	
	public boolean isPropertySet(Object property) {
		return false;
	}

	/**
	 * @see IPropertySource#resetPropertyValue
	 */	
	public void resetPropertyValue(Object property) {
	}
}