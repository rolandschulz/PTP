/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Abhishek Sharma, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.ui.browser;

import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The Dependencies tab in the VPG Browser.
 * 
 * @author Abhishek Sharma
 */
class DependenciesTab
{
    private List incomingDependenciesList;
    private List outgoingDependenciesList;
    private Text fileNameTextBox;
    private Text timestampTextBox;

    public DependenciesTab(TabItem dependenciesTab, TabFolder tabfolder)
    {
        Composite composite = new Composite(tabfolder, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
       
        createDependentsList(composite);
        createFileNameField(composite);
        createTimeStampField(composite);
        createDependenciesList(composite);

        dependenciesTab.setControl(composite);
    }

    /**
     * Creates the group that shows files dependent on the selected file.
     * Consists of a group that contains a list.
     */
    private void createDependentsList(Composite composite)
    {
        Group group = new Group(composite, SWT.NULL);
        group.setLayout(new FillLayout());
        group.setText(Messages.DependenciesTab_FilesThatDependOnTheSelectedFile);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        group.setLayoutData(gridData);

        incomingDependenciesList = new List(group, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
    }

    private void createFileNameField(Composite composite)
    {
        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.DependenciesTab_FileName);

        fileNameTextBox = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        fileNameTextBox.setBounds(100, 100, 100, 100);
        fileNameTextBox.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }

    private void createTimeStampField(Composite composite)
    {
        Label label = new Label(composite, SWT.NONE);
        label.setText(Messages.DependenciesTab_TimeStamp);

        timestampTextBox = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        timestampTextBox.setBounds(100, 100, 100, 100);
        timestampTextBox.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
    }


    /**
     * Creates the group that shows the files
     * that the current file depends on. Consists of a group that contains a list.
     */
    private void createDependenciesList(Composite composite)
    {
        Group group = new Group(composite, SWT.NULL);
        group.setLayout(new FillLayout());
        group.setText(Messages.DependenciesTab_FilesTheSelectedFileDependsOn);

        outgoingDependenciesList = new List(group, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 2;
        group.setLayoutData(gridData);
    }

    /**
     * Once a file is selected, clears whatever was previously in the dependents and dependencies
     * section and shows data related to the current file. Dependents and Dependencies are pulled
     * from the database.
     */
    @SuppressWarnings("unchecked")
    public void showDependentsAndDependencies(String filename, EclipseVPG vpg)
    {
        incomingDependenciesList.removeAll();
        outgoingDependenciesList.removeAll();

        fileNameTextBox.setText(filename);

        for (String dependentFile : (Iterable<String>)vpg.db.getIncomingDependenciesTo(filename))
            incomingDependenciesList.add(dependentFile);

        for (String dependentFile : (Iterable<String>)vpg.db.getOutgoingDependenciesFrom(filename))
            outgoingDependenciesList.add(dependentFile);
    }
}
