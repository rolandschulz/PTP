/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.ui.wizards;

import java.awt.Container;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.internal.core.elements.LguiType;
import org.eclipse.ptp.rm.lml.internal.core.elements.Nodedisplay;
import org.eclipse.ptp.rm.lml.internal.core.elements.ObjectsType;
import org.eclipse.ptp.rm.lml.internal.core.elements.TableType;

import org.xml.sax.SAXException;

public class SelectFilesWizard extends Wizard {
	
	public class SelectFilesWizardPage extends WizardPage {
		
		private Text xmlFileField;
		
		public SelectFilesWizardPage() {
			super("Select Files");
			wizardPage = this;
			setTitle("Select XML-files");
			setDescription("Select the XML file");
			
		}

		@Override
		public void createControl(Composite parent) {
			
			Composite container = new Composite(parent, SWT.NULL);
			final  GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 3;
			container.setLayout(gridLayout);
			setControl(container);
			
			final Label label_1 = new Label(container, SWT.NONE);
			final GridData gridData_1 = new GridData();
			gridData_1.horizontalSpan = 3;
			label_1.setLayoutData(gridData_1);
			label_1.setText("Select the XML file to generate an Object");
			
			final Label label_2 = new Label(container, SWT.NONE);
			final GridData gridData_2 = new GridData(GridData.HORIZONTAL_ALIGN_END);
			label_2.setLayoutData(gridData_2);
			label_2.setText("XML File: ");
			
			xmlFileField = new Text(container, SWT.BORDER);
			xmlFileField.addModifyListener( new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updatePageComplete();
				}
			});
			xmlFileField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			final Button button = new Button(container, SWT.NONE);
			button.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {
					browseForXmlFile();
				}
			});
			button.setText("Browse...");
			
			updatePageComplete();
			
		}
		
		private void updatePageComplete() {
			setPageComplete(false);
			
			IPath xmlFileLocation = getXmlLocation();
			if (xmlFileLocation == null) {
				setMessage(null);
				setErrorMessage("Please select a XML file.");
				return;
			}

			if (!xmlFileLocation.lastSegment().endsWith(".xml")) {
				setMessage(null);
				setErrorMessage("Select an XML-file!");
				return;
			}
			
			setPageComplete(true);
			setMessage(null);
			setErrorMessage(null);
		}
		
		protected void browseForXmlFile() {
			IPath path = browse(getXmlLocation(), false);
			if (path == null) {
				return;
			}
			IPath rootLoc = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			if (rootLoc.isPrefixOf(path)) {
				path = path.setDevice(null).removeFirstSegments(rootLoc.segmentCount());
			}
			xmlFileField.setText(path.toString());
			try{
				xmlFile = path.toFile().toURI().toURL();
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
				setMessage(null);
				setErrorMessage("URL (XML file) not allowed");
			}
		}
		
		private IPath browse(IPath path, boolean mustExist) {
			FileDialog dialog = new FileDialog(getShell(), mustExist ? SWT.OPEN : SWT.SAVE);
			if (path != null) {
				if (path.segmentCount() > 1) {
					dialog.setFilterPath(path.removeLastSegments(1).toOSString());
				}
				if (path.segmentCount() > 0) {
					dialog.setFileName(path.lastSegment());
				}
			}
			String result = dialog.open();
			if (result == null) {
				return null;
			}
			return new Path(result);
		}
		
		public IPath getXmlLocation() {
			String text = xmlFileField.getText().trim();
			if (text.length() == 0) {
				return null;
			}
			IPath path = new Path(text);
			if (!path.isAbsolute()) {
				path = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(path);
			}
			return path;
		}

	}

	private final ArrayList<IWizardPage> fWizardPages = new ArrayList<IWizardPage>();
	private final SelectFilesWizardPage fSelectFilesPage = new SelectFilesWizardPage();

	private URL xmlFile = null;
	
	private WizardPage wizardPage;

	/*
	 * Constructor used when creating a new resource manager.
	 */
	public SelectFilesWizard() {
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(fSelectFilesPage);
		super.addPages();
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		boolean existingLgui = LMLCorePlugin.getDefault().getLMLManager().addLgui(xmlFile);
		if (!existingLgui) {
			wizardPage.setErrorMessage("This XML-File is already loaded. Choose another XML-File!");
		} else {
			wizardPage.setErrorMessage(null);
		}
		return existingLgui;
	}

	/**
	 * @return
	 */
	private int getNumPages() {
		return fWizardPages.size();
	}

}
