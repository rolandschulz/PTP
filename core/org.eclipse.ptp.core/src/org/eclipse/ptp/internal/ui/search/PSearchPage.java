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
package org.eclipse.ptp.internal.ui.search;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.search.internal.ui.util.RowLayouter;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class PSearchPage extends DialogPage implements ISearchPage, IPSearchConstants {
    private IPElement fElement;
    private SearchPatternData fInitialData;    
	private boolean fFirstTime = true;

	private Combo fPattern;
	private ISearchPageContainer fContainer;

	private Button[] fSearchFor;
	private Button[] fLimitTo;
	private String[] fSearchForText= { "Node", "Process" };
	private String[] fLimitToText= { "Number", "PID", "Exit code" };
	
	private static List fgPreviousSearchPatterns = new ArrayList(20);
	
	private boolean isInteger(String text) {
	    try {
	        Integer.parseInt(text);
	    } catch (NumberFormatException e) {
	        return false;
	    }
	    return true;
	}
	public boolean performAction() {
	    SearchPatternData data = getPatternData();    	    
				
		PSearchQuery job = new PSearchQuery(data.pattern, data.searchFor, data.limitTo);
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQuery(job);

		return true;
	}
	
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.horizontalSpacing = 10;
		result.setLayout(layout);
		result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		RowLayouter layouter = new RowLayouter(layout.numColumns);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL;
		
		layouter.setDefaultGridData(gd, 0);
		layouter.setDefaultGridData(gd, 1);
		layouter.setDefaultSpan();
		
		layouter.perform(createExpression(result));
		layouter.perform(createSearchFor(result), createLimitTo(result), 2);
		
		SelectionAdapter ptpElementInitializer = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(getSearchFor() == fInitialData.getSearchFor())
					fElement = fInitialData.getElement();
				else
				    fElement = null;

				setLimitToEnable(getSearchFor());
			}
		};
		fSearchFor[SEARCH_NODE].addSelectionListener(ptpElementInitializer);
		fSearchFor[SEARCH_PROCESS].addSelectionListener(ptpElementInitializer);

		setControl(result);
		Dialog.applyDialogFont(result);

		initSelections();
	}
	
	private Control createExpression(Composite parent) {
		Composite  result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		result.setLayout(layout);
		GridData gd = new GridData( GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL );
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 0;
		result.setLayoutData( gd );

		// Pattern text + info
		Label label = new Label( result, SWT.LEFT );
		label.setText("Search pattern -- (* == any thing, ? == any character) ");
		gd = new GridData( GridData.BEGINNING );
		gd.horizontalSpan = 2;
		label.setLayoutData( gd );

		// Pattern combo
		fPattern = new Combo(result, SWT.SINGLE | SWT.BORDER);
		fPattern.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handlePatternSelected();
			}
		});

		fPattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getContainer().setPerformActionEnabled(isPerformActionEnabled());
			}
		});

		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalIndent = -gd.horizontalIndent;
		fPattern.setLayoutData( gd );
		
		return result;
	}
	
	private void handlePatternSelected() {
		if(fPattern.getSelectionIndex() < 0)
			return;

		int index = fgPreviousSearchPatterns.size() - fPattern.getSelectionIndex() - 1;
		fInitialData = (SearchPatternData) fgPreviousSearchPatterns.get(index);
		updateSelections();
	}
	
	private boolean isPerformActionEnabled() {
	    //return isInteger(getPattern());
	    return getPattern().length() > 0;
	}

	private Control createSearchFor(Composite parent) {
		Group result= new Group(parent, SWT.NONE);
		result.setText("Search For");
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		result.setLayout(layout);

		fSearchFor = new Button[fSearchForText.length];
		for (int i= 0; i < fSearchForText.length; i++) {
			Button button= new Button(result, SWT.RADIO);
			button.setText(fSearchForText[i]);
			fSearchFor[i]= button;
		}
		return result;
	}	
	
	private Control createLimitTo( Composite parent ) {
		Group result = new Group(parent, SWT.NONE);
		result.setText("Limit To");
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		result.setLayout( layout );

		fLimitTo = new Button[fLimitToText.length];
		for( int i = 0; i < fLimitToText.length; i++ ){
			Button button = new Button(result, SWT.RADIO);
			button.setText( fLimitToText[i] );
			fLimitTo[i] = button;
		}
		return result;
	}
	
	private void initSelections() {
		IStructuredSelection fStructuredSelection = asStructuredSelection();
		fInitialData = tryStructuredSelection(fStructuredSelection);
		updateSelections();
	}
	
	private void resetButton() {
		for (int i= 0; i<fSearchFor.length; i++)
			fSearchFor[i].setSelection(false);
		for (int i= 0; i<fLimitTo.length; i++)
			fLimitTo[i].setSelection(false);
	}
	
	private void updateSelections() {
		resetButton();
		
		if (fInitialData == null)
			fInitialData = getDefaultInitValues();

		fElement = fInitialData.getElement();
		fSearchFor[fInitialData.getSearchFor()].setSelection(true);
		setLimitToEnable(fInitialData.getSearchFor());
		fLimitTo[fInitialData.getLimitTo()].setSelection(true);
		fPattern.setText(fInitialData.getPattern());
	}
	
	private SearchPatternData tryStructuredSelection(IStructuredSelection selection) {
		if (selection == null || selection.size() > 1)
			return null;

		Object obj = selection.getFirstElement();
		if (obj instanceof IPElement)
			return determineInitValuesFrom((IPElement)obj);

		return null;
	}
	
	private SearchPatternData determineInitValuesFrom(IPElement element) {
		if (element == null)
			return null;
		
		int searchFor = SEARCH_NODE;
		int limitTo = LIMIT_NUMBER;
		
		String pattern = String.valueOf(element.getKeyNumber());		
		switch (element.getElementType()) {
		    case IPElement.P_NODE:
		        searchFor = SEARCH_NODE;
		        break;
		    case IPElement.P_PROCESS:
		        searchFor = SEARCH_PROCESS;
		        break;
		}
		return new SearchPatternData(searchFor, limitTo, pattern, element);
	}
	
	
	private SearchPatternData getDefaultInitValues() {
		return new SearchPatternData(SEARCH_NODE, LIMIT_NUMBER, "", null);
	}
	
	/**
	 * Return search pattern data and update previous searches.
	 * An existing entry will be updated.
	 */
	private SearchPatternData getPatternData() {
		String pattern = getPattern();
		SearchPatternData match = null;
		int i = 0;
		int size = fgPreviousSearchPatterns.size();
		while (match == null && i < size) {
			match = (SearchPatternData) fgPreviousSearchPatterns.get(i);
			i++;
			if (!pattern.equals(match.getPattern()))
				match = null;
		}
		if (match == null) {
			match = new SearchPatternData(getSearchFor(), getLimitTo(), pattern, fElement);
			fgPreviousSearchPatterns.add(match);
		}
		else {
			match.setSearchFor(getSearchFor());
			match.setLimitTo(getLimitTo());
			match.setElement(fElement);
		}
		return match;
	}
	
	/*
	 * Implements method from IDialogPage
	 */
	public void setVisible(boolean visible) {
		if (visible && fPattern != null) {
			if (fFirstTime) {
				fFirstTime = false;
				// Set item and text here to prevent page from resizing
				fPattern.setItems(getPreviousSearchPatterns());
				initSelections();
			}
			fPattern.setFocus();
			getContainer().setPerformActionEnabled(fPattern.getText().length() > 0);
		}
		super.setVisible(visible);
	}	
	
	private String[] getPreviousSearchPatterns() {
		// Search results are not persistent
		int patternCount= fgPreviousSearchPatterns.size();
		String [] patterns= new String[patternCount];
		for (int i= 0; i < patternCount; i++)
			patterns[i]= ((SearchPatternData) fgPreviousSearchPatterns.get(patternCount - 1 - i)).getPattern();

		return patterns;
	}

	private int getSearchFor() {
		for (int i= 0; i < fSearchFor.length; i++) {
			if (fSearchFor[i].getSelection())
				return i;
		}
		Assert.isTrue(false, "shouldNeverHappen");
		return -1;
	}

	private String getPattern() {
		return fPattern.getText();
	}
	
	private int getLimitTo() {
		for (int i= 0; i<fLimitTo.length; i++) {
			if (fLimitTo[i].getSelection())
				return i;
		}
		Assert.isTrue(false, "shouldNeverHappen");
		return -1;
	}

	private void setLimitToEnable(int searchFor) {
		switch (searchFor) {
			case SEARCH_PROCESS:
			    fLimitTo[LIMIT_NUMBER].setEnabled(true);
				fLimitTo[LIMIT_PID].setEnabled(true);
				fLimitTo[LIMIT_EXITCODE].setEnabled(true);
				break;
			case SEARCH_NODE:
				for (int i= 0; i<fLimitTo.length; i++)
					fLimitTo[i].setSelection(false);
			    
			    fLimitTo[LIMIT_NUMBER].setSelection(true);
			    fLimitTo[LIMIT_NUMBER].setEnabled(true);
				fLimitTo[LIMIT_PID].setEnabled(false);
				fLimitTo[LIMIT_EXITCODE].setEnabled(false);
				break;
		}
	}	
	
	public void setContainer(ISearchPageContainer container) {
		fContainer = container;
	}

	private ISearchPageContainer getContainer() {
		return fContainer;
	}
	
	/**
	 * Returns the structured selection from the selection.
	 */
	private IStructuredSelection asStructuredSelection() {
		IWorkbenchWindow wbWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (wbWindow != null) {
			IWorkbenchPage page = wbWindow.getActivePage();
			if (page != null) {
				IWorkbenchPart part = page.getActivePart();
				if (part != null)
					return SelectionConverter.getStructuredSelection(part);
			}
		}
	    return StructuredSelection.EMPTY;
	}	
	
	private static class SearchPatternData {
		private int	searchFor;
		private int limitTo;
		private String pattern;
		private IPElement element;

		public SearchPatternData(int searchFor, int limitTo, String pattern, IPElement element) {
			setSearchFor(searchFor);
			setLimitTo(limitTo);
			setPattern(pattern);
			setElement(element);
		}

		public void setElement(IPElement element) {
			this.element= element;
		}

		public IPElement getElement() {
			return element;
		}

		public void setLimitTo(int limitTo) {
			this.limitTo= limitTo;
		}

		public int getLimitTo() {
			return limitTo;
		}

		public void setPattern(String pattern) {
			this.pattern= pattern;
		}

		public String getPattern() {
			return pattern;
		}

		public void setSearchFor(int searchFor) {
			this.searchFor= searchFor;
		}

		public int getSearchFor() {
			return searchFor;
		}
	}	
}
