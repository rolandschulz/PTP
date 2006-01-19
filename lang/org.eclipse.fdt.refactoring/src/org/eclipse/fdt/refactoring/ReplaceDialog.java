/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.refactoring;

/**
 * Dialog template from FindReplaceDialog
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceColors;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.IFindReplaceTargetExtension2;


/**
 * Find/Replace dialog. The dialog is opened on a particular
 * target but can be re-targeted. Internally used by the <code>FindReplaceAction</code>
 */
class ReplaceDialog extends Dialog {
	
	public static String FindReplace_title = "Replace Constant";
	public static String FindReplace_Find_label = "Replace Target:";
	public static String FindReplace_Replace_label = "Replace With:";
	public static String FindReplace_FindNextButton_label = "Next";
	public static String FindReplace_ReplaceFindButton_label = "Replace/Next";
	public static String FindReplace_ReplaceSelectionButton_label = "Replace";
	public static String FindReplace_ReplaceAllButton_label = "Replace All";
	public static String FindReplace_CloseButton_label = "Close";
	public static String FindReplace_Status_noMatch_label = "Status noMatch";
	public static String FindReplace_Status_replacement_label = "Statue Replacement";
	public static String FindReplace_Status_replacements_label = "Status Replacements";
	public static String FindNext_Status_noMatch_label = "Status nomatch";

	

	/**
	 * Updates the find replace dialog on activation changes.
	 */
	class ActivationListener extends ShellAdapter {
		/*
		 * @see ShellListener#shellActivated(ShellEvent)
		 */
		public void shellActivated(ShellEvent e) {
			fActiveShell= (Shell)e.widget;
			updateButtonState();

			if (fGiveFocusToFindField && getShell() == fActiveShell && okToUse(fFindField))
				fFindField.setFocus();

		}

		/*
		 * @see ShellListener#shellDeactivated(ShellEvent)
		 */
		public void shellDeactivated(ShellEvent e) {
			fGiveFocusToFindField= false;

			storeSettings();

			if (fTarget != null && (fTarget instanceof IFindReplaceTargetExtension))
				((IFindReplaceTargetExtension) fTarget).setScope(null);

			fOldScope= null;

			fActiveShell= null;
			updateButtonState();
		}
	}

	/**
	 * Modify listener to update the search result in case of incremental search.
	 * @since 2.0
	 */
	private class FindModifyListener implements ModifyListener {

		/*
		 * @see ModifyListener#modifyText(ModifyEvent)
		 */
		public void modifyText(ModifyEvent e) {
				if (fFindField.getText().equals("") && fTarget != null) { //$NON-NLS-1$
					// empty selection at base location
					int offset= fIncrementalBaseLocation.x;

				} else {
					performSearch(false);
				}

//			updateButtonState(!isIncrementalSearch());
		}
	}

	/** The size of the dialogs search history. */
	private static final int HISTORY_SIZE= 5;

	private Point fLocation;
	private Point fIncrementalBaseLocation;

	private List fFindHistory;
	private List fReplaceHistory;
	private IRegion fOldScope;

	private boolean fIsTargetEditable;
	private IFindReplaceTarget fTarget;
	private Shell fParentShell;
	private Shell fActiveShell;

	private final ActivationListener fActivationListener= new ActivationListener();
	private final ModifyListener fFindModifyListener= new FindModifyListener();

	private Label fReplaceLabel, fStatusLabel;

	private Button fReplaceSelectionButton, fReplaceFindButton, fFindNextButton, fReplaceAllButton;
	private Combo fFindField, fReplaceField;
	private Rectangle fDialogPositionInit;

	/**
	 * The content assist handler for the find combo.
	 * @since 3.0
	 */
	private ContentAssistHandler fFindContentAssistHandler;
	/**
	 * The content assist handler for the replace combo.
	 * @since 3.0
	 */
	private ContentAssistHandler fReplaceContentAssistHandler;
	/**
	 * Content assist's proposal popup background color.
	 * @since 3.0
	 */
	private Color fProposalPopupBackgroundColor;
	/**
	 * Content assist's proposal popup foreground color.
	 * @since 3.0
	 */
	private Color fProposalPopupForegroundColor;
	/**
	 * <code>true</code> if the find field should receive focus the next time
	 * the dialog is activated, <code>false</code> otherwise.
	 * @since 3.0
	 */
	private boolean fGiveFocusToFindField= true;


	/**
	 * Creates a new dialog with the given shell as parent.
	 * @param parentShell the parent shell
	 */
	public ReplaceDialog(Shell parentShell) {
		super(parentShell);
		
		// initialize instance variables (CER)
		
		fParentShell= null;
		fTarget= null;

		fDialogPositionInit= null;
		fFindHistory= new ArrayList(HISTORY_SIZE - 1);
		fReplaceHistory= new ArrayList(HISTORY_SIZE - 1);

		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}

	/**
	 * Returns this dialog's parent shell.
	 * @return the dialog's parent shell
	 */
	public Shell getParentShell() {
		return super.getParentShell();
	}


	/**
	 * Returns <code>true</code> if control can be used.
	 *
	 * @param control the control to be checked
	 * @return <code>true</code> if control can be used
	 */
	private boolean okToUse(Control control) {
		return control != null && !control.isDisposed();
	}

	/*
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {

		super.create();

		Shell shell= getShell();
		shell.addShellListener(fActivationListener);
		if (fLocation != null)
			shell.setLocation(fLocation);

		// set help context
		//cer PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IAbstractTextEditorHelpContextIds.FIND_REPLACE_DIALOG);

		// fill in combo contents
		fFindField.removeModifyListener(fFindModifyListener);
		updateCombo(fFindField, fFindHistory);
		fFindField.addModifyListener(fFindModifyListener);
		updateCombo(fReplaceField, fReplaceHistory);

		// get find string
		initFindStringFromSelection();
		fReplaceField.setText("3.3D0");

		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);

		shell.setText(FindReplace_title);
		// shell.setImage(null);
	}

	/**
	 * Create the button section of the find/replace dialog.
	 *
	 * @param parent the parent composite
	 * @return the button section
	 */
	private Composite createButtonSection(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= -2;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);

		fFindNextButton= makeButton(panel, FindReplace_FindNextButton_label, 102, true, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performSearch();
				updateFindHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fFindNextButton, GridData.FILL, true, GridData.FILL, false);

		fReplaceFindButton= makeButton(panel, FindReplace_ReplaceFindButton_label, 103, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (performReplaceSelection())
					performSearch();
				updateFindAndReplaceHistory();
				fReplaceFindButton.setFocus();
			}
		});
		setGridData(fReplaceFindButton, GridData.FILL, true, GridData.FILL, false);

		fReplaceSelectionButton= makeButton(panel, FindReplace_ReplaceSelectionButton_label, 104, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceSelection();
				updateFindAndReplaceHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fReplaceSelectionButton, GridData.FILL, true, GridData.FILL, false);

		fReplaceAllButton= makeButton(panel, FindReplace_ReplaceAllButton_label, 105, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceAll();
				updateFindAndReplaceHistory();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fReplaceAllButton, GridData.FILL, true, GridData.FILL, false);

		// Make the all the buttons the same size as the Remove Selection button.
		fReplaceAllButton.setEnabled(isEditable());

		return panel;
	}


	/*
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.makeColumnsEqualWidth= true;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite inputPanel= createInputPanel(panel);
		setGridData(inputPanel, GridData.FILL, true, GridData.CENTER, false);

		Composite buttonPanelB= createButtonSection(panel);
		setGridData(buttonPanelB, GridData.FILL, true, GridData.CENTER, false);

		Composite statusBar= createStatusAndCloseButton(panel);
		setGridData(statusBar, GridData.FILL, true, GridData.CENTER, false);

		updateButtonState();

		applyDialogFont(panel);

		// Setup content assistants for find and replace combo boxes
		fProposalPopupBackgroundColor= new Color(getShell().getDisplay(), new RGB(254, 241, 233));
		fProposalPopupForegroundColor= new Color(getShell().getDisplay(), new RGB(0, 0, 0));

		return panel;
	}

	private void setContentAssistsEnablement(boolean enable) {
		if (enable) {
		} else {
			if (fFindContentAssistHandler == null)
				return;
			fFindContentAssistHandler.setEnabled(false);
			fReplaceContentAssistHandler.setEnabled(false);
		}
	}


	/**
	 * Tells the dialog to perform searches only in the scope given by the actually selected lines.
	 * @param selectedLines <code>true</code> if selected lines should be used
	 * @since 2.0
	 */
	private void useSelectedLines(boolean selectedLines) {
		if (fTarget == null || !(fTarget instanceof IFindReplaceTargetExtension))
			return;

		IFindReplaceTargetExtension extensionTarget= (IFindReplaceTargetExtension) fTarget;

		if (selectedLines) {

			IRegion scope;
			if (fOldScope == null) {
				Point lineSelection= extensionTarget.getLineSelection();
				scope= new Region(lineSelection.x, lineSelection.y);
			} else {
				scope= fOldScope;
				fOldScope= null;
			}

			int offset= scope.getOffset();
			
			extensionTarget.setSelection(offset, 0);
			extensionTarget.setScope(scope);
		} else {
			fOldScope= extensionTarget.getScope();
			extensionTarget.setScope(null);
		}
	}

	/**
	 * Creates the panel where the user specifies the text to search
	 * for and the optional replacement text.
	 *
	 * @param parent the parent composite
	 * @return the input panel
	 */
	private Composite createInputPanel(Composite parent) {

		ModifyListener listener= new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonState();
			}
		};

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		panel.setLayout(layout);

		Label findLabel= new Label(panel, SWT.LEFT);
		findLabel.setText(FindReplace_Find_label);
		setGridData(findLabel, GridData.BEGINNING, false, GridData.CENTER, false);

		fFindField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		setGridData(fFindField, GridData.FILL, true, GridData.CENTER, false);
		fFindField.addModifyListener(fFindModifyListener);

		fReplaceLabel= new Label(panel, SWT.LEFT);
		fReplaceLabel.setText(FindReplace_Replace_label);
		setGridData(fReplaceLabel, GridData.BEGINNING, false, GridData.CENTER, false);

		fReplaceField= new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
		setGridData(fReplaceField, GridData.FILL, true, GridData.CENTER, false);
		fReplaceField.addModifyListener(listener);

		return panel;
	}

	/**
	 * Creates the status and close section of the dialog.
	 *
	 * @param parent the parent composite
	 * @return the status and close button
	 */
	private Composite createStatusAndCloseButton(Composite parent) {

		Composite panel= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		panel.setLayout(layout);

		fStatusLabel= new Label(panel, SWT.LEFT);
		setGridData(fStatusLabel, GridData.FILL, true, GridData.CENTER, false);

		String label= FindReplace_CloseButton_label;
		Button closeButton= createButton(panel, 101, label, false);
		setGridData(closeButton, GridData.END, false, GridData.END, false);

		return panel;
	}

	/*
	 * @see Dialog#buttonPressed
	 */
	protected void buttonPressed(int buttonID) {
		if (buttonID == 101)
			close();
	}



	// ------- action invocation ---------------------------------------

	/**
	 * Returns the position of the specified search string, or <code>-1</code> if the string can
	 * not be found when searching using the given options.
	 *
	 * @param findString the string to search for
	 * @param startPosition the position at which to start the search
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive	should the search be case sensitive
	 * @param wrapSearch	should the search wrap to the start/end if arrived at the end/start
	 * @param wholeWord does the search string represent a complete word
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return the occurrence of the find string following the options or <code>-1</code> if nothing found
	 * @since 3.0
	 */
	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regExSearch) {

		if (forwardSearch) {
			return findAndSelect(startPosition, findString, true, caseSensitive, wholeWord, regExSearch);
		}
		return findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord, regExSearch);
	}

	/**
	 * Searches for a string starting at the given offset and using the specified search
	 * directives. If a string has been found it is selected and its start offset is
	 * returned.
	 *
	 * @param offset the offset at which searching starts
	 * @param findString the string which should be found
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
	 * @param wholeWord if <code>true</code> only occurrences are reported in which the findString stands as a word by itself
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return the position of the specified string, or -1 if the string has not been found
	 * @since 3.0
	 */
	private int findAndSelect(int offset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		if (fTarget instanceof IFindReplaceTargetExtension3)
			return ((IFindReplaceTargetExtension3)fTarget).findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
		return fTarget.findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord);
	}

	/**
	 * Replaces the selection with <code>replaceString</code>. If
	 * <code>regExReplace</code> is <code>true</code>,
	 * <code>replaceString</code> is a regex replace pattern which will get
	 * expanded if the underlying target supports it. Returns the region of the
	 * inserted text; note that the returned selection covers the expanded
	 * pattern in case of regex replace.
	 *
	 * @param replaceString the replace string (or a regex pattern)
	 * @param regExReplace <code>true</code> if <code>replaceString</code>
	 *        is a pattern
	 * @return the selection after replacing, i.e. the inserted text
	 * @since 3.0
	 */
	Point replaceSelection(String replaceString, boolean regExReplace) {
		if (fTarget instanceof IFindReplaceTargetExtension3)
			((IFindReplaceTargetExtension3)fTarget).replaceSelection(replaceString, regExReplace);
		else
			fTarget.replaceSelection(replaceString);

		return fTarget.getSelection();
	}

	/**
	 * Returns whether the specified search string can be found using the given options.
	 *
	 * @param findString the string to search for
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive	should the search be case sensitive
	 * @param wrapSearch	should the search wrap to the start/end if arrived at the end/start
	 * @param wholeWord does the search string represent a complete word
	 * @param incremental is this an incremental search
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return <code>true</code> if the search string can be found using the given options
	 *
	 * @since 3.0
	 */
	private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean incremental, boolean regExSearch) {

		if (fTarget == null)
			return false;

		Point r= null;
		if (incremental)
			r= fIncrementalBaseLocation;
		else
			r= fTarget.getSelection();

		int findReplacePosition= r.x;
		if (forwardSearch)
			findReplacePosition += r.y;

		int index= findIndex(findString, findReplacePosition, forwardSearch, caseSensitive, wrapSearch, wholeWord, regExSearch);

		if (index != -1)
			return true;

		return false;
	}

	/**
	 * Returns the dialog's boundaries.
	 * @return the dialog's boundaries
	 */
	private Rectangle getDialogBoundaries() {
		if (okToUse(getShell()))
			return getShell().getBounds();
		return fDialogPositionInit;
	}


	// ------- accessors ---------------------------------------

	/**
	 * Retrieves the string to search for from the appropriate text input field and returns it.
	 * @return the search string
	 */
	private String getFindString() {
		if (okToUse(fFindField)) {
			return fFindField.getText();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Retrieves the replacement string from the appropriate text input field and returns it.
	 * @return the replacement string
	 */
	private String getReplaceString() {
		if (okToUse(fReplaceField)) {
			return fReplaceField.getText();
		}
		return ""; //$NON-NLS-1$
	}

	// ------- init / close ---------------------------------------

	/**
	 * Returns the actual selection of the find replace target.
	 * @return the selection of the target
	 */
	private String getSelectionString() {
		String selection= fTarget.getSelectionText();
		if (selection != null && selection.length() > 0) {
			int[] info= TextUtilities.indexOf(TextUtilities.DELIMITERS, selection, 0);
			if (info[0] > 0)
				return selection.substring(0, info[0]);
			else if (info[0] == -1)
				return selection;
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		handleDialogClose();
		return super.close();
	}

	/**
	 * Removes focus changed listener from browser and stores settings for re-open.
	 */
	private void handleDialogClose() {

		// remove listeners
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);
		}

		if (fParentShell != null) {
			fParentShell.removeShellListener(fActivationListener);
			fParentShell= null;
		}

		getShell().removeShellListener(fActivationListener);

		// store current settings in case of re-open
		storeSettings();

		if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).endSession();

		setContentAssistsEnablement(false);
		fFindContentAssistHandler= null;
		fReplaceContentAssistHandler= null;

		fProposalPopupBackgroundColor.dispose();
		fProposalPopupForegroundColor.dispose();

		// prevent leaks
		fActiveShell= null;
		fTarget= null;

	}

	/**
	 * Writes the current selection to the dialog settings.
	 * @since 3.0
	 */
	private void writeSelection() {
		if (fTarget == null)
			return;
		String selection= fTarget.getSelectionText();
		if (selection == null)
			selection= ""; //$NON-NLS-1$

		//cer IDialogSettings s= getDialogSettings();
		//cer s.put("selection", selection); //$NON-NLS-1$
	}

	/**
	 * Stores the current state in the dialog settings.
	 * @since 2.0
	 */
	private void storeSettings() {
		// store (set from field settings) instance variables (CER)
		fDialogPositionInit= getDialogBoundaries();
	}

	/**
	 * Initializes the string to search for and the appropriate
	 * text in the Find field based on the selection found in the
	 * action's target.
	 */
	private void initFindStringFromSelection() {
		fFindField.setText("Hello world!");
		if (fTarget != null && okToUse(fFindField)) {
			String selection= getSelectionString();
			fFindField.removeModifyListener(fFindModifyListener);
			if (selection != null) {
				fFindField.setText(selection);
				if (!selection.equals(fTarget.getSelectionText())) {
					useSelectedLines(true);
//					fGlobalRadioButton.setSelection(false);
//					fSelectedRangeRadioButton.setSelection(true);
//					fUseSelectedLines= true;
				}
			} else {
				if ("".equals(fFindField.getText())) { //$NON-NLS-1$
					if (fFindHistory.size() > 0)
						fFindField.setText((String) fFindHistory.get(0));
					else
						fFindField.setText(""); //$NON-NLS-1$
				}
			}
			fFindField.setSelection(new Point(0, fFindField.getText().length()));
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Initializes the anchor used as starting point for incremental searching.
	 * @since 2.0
	 */
	private void initIncrementalBaseLocation() {
		fIncrementalBaseLocation= new Point(0, 0);
	}


	/**
	 * Creates a button.
	 * @param parent the parent control
	 * @param label the button label
	 * @param id the button id
	 * @param dfltButton is this button the default button
	 * @param listener a button pressed listener
	 * @return the new button
	 */
	private Button makeButton(Composite parent, String label, int id, boolean dfltButton, SelectionListener listener) {
		Button b= createButton(parent, id, label, dfltButton);
		b.addSelectionListener(listener);
		return b;
	}

	/**
	 * Returns the status line manager of the active editor or <code>null</code> if there is no such editor.
	 * @return the status line manager of the active editor
	 */
	private IEditorStatusLine getStatusLineManager() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page= window.getActivePage();
		if (page == null)
			return null;

		IEditorPart editor= page.getActiveEditor();
		if (editor == null)
			return null;

		return (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
	}

	/**
	 * Sets the given status message in the status line.
	 *
	 * @param error <code>true</code> if it is an error
	 * @param message the error message
	 */
	private void statusMessage(boolean error, String message) {
		fStatusLabel.setText(message);

		if (error)
			fStatusLabel.setForeground(JFaceColors.getErrorText(fStatusLabel.getDisplay()));
		else
			fStatusLabel.setForeground(null);

		IEditorStatusLine statusLine= getStatusLineManager();
		if (statusLine != null)
			statusLine.setMessage(error, message, null);

		if (error)
			getShell().getDisplay().beep();
	}

	/**
	 * Sets the given error message in the status line.
	 * @param message the message
	 */
	private void statusError(String message) {
		statusMessage(true, message);
	}

	/**
	 * Sets the given message in the status line.
	 * @param message the message
	 */
	private void statusMessage(String message) {
		statusMessage(false, message);
	}

	/**
	 * Replaces all occurrences of the user's findString with
	 * the replace string.  Indicate to the user the number of replacements
	 * that occur.
	 */
	private void performReplaceAll() {

		int replaceCount= 0;
		final String replaceString= getReplaceString();
		final String findString= getFindString();

		if (findString != null && findString.length() > 0) {

			class ReplaceAllRunnable implements Runnable {
				public int numberOfOccurrences;
				public void run() {
					numberOfOccurrences = 0;
					// numberOfOccurrences= replaceAll(findString, replaceString == null ? "" : replaceString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch(), isRegExSearchAvailableAndChecked());	//$NON-NLS-1$
				}
			}

			try {
				ReplaceAllRunnable runnable= new ReplaceAllRunnable();
				BusyIndicator.showWhile(fActiveShell.getDisplay(), runnable);
				replaceCount= runnable.numberOfOccurrences;

				if (replaceCount != 0) {
					if (replaceCount == 1) { // not plural
						statusMessage(FindReplace_Status_replacement_label);
					} else {
						String msg= FindReplace_Status_replacements_label;
						msg= MessageFormat.format(msg, new Object[] {String.valueOf(replaceCount)});
						statusMessage(msg);
					}
				} else {
					statusMessage(FindReplace_Status_noMatch_label);
				}
			} catch (PatternSyntaxException ex) {
				statusError(ex.getLocalizedMessage());
			} catch (IllegalStateException ex) {
				// we don't keep state in this dialog
			}
		}
		writeSelection();
		updateButtonState();
	}

	/**
	 * Validates the state of the find/replace target.
	 * @return <code>true</code> if target can be changed, <code>false</code> otherwise
	 * @since 2.1
	 */
	private boolean validateTargetState() {

		if (fTarget instanceof IFindReplaceTargetExtension2) {
			IFindReplaceTargetExtension2 extension= (IFindReplaceTargetExtension2) fTarget;
			if (!extension.validateTargetState()) {
				statusError("Invalid Target State");
				updateButtonState();
				return false;
			}
		}
		return isEditable();
	}

	/**
	 * Replaces the current selection of the target with the user's
	 * replace string.
	 *
	 * @return <code>true</code> if the operation was successful
	 */
	private boolean performReplaceSelection() {

		if (!validateTargetState())
			return false;

		String replaceString= getReplaceString();
		if (replaceString == null)
			replaceString= ""; //$NON-NLS-1$

		boolean replaced;
		try {
			replaceSelection(replaceString, false);
			replaced= true;
			writeSelection();
		} catch (PatternSyntaxException ex) {
			statusError(ex.getLocalizedMessage());
			replaced= false;
		} catch (IllegalStateException ex) {
			replaced= false;
		}

		updateButtonState();
		return replaced;
	}

	/**
	 * Locates the user's findString in the text of the target.
	 */
	private void performSearch() {
		//performSearch(isIncrementalSearch() && !isRegExSearchAvailableAndChecked());
		performSearch(true);
	}

	/**
	 * Locates the user's findString in the text of the target.
	 *
	 * @param mustInitIncrementalBaseLocation <code>true</code> if base location must be initialized
	 * @since 3.0
	 */
	private void performSearch(boolean mustInitIncrementalBaseLocation) {

		if (mustInitIncrementalBaseLocation)
			initIncrementalBaseLocation();

		String findString= getFindString();

		if (findString != null && findString.length() > 0) {

			try {
				boolean somethingFound = true;
				//boolean somethingFound= findNext(findString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(), isWholeWordSearch(), isIncrementalSearch() && !isRegExSearchAvailableAndChecked(), isRegExSearchAvailableAndChecked());
				if (somethingFound) {
					statusMessage(""); //$NON-NLS-1$
				} else {
					statusMessage(FindReplace_Status_noMatch_label);
				}
			} catch (PatternSyntaxException ex) {
				statusError(ex.getLocalizedMessage());
			} catch (IllegalStateException ex) {
				// we don't keep state in this dialog
			}
		}
		writeSelection();
		updateButtonState();
	}

	/**
	 * Replaces all occurrences of the user's findString with
	 * the replace string.  Returns the number of replacements
	 * that occur.
	 *
	 * @param findString the string to search for
	 * @param replaceString the replacement string
	 * @param forwardSearch	the search direction
	 * @param caseSensitive should the search be case sensitive
	 * @param wrapSearch	should search wrap to start/end if end/start is reached
	 * @param wholeWord does the search string represent a complete word
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return the number of occurrences
	 *
	 * @since 3.0
	 */
	private int replaceAll(String findString, String replaceString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regExSearch) {

		int replaceCount= 0;
		int findReplacePosition= 0;

		if (wrapSearch) { // search the whole text
			findReplacePosition= 0;
			forwardSearch= true;
		} else if (fTarget.getSelectionText() != null) {
			// the cursor is set to the end or beginning of the selected text
			Point selection= fTarget.getSelection();
			findReplacePosition= selection.x;
		}

		if (!validateTargetState())
			return replaceCount;

		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).setReplaceAllMode(true);

		try {
			int index= 0;
			while (index != -1) {
				index= findAndSelect(findReplacePosition, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
				if (index != -1) { // substring not contained from current position
					Point selection= replaceSelection(replaceString, regExSearch);
					replaceCount++;

					if (forwardSearch)
						findReplacePosition= selection.x + selection.y;
					else {
						findReplacePosition= selection.x - 1;
						if (findReplacePosition == -1)
							break;
					}
				}
			}
		} finally {
			if (fTarget instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) fTarget).setReplaceAllMode(false);
		}

		return replaceCount;
	}

	// ------- UI creation ---------------------------------------

	/**
	 * Attaches the given layout specification to the <code>component</code>.
	 *
	 * @param component the component
	 * @param horizontalAlignment horizontal alignment
	 * @param grabExcessHorizontalSpace grab excess horizontal space
	 * @param verticalAlignment vertical alignment
	 * @param grabExcessVerticalSpace grab excess vertical space
	 */
	private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace, int verticalAlignment, boolean grabExcessVerticalSpace) {
		GridData gd= new GridData();
		gd.horizontalAlignment= horizontalAlignment;
		gd.grabExcessHorizontalSpace= grabExcessHorizontalSpace;
		gd.verticalAlignment= verticalAlignment;
		gd.grabExcessVerticalSpace= grabExcessVerticalSpace;
		component.setLayoutData(gd);
	}

	/**
	 * Updates the enabled state of the buttons.
	 */
	private void updateButtonState() {
		updateButtonState(false);
	}

	/**
	 * Updates the enabled state of the buttons.
	 *
	 * @param disableReplace <code>true</code> if replace button must be disabled
	 * @since 3.0
	 */
	private void updateButtonState(boolean disableReplace) {
		if (okToUse(getShell()) && okToUse(fFindNextButton)) {

			boolean selection= false;
			if (fTarget != null) {
				String selectedText= fTarget.getSelectionText();
				selection= (selectedText != null && selectedText.length() > 0);
			}
			boolean enable= fTarget != null && (fActiveShell == fParentShell || fActiveShell == getShell());
			String str= getFindString();
			boolean findString= str != null && str.length() > 0;

//			fWholeWordCheckBox.setEnabled(isWord(str) && !isRegExSearchAvailableAndChecked());

			fFindNextButton.setEnabled(enable && findString);
			fReplaceSelectionButton.setEnabled(!disableReplace && enable && isEditable() && selection);
			fReplaceFindButton.setEnabled(!disableReplace && enable && isEditable() && findString && selection);
			fReplaceAllButton.setEnabled(enable && isEditable() && findString);
		}
	}

	/**
	 * Tests whether each character in the given
	 * string is a letter.
	 *
	 * @param str
	 * @return <code>true</code> if the given string is a word
	 * @since 3.0
	 */
	private boolean isWord(String str) {
		if (str == null || str.length() == 0)
			return false;

		for (int i= 0; i < str.length(); i++) {
			if (!Character.isJavaIdentifierPart(str.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Updates the given combo with the given content.
	 * @param combo combo to be updated
	 * @param content to be put into the combo
	 */
	private void updateCombo(Combo combo, List content) {
		combo.removeAll();
		for (int i= 0; i < content.size(); i++) {
			combo.add(content.get(i).toString());
		}
	}

	// ------- open / reopen ---------------------------------------

	/**
	 * Called after executed find/replace action to update the history.
	 */
	private void updateFindAndReplaceHistory() {
		updateFindHistory();
		if (okToUse(fReplaceField)) {
			updateHistory(fReplaceField, fReplaceHistory);
		}

	}

	/**
	 * Called after executed find action to update the history.
	 */
	private void updateFindHistory() {
		if (okToUse(fFindField)) {
			fFindField.removeModifyListener(fFindModifyListener);
			updateHistory(fFindField, fFindHistory);
			fFindField.addModifyListener(fFindModifyListener);
		}
	}

	/**
	 * Updates the combo with the history.
	 * @param combo to be updated
	 * @param history to be put into the combo
	 */
	private void updateHistory(Combo combo, List history) {
		String findString= combo.getText();
		int index= history.indexOf(findString);
		if (index != 0) {
			if (index != -1) {
				history.remove(index);
			}
			history.add(0, findString);
			updateCombo(combo, history);
			combo.setText(findString);
		}
	}

	/**
	 * Returns whether the target is editable.
	 * @return <code>true</code> if target is editable
	 */
	private boolean isEditable() {
		boolean isEditable= (fTarget == null ? false : fTarget.isEditable());
		return fIsTargetEditable && isEditable;
	}

	/**
	 * Updates this dialog because of a different target.
	 * @param target the new target
	 * @param isTargetEditable <code>true</code> if the new target can be modified
	 * @param initializeFindString <code>true</code> if the find string of this dialog should be initialized based on the viewer's selection
	 * @since 2.0
	 */
	public void updateTarget(IFindReplaceTarget target, boolean isTargetEditable, boolean initializeFindString) {

		fIsTargetEditable= isTargetEditable;

		if (target != fTarget) {
			if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
				((IFindReplaceTargetExtension) fTarget).endSession();

			fTarget= target;
//			if (fTarget != null)
//				fIsTargetSupportingRegEx= fTarget instanceof IFindReplaceTargetExtension3;

			if (fTarget instanceof IFindReplaceTargetExtension) {
				((IFindReplaceTargetExtension) fTarget).beginSession();

//				fGlobalInit= true;
//				fGlobalRadioButton.setSelection(fGlobalInit);
//				fSelectedRangeRadioButton.setSelection(!fGlobalInit);
//				fUseSelectedLines= !fGlobalInit;
			}
		}

//		if (okToUse(fIsRegExCheckBox))
//			fIsRegExCheckBox.setEnabled(fIsTargetSupportingRegEx);

//		if (okToUse(fWholeWordCheckBox))
//			fWholeWordCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());

//		if (okToUse(fIncrementalCheckBox))
//			fIncrementalCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());

		if (okToUse(fReplaceLabel)) {
			fReplaceLabel.setEnabled(isEditable());
			fReplaceField.setEnabled(isEditable());
			if (initializeFindString) {
				initFindStringFromSelection();
				fGiveFocusToFindField= true;
			}
			initIncrementalBaseLocation();
			updateButtonState();
		}

		setContentAssistsEnablement(false);
	}

	/**
	 * Sets the parent shell of this dialog to be the given shell.
	 *
	 * @param shell the new parent shell
	 */
	public void setParentShell(Shell shell) {
		if (shell != fParentShell) {

			if (fParentShell != null)
				fParentShell.removeShellListener(fActivationListener);

			fParentShell= shell;
			fParentShell.addShellListener(fActivationListener);
		}

		fActiveShell= shell;
	}
}
