package org.eclipse.cldt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.cldt.internal.ui.ICHelpContextIds;
import org.eclipse.cldt.internal.ui.editor.FortranEditor;
import org.eclipse.cldt.internal.ui.text.FortranSourceViewerConfiguration;
import org.eclipse.cldt.internal.ui.text.FortranTextTools;
import org.eclipse.cldt.internal.ui.text.IFortranColorConstants;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.cldt.ui.PreferenceConstants;
import org.eclipse.cldt.utils.ui.controls.TabFolderLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

/*
 * The page for setting the editor options.
 */
public class FortranEditorPreferencePage extends AbstractPreferencePage implements IWorkbenchPreferencePage {

	protected final String[][] fListModel = new String[][] { { PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.MultiLine"), IFortranColorConstants.FORTRAN_MULTI_LINE_COMMENT }, { //$NON-NLS-1$
		PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.singleLine"), IFortranColorConstants.FORTRAN_SINGLE_LINE_COMMENT }, { //$NON-NLS-1$
		PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.keywords"), IFortranColorConstants.FORTRAN_KEYWORD }, { //$NON-NLS-1$
		PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.builtInTypes"), IFortranColorConstants.FORTRAN_TYPE }, { //$NON-NLS-1$
		PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.strings"), IFortranColorConstants.FORTRAN_STRING }, { //$NON-NLS-1$
        PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.operators"), IFortranColorConstants.FORTRAN_OPERATOR }, { //$NON-NLS-1$
        PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.braces"), IFortranColorConstants.FORTRAN_BRACES }, { //$NON-NLS-1$            
        PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.numbers"), IFortranColorConstants.FORTRAN_NUMBER }, { //$NON-NLS-1$            
		PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags.others"), IFortranColorConstants.FORTRAN_DEFAULT }, { //$NON-NLS-1$
        PreferencesMessages.getString("FortranEditorPreferencePage.cCommentTaskTags"), PreferenceConstants.EDITOR_TASK_TAG_COLOR } //$NON-NLS-1$
	};

	protected final String[][] fAppearanceColorListModel = new String[][] { 
			{PreferencesMessages.getString("FortranEditorPreferencePage.behaviorPage.matchingBracketColor"), FortranEditor.MATCHING_BRACKETS_COLOR, null }, //$NON-NLS-1$
			{PreferencesMessages.getString("FortranEditorPreferencePage.behaviorPage.linkedPositionColor"), FortranEditor.LINKED_POSITION_COLOR, null }, //$NON-NLS-1$
	};

	private FortranTextTools fCTextTools;

	/**
	 * List of master/slave listeners when there's a dependency.
	 * 
	 * @see #createDependency(Button, String, Control)
	 * @since 3.0
	 */
	private ArrayList fMasterSlaveListeners= new ArrayList();

	protected List fList;
	protected ColorEditor fForegroundColorEditor;
	protected Button fBoldCheckBox;
	protected SourceViewer fPreviewViewer;

	private CEditorHoverConfigurationBlock fCEditorHoverConfigurationBlock;
	private FoldingConfigurationBlock fFoldingConfigurationBlock;


	public FortranEditorPreferencePage() {
		super();
		setDescription(FortranUIPlugin.getResourceString("FortranEditorPreferencePage.description")); //$NON-NLS-1$
	}

	protected OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList overlayKeys = new ArrayList();
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, FortranSourceViewerConfiguration.PREFERENCE_TAB_WIDTH));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_MULTI_LINE_COMMENT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_MULTI_LINE_COMMENT + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_SINGLE_LINE_COMMENT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_SINGLE_LINE_COMMENT + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_KEYWORD));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_KEYWORD + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_TYPE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_TYPE + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_STRING));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_STRING + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_DEFAULT));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_DEFAULT + "_bold")); //$NON-NLS-1$        
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_BRACES));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_BRACES + "_bold")); //$NON-NLS-1$
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_NUMBER));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_NUMBER + "_bold")); //$NON-NLS-1$
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, IFortranColorConstants.FORTRAN_OPERATOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, IFortranColorConstants.FORTRAN_OPERATOR + "_bold")); //$NON-NLS-1$
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, FortranEditor.MATCHING_BRACKETS_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, FortranEditor.MATCHING_BRACKETS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, FortranEditor.SPACES_FOR_TABS));
		
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, FortranEditor.LINKED_POSITION_COLOR));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, PreferenceConstants.EDITOR_TASK_TAG_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_TASK_TAG_BOLD));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, PreferenceConstants.EDITOR_TASK_INDICATION_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_TASK_INDICATION));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER));
      
        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	public static void initDefaults(IPreferenceStore store) {

		store.setDefault(FortranEditor.MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store, FortranEditor.MATCHING_BRACKETS_COLOR, new RGB(170,170,170));

		store.setDefault(FortranSourceViewerConfiguration.PREFERENCE_TAB_WIDTH, 4);

		store.setDefault(FortranEditor.SPACES_FOR_TABS, false);

		PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_MULTI_LINE_COMMENT, new RGB(63, 127, 95));
		store.setDefault(IFortranColorConstants.FORTRAN_MULTI_LINE_COMMENT + "_bold", false); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_SINGLE_LINE_COMMENT, new RGB(63, 125, 95));
		store.setDefault(IFortranColorConstants.FORTRAN_SINGLE_LINE_COMMENT + "_bold", false); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_KEYWORD, new RGB(127, 0, 85));
		store.setDefault(IFortranColorConstants.FORTRAN_KEYWORD + "_bold", true); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_TYPE, new RGB(127, 0, 85));
		store.setDefault(IFortranColorConstants.FORTRAN_TYPE + "_bold", true); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_STRING, new RGB(42, 0, 255));
		store.setDefault(IFortranColorConstants.FORTRAN_STRING + "_bold", false); //$NON-NLS-1$

		PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_DEFAULT, new RGB(0, 0, 0));
		store.setDefault(IFortranColorConstants.FORTRAN_DEFAULT + "_bold", false); //$NON-NLS-1$

        PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_OPERATOR, new RGB(0, 0, 0));
        store.setDefault(IFortranColorConstants.FORTRAN_DEFAULT + "_bold", false); //$NON-NLS-1$

        PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_BRACES, new RGB(0, 0, 0));
        store.setDefault(IFortranColorConstants.FORTRAN_DEFAULT + "_bold", false); //$NON-NLS-1$

        PreferenceConverter.setDefault(store, IFortranColorConstants.FORTRAN_NUMBER, new RGB(0, 0, 0));
        store.setDefault(IFortranColorConstants.FORTRAN_DEFAULT + "_bold", false); //$NON-NLS-1$

        PreferenceConverter.setDefault(store, FortranEditor.LINKED_POSITION_COLOR, new RGB(0, 200, 100));

	}

	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ICHelpContextIds.C_EDITOR_PREF_PAGE);
	}

	protected void handleListSelection() {
		int i = fList.getSelectionIndex();
		String key = fListModel[i][1];
		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
		fForegroundColorEditor.setColorValue(rgb);
		fBoldCheckBox.setSelection(fOverlayStore.getBoolean(key + "_bold")); //$NON-NLS-1$
	}	

	private Control createSyntaxPage(Composite parent) {

		Composite colorComposite = new Composite(parent, SWT.NULL);
		colorComposite.setLayout(new GridLayout());

		Label label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.getString("FortranEditorPreferencePage.colorPage.foreground")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite editorComposite = new Composite(colorComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		editorComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		editorComposite.setLayoutData(gd);

		fList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(5);
		fList.setLayoutData(gd);

		Composite stylesComposite = new Composite(editorComposite, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		label = new Label(stylesComposite, SWT.LEFT);
		label.setText(PreferencesMessages.getString("FortranEditorPreferencePage.colorPage.color")); //$NON-NLS-1$
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		fForegroundColorEditor = new ColorEditor(stylesComposite);
		Button foregroundColorButton = fForegroundColorEditor.getButton();
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = GridData.BEGINNING;
		foregroundColorButton.setLayoutData(gd);

		fBoldCheckBox = new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText(PreferencesMessages.getString("FortranEditorPreferencePage.colorPage.bold")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan=2;
		gd.horizontalAlignment = GridData.BEGINNING;
		fBoldCheckBox.setLayoutData(gd);

		label = new Label(colorComposite, SWT.LEFT);
		label.setText(PreferencesMessages.getString("FortranEditorPreferencePage.colorPage.preview")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control previewer = createPreviewer(colorComposite);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = convertWidthInCharsToPixels(80);
		gd.heightHint = convertHeightInCharsToPixels(15);
		previewer.setLayoutData(gd);

		fList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				handleListSelection();
			}
		});

		foregroundColorButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i = fList.getSelectionIndex();
				String key = fListModel[i][1];

				PreferenceConverter.setValue(fOverlayStore, key, fForegroundColorEditor.getColorValue());
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				int i = fList.getSelectionIndex();
				String key = fListModel[i][1];
				fOverlayStore.setValue(key + "_bold", fBoldCheckBox.getSelection()); //$NON-NLS-1$
			}
		});

		PlatformUI.getWorkbench().getHelpSystem().setHelp(colorComposite, ICHelpContextIds.C_EDITOR_COLORS_PREF_PAGE);

		return colorComposite;
	}

	private Control createPreviewer(Composite parent) {

		fCTextTools = FortranUIPlugin.getDefault().getTextTools();
		fPreviewViewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
		fPreviewViewer.configure(new FortranSourceViewerConfiguration(fCTextTools, null));
		fPreviewViewer.getTextWidget().setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		fPreviewViewer.setEditable(false);

		String content = loadPreviewContentFromFile("ColorSettingPreviewCode.txt"); //$NON-NLS-1$
		IDocument document = new Document(content);
		IDocumentPartitioner partitioner = fCTextTools.createDocumentPartitioner();
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);

		fPreviewViewer.setDocument(document);

		fOverlayStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				fPreviewViewer.getDocument().set(fPreviewViewer.getDocument().get());
				fPreviewViewer.invalidateTextPresentation();
				//fPreviewViewer.refresh();
			}
		});

		return fPreviewViewer.getControl();
	}

	// sets enabled flag for a control and all its sub-tree
	protected static void setEnabled(Control control, boolean enable) {
		control.setEnabled(enable);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			Control[] children = composite.getChildren();
			for (int i = 0; i < children.length; i++)
				setEnabled(children[i], enable);
		}
	}

	private Control createAppearancePage(Composite parent) {

		Composite behaviorComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		behaviorComposite.setLayout(layout);

		String label = PreferencesMessages.getString("FortranEditorPreferencePage.behaviorPage.matchingBrackets"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, FortranEditor.MATCHING_BRACKETS, 0);

		label = PreferencesMessages.getString("FortranEditorPreferencePage.behaviorPage.tabSpace"); //$NON-NLS-1$
		addCheckBox(behaviorComposite, label, FortranEditor.SPACES_FOR_TABS, 0);
		return behaviorComposite;
	}

	private Control createHeader(Composite parent) {
		String text = PreferencesMessages.getString("FortranEditorPreferencePage.link"); //$NON-NLS-1$
		Link link = new Link(parent, SWT.NONE);
		link.setText(text);
		link.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				String u = event.text;
				System.out.println("Selection: " + u);
				PreferencesUtil.createPreferenceDialogOn(getShell(), u, null, null);
			}
		});

		GridData gridData= new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.widthHint= 150; // only expand further if anyone else requires it
		link.setLayoutData(gridData);
		return link;
	}

	private void createDependency(final Button master, String masterKey, final Control slave) {
		indent(slave);
		boolean masterState= fOverlayStore.getBoolean(masterKey);
		slave.setEnabled(masterState);
		SelectionListener listener= new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				slave.setEnabled(master.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		};
		master.addSelectionListener(listener);
		fMasterSlaveListeners.add(listener);
	}

	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		fCEditorHoverConfigurationBlock= new CEditorHoverConfigurationBlock(this, fOverlayStore);
		fFoldingConfigurationBlock= new FoldingConfigurationBlock(fOverlayStore);

		createHeader(parent);

		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("FortranEditorPreferencePage.generalTabTitle")); //$NON-NLS-1$
		item.setControl(createAppearancePage(folder));

		item = new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("FortranEditorPreferencePage.colorsTabTitle")); //$NON-NLS-1$
		item.setControl(createSyntaxPage(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("FortranEditorPreferencePage.hoverTab.title")); //$NON-NLS-1$
		item.setControl(fCEditorHoverConfigurationBlock.createControl(folder));

		item= new TabItem(folder, SWT.NONE);
		item.setText(PreferencesMessages.getString("FortranEditorPreferencePage.folding.title")); //$NON-NLS-1$
		item.setControl(fFoldingConfigurationBlock.createControl(folder));

		initialize();

		return folder;
	}

	private void initialize() {

		initializeFields();

		for (int i = 0; i < fListModel.length; i++)
			fList.add(fListModel[i][0]);
		fList.getDisplay().asyncExec(new Runnable() {
			public void run() {
				fList.select(0);
				handleListSelection();
			}
		});

		fFoldingConfigurationBlock.initialize();

	}

	/*
	 * @see PreferencePage#performOk()
	 */
	public boolean performOk() {
		fCEditorHoverConfigurationBlock.performOk();
		fFoldingConfigurationBlock.performOk();
		return super.performOk();
	}

	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {

		fOverlayStore.loadDefaults();
		initializeFields();
		handleListSelection();

		fCEditorHoverConfigurationBlock.performDefaults();
		fFoldingConfigurationBlock.performDefaults();

		super.performDefaults();

		fPreviewViewer.invalidateTextPresentation();
	}

	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {

		fFoldingConfigurationBlock.dispose();

		if (fCTextTools != null) {
			fCTextTools = null;
		}

		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}

		super.dispose();
	}


	private String loadPreviewContentFromFile(String filename) {
		String line;
		String separator = System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer(512);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				buffer.append(separator);
			}
		} catch (IOException io) {
			FortranUIPlugin.getDefault().log(io);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return buffer.toString();
	}

}
