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
 *   UIUC
 *******************************************************************************/
package org.eclipse.rephraserengine.ui.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.internal.ui.Activator;
import org.eclipse.rephraserengine.ui.WorkbenchSelectionInfo;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
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
import org.eclipse.ui.IWorkingSet;

/**
 * A generic search page with "Search For" and "Limit To" panes.
 * <p>
 * Based on org.eclipse.cdt.internal.ui.search.PDOMSearchPage
 *
 * @author Doug Schaefer
 * @author Jeff Dammeyer, Andrew Deason, Joe Digiovanna, Nick Sexmith
 * @author Jeff Overbey - Rephraser Engine generalization
 *
 * @since 2.0
 */
public abstract class SearchPage extends DialogPage implements ISearchPage
{
    // Dialog store id constants
    /** @return a string used as a key to store dialog settings; should be unique per search page */
    protected abstract String PAGE_NAME();
    private final static String STORE_PREVIOUS_PATTERNS = "previousPatterns"; //$NON-NLS-1$
    private final static String STORE_REGEX_SEARCH = "regexSearch"; //$NON-NLS-1$
    private final static String STORE_SEARCH_FLAGS = "searchFlags"; //$NON-NLS-1$

    private Combo patternCombo;
    private String[] previousPatterns;

    private MessageBox errorBox;

    private Button regexButton;

    private boolean isRegex;
    private boolean firstTime = true;
    private IStructuredSelection structuredSelection;
    private ITextSelection textSelection;

    private ISearchPageContainer pageContainer;

    private Label patternLabel;
    private final String globPatternString = Messages.SearchPage_AnyStringAnyCharLabel;
    
    /**
     * This method receives the workbench selection as an argument and returns the corresponding
     * {@link IResource}, if any.
     * <p>
     * This implementation can only identify a resource if the selection <i>is</i> an
     * {@link IResource}, or if an editor is open and it is editing an {@link IFile}. Override this
     * method to support other types of selections. (E.g., Photran overrides this method to handle
     * the case where an <code>ICElement</code> -- a C model element -- is selected.)
     */
    protected IResource getResource(Object obj)
    {
        if (obj instanceof IResource)
        {
            return (IResource)obj;
        }
        else if (obj instanceof IAdaptable)
        {
            Object res = ((IAdaptable)obj).getAdapter(IResource.class);
            return res == null ? null : (IResource)res;
        }
        else return null;
    }

    
    /**
     * Performs the search when the Search button is clicked
     */
    public boolean performAction()
    {
        String patternStr = patternCombo.getText();

        // get the list of elements for the scope
        List<IResource> scope = new ArrayList<IResource>();
        String scopeDescription = determineScope(scope);

        int searchFlags = getSearchFlagsFromSelectedButtons();

        ISearchQuery job;
        try
        {
            patternStr = patternStr.trim();
            job = createSearchQuery(scope, scopeDescription, patternStr, convertPattern(isRegex, patternStr), searchFlags);
        }
        catch (PatternSyntaxException e)
        {
            errorBox = new MessageBox(this.getShell(), SWT.ICON_ERROR | SWT.OK);
            errorBox.setText(Messages.SearchPage_InvalidSearchPatternTitle);
            errorBox.setMessage(Messages.SearchPage_SearchPatternIsInvalid + e.getMessage());
            errorBox.open();
            return false;
        }

        NewSearchUI.activateSearchResultView();

        NewSearchUI.runQueryInBackground(job);

        saveDialogSettings(patternStr, searchFlags);

        return true;
    }
    
    /**
     * @param isRegex is this a regular expression or not
     * @param patternStr the string to be converted
     * @return the converted string
     * If the input string is a regex, compile it and return the same string.
     * If the input string is not a regex (e.g. a Glob), convert the Glob to regex syntax
     * and pass
     */
    protected String convertPattern(boolean isRegex, String patternStr) throws PatternSyntaxException
    {
        if (isRegex)
        {
            Pattern.compile(patternStr);
            return patternStr;
        }
        else
        {
            return convertGlobToRegex(patternStr);
        }
    }

    public static String convertGlobToRegex(String patternStr)
    {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < patternStr.length(); ++i) {
            char c = patternStr.charAt(i);
            switch (c) {
            case '*':
                buff.append(".*"); //$NON-NLS-1$
                break;
            case '?':
                buff.append("."); //$NON-NLS-1$
                break;
            case '$':
                buff.append("\\$"); //$NON-NLS-1$
                break;
            default:
                if (!Character.toString(c).matches("[0-9a-zA-Z._]")) { //$NON-NLS-1$
                    throw new PatternSyntaxException(Messages.SearchPage_IllegalCharacterInPatternString, patternStr, i+1);
                }
                buff.append(Messages.SearchPage_11 + c);
            }
        }
        return buff.toString();
    }
    
    protected abstract ISearchQuery createSearchQuery(
        List<IResource> scope,
        String scopeDescription,
        String patternDescription,
        String patternRegex,
        int searchFlags);
    
    /**
     * @param scope The list of resources to be populated. This function populates the list.
     * @return the description of the search Scope.
     */
    private String determineScope(List<IResource> scope)
    {
        String scopeDescription;
        switch (getContainer().getSelectedScope())
        {
            case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
                scopeDescription = Messages.SearchPage_ScopeDescription_EnclosingProjects;
                if (structuredSelection != null)
                {
                    for (Iterator< ? > i = structuredSelection.iterator(); i.hasNext();)
                    {
                        IResource res = getResource(i.next());
                        if (res != null)
                        {
                            scope.add(res);
                        }
                    }
                }
                else
                {
                    IFile fileInEditor = new WorkbenchSelectionInfo().getFileInEditor();
                    if (fileInEditor != null) scope.add(fileInEditor.getProject());
                }
                break;
                
            case ISearchPageContainer.SELECTION_SCOPE:
                scopeDescription = Messages.SearchPage_ScopeDescription_SelectedResources;
                if (structuredSelection != null)
                {
                    for (Iterator< ? > i = structuredSelection.iterator(); i.hasNext();)
                    {
                        IResource res = getResource(i.next());
                        if (res != null)
                        {
                            scope.add(res);
                        }
                    }
                }
                break;
                
            case ISearchPageContainer.WORKING_SET_SCOPE:
                IWorkingSet[] workingSets = getContainer().getSelectedWorkingSets();
                scopeDescription = Messages.SearchPage_ScopeDescription_WorkingSet + toString(workingSets); // CSearchUtil.toString(workingSets);
                for (int i = 0; i < workingSets.length; ++i)
                {
                    IAdaptable[] wsElements = workingSets[i].getElements();
                    for (IAdaptable elem : wsElements)
                    {
                        IResource res = getResource(elem);
                        if (res != null)
                        {
                            scope.add(res);
                        }
                    }
                }
                break;
                
            case ISearchPageContainer.WORKSPACE_SCOPE:
            default:
                scope.add(ResourcesPlugin.getWorkspace().getRoot());
                scopeDescription = Messages.SearchPage_ScopeDescription_Workspace;
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
        else
        {
            // Add only if we don't have it already
            boolean addit = true;
            for (int i = 0; i < previousPatterns.length; ++i)
            {
                if (patternStr.equals(previousPatterns[i]))
                {

                    // move used pattern to the top of the list
                    String tmpPattern = previousPatterns[i];
                    System.arraycopy(previousPatterns, 0, previousPatterns, 1, i);
                    previousPatterns[0] = tmpPattern;

                    addit = false;
                    break;
                }
            }
            if (addit)
            {
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
        label.setText(Messages.SearchPage_SearchPatternLabel);
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
        regexButton.setText(Messages.SearchPage_RegularExpressionLabel);
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

        prepareToCreateSearchAndLimitButtons(groupsComposite);
        
        Group group = new Group(groupsComposite, SWT.NONE);
        group.setText(Messages.SearchPage_SearchForLabel);
        layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

        addSelectionListener(createSearchForButtons(group));

        group = new Group(groupsComposite, SWT.NONE);
        group.setText(Messages.SearchPage_LimitToLabel);
        layout = new GridLayout();
        layout.numColumns = 1;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

        addSelectionListener(createLimitToButtons(group));

        createAdditionalGroups(groupsComposite);
        
        setControl(result);
        
        Dialog.applyDialogFont(result);
    }

    private void addSelectionListener(Button[] buttons)
    {
        for (Button button : buttons)
        {
            button.addSelectionListener(new SelectionAdapter()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    setPerformActionEnabled();
                }
            });
        }
    }

    /**
     * If any groups other than "Search For" and "Limit To" should appear in this dialog,
     * this method should be overridden and those groups created here.
     */
    protected void createAdditionalGroups(Composite groupsComposite)
    {
    }

    public void setContainer(ISearchPageContainer container)
    {
        pageContainer = container;
    }

    private ISearchPageContainer getContainer()
    {
        return pageContainer;
    }

    protected void setPerformActionEnabled()
    {
        boolean enable = true;

        // if regex button is checked, remove description.
        patternLabel.setVisible(!isRegex);

        // Need a text string to search
        if (patternCombo.getText().length() == 0) enable = false;

        // Need a type
        if (!isAtLeastOneSearchForButtonChecked()) enable = false;

        getContainer().setPerformActionEnabled(enable);
    }
    
    private IDialogSettings getDialogSettings()
    {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings searchSettings = settings.getSection(PAGE_NAME());
        if (searchSettings == null) searchSettings = settings.addNewSection(PAGE_NAME());
        return searchSettings;
    }

    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            if (firstTime)
            {
                firstTime = false;
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

        int searchFlags = defaultSearchFlags();
        try
        {
            searchFlags = settings.getInt(STORE_SEARCH_FLAGS);
        }
        catch (NumberFormatException e)
        {
            // was uninitialized, assume the defaults
        }

        previousPatterns = settings.getArray(STORE_PREVIOUS_PATTERNS);
        if (previousPatterns != null) patternCombo.setItems(previousPatterns);
        // Initialize the selection
        ISelection selection = getContainer().getSelection();
        if (selection instanceof IStructuredSelection)
        {
            structuredSelection = (IStructuredSelection)selection;
        }
        else if (selection instanceof ITextSelection)
        {
            textSelection = (ITextSelection)selection;
            patternCombo.setText(textSelection.getText());
        }

        checkButtonsCorrespondingTo(searchFlags);

        isRegex = settings.getBoolean(STORE_REGEX_SEARCH);
        regexButton.setSelection(isRegex);
    }

    protected abstract int defaultSearchFlags();
    
    // Copy of CSearchUtil#toString
    public static String toString(IWorkingSet[] workingSets)
    {
        if (workingSets != null && workingSets.length > 0)
        {
            String string = new String();
            for (int i = 0; i < workingSets.length; i++)
            {
                if (i > 0) string += ", "; //$NON-NLS-1$
                string += workingSets[i].getName();
            }

            return string;
        }

        return null;
    }
    
    ////////////////////////////// SEARCH FOR, LIMIT TO BUTTONS //////////////////////////////
    
    // If this is too restrictive (i.e., check boxes and option buttons aren't sufficient),
    // override the protected methods in this section to create custom controls and give
    // the "search flags" integer a custom meaning.

    private String[] searchForText;
    private Integer[] searchForData;

    private String[] limitToText;
    private Integer[] limitToData;
    
    private Button[] searchForButtons;
    private Button[] limitToButtons;

    protected void prepareToCreateSearchAndLimitButtons(Composite groupsComposite)
    {
        List<Pair<String, Integer>> searchFor = searchFor();
        searchForText = new String[searchFor.size()];
        searchForData = new Integer[searchFor.size()];
        int i = 0;
        for (Pair<String, Integer> pair : searchFor)
        {
            searchForText[i] = pair.fst;
            searchForData[i] = pair.snd;
            i++;
        }

        List<Pair<String, Integer>> limitTo = limitTo();
        limitToText = new String[limitTo.size()];
        limitToData = new Integer[limitTo.size()];
        i = 0;
        for (Pair<String, Integer> pair : limitTo)
        {
            limitToText[i] = pair.fst;
            limitToData[i] = pair.snd;
            i++;
        }
    }

    protected Button[] createSearchForButtons(Group group)
    {
        searchForButtons = new Button[searchForText.length];
        for (int i = 0; i < searchForText.length; i++)
        {
            Button button = new Button(group, SWT.CHECK);
            button.setText(searchForText[i]);
            button.setData(searchForData[i]);
            searchForButtons[i] = button;
        }
        return searchForButtons;
    }

    protected Button[] createLimitToButtons(Group group)
    {
        limitToButtons = new Button[limitToText.length];
        for (int i = 0; i < limitToText.length; i++)
        {
            Button button = new Button(group, SWT.RADIO);
            button.setText(limitToText[i]);
            button.setData(limitToData[i]);
            limitToButtons[i] = button;
        }
        return limitToButtons;
    }

    protected boolean isAtLeastOneSearchForButtonChecked()
    {
        for (int i = 0; i < searchForButtons.length; ++i)
            if (searchForButtons[i].getSelection())
                return true;
        return false;
    }

    protected int getSearchFlagsFromSelectedButtons()
    {
        int searchFlags = 0;
        for (int i = 0; i < searchForButtons.length; ++i) {
            if (searchForButtons[i].getSelection())
                searchFlags |= ((Integer)searchForButtons[i].getData()).intValue();
        }
        for (int i = 0; i < limitToButtons.length; ++i) {
            if (limitToButtons[i].getSelection())
                searchFlags |= ((Integer)limitToButtons[i].getData()).intValue();
        }
        return searchFlags;
    }

    protected void checkButtonsCorrespondingTo(int searchFlags)
    {
        checkAllSearchForButtonsCorrespondingTo(searchFlags);
        selectFirstLimitToButtonCorrespondingTo(searchFlags);
    }

    protected void checkAllSearchForButtonsCorrespondingTo(int searchFlags)
    {
        for (int i = 0; i < searchForButtons.length; ++i)
        {
            boolean shouldCheck = (searchFlags & ((Integer)searchForButtons[i].getData()).intValue()) != 0;
            searchForButtons[i].setSelection(shouldCheck);
        }
    }

    protected void selectFirstLimitToButtonCorrespondingTo(int searchFlags)
    {
        boolean buttonHasBeenSelected = false;
        for (int i = 0; i < limitToButtons.length; ++i)
        {
            if (buttonHasBeenSelected)
            {
                limitToButtons[i].setSelection(false);
            }
            else
            {
                buttonHasBeenSelected = (searchFlags & ((Integer)limitToButtons[i].getData()).intValue()) != 0;
                limitToButtons[i].setSelection(buttonHasBeenSelected);
            }
        }
    }

    protected abstract List<Pair<String, Integer>> searchFor();

    protected abstract List<Pair<String, Integer>> limitTo();
}
