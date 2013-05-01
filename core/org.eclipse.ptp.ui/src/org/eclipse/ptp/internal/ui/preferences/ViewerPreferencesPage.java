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
package org.eclipse.ptp.internal.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.ui.IPTPUIConstants;
import org.eclipse.ptp.internal.ui.PTPUIPlugin;
import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Clement chu
 * 
 */
public class ViewerPreferencesPage extends AbstractPreferencePage {
	private ViewIntFieldEditor toolTipField = null;
	private Button tooltipIsWrapButton = null;
	private Button tooltipShowAllTimeButton = null;
	
	/** Inner class ViewIntFieldEditor
	 *	used for validation of text field
	 *
	 */
	private class ViewIntFieldEditor {
		private int textLimit = 5;
		private String labelText = null;
		private int min = 0;
		private int max = 10;
		private Text textField = null;		
		private String msg = ""; //$NON-NLS-1$
		
		/** Constructor
		 * @param labelText
		 * @param min
		 * @param max
		 * @param parent
		 */
		ViewIntFieldEditor(String labelText, int min, int max, Composite parent) {
			this.labelText = labelText;
			this.min = min;
			this.max = max;
			createControl(parent);
		}
		
		/** Create control
		 * @param parent
		 */
		protected void createControl(Composite parent) {
			doFillIntoGrid(parent);
		}
		
		/** Create text field into given composite
		 * @param parent
		 */
		protected void doFillIntoGrid(Composite parent) {
			new Label(parent, SWT.LEFT).setText(labelText);
            
			textField = new Text(parent, SWT.SINGLE | SWT.BORDER);
            textField.setFont(parent.getFont());
            textField.setTextLimit(textLimit);
            textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        	textField.addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			ViewerPreferencesPage.this.isValid();
        		}	        		
        	});
			
			new Label(parent, SWT.RIGHT).setText("(" + min + "-" + max + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		/** Set text field enabled
		 * @param isEnable
		 */
		public void setEnabled(boolean isEnable) {
			if (textField != null) {
				textField.setEnabled(isEnable);
			}
		}
		
	    /** Set value into text field
	     * @param value
	     */
	    public void setValue(int value) {
	        if (textField != null) {
	        	textField.setText(String.valueOf(value));
	        }
	    }
	    
	    /** Get value from text field
	     * @return
	     */
	    public int getValue() {
	    	try {
	    		return Integer.parseInt(textField.getText());
	    	} catch (NumberFormatException e) {
	    		return 0;
	    	}
	    }
	    
	    /** Set error message 
	     * @param msg
	     */
	    public void setErrorMessage(String msg) {
	    	this.msg = msg;
	    }
	    
	    /** Get error message
	     * @return
	     */
	    public String getErrorMessage() {
	    	return msg;
	    }
	    
	    /** Is text field valid
	     * @return
	     */
	    public boolean isValid() {
	    	setErrorMessage(""); //$NON-NLS-1$
	    	try {
	    		int value = Integer.parseInt(textField.getText());
	    		if (value < min || value > max) {
	    			setErrorMessage(NLS.bind(Messages.ViewerPreferencesPage_0, new Object[] {min, max}));
	    			return false;
	    		}
	    	} catch (NumberFormatException e) {
	    		setErrorMessage(NLS.bind(Messages.ViewerPreferencesPage_1, e.getMessage()));
	    		return false;
	    	}
	    	return true;
	    }
	}
	
	/** Constructor
	 * 
	 */
	public ViewerPreferencesPage() {
		super();
		setPreferenceStore(PTPUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.ViewerPreferencesPage_2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		//getWorkbench().getHelpSystem().setHelp(getControl(), IPDebugHelpContextIds.P_DEBUG_PREFERENCE_PAGE);
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(1, false);
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		createSpacer(composite, 1);
		createViewSettingPreferences(composite);
		setValues();
		return composite;
	}
	
	/** Create tooltip group composite
	 * @param parent
	 */
	protected void createTooltipGroup(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, Messages.ViewerPreferencesPage_3);
		Composite compTooltip = createComposite(group, 1);

		tooltipShowAllTimeButton = createCheckButton(compTooltip, Messages.ViewerPreferencesPage_4);
		tooltipShowAllTimeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				toolTipField.setEnabled(!tooltipShowAllTimeButton.getSelection());
				if (tooltipShowAllTimeButton.getSelection())
					toolTipField.setValue((int)getPreferenceStore().getDefaultLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT));
			}
		});
		toolTipField = new ViewIntFieldEditor(Messages.ViewerPreferencesPage_5, 1000, 10000, createComposite(compTooltip, 3));
		tooltipIsWrapButton = createCheckButton(compTooltip, Messages.ViewerPreferencesPage_6);
	}
	
	/** Create icon group composite
	 * @param parent
	 */
	protected void createIconGroup(Composite parent) {
		/*
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("ViewerPreferencesPage.iconName"));
		Composite compIcon = createComposite(group, 3);

		iconSpacingXField = new ViewIntFieldEditor(PreferenceMessages.getString("ViewerPreferencesPage.icon_spacing_x"), 1, 10, compIcon);
		iconSpacingYField = new ViewIntFieldEditor(PreferenceMessages.getString("ViewerPreferencesPage.icon_spacing_y"), 1, 10, compIcon);

		iconWidthField = new ViewIntFieldEditor(PreferenceMessages.getString("ViewerPreferencesPage.icon_width"), 12, 100, compIcon);
		iconHeightField = new ViewIntFieldEditor(PreferenceMessages.getString("ViewerPreferencesPage.icon_height"), 12, 100, compIcon);
		*/		
	}
	
	/** Create view setting preferences
	 * @param parent
	 */
	protected void createViewSettingPreferences(Composite parent) {
		//createIconGroup(parent);
		createTooltipGroup(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	public void performDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		/*
		iconSpacingXField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_SPACING_X));
		iconSpacingYField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
		iconWidthField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_WIDTH));
		iconHeightField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
		*/
		toolTipField.setValue((int)store.getDefaultLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT));
		tooltipIsWrapButton.setSelection(store.getDefaultBoolean(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP));
		tooltipShowAllTimeButton.setSelection(store.getDefaultBoolean(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME));
		super.performDefaults();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		storeValues();
		PTPUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#setValues()
	 */
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		/*
		iconSpacingXField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_X));
		iconSpacingYField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
		iconWidthField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_WIDTH));
		iconHeightField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
		*/
		toolTipField.setValue((int)store.getLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT));
		tooltipIsWrapButton.setSelection(store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP));
		tooltipShowAllTimeButton.setSelection(store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.preferences.AbstractPreferencePage#storeValues()
	 */
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		/*
		store.setValue(IPTPUIConstants.VIEW_ICON_SPACING_X, iconSpacingXField.getValue());
		store.setValue(IPTPUIConstants.VIEW_ICON_SPACING_Y, iconSpacingYField.getValue());
		store.setValue(IPTPUIConstants.VIEW_ICON_WIDTH, iconWidthField.getValue());
		store.setValue(IPTPUIConstants.VIEW_ICON_HEIGHT, iconHeightField.getValue());
		*/
		store.setValue(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT, (long)toolTipField.getValue());
		store.setValue(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP, tooltipIsWrapButton.getSelection());
		store.setValue(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME, tooltipShowAllTimeButton.getSelection());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#isValid()
	 */
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
		/*
		if (!iconSpacingXField.isValid()) {
			setErrorMessage(iconSpacingXField.getErrorMessage());
			return false;
		}
		if (!iconSpacingYField.isValid()) {
			setErrorMessage(iconSpacingYField.getErrorMessage());
			return false;
		}
		if (!iconWidthField.isValid()) {
			setErrorMessage(iconWidthField.getErrorMessage());
			return false;
		}
		if (!iconHeightField.isValid()) {
			setErrorMessage(iconHeightField.getErrorMessage());
			return false;
		}
		*/
		if (!toolTipField.isValid()) {
			setErrorMessage(toolTipField.getErrorMessage());
			return false;
		}
		return true;
	}	
}
