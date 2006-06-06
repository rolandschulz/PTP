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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceColors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

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
	public static String FindReplace_Status_replacement_label = "Replaced";
	public static String FindReplace_Status_replacements_label = "Replaced";
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
		}

		/*
		 * @see ShellListener#shellDeactivated(ShellEvent)
		 */
		public void shellDeactivated(ShellEvent e) {

			if (fTarget != null && (fTarget instanceof IFindReplaceTargetExtension))
				((IFindReplaceTargetExtension) fTarget).setScope(null);

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
			updateButtonState();
		}
	}

	/** The size of the dialogs search history. */
	private static final int HISTORY_SIZE= 5;

	private Point fLocation;

	private List fFindHistory;
	private List fReplaceHistory;

	private IFindReplaceTarget fTarget;
	private Shell fParentShell;
	private Shell fActiveShell;

	private final ActivationListener fActivationListener= new ActivationListener();
	private final ModifyListener fFindModifyListener= new FindModifyListener();

	private Label fReplaceLabel, fStatusLabel;

	private Button fReplaceSelectionButton, fReplaceFindButton, fFindNextButton, fReplaceAllButton;
	private Combo fFindField, fReplaceField;
	private Rectangle fDialogPositionInit;
	
	/** Used to keep track of column changes when text substitutions are done */
	private int fPrevLine;
	private int fExtraColumns;

	private IWorkbenchPart fActiveEditor;	/* editor associated with action delegate */
	private String[] fConstants;	/* list of constant work items */
	private int fTargetIndex;		/* current index into fConstants */
	private int fTargetOffset;	/* offset to current target */
	private int fTargetLength;	/* length of current target */

	/**
	 * Creates a new dialog with the given shell as parent.
	 * @param parentShell the parent shell
	 */
	public ReplaceDialog(IWorkbenchPart activeEditor, String[] constants) {
		super(activeEditor.getSite().getShell());
		
		// initialize instance variables (CER)
		
		fParentShell= null;
		fTarget= null;
		
		fActiveEditor = activeEditor;
		fConstants = constants;
		fTargetIndex = 0;
		fTargetOffset = -1;
		fTargetLength = -1;
		
		fPrevLine = -1;
		fExtraColumns = 0;

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

		// fill in combo contents
		fFindField.removeModifyListener(fFindModifyListener);
		updateCombo(fFindField, fFindHistory);
		fFindField.addModifyListener(fFindModifyListener);
		updateCombo(fReplaceField, fReplaceHistory);

		// get replacement target
		performSeekNextTarget();

		// set dialog position
		if (fDialogPositionInit != null)
			shell.setBounds(fDialogPositionInit);

		shell.setText(FindReplace_title);
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
				performSeekNextTarget();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fFindNextButton, GridData.FILL, true, GridData.FILL, false);

		fReplaceFindButton= makeButton(panel, FindReplace_ReplaceFindButton_label, 103, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (performReplaceSelection()) {
					performSeekNextTarget();
				}
				fReplaceFindButton.setFocus();
			}
		});
		setGridData(fReplaceFindButton, GridData.FILL, true, GridData.FILL, false);

		fReplaceSelectionButton= makeButton(panel, FindReplace_ReplaceSelectionButton_label, 104, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceSelection();
				fFindNextButton.setFocus();
			}
		});
		setGridData(fReplaceSelectionButton, GridData.FILL, true, GridData.FILL, false);

		fReplaceAllButton= makeButton(panel, FindReplace_ReplaceAllButton_label, 105, false, new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performReplaceAll();
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

		return panel;
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


	// ------- accessors ---------------------------------------

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

		if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).endSession();

		// prevent leaks
		fActiveShell= null;
		fTarget= null;

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
	 * Replaces all occurrences of the target strings with
	 * the replace string.  Indicate to the user the number of replacements
	 * that occur.
	 */
	private void performReplaceAll() {

		int replaceCount = 0;

		if (replaceCount >= 0) {

			class ReplaceAllRunnable implements Runnable {
				public int numberOfOccurrences;
				public void run() {
					numberOfOccurrences = 0;
					numberOfOccurrences= replaceAll();
				}
			}

			try {
				ReplaceAllRunnable runnable= new ReplaceAllRunnable();
				BusyIndicator.showWhile(fActiveShell.getDisplay(), runnable);
				replaceCount= runnable.numberOfOccurrences;

				if (replaceCount != 0) {
					String msg = FindReplace_Status_replacement_label + " " + String.valueOf(replaceCount);
					if (replaceCount == 1) { // not plural
						statusMessage(msg + " occurrence");
					} else {
						statusMessage(msg + " occurences");
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
		updateButtonState();
	}

	/**
	 * Validates the state of the find/replace target.
	 * @return <code>true</code> if target can be changed, <code>false</code> otherwise
	 * @since 2.1
	 */
	private boolean validateTargetState() {
		if (fTargetOffset < 0) {
			updateButtonState();
			return false;
		}
		return isEditable();
	}

	/**
	 * Replaces the current selection of the text target with the
	 * replacement string.
	 *
	 * @return <code>true</code> if the operation was successful
	 */
	private boolean performReplaceSelection() {
		if (fActiveEditor == null || !(fActiveEditor instanceof ITextEditor)) {
			return false;
		}
				
		if (!validateTargetState())
			return false;

		boolean replaced;
		String replacement = getReplaceString();
		try {
			ITextEditor editor = (ITextEditor) fActiveEditor;
			editor.getDocumentProvider().getDocument(editor.getEditorInput()).replace(fTargetOffset, fTargetLength, replacement);
			replaced = true;
		} catch (BadLocationException e) {
			replaced = false;
		}
		
		if (replaced) {
			fExtraColumns += replacement.length() - fTargetLength;
			fTargetOffset = -1;
			fTargetLength = -1;
			fFindField.setText("");
			fReplaceField.setText("");
		}
		
		updateButtonState();
		return replaced;
	}

	/**
	 * Locates the next replacement in the text of the target.
	 */
	private void performSeekNextTarget() {
		if (fActiveEditor == null || !(fActiveEditor instanceof ITextEditor)) {
			return;
		}
				
		if (fTargetIndex < fConstants.length) {
			ITextEditor editor = (ITextEditor) fActiveEditor;
			String target = fConstants[fTargetIndex];
			String[] changeElements = TextChanges.changeElements(target);
			String text = TextChanges.text(changeElements);
    		String replacement = TextChanges.replacement(text);
			int line = TextChanges.line(changeElements);

			if (fPrevLine < line) {
    			fPrevLine = line;
    			fExtraColumns = 0;
    		}

			int column = TextChanges.column(changeElements) + fExtraColumns;
			
			try {
				fTargetOffset = column + editor.getDocumentProvider().getDocument(editor.getEditorInput()).getLineOffset(line);
				fTargetLength = TextChanges.length(changeElements);
				editor.selectAndReveal(fTargetOffset, fTargetLength);
			} catch (BadLocationException e) {
				fTargetOffset = -1;
				fTargetLength = -1;
				return;
			}
		
			fFindField.setText(text);
			fReplaceField.setText(replacement);
			fTargetIndex += 1;			
		}
		updateButtonState();	// no more work to do
	}

	/**
	 * Replaces all occurrences of the target strings with
	 * the replace string.  Returns the number of replacements
	 * that occur.
	 * @return the number of occurrences
	 *
	 */
	private int replaceAll() {

		int replaceCount= 0;
		
		if (!validateTargetState())
			return replaceCount;

		while (performReplaceSelection()) {
			replaceCount += 1;
			performSeekNextTarget();
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
			fFindNextButton.setEnabled(fTargetIndex < fConstants.length);
			fReplaceSelectionButton.setEnabled(fTargetOffset >= 0);
			fReplaceFindButton.setEnabled(fTargetOffset >= 0);
			fReplaceAllButton.setEnabled(fTargetOffset >= 0);			
		}
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
	 * Returns whether the target is editable.
	 * @return <code>true</code> if target is editable
	 */
	private boolean isEditable() {
		return (fActiveEditor != null);
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
