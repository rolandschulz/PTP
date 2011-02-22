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

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.Workbench;

/**
 * Creates the VPG Browser shell.
 *
 * @author Abhishek Sharma
 */
@SuppressWarnings("restriction")
public class VPGBrowser
{
    private EclipseVPG vpg;
    private ArrayList<String> fileNameArray; //to copy all file names

    private Shell shell;
    private SashForm sashForm;
    private Composite composite;
    private Text fileNameTextBox;
    private List fileList;
    private TabFolder tabFolder;
    private DependenciesTab dependenciesTab;
    private EdgesTab edgesTab ;
 
    private AnnotationsTab annotationsTab ;

    public VPGBrowser(EclipseVPG vpg)
    {
        this.vpg = vpg;

        createShell();
        createSashForm();
        createComposite();
        createFileTextField();
        createList();
        createTabFolder();

        listAllFiles();
    }

    private void createShell()
    {
        shell = new Shell(Workbench.getInstance().getDisplay());
        shell.setText(Messages.VPGBrowser_WindowTitle);
        centerShellInDisplay();
        closeShellWhenEscapeIsPressed();
        shell.setLayout(new GridLayout());
        
    }

    private void centerShellInDisplay()
    {
        Rectangle monitor = shell.getDisplay().getPrimaryMonitor().getBounds();
        shell.setSize(monitor.width * 3 / 4, monitor.height * 3 / 4);
        Rectangle window = shell.getBounds();
        int x = monitor.x + (monitor.width - window.width) / 2;
        int y = monitor.y + (monitor.height - window.height) / 2;
        shell.setLocation(x, y);
    }

    private void closeShellWhenEscapeIsPressed()
    {
        shell.addListener(SWT.Traverse, new Listener()
        {
            public void handleEvent(Event event)
            {
                switch (event.detail)
                {
                    case SWT.TRAVERSE_ESCAPE:
                        shell.close();
                        event.detail = SWT.TRAVERSE_NONE;
                        event.doit = false;
                        break;
                }
            }
        });
    }

    /** Creates the sashform with the parent being shell*/
    private void createSashForm()
    {
        sashForm = new SashForm(shell, SWT.HORIZONTAL);
        sashForm.setLayout(new FillLayout());
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    /** Creates the composite on left side of sash with the parent being sash */
    private void createComposite()
    {
        composite = new Composite(sashForm, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout(gridLayout);
    }

    /** Creates the text field on top left side with the parent being composite */
    private void createFileTextField()
    {
        // creating a label for the text field
        Label fileNameLabel = new Label(composite, SWT.NONE);
        fileNameLabel.setText(Messages.VPGBrowser_FileNmae);

       
        fileNameTextBox = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
        fileNameTextBox.setBounds(100, 100, 100, 100);
        fileNameTextBox.setText(""); //$NON-NLS-1$
        fileNameTextBox.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        fileNameTextBox.addModifyListener(new TextBoxModifyListener());
    }

    /** Filters the list of filenames when the user enters text in the text box */
    private final class TextBoxModifyListener implements ModifyListener
    {
        public void modifyText(ModifyEvent e)
        {
            if (fileNameTextBox.getText().length() == 0)
                listAllFiles();
            else
                listFilteredFiles();
        }

        private void listFilteredFiles()
        {
            fileList.removeAll();

            // this loop checks if the user typed string is a substring of any of the
            // files present in the workspace and filters the list based
            // on the user-inputted string
            for (int i = 0; i < fileNameArray.size(); i++)
            {
                int substringIndex = fileNameArray.get(i).toLowerCase().indexOf(fileNameTextBox.getText().toLowerCase());
                if (substringIndex != -1)
                    fileList.add(fileNameArray.get(i));
            }

            if (fileList.getItemCount() == 1){
                fileList.setSelection(0); 
                showFile(fileList.getItem(0));
            }
        }
    }

    /**
     * @param index index of the file to display in {@link #fileNameArray}
     */
    private void showFile(String filename)
    {
        dependenciesTab.showDependentsAndDependencies(filename, vpg);
        edgesTab.showEdges(filename, vpg) ;
        annotationsTab.showAnnotations(filename);
    }

    /** Creates the List for files on bottom left side with the parent being composite */
    private void createList()
    {
        // creating a label for the list
        Label fileNameLabel = new Label(composite, SWT.NONE);
        fileNameLabel.setText(Messages.VPGBrowser_Files);

        
        fileList = new List(composite, SWT.PUSH | SWT.SINGLE | SWT.V_SCROLL); // TODO: Remove SWT.PUSH?
        fileList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fileList.addSelectionListener(new FileListSelectionListener());
    }

    /** Shows the contents of the selected file when the user selects a file in the file list */
    private final class FileListSelectionListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            int index = fileList.getSelectionIndex();
            if (index < 0) return;

            String filename = fileList.getItem(index);
            
            showFile(filename);
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
            widgetSelected(e);
        }
    }

    /** Creates the tab folder on the right side with the parent being composite. Two tabs are created. */
    private void createTabFolder()
    {
        tabFolder = new TabFolder(sashForm, SWT.NULL);

        TabItem edges = new TabItem(tabFolder, SWT.NULL);
        edges.setText(Messages.VPGBrowser_Edges);
        edgesTab = new EdgesTab(edges,tabFolder,vpg);

        TabItem dependencies = new TabItem(tabFolder, SWT.NULL);
        dependencies.setText(Messages.VPGBrowser_Dependencies);
        dependenciesTab = new DependenciesTab(dependencies, tabFolder);
        
        TabItem annotations = new TabItem(tabFolder, SWT.NULL);
        annotations.setText(Messages.VPGBrowser_Annotations);
        annotationsTab = new AnnotationsTab(annotations,tabFolder,vpg);
    }

    /** Return the shell to the {@link BrowseDBAction} class*/
    public Shell getShell()
    {
        return shell;
    }

    /** Populates the filename list with all files from the workspace */
    @SuppressWarnings("unchecked")
    private void listAllFiles()
    {
        fileList.removeAll();
        fileNameArray = new ArrayList<String>();
        int i = 0;
        //this loops gets all the files from the workspace
        for (String filename : (Iterable<String>)vpg.listAllFilenames())
        {
            fileList.add(filename);
            fileNameArray.add(filename);
            i++;
        }
    }

    /**
     * Opens the VPG Browser window.
     */
    public void open()
    {
        shell.open();
        runEventLoop();
    }

    private void runEventLoop()
    {
        Display display = shell.getDisplay();

        while (shell != null && !shell.isDisposed())
        {
            try
            {
                if (!display.readAndDispatch())
                    display.sleep();
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                //Activator.log(e);
                MessageDialog.openError(null,
                    e.getClass().getName(),
                    describe(e));
            }
        }

        if (!display.isDisposed())
            display.update();
    }

    private String describe(Throwable e)
    {
        return e.getClass().getName() + ": " + e.getMessage() //$NON-NLS-1$
            + "\n\nPlease see the stack trace printed to the console."; //$NON-NLS-1$
    }
}
