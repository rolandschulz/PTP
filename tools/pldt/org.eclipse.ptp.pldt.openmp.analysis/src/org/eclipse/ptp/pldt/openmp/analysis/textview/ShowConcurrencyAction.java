/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.textview;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ptp.pldt.common.util.Utility;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPAnalysisManager;
import org.eclipse.ptp.pldt.openmp.analysis.internal.core.OpenMpIDs;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action to Select a line and show the concurrency
 * @author pazel
 *
 */
public class ShowConcurrencyAction extends ActionDelegate 
                                   implements IEditorActionDelegate
{
    protected CEditor  editor_ = null;
    
    protected static final String TITLE = "Show Concurrency";
    
    /**
     * Sets the active editor for the delegate.  
     * Implementors should disconnect from the old editor, connect to the 
     * new editor, and update the action to reflect the new editor.
     *
     * @param action the action proxy that handles presentation portion of the action
     * @param targetEditor the new editor target
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        if (targetEditor instanceof CEditor)
            editor_ = (CEditor)targetEditor;
    }
    

    /**
     * The <code>ActionDelegate</code> implementation of this
     * <code>IActionDelegate2</code> method redirects to the <code>run</code>
     * method. Subclasses may reimplement.
     */
    public void runWithEvent(IAction action, Event event) {
        TextSelection selection = null;
        if (editor_!=null && 
            editor_.getSelectionProvider().getSelection() instanceof TextSelection) {
            selection = (TextSelection)editor_.getSelectionProvider().getSelection();
        }
        if (selection!=null) {
            showConcurrency(selection);
        }
        else 
            showMessage(TITLE, "No selections made");
    }
    
    protected void showMessage(String title, String message)
    {
        MessageDialog.openInformation(getStandardDisplay().getActiveShell(), title, message);
    }
    
    public static Display getStandardDisplay() {
        Display display= Display.getCurrent();
        if (display == null) {
            display= Display.getDefault();
        }
        return display;     
    }
    
    /**
     * showConcurrency - triggered by the context menu action
     * @param selection - TextSelection
     */
    protected void showConcurrency(TextSelection selection)
    {
        if (selection.getOffset()==0 && selection.getLength()==0) {
            showMessage(TITLE, "No selections made");
            return;
        }

        // Get the last analysis
        OpenMPAnalysisManager oam = OpenMPAnalysisManager.getCurrentManager();
        if (oam==null) {
            showMessage(TITLE, "No OpenMP Analysis has been performed");
            return;
        }
        
        // Now we check if the analysis is for this file
        if (oam.getTU()==null ) {
            showMessage(TITLE, "Error in last OpenMP Analysis");
            return;
        }
        //String f1 = oam.getTU().getFilePath();
        //String f2 = editor_.getInputFile().getLocation().toOSString();
        IEditorInput ieu = editor_.getEditorInput();
        IFile inputFile=null;
        if(ieu instanceof IFileEditorInput){
        	inputFile = ((IFileEditorInput)ieu).getFile();//cdt40
        }
        else{
        	showMessage(TITLE, "Cannot locate file in editor");//cdt40
        	return;
        }

        if (!oam.getTU().getFilePath().equals(inputFile.getLocation().toOSString())) {//cdt40
            showMessage(TITLE, "OpenMP analysis required on editor file");
            return;
        }
        
        if (oam.getFileMap()==null) {
            showMessage(TITLE, "Internal Error: no file map");
            return;
        }
        
        IASTNode node = oam.getFileMap().find(selection.getOffset(), 1);  // only 1 - where we are
        if (node==null) {
            showMessage(TITLE, "Cannot locate statement at given selection point");
            return;
        }
        
        removeConcurrencyMarkers(editor_.getDocumentProvider().getAnnotationModel(editor_.getEditorInput()));
        
        Set cSet = oam.getNodesConcurrentTo(node);
        
        // Display all concurrent statements
        for(Iterator i=cSet.iterator(); i.hasNext();) {
            IASTNode n = (IASTNode)i.next();
            showNode(n, OpenMpIDs.ConcurrencyType);
        }
        
        // If node is not concurrent to itself, we adopt a different marker color
        if (!cSet.contains(node))
           showNode(node, OpenMpIDs.NonConcurrencyType); 
        
        // select the key stmt
        Utility.Location l = Utility.getLocation(node);
        editor_.selectAndReveal(l.low_, l.high_-l.low_+1); 

    }
    
    /**
     * showNode - Show the node of the right marker type
     * @param node       - IASTNode
     * @param markerType - String 
     */
    protected void showNode(IASTNode node, String markerType)
    {
        Utility.Location l = Utility.getLocation(node);

        
        IAnnotationModel am = editor_.getDocumentProvider().getAnnotationModel(editor_.getEditorInput());
        
        // We need to add an annotation type to the annotation painter (see SourceViewerDecorationSupport)
        Annotation a = new Annotation(markerType, true, "Hi");
        int end = l.high_-l.low_+1;
        Position   p = new Position(l.low_, end);
        am.addAnnotation(a, p);
        
        //System.out.println("annotate node="+node.getClass().toString()+" begin="+l.low_+" end="+end);
    }
    
    /**
     * remove the concurrency markers from the screen
     * @param am - IAnnotationModel
     */
    public static void removeConcurrencyMarkers(IAnnotationModel am)
    {
        LinkedList ais = new LinkedList();
        for(Iterator ai=am.getAnnotationIterator(); ai.hasNext();) { ais.add(ai.next()); }
        for(Iterator it=ais.iterator(); it.hasNext();) {
            Annotation a = (Annotation)it.next();
            if (a.getType().equals(OpenMpIDs.ConcurrencyType) || a.getType().equals(OpenMpIDs.NonConcurrencyType)) {
                am.removeAnnotation(a);
            }
        }
    }

}
