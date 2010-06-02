/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.properties;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.photran.internal.core.sourceform.SourceForm;
import org.eclipse.photran.internal.core.sourceform.SourceFormProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A field editor to edit which file types to send to the C Preprocessor.
 * 
 * @author Jeff Overbey, Kurt Hendle
 * 
 * @see org.eclipse.jface.preference.ListEditor
 */
public class FortranSourceFormEditor extends FieldEditor
{
    /** Width of the content types combo box in pixels */
    private static final int COMBO_WIDTH = 250;

    /** Minimum height of a combo box */
    private static final int DEFAULT_COMBO_HEIGHT = 17;

    /** Total number of pixels of vertical padding surrounding
     *  a combo box in a table */
    private static final int COMBO_VERTICAL_PADDING = 5;
    
    private Table table;
    
    private Listener measureItemListener = null;

    /**
     * Creates a Fortran Source Form field editor.
     * 
     * @param propertyName the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public FortranSourceFormEditor(String propertyName, String labelText, Composite parent)
    {
        init(propertyName, labelText);
        createControl(parent);
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns)
    {
        Control control = getLabelControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        control.setLayoutData(gd);

        createTableControl(parent);
        gd = new GridData();
        gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalSpan = numColumns;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        table.setLayoutData(gd);
    }

    private void createTableControl(Composite parent)
    {
        table = new Table(parent, SWT.BORDER | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for (int i=0; i<2; i++) {
            new TableColumn(table, SWT.NONE);
        }
        table.getColumn(0).setText(UIMessages.FortranSourceFormEditor_FileNameOrExtensionColumnLabel);
        table.getColumn(1).setText(UIMessages.FortranSourceFormEditor_SourceFormColumnLabel);
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    @Override
    protected void adjustForNumColumns(int numColumns)
    {
        Control control = getLabelControl();
        ((GridData)control.getLayoutData()).horizontalSpan = numColumns;
        ((GridData)table.getLayoutData()).horizontalSpan = numColumns;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    @Override
    protected void doLoad()
    {
        if (table != null)
            populateTable(SourceFormProperties.parseValue(getPreferenceStore().getString(getPreferenceName())));
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    @Override
    protected void doLoadDefault()
    {
        if (table != null)
            populateTable(SourceFormProperties.parseValue(getPreferenceStore().getDefaultString(getPreferenceName())));
    }

    private void populateTable(Map<String, String> prefValue)
    {
        table.removeAll();
        for (Control c : table.getChildren())
            if (c instanceof Combo)
                c.dispose();
        
        Combo combo = null;
        for (String spec : prefValue.keySet())
            combo = addItem(spec, prefValue.get(spec));

        table.getColumn(0).pack();
        table.getColumn(1).setWidth(COMBO_WIDTH);

        if (measureItemListener == null)
            table.addListener(SWT.MeasureItem, createMeasureItemListener(combo));
    }

    private Listener createMeasureItemListener(Combo combo)
    {
        final int comboHeight = combo == null ? DEFAULT_COMBO_HEIGHT : Math.max(combo.getSize().y, DEFAULT_COMBO_HEIGHT);
        
        /* NOTE: This callback must be efficient: It is called frequently */
        return measureItemListener = new Listener() {
            public void handleEvent(Event event) {
                event.height = Math.max(event.height, comboHeight + COMBO_VERTICAL_PADDING);
            }
        };
    }

    private Combo addItem(String spec, String sourceFormDescription)
    {
        final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { spec, sourceFormDescription });
        
        final Combo combo = new Combo(table, SWT.FLAT | SWT.BORDER);
        for (String sourceForm : SourceForm.allSourceForms())
            combo.add(sourceForm);

        combo.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                item.setText(1, combo.getText());
            }
            
            public void widgetDefaultSelected(SelectionEvent e)
            {
                widgetSelected(e);
            }
        });
        
        combo.setText(sourceFormDescription);
        int index = combo.indexOf(sourceFormDescription);
        if (index >= 0)
            combo.select(index);
        
        TableEditor editor = new TableEditor(table);
        editor.grabHorizontal = editor.grabVertical = true;
        editor.minimumWidth = COMBO_WIDTH;
        editor.setEditor(combo, item, 1);
        return combo;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    @Override
    protected void doStore()
    {
        Map<String, String> prefValue = new TreeMap<String, String>();
        for (TableItem item : table.getItems())
            prefValue.put(item.getText(0), item.getText(1));
        getPreferenceStore().setValue(getPreferenceName(), SourceFormProperties.unparseValue(prefValue));
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    @Override
    public int getNumberOfControls()
    {
        return 2;
    }

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    @Override
    public void setFocus()
    {
        if (table != null)
            table.setFocus();
    }
}
