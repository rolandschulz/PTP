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
package org.eclipse.ptp.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.ptp.ui.PTPUIPlugin;
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
public class PTPViewerPreferencesPage extends AbstractPerferencePage {
	private ViewIntFieldEditor iconSpacingXField = null;
	private ViewIntFieldEditor iconSpacingYField = null;
	private ViewIntFieldEditor iconWidthField = null;
	private ViewIntFieldEditor iconHeightField = null;
	private ViewIntFieldEditor toolTipField = null;
	private Button tooltipIsWrapButton = null;
	private Button tooltipShowAllTimeButton = null;
	
	private class ViewIntFieldEditor {
		private int textLimit = 5;
		private String labelText = null;
		private int min = 0;
		private int max = 10;
		private Text textField = null;		
		private String msg = "";
		
		ViewIntFieldEditor(String labelText, int min, int max, Composite parent) {
			this.labelText = labelText;
			this.min = min;
			this.max = max;
			createControl(parent);
		}
		protected void createControl(Composite parent) {
			doFillIntoGrid(parent);
		}
		protected void doFillIntoGrid(Composite parent) {
			new Label(parent, SWT.LEFT).setText(labelText);
            
			textField = new Text(parent, SWT.SINGLE | SWT.BORDER);
            textField.setFont(parent.getFont());
            textField.setTextLimit(textLimit);
            textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        	textField.addModifyListener(new ModifyListener() {
        		public void modifyText(ModifyEvent e) {
        			PTPViewerPreferencesPage.this.isValid();
        		}	        		
        	});
			
			new Label(parent, SWT.RIGHT).setText("(" + min + "-" + max + ")");
		}
		public void setEnabled(boolean isEnable) {
			if (textField != null) {
				textField.setEnabled(isEnable);
			}
		}
	    public void setValue(int value) {
	        if (textField != null) {
	        	textField.setText(String.valueOf(value));
	        }
	    }
	    public int getValue() {
	    	try {
	    		return Integer.parseInt(textField.getText());
	    	} catch (NumberFormatException e) {
	    		return 0;
	    	}
	    }
	    public void setErrorMessage(String msg) {
	    	this.msg = msg;
	    }
	    public String getErrorMessage() {
	    	return msg;
	    }
	    public boolean isValid() {
	    	setErrorMessage("");
	    	try {
	    		int value = Integer.parseInt(textField.getText());
	    		if (value < min || value > max) {
	    			setErrorMessage("Value must be in the range of (" + min + "-" + max + ")");
	    			return false;
	    		}
	    	} catch (NumberFormatException e) {
	    		setErrorMessage("Value must be integer: " + e.getMessage());
	    		return false;
	    	}
	    	return true;
	    }
	}
	
	public PTPViewerPreferencesPage() {
		super();
		setPreferenceStore(PTPUIPlugin.getDefault().getPreferenceStore());
		setDescription(PreferenceMessages.getString("PTPViewerPreferencePage.desc"));
	}
	
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
	protected void createTooltipGroup(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PTPViewerPreferencesPage.tooltipName"));
		Composite compTooltip = createComposite(group, 1);

		tooltipShowAllTimeButton = createCheckButton(compTooltip, PreferenceMessages.getString("PTPViewerPreferencesPage.tooltip_showAllTime"));
		tooltipShowAllTimeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				toolTipField.setEnabled(!tooltipShowAllTimeButton.getSelection());
				if (tooltipShowAllTimeButton.getSelection())
					toolTipField.setValue((int)getPreferenceStore().getDefaultLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT));
			}
		});
		toolTipField = new ViewIntFieldEditor(PreferenceMessages.getString("PTPViewerPreferencesPage.tooltip_timeout"), 1000, 10000, createComposite(compTooltip, 3));
		tooltipIsWrapButton = createCheckButton(compTooltip, PreferenceMessages.getString("PTPViewerPreferencesPage.tooltip_iswrap"));
	}
	protected void createIconGroup(Composite parent) {
		Composite group = createGroupComposite(parent, 1, false, PreferenceMessages.getString("PTPViewerPreferencesPage.iconName"));
		Composite compIcon = createComposite(group, 3);

		iconSpacingXField = new ViewIntFieldEditor(PreferenceMessages.getString("PTPViewerPreferencesPage.icon_spacing_x"), 1, 10, compIcon);
		iconSpacingYField = new ViewIntFieldEditor(PreferenceMessages.getString("PTPViewerPreferencesPage.icon_spacing_y"), 1, 10, compIcon);

		iconWidthField = new ViewIntFieldEditor(PreferenceMessages.getString("PTPViewerPreferencesPage.icon_width"), 12, 100, compIcon);
		iconHeightField = new ViewIntFieldEditor(PreferenceMessages.getString("PTPViewerPreferencesPage.icon_height"), 12, 100, compIcon);		
	}
	protected void createViewSettingPreferences(Composite parent) {
		createIconGroup(parent);
		createTooltipGroup(parent);
	}
	public void performDefaults() { 
		IPreferenceStore store = getPreferenceStore();
		iconSpacingXField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_SPACING_X));
		iconSpacingYField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
		iconWidthField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_WIDTH));
		iconHeightField.setValue(store.getDefaultInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
		toolTipField.setValue((int)store.getDefaultLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT));
		tooltipIsWrapButton.setSelection(store.getDefaultBoolean(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP));
		tooltipShowAllTimeButton.setSelection(store.getDefaultBoolean(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME));
		super.performDefaults();
	}
	public boolean performOk() {
		storeValues();
		PTPUIPlugin.getDefault().savePluginPreferences();
		return true;
	}
	
	protected void setValues() {
		IPreferenceStore store = getPreferenceStore();
		iconSpacingXField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_X));
		iconSpacingYField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_SPACING_Y));
		iconWidthField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_WIDTH));
		iconHeightField.setValue(store.getInt(IPTPUIConstants.VIEW_ICON_HEIGHT));
		toolTipField.setValue((int)store.getLong(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT));
		tooltipIsWrapButton.setSelection(store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP));
		tooltipShowAllTimeButton.setSelection(store.getBoolean(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME));
	}
	protected void storeValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(IPTPUIConstants.VIEW_ICON_SPACING_X, iconSpacingXField.getValue());
		store.setValue(IPTPUIConstants.VIEW_ICON_SPACING_Y, iconSpacingYField.getValue());
		store.setValue(IPTPUIConstants.VIEW_ICON_WIDTH, iconWidthField.getValue());
		store.setValue(IPTPUIConstants.VIEW_ICON_HEIGHT, iconHeightField.getValue());
		store.setValue(IPTPUIConstants.VIEW_TOOLTIP_TIMEOUT, (long)toolTipField.getValue());
		store.setValue(IPTPUIConstants.VIEW_TOOLTIP_ISWRAP, tooltipIsWrapButton.getSelection());
		store.setValue(IPTPUIConstants.VIEW_TOOLTIP_SHOWALLTIME, tooltipShowAllTimeButton.getSelection());
	}
	
	public boolean isValid() {
		setErrorMessage(null);
		setMessage(null);
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
		if (!toolTipField.isValid()) {
			setErrorMessage(toolTipField.getErrorMessage());
			return false;
		}
		return true;
	}	
}
