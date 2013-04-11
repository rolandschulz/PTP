/*******************************************************************************
 * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.includebrowser.IncludeBrowserUI
 * Version: 1.1
 */

package org.eclipse.ptp.internal.rdt.ui.includebrowser;

import java.net.URI;

import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.includebrowser.IBMessages;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIndexIncludeValue;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IncludeBrowserServiceFactory;
import org.eclipse.ptp.internal.rdt.ui.util.PathReplacer;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class IncludeBrowserUI {

	public static void open(final IWorkbenchWindow window, final ICElement input) {
        try {
        	ITranslationUnit tu= convertToTranslationUnit(input);
        	if (tu != null) {
        		IWorkbenchPage page= window.getActivePage();
        		IBViewPart result= (IBViewPart)page.showView(UIPlugin.INCLUDE_BROWSER_VIEW_ID);
        		result.setInput(tu);
        	}
        } catch (CoreException e) {
            ExceptionHandler.handle(e, window.getShell(), IBMessages.OpenIncludeBrowserAction_label, null); 
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
		}
    }

	public static void open(final ITextEditor editor, final ITextSelection sel) {
		if (editor != null) {
			ICElement inputCElement = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
			open (editor.getSite().getWorkbenchWindow(), inputCElement);
		}
    }

    private static ITranslationUnit convertToTranslationUnit(ICElement input) throws CoreException, InterruptedException {
    	ITranslationUnit result= null;
    	if (input instanceof IInclude) {
    		result= findTargetTranslationUnit((IInclude) input);
    	}
    	if (result == null && input instanceof ISourceReference) {
    		result= ((ISourceReference) input).getTranslationUnit();
    	}
		return result;
	}

	private static ITranslationUnit findTargetTranslationUnit(IInclude input) throws CoreException, InterruptedException {
		ICProject project= input.getCProject();
		if (project != null) {
			
			IIncludeBrowserService service = new IncludeBrowserServiceFactory().getIncludeBrowserService(project);
			
			IIndexIncludeValue include= service.findInclude(input, null);
			if (include != null) {
				IIndexFileLocation loc= include.getIncludesLocation();
				if (loc != null) {
					final URI uri = PathReplacer.replacePath(project.getProject(), input.getLocationURI(), loc.getURI().getPath());
					if(uri != null)
						return CoreModelUtil.findTranslationUnitForLocation(uri, project);
				}
			}
		}
		return null;
	}
	
	public static boolean isIndexed(ICElement element, IProgressMonitor monitor) throws CoreException 
	{
		if (element instanceof ISourceReference) 
		{
			ISourceReference sf = ((ISourceReference)element);
			ITranslationUnit tu= sf.getTranslationUnit();
			if (tu != null) 
			{
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) 
				{
					ICProject project = element.getCProject();
					
					IIncludeBrowserService service = new IncludeBrowserServiceFactory().getIncludeBrowserService(project);

					return (service != null) ? service.isIndexed(location, project, monitor) : false;
				}
			}
		}
		return false;
	}

}
