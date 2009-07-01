/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   QNX - Initial API and implementation
 *   IBM Corporation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.ui.vpg.Activator;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

/**
 * A dialog used to run a {@link VPGSearchQuery}.
 * <p>
 * Based on org.eclipse.cdt.internal.ui.search.PDOMSearchPage
 *
 * @author Doug Schaefer
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 * 
 * @see VPGSearchQuery
 */
//@SuppressWarnings("restriction")
public class VPGSearchPage extends DialogPage implements ISearchPage {
    
    public static final String EXTENSION_ID = Activator.PLUGIN_ID + ".vpgSearchPage";
    
    // Dialog store id constants
    private final static String PAGE_NAME = "VPGSearchPage";
    private final static String STORE_PREVIOUS_PATTERNS = "previousPatterns";
    private final static String STORE_REGEX_SEARCH = "regexSearch";
    private final static String STORE_SEARCH_FLAGS = "searchFlags";

    private static final String[] searchForText= {
        "Common block", 
        "Function",
        "Subroutine",     
        "Module",        
        "Variable",       
        "Program"
    };

    // These must be in the same order as the Text
    private static final Integer[] searchForData = {
        new Integer(VPGSearchQuery.FIND_COMMON_BLOCK),
        new Integer(VPGSearchQuery.FIND_FUNCTION),
        new Integer(VPGSearchQuery.FIND_SUBROUTINE),
        new Integer(VPGSearchQuery.FIND_MODULE),
        new Integer(VPGSearchQuery.FIND_VARIABLE),
        new Integer(VPGSearchQuery.FIND_PROGRAM)
        
    };

    private static String[] limitToText = {
        "All occurrences",
        "Declarations", 
        "References"
    };
    // Must be in the same order as the text
    private static Integer[] limitToData = {
        new Integer(VPGSearchQuery.FIND_ALL_OCCURANCES),
        new Integer(VPGSearchQuery.FIND_DECLARATIONS),
        new Integer(VPGSearchQuery.FIND_REFERENCES)
    };

    private Combo patternCombo;
    private String[] previousPatterns;

    private MessageBox errorBox;

    private Button[] searchForButtons;
    private Button[] limitToButtons;
    private Button regexButton;

    private boolean isRegex;
    private boolean firstTime = true;
    private IStructuredSelection structuredSelection;
    private ITextSelection textSelection;

    private ISearchPageContainer pageContainer;

    private Label patternLabel;
    private final String globPatternString = "(* = any string, ? = any character)";

    private static IResource getResource(Object obj) {
        if (obj instanceof ICElement) {
            return ((ICElement)obj).getResource();
        }
        if (obj instanceof IResource) {
            return (IResource)obj;
        }
        return null;
    }
    
    /**
     * Performs the search when the Search button is clicked
     */
    public boolean performAction() {
        String patternStr = patternCombo.getText();
        
        // Get search flags
        int searchFlags = 0;
        for (int i = 0; i < searchForButtons.length; ++i) {
            if (searchForButtons[i].getSelection())
                searchFlags |= ((Integer)searchForButtons[i].getData()).intValue();
        }
        for (int i = 0; i < limitToButtons.length; ++i) {
            if (limitToButtons[i].getSelection())
                searchFlags |= ((Integer)limitToButtons[i].getData()).intValue();
        }

        // get the list of elements for the scope
        List<IResource> scope = new ArrayList<IResource>();
        String scopeDescription = determineScope(scope);
        
        VPGSearchQuery job;
        try {
            job = new VPGSearchQuery(scope, scopeDescription,
                patternStr, searchFlags, isRegex);
        } catch (PatternSyntaxException e) {
            errorBox = new MessageBox(this.getShell(),SWT.ICON_ERROR| SWT.OK);
            errorBox.setText("Invalid Search Pattern");
            errorBox.setMessage("The search pattern entered is invalid:\n" + e.getMessage());
            errorBox.open();
            return false;
        }

        NewSearchUI.activateSearchResultView();

        NewSearchUI.runQueryInBackground(job);
        
        // Save our settings
        saveDialogSettings(patternStr, searchFlags);

        return true;
    }
    /**
     * @param scope The list of resources to be populated. This function populates the list.
     * @return the description of the search Scope.
     */
    private String determineScope(List<IResource> scope)
    {
        String scopeDescription;
        switch (getContainer().getSelectedScope()) {
        case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
            
            scopeDescription = "enclosing projects";
            if (structuredSelection != null) {
                for (Iterator<?> i = structuredSelection.iterator(); i.hasNext();) {
                    IResource res = getResource(i.next());
                    if (res != null) {
                        scope.add(res);
                    }
                }
            } else {
                IWorkbenchWindow wbWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                IEditorPart editor = wbWindow.getActivePage().getActiveEditor();
                AbstractFortranEditor fEditor = editor instanceof AbstractFortranEditor ? (AbstractFortranEditor)editor : null;
                scope.add(fEditor.getIFile().getProject());
            }
            break;
        case ISearchPageContainer.SELECTION_SCOPE:
            scopeDescription = "selected resources";
            if( structuredSelection != null) {
                for (Iterator<?> i = structuredSelection.iterator(); i.hasNext();) {
                    IResource res = getResource(i.next());
                    if (res != null) {
                        scope.add(res);
                    }
                }
            }
            break;
        case ISearchPageContainer.WORKING_SET_SCOPE:
            IWorkingSet[] workingSets= getContainer().getSelectedWorkingSets();
            scopeDescription = "Working Set - " + toString(workingSets); //CSearchUtil.toString(workingSets); 
            for (int i = 0; i < workingSets.length; ++i) {
                IAdaptable[] wsElements = workingSets[i].getElements();
                for (IAdaptable elem : wsElements) {
                    IResource res = getResource(elem);
                    if (res != null) {
                        scope.add(res);
                    }
                }
            }
            break;
        case ISearchPageContainer.WORKSPACE_SCOPE:
        default:
            scope.add(ResourcesPlugin.getWorkspace().getRoot());
            scopeDescription = "Workspace"; 
            break;
        }
        return scopeDescription;
    }
    /**
     * @param patternStr The search string
     * @param searchFlags The search flags (e.g. The check-boxes and radio buttons)
     * Saves all the settings in search page, so that when the user re-opens the page,
     * the same settings are restored.
     */
    private void saveDialogSettings(String patternStr, int searchFlags)
    {
        IDialogSettings settings = getDialogSettings();
        
        settings.put(STORE_REGEX_SEARCH, isRegex);
        
        if (previousPatterns == null)
            previousPatterns = new String[] { patternStr };
        else {
            // Add only if we don't have it already
            boolean addit = true;
            for (int i = 0; i < previousPatterns.length; ++i) {
                if (patternStr.equals(previousPatterns[i])) {
                    
                    // move used pattern to the top of the list
                    String tmpPattern = previousPatterns[i];
                    System.arraycopy(previousPatterns, 0, previousPatterns, 1, i);
                    previousPatterns[0] = tmpPattern;
                    
                    addit = false;
                    break;
                }
            }
            if (addit) {
                // Insert it into the beginning of the list
                String[] newPatterns = new String[previousPatterns.length + 1];
                System.arraycopy(previousPatterns, 0, newPatterns, 1, previousPatterns.length);
                newPatterns[0] = patternStr;
                previousPatterns = newPatterns;
            }
        }

        settings.put(STORE_PREVIOUS_PATTERNS, previousPatterns);
        settings.put(STORE_SEARCH_FLAGS, searchFlags);
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);
        
        GridData gd;
        Composite result = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        result.setLayout(layout);
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL;
        gd.horizontalSpan = 2;

        Label label = new Label(result, SWT.LEFT);
        label.setText("Search pattern:");
        label.setLayoutData(gd);

        // Pattern combo
        patternCombo = new Combo(result, SWT.SINGLE | SWT.BORDER);

        patternCombo.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                setPerformActionEnabled();
            }
        });
        
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        regexButton = new Button(result, SWT.CHECK);
        regexButton.setText("Regular e&xpression");
        gd = new GridData();
        regexButton.setLayoutData(gd);
        regexButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                isRegex = regexButton.getSelection();
                setPerformActionEnabled();
            }
        });

        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        gd.horizontalIndent = -gd.horizontalIndent;
        patternCombo.setLayoutData(gd);
        
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL;
        gd.horizontalSpan = 2;
        patternLabel = new Label(result, SWT.LEFT);
        patternLabel.setText(globPatternString);
        patternLabel.setLayoutData(gd);

        Composite groupsComposite = new Composite(result, SWT.NONE);
        layout = new GridLayout(2, false);
        groupsComposite.setLayout(layout);
        
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        groupsComposite.setLayoutData(gd);

        Group group = new Group(groupsComposite, SWT.NONE);
        group.setText("Search for");
        layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
            | GridData.VERTICAL_ALIGN_BEGINNING));

        searchForButtons = new Button[searchForText.length];
        for (int i = 0; i < searchForText.length; i++)
        {
            Button button = new Button(group, SWT.CHECK);
            button.setText(searchForText[i]);
            button.setData(searchForData[i]);
            searchForButtons[i] = button;
            button.addSelectionListener(new SelectionAdapter()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    setPerformActionEnabled();
                }
            });
        }

        group = new Group(groupsComposite, SWT.NONE);
        group.setText("Limit to");
        layout = new GridLayout();
        layout.numColumns = 1;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
            | GridData.VERTICAL_ALIGN_BEGINNING));

        limitToButtons = new Button[limitToText.length];
        for (int i = 0; i < limitToText.length; i++)
        {
            Button button = new Button(group, SWT.RADIO);
            button.setText(limitToText[i]);
            button.setData(limitToData[i]);
            limitToButtons[i] = button;
        }

        setControl(result);
        
        Dialog.applyDialogFont(result);
    }

    public void setContainer(ISearchPageContainer container) {
        pageContainer = container;
    }
    
    private ISearchPageContainer getContainer() {
        return pageContainer;
    }
    
    private void setPerformActionEnabled() {
        boolean enable = true;
        
        //if regex button is checked, remove description.
        patternLabel.setVisible(!isRegex);
        
        // Need a text string to search
        if (patternCombo.getText().length() == 0)
            enable = false;
        
        // Need a type
        boolean any = false;
        for (int i = 0; i < searchForButtons.length; ++i)
            if (searchForButtons[i].getSelection()) {
                any = true;
                break;
            }
        if (!any)
            enable = false;
        
        getContainer().setPerformActionEnabled(enable);
    }
    
    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings searchSettings = settings.getSection( PAGE_NAME );
        if( searchSettings == null )
            searchSettings = settings.addNewSection( PAGE_NAME );
        return searchSettings;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (firstTime) {
                firstTime= false;
                restoreDialogSettings();
            }
            
            patternCombo.setFocus();
            setPerformActionEnabled();
        }
        super.setVisible(visible);
    }
    
    private void restoreDialogSettings()
    {
        IDialogSettings settings = getDialogSettings();
        
        int searchFlags = VPGSearchQuery.FIND_ALL_TYPES | VPGSearchQuery.FIND_ALL_OCCURANCES;
        try {
            searchFlags = settings.getInt(STORE_SEARCH_FLAGS);
        } catch (NumberFormatException e) {
            // was uninitialized, assume the defaults
        }

        previousPatterns = settings.getArray(STORE_PREVIOUS_PATTERNS);
        if (previousPatterns != null)
            patternCombo.setItems(previousPatterns);
        // Initialize the selection
        ISelection selection = getContainer().getSelection();
        if (selection instanceof IStructuredSelection) {
            structuredSelection = (IStructuredSelection)selection;
        } else
            if (selection instanceof ITextSelection) {
            textSelection = (ITextSelection)selection;
            patternCombo.setText(textSelection.getText());
        }
        
        for (int i = 0; i < searchForButtons.length; ++i) {
            searchForButtons[i]
                .setSelection((searchFlags & ((Integer)searchForButtons[i].getData()).intValue()) != 0);
        }

        for (int i = 0; i < limitToButtons.length; ++i) {
            limitToButtons[i]
                .setSelection(((searchFlags & VPGSearchQuery.FIND_ALL_OCCURANCES) == ((Integer)limitToButtons[i]
                    .getData()).intValue()));
        }
        isRegex = settings.getBoolean(STORE_REGEX_SEARCH);
        regexButton.setSelection(isRegex);
        
    }

    // Copy of CSearchUtil#toString
    public static String toString(IWorkingSet[] workingSets) {
        if( workingSets != null && workingSets.length > 0 ){
            String string = new String();
            for( int i = 0; i < workingSets.length; i++ ){
                if( i > 0 )
                    string += ", ";  //$NON-NLS-1$
                string += workingSets[i].getName();
            }
            
            return string;
        }
        
        return null;
    }
}
