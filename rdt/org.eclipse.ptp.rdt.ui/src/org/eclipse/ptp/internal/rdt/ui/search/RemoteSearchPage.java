/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchPage
 * Version: 1.9
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.CSearchUtil;
import org.eclipse.cdt.internal.ui.search.PDOMSearchPatternQuery;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.util.RowLayouter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.core.index.RemoteFastIndexer;
import org.eclipse.ptp.internal.rdt.core.model.Scope;
import org.eclipse.ptp.internal.rdt.core.search.RemoteSearchQuery;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.IServiceModelManager;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

public class RemoteSearchPage extends DialogPage implements ISearchPage {
	
	public static final String EXTENSION_ID = UIPlugin.PLUGIN_ID + ".searchPage"; //$NON-NLS-1$
	
	//Dialog store id constants
	private final static String PAGE_NAME = "PDOMSearchPage"; //$NON-NLS-1$
	private final static String STORE_CASE_SENSITIVE = "caseSensitive"; //$NON-NLS-1$
	private final static String STORE_PREVIOUS_PATTERNS = "previousPatterns"; //$NON-NLS-1$
	private final static String STORE_SEARCH_FLAGS = "searchFlags"; //$NON-NLS-1$

	/** Preference key for external marker enablement */
    public final static String EXTERNALMATCH_ENABLED = "externMatchEnable"; //$NON-NLS-1$
    /** Preference key for external marker visibilty */
    public final static String EXTERNALMATCH_VISIBLE = "externMatchVisible"; //$NON-NLS-1$

	private static final String[] searchForText= {
		CSearchMessages.CSearchPage_searchFor_classStruct, 
		CSearchMessages.CSearchPage_searchFor_function,	
		CSearchMessages.CSearchPage_searchFor_variable, 	
		CSearchMessages.CSearchPage_searchFor_union,		
		CSearchMessages.CSearchPage_searchFor_method, 		
		CSearchMessages.CSearchPage_searchFor_field,		
		CSearchMessages.CSearchPage_searchFor_enum,		
		CSearchMessages.CSearchPage_searchFor_enumr,		
		CSearchMessages.CSearchPage_searchFor_namespace,	
		CSearchMessages.CSearchPage_searchFor_typedef,	
		CSearchMessages.CSearchPage_searchFor_macro,	
		CSearchMessages.CSearchPage_searchFor_any
	};

	// These must be in the same order as the Text
	private static final Integer[] searchForData = {
		new Integer(PDOMSearchPatternQuery.FIND_CLASS_STRUCT),
		new Integer(PDOMSearchPatternQuery.FIND_FUNCTION),
		new Integer(PDOMSearchPatternQuery.FIND_VARIABLE),
		new Integer(PDOMSearchPatternQuery.FIND_UNION),
		new Integer(PDOMSearchPatternQuery.FIND_METHOD),
		new Integer(PDOMSearchPatternQuery.FIND_FIELD),
		new Integer(PDOMSearchPatternQuery.FIND_ENUM),
		new Integer(PDOMSearchPatternQuery.FIND_ENUMERATOR),
		new Integer(PDOMSearchPatternQuery.FIND_NAMESPACE),
		new Integer(PDOMSearchPatternQuery.FIND_TYPEDEF),
		new Integer(PDOMSearchPatternQuery.FIND_MACRO),
		new Integer(PDOMSearchPatternQuery.FIND_ALL_TYPES)
	};
	
	// the index of FIND_ALL_TYPES
	private static final int searchAllButtonIndex = searchForData.length - 1;

	private static String[] limitToText = {
		CSearchMessages.CSearchPage_limitTo_declarations, 
		CSearchMessages.CSearchPage_limitTo_definitions, 
		CSearchMessages.CSearchPage_limitTo_references, 
		CSearchMessages.CSearchPage_limitTo_allOccurrences
	};

	// Must be in the same order as the text
	private static Integer[] limitToData = {
		new Integer(RemoteSearchQuery.FIND_DECLARATIONS),
		new Integer(RemoteSearchQuery.FIND_DEFINITIONS),
		new Integer(RemoteSearchQuery.FIND_REFERENCES),
		new Integer(RemoteSearchQuery.FIND_ALL_OCCURANCES),
	};
	
	// The index of FIND_ALL_OCCURANCES
	private static final int limitToAllButtonIndex = limitToData.length - 1;
	
	private Combo patternCombo;
	private String[] previousPatterns;
	private Button caseSensitiveButton;
	
	private Button[] searchForButtons;
	private Button[] limitToButtons;
	
	private boolean firstTime = true;
	private IStructuredSelection structuredSelection;
	private ITextSelection textSelection;

	private ISearchPageContainer pageContainer;
	
	private IStatusLineManager fLineManager;

	private static ICProject getProject(Object object) {
		if (object instanceof ICElement)
			return ((ICElement)object).getCProject();
		else if (object instanceof IResource) {
			return CoreModel.getDefault().create(((IResource)object).getProject());
		}
		else
			return null;
	}
	
	public boolean performAction() {
	    fLineManager.setErrorMessage(null);

	    boolean isCaseSensitive = caseSensitiveButton.getSelection();

	    // get the pattern and turn it into a regular expression
	    String patternStr = patternCombo.getText();

	    // Get search flags
	    int searchFlags = 0;
	    if (searchForButtons[searchAllButtonIndex].getSelection())
	    	searchFlags |= PDOMSearchPatternQuery.FIND_ALL_TYPES;
	    else {
	    	for (int i = 0; i < searchForButtons.length; ++i) {
	    		if (searchForButtons[i].getSelection())
	    			searchFlags |= ((Integer)searchForButtons[i].getData()).intValue();
	    	}
	    }
	    for (int i = 0; i < limitToButtons.length; ++i) {
	    	if (limitToButtons[i].getSelection())
    			searchFlags |= ((Integer)limitToButtons[i].getData()).intValue();
	    }
	    
		// get the list of elements for the scope
		List elements = new ArrayList();
		String scopeDescription = ""; //$NON-NLS-1$
		switch (getContainer().getSelectedScope()) {
		case ISearchPageContainer.SELECTED_PROJECTS_SCOPE:
			if (structuredSelection != null) {
				scopeDescription = CSearchMessages.ProjectScope; 
				for (Iterator i = structuredSelection.iterator(); i.hasNext();) {
					ICProject project = getProject(i.next());
					if (project != null)
						elements.add(project);
				}
			}
			break;
		case ISearchPageContainer.SELECTION_SCOPE:
			if( structuredSelection != null) {
				scopeDescription = CSearchMessages.SelectionScope; 
				for (Iterator i = structuredSelection.iterator(); i.hasNext();) {
					Object obj = i.next();
					if (obj instanceof IResource)
						elements.add(CoreModel.getDefault().create((IResource)obj));
					else if (obj instanceof ICElement)
						elements.add(obj);
				}
				break;
			}
			break;
		case ISearchPageContainer.WORKSPACE_SCOPE:
			scopeDescription = CSearchMessages.WorkspaceScope; 
			// Don't add anything
			break;
		case ISearchPageContainer.WORKING_SET_SCOPE:
			IWorkingSet[] workingSets= getContainer().getSelectedWorkingSets();
			scopeDescription = Messages.format(CSearchMessages.WorkingSetScope, CSearchUtil.toString(workingSets)); 
			for (int i = 0; i < workingSets.length; ++i) {
				IAdaptable[] wsElements = workingSets[i].getElements();
				for (int j = 0; j < wsElements.length; ++j) {
					ICProject project = getProject(wsElements[j]);
					if (project != null)
						elements.add(project);
				}
			}
			break;
		}
		
		ICElement[] scope
			= elements.isEmpty()
			? null
			: (ICElement[])elements.toArray(new ICElement[elements.size()]);
		
		try {
			// TODO: Where are we going to find an IProject when doing a global search?
			IProject project = null;
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject p : projects) {
				try {
					if (p.isOpen() && p.hasNature(RemoteNature.REMOTE_NATURE_ID)) {
						project = p;
					}
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
			if (project == null) {
				return false;
			}
			
			IServiceModelManager smm = ServiceModelManager.getInstance();
			IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);

			IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);

			IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);

			if (!(serviceProvider instanceof IIndexServiceProvider2)) {
				return false;
			}
			ISearchService service = ((IIndexServiceProvider2) serviceProvider).getSearchService();
			ISearchQuery job = service.createSearchPatternQuery(Scope.WORKSPACE_ROOT_SCOPE, scope, scopeDescription, patternStr, isCaseSensitive, searchFlags);

			NewSearchUI.activateSearchResultView();
		
			NewSearchUI.runQueryInBackground(job);
		} catch (PatternSyntaxException e) {
			fLineManager.setErrorMessage(CSearchMessages.PDOMSearch_query_pattern_error); 
			return false;
		}

		// Save our settings
		IDialogSettings settings = getDialogSettings();
		settings.put(STORE_CASE_SENSITIVE, isCaseSensitive);
		
		if (previousPatterns == null)
			previousPatterns = new String[] { patternStr };
		else {
			// Add only if we don't have it already
			boolean addit = true;
			for (int i = 0; i < previousPatterns.length; ++i) {
				if (patternStr.equals(previousPatterns[i])) {
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

		return true;
	}

	public void createControl(Composite parent) {
		initializeDialogUnits( parent );
		
		GridData gd;
		Composite  result = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( 2, false );
		layout.horizontalSpacing = 10;
		result.setLayout( layout );
		result.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
		
		RowLayouter layouter = new RowLayouter( layout.numColumns );
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment   = GridData.VERTICAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL;
	
		layouter.setDefaultGridData( gd, 0 );
		layouter.setDefaultGridData( gd, 1 );
		layouter.setDefaultSpan();

		layouter.perform( createExpression(result) );
		layouter.perform( createSearchFor(result), createLimitTo(result), -1 );
		
		setControl( result );
		
		fLineManager = getStatusLineManager();
		
		Dialog.applyDialogFont( result );
		PlatformUI.getWorkbench().getHelpSystem().setHelp(result, ICHelpContextIds.C_SEARCH_PAGE);	
	}

	private IStatusLineManager getStatusLineManager(){
		
		IWorkbenchWindow wbWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wbWindow != null) {
			IWorkbenchPage page= wbWindow.getActivePage();
			if (page != null) {
				 IWorkbenchPartSite workbenchSite = page.getActivePart().getSite();
				 if (workbenchSite instanceof IViewSite){
				 	return ((IViewSite) workbenchSite).getActionBars().getStatusLineManager();
				 }
				 else if (workbenchSite instanceof IEditorSite){
				 	return ((IEditorSite) workbenchSite).getActionBars().getStatusLineManager();
				 }
			}
		}
		
		return null;
	}

	private Control createExpression( Composite parent ) {
		Composite  result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		result.setLayout(layout);
		GridData gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL );
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 0;
		result.setLayoutData( gd );

		// Pattern text + info
		Label label = new Label( result, SWT.LEFT );
		label.setText( CSearchMessages.CSearchPage_expression_label ); 
		gd = new GridData( GridData.BEGINNING );
		gd.horizontalSpan = 2;
		label.setLayoutData( gd );

		// Pattern combo
		patternCombo = new Combo( result, SWT.SINGLE | SWT.BORDER );
		patternCombo.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
				//handlePatternSelected();
			}
		});
		
		patternCombo.addModifyListener( new ModifyListener() {
			public void modifyText( ModifyEvent e ) {
				setPerformActionEnabled();
			}
		});
		
		gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL );
		gd.horizontalIndent = -gd.horizontalIndent;
		patternCombo.setLayoutData( gd );


		// Ignore case checkbox		
		caseSensitiveButton= new Button(result, SWT.CHECK);
		caseSensitiveButton.setText(CSearchMessages.CSearchPage_expression_caseSensitive); 
		gd= new GridData();
		caseSensitiveButton.setLayoutData(gd);
		caseSensitiveButton.addSelectionListener( new SelectionAdapter() {
			public void widgetSelected( SelectionEvent e ) {
//				isCaseSensitive = caseSensitiveButton.getSelection();
				setPerformActionEnabled();
			}
		});
	
		return result;
	}

	private Control createLimitTo( Composite parent ) {
		Group result = new Group(parent, SWT.NONE);
		result.setText( CSearchMessages.CSearchPage_limitTo_label ); 
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		result.setLayout( layout );

		limitToButtons = new Button[limitToText.length];
		for( int i = 0; i < limitToText.length; i++ ){
			Button button = new Button(result, SWT.RADIO);
			button.setText( limitToText[i] );
			button.setData( limitToData[i] );
			limitToButtons[i] = button;
		}

		return result;		
	}
	
	private Control createSearchFor(Composite parent) {
		Group result= new Group(parent, SWT.NONE);
		result.setText(CSearchMessages.CSearchPage_searchFor_label); 
		GridLayout layout= new GridLayout();
		layout.numColumns= 3;
		result.setLayout(layout);
		
		SelectionAdapter searchForSelectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Button me = (Button)event.widget;
				if (me == searchForButtons[searchAllButtonIndex]) {
					if (me.getSelection()) {
						for (int i = 0; i < searchForButtons.length; ++i) {
							if (i != searchAllButtonIndex) {
								searchForButtons[i].setSelection(true);
								searchForButtons[i].setEnabled(false);
							}
						}
					} else {
						for (int i = 0; i < searchForButtons.length; ++i) {
							if (i != searchAllButtonIndex) {
								searchForButtons[i].setSelection(false);
								searchForButtons[i].setEnabled(true);
							}
						}
					}
				}
				setPerformActionEnabled();
			}
		};

		searchForButtons= new Button[searchForText.length];
		for (int i= 0; i < searchForText.length; i++) {
			Button button= new Button(result, SWT.CHECK);
			button.setText(searchForText[i]);
			button.setData(searchForData[i]);
			button.addSelectionListener(searchForSelectionAdapter);
			searchForButtons[i]= button;
		}

		return result;		
	}
	
	public void setContainer(ISearchPageContainer container) {
		pageContainer = container;
	}
	
	private ISearchPageContainer getContainer() {
		return pageContainer;
	}
	
	private void setPerformActionEnabled() {
		boolean enable = true;
		
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
		IDialogSettings settings = CUIPlugin.getDefault().getDialogSettings();
		IDialogSettings searchSettings = settings.getSection( PAGE_NAME );
		if( searchSettings == null )
			searchSettings = settings.addNewSection( PAGE_NAME );
		return searchSettings;
	}

	public void setVisible(boolean visible) {
		if (visible) {
			if (firstTime) {
				firstTime= false;
				
				IDialogSettings settings = getDialogSettings();
				
				int searchFlags = PDOMSearchPatternQuery.FIND_ALL_TYPES | RemoteSearchQuery.FIND_ALL_OCCURANCES;
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
					Object obj = structuredSelection.getFirstElement();
					if (obj instanceof ICElement) {
						ICElement element = (ICElement)obj;
						patternCombo.setText(element.getElementName());
						// Clear the type flags so we can set them correctly for what we have selected
						searchFlags = searchFlags & ~PDOMSearchPatternQuery.FIND_ALL_TYPES;
						switch (element.getElementType()) {
						case ICElement.C_CLASS:
						case ICElement.C_STRUCT:
							searchFlags |= PDOMSearchPatternQuery.FIND_CLASS_STRUCT;
							break;
						case ICElement.C_FUNCTION:
							searchFlags |= PDOMSearchPatternQuery.FIND_FUNCTION;
							break;
						case ICElement.C_VARIABLE:
							searchFlags |= PDOMSearchPatternQuery.FIND_VARIABLE;
							break;
						case ICElement.C_UNION:
							searchFlags |= PDOMSearchPatternQuery.FIND_UNION;
							break;
						case ICElement.C_METHOD:
							searchFlags |= PDOMSearchPatternQuery.FIND_METHOD;
							break;
						case ICElement.C_FIELD:
							searchFlags |= PDOMSearchPatternQuery.FIND_FIELD;
							break;
						case ICElement.C_ENUMERATION:
							searchFlags |= PDOMSearchPatternQuery.FIND_ENUM;
							break;
						case ICElement.C_ENUMERATOR:
							searchFlags |= PDOMSearchPatternQuery.FIND_ENUMERATOR;
							break;
						case ICElement.C_NAMESPACE:
							searchFlags |= PDOMSearchPatternQuery.FIND_NAMESPACE;
							break;
						case ICElement.C_TYPEDEF:
							searchFlags |= PDOMSearchPatternQuery.FIND_TYPEDEF;
							break;
						case ICElement.C_MACRO:
							searchFlags |= PDOMSearchPatternQuery.FIND_MACRO;
							break;
						default:
							// Not sure, set to all
							searchFlags |= PDOMSearchPatternQuery.FIND_ALL_TYPES;
							patternCombo.setText(""); //$NON-NLS-1$
						}
					}
				} else if (selection instanceof ITextSelection) {
					textSelection = (ITextSelection)selection;
					patternCombo.setText(textSelection.getText());
					// TODO it might be good to do a selection parse to ensure that
					// the selection is valid.
				}

				caseSensitiveButton.setSelection(settings.getBoolean(STORE_CASE_SENSITIVE));
				
				if ((searchFlags & PDOMSearchPatternQuery.FIND_ALL_TYPES) == PDOMSearchPatternQuery.FIND_ALL_TYPES) {
					searchForButtons[searchAllButtonIndex].setSelection(true);
					for (int i = 0; i < searchForButtons.length; ++i) {
						if (i != searchAllButtonIndex) {
							searchForButtons[i].setSelection(true);
							searchForButtons[i].setEnabled(false);
						}
					}
				} else {
					searchForButtons[searchAllButtonIndex].setSelection(false);
					for (int i = 0; i < searchForButtons.length; ++i) {
						if (i != searchAllButtonIndex) {
							searchForButtons[i].setSelection(
								(searchFlags & ((Integer)searchForButtons[i].getData()).intValue()) != 0);
						}
					}
				}
				
				if ((searchFlags & RemoteSearchQuery.FIND_ALL_OCCURANCES) == RemoteSearchQuery.FIND_ALL_OCCURANCES) {
					limitToButtons[limitToAllButtonIndex].setSelection(true);
				} else {
					limitToButtons[limitToAllButtonIndex].setSelection(false);
					for (int i = 0; i < limitToButtons.length - 2; ++i) {
						limitToButtons[i].setSelection(
								(searchFlags & ((Integer)limitToButtons[i].getData()).intValue()) != 0);
					}
				}
			}
			
			patternCombo.setFocus();
			setPerformActionEnabled();
		}
		super.setVisible(visible);
	}
	
}