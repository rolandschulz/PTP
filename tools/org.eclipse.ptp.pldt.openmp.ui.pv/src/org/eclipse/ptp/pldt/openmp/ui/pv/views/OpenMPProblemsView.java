/*******************************************************************************
 * Copyright (c) 2006,2007 IBM Corp. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.ui.pv.views;


import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.ptp.pldt.openmp.ui.pv.PvPlugin;
import org.eclipse.ptp.pldt.openmp.ui.pv.views.ProblemMarkerAttrIds;

/**
 * OpenMP Problems View
 * <p>
 */

public class OpenMPProblemsView extends ViewPart {
	private TableViewer viewer;
	private Action action1;
	//private Action action2;
	private Action doubleClickAction;
	private Action removeMarkerAction;
    
    private   String                  markerID_   = PvPlugin.MARKER_ID;
    protected UpdateVisitor           visitor_    = new UpdateVisitor();
    
    private String                    iconName_          = "icons/openMPproblem.gif";
    private AbstractUIPlugin          thePlugin_;
    
    protected boolean                 traceOn     = false;
    
    private static final String [] columns_ = {" ",
                                               "Description",
                                               "Resource",
                                               "In Folder",
                                               "Location"
    };

    /**
     * OpenMPProblemsView - Constructor
     *
     */
    public OpenMPProblemsView() {
        thePlugin_ = PvPlugin.getDefault(); 
    }

    /**
     * This is a callback that will allow us
     * to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {
        Table       table  = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        for(int i=0; i<columns_.length; i++) {
            layout.addColumnData(new ColumnWeightData(columns_[i].length(), columns_[i].length(), true));
            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setText(columns_[i]);
            tc.setAlignment(SWT.LEFT);
            tc.setResizable(true);
        }
         
        viewer = new TableViewer(table); //new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setSorter(new NameSorter());
        viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
        // markers from workspace
        
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        contributeToActionBars();
    }
   

    /**
     * It might be useful for subclasses to override this, to say which filenames should allow the action "run analysis"  to create
     * new artifacts and thus new markers. <br>
     * This is a default implementation
     * 
     * @param filename
     * @return
     */
    public boolean validForAnalysis(String filename)
    {
        //return MpiUtil.validForAnalysis(filename);
        return true;

    }


	/*
	 * The content provider class is responsible for providing objects to the view. It can wrap
	 * existing objects in adapters or simply return objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore it and always show the same content 
	 * (like Task List, for example).
	 */
	class ViewContentProvider implements IStructuredContentProvider, IResourceChangeListener {
        private IResource    input         = null;
        private boolean      hasRegistered = false;
        
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
              if (!hasRegistered) {
                  // add me as a resource change listener so i can refresh at
                  // least when markers are changed
                  ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
                  hasRegistered = true;
              }
              if (newInput instanceof IResource)
                  input = (IResource)newInput;
		}
        
		public void dispose() {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		}
        
		public Object[] getElements(Object parent) 
        {
            Object [] objs = null;
            try {
                String id = markerID_;
                if (input!=null) {
                     // use the cached input object instead of querying from workspace
                     objs = input.findMarkers(id, false, IResource.DEPTH_INFINITE);
                }
            }
            catch(CoreException e) {
                System.out.println("OpenMPProblemsView exception gettting model elements (markers");
                e.printStackTrace();
            }
            return objs;
		}
        
        /**
         *  resourceChanged - react to a resource change event
         */
        public void resourceChanged(IResourceChangeEvent event)
        {
            final IResourceDelta delta = event.getDelta();
            Control              ctrl  = viewer.getControl();
            if (ctrl!=null && !ctrl.isDisposed()) {
                ctrl.getDisplay().syncExec(new Runnable()  {
                    public void run() 
                    {
                        processResourceChangeDelta(delta);
                        // we should have updated the indiv. rows we care about.
                        // but need this for Marker display after initial analysis,
                        // and for markers deleted, etc.  Can remove when we more completely
                        // handle things in processResourceChangeDelta
                        viewer.refresh();
                    }
                });
            }
        }
        
        protected void processResourceChangeDelta(IResourceDelta delta)
        {
            try {
                delta.accept(visitor_);
            }
            catch(CoreException e) {
                System.out.println("OpenMPProblemsView error in processResoruceChangeDelta");
                e.printStackTrace();
            }
        }
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
        private HashMap   iconHash = new HashMap();
        
        public Image getColumnImage(Object obj, int index)
        {
            // we only put image icon in the first column
            switch (index) {
                case 0:
                    return getCustomImage(obj);
                default:
                    return null;
            }
        }
        protected Image getCustomImage(Object obj)
        {
            // if we've already created one of this type of icon, reuse it.
            Image img = (Image) iconHash.get(iconName_);
            if (img == null) {
                Path path= new Path(iconName_);
                // BRT make sure the specific plugin is being used here to find its OWN icons
                URL url = thePlugin_.find(path);
                ImageDescriptor id = ImageDescriptor.createFromURL(url);
                img = id.createImage();
                if (traceOn) System.out.println("ATV: ***** created image for " + iconName_);
                iconHash.put(iconName_, img);// save for reuse
            }
            return img;
        }
        
        public void dispose()
        {
            if (traceOn) System.out.println("ATV.ViewLabelProvider.dispose(); dispose of icon images");
            for (Iterator iter = iconHash.values().iterator(); iter.hasNext();) {
                Image img = (Image) iter.next();
                img.dispose();
            }
            super.dispose();
        }


        /**
         * Determine the text to go in each column
         * 
         * @param obj the Marker (we hope) that goes on the current row
         * @param index the column number in the table
         * 
         */
        public String getColumnText(Object obj, int index)
        {
            if (obj == null) {
                System.out.println("ATV: LabelProv obj is null; index=" + index);
                return "ATV obj null";
            }
            IMarker marker = (IMarker) obj;
            try {
                switch (index) {
                    case 0:
                        return "";
                    case 1:
                        String id = (String) marker.getAttribute(ProblemMarkerAttrIds.DESCRIPTION);
                        return id;
                    case 2:
                        return (String) marker.getAttribute(ProblemMarkerAttrIds.RESOURCE);
                    case 3:
                        return (String) marker.getAttribute(ProblemMarkerAttrIds.INFOLDER);
                    case 4:
                        return ((Integer) marker.getAttribute((ProblemMarkerAttrIds.LOCATION))).toString();
                    default:
                        return "";
                }
            } catch (CoreException ce) {
                return ("ViewLabelProvider.getColumnText error: "+ce);
            }
        }

	}
	class NameSorter extends ViewerSorter {
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				OpenMPProblemsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		//manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		//manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(removeMarkerAction);

	}

	private void makeActions() {
        //makeShowInfoAction();
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
        
        doubleClickAction = new GotoLineAction(this, viewer);
        makeRemoveMarkerAction();
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	/**
	 * Make "remove marker" action to display artifact information
	 */
	protected void makeRemoveMarkerAction() {
		removeMarkerAction = new Action() {
			public void run() {
				// batch changes so we get only one resource change event
				final IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
				
				IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
					public void run(IProgressMonitor monitor) throws CoreException {
					try {
						int depth = IResource.DEPTH_INFINITE;
						wsResource.deleteMarkers(markerID_,false,depth);
						if(traceOn)
							System.out.println("markers removed id="+markerID_);
					} catch (CoreException e) {
						System.out.println("RM: exception deleting markers.");
						// e.printStackTrace();
					}
					}};
					try {
						runnable.run(null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				 
			}//end run()
		};// end new action
		removeMarkerAction.setText("Remove Markers");
		removeMarkerAction.setToolTipText("Remove all markers");
		removeMarkerAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));// nice "red X" image

	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
    

    /**
     * Visit the resource delta to look for the marker changes we are interested in
     * 
     * @author Beth Tibbitts
     */
    public class UpdateVisitor implements IResourceDeltaVisitor
    {

        /**
         * Visit appropriate parts of the resource delta to find the markers that changed that we care about.
         * 
         * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
         */
        public boolean visit(IResourceDelta delta) throws CoreException
        {
            IResource resource = delta.getResource();
            //String name = resource.getName();
            if (resource.getType() == IResource.FILE) {
                if (delta.getKind() == IResourceDelta.CHANGED) {
                    //if (traceOn) System.out.println("UpdateVisitor: file changed: " + name);

                    // Handle file changes (saves) by reporting the changes
                    // made to the file, to update backend analysis
                    // representation
                    IFile f = (IFile) resource;
                    int flags = delta.getFlags();
                    int contentChanged = flags & IResourceDelta.CONTENT;

                    if (validForAnalysis(f.getName())) {
                        if (traceOn)
                            System.out.println("File " + f.getName() + " is valid for analysis so will process the change...");
                        if (contentChanged != 0) {
                            // do we need to tell back end (analysis engine) that file changed?
                        }

                        // refresh markers for that file?
                        IMarkerDelta[] mDeltas = delta.getMarkerDeltas();
                        int len = mDeltas.length;
                        for (int j = 0; j < len; j++) {
                            IMarkerDelta delta3 = mDeltas[j];
                            if (traceOn) showMarkerDeltaKind(delta3);
                            IMarker m = delta3.getMarker();
                            String ln = IMarker.LINE_NUMBER;
                            if (traceOn) System.out.println("---UpdateVisitor.visit():viewer update marker: (lineNo)");
                            // showMarker(m);
                            String[] props = new String[1]; // awkward. why???
                            props[0] = ln;
                            // just update viewer item, not the whole view
                            // viewer.refresh();
                            viewer.update(m, props);
                        } // end loop
                    } else {
                        if (traceOn)
                            System.out.println("File " + f.getName() + " is NOT valid for analysis so will ignore change...");

                    }
                } // end if CHANGED
                else if (delta.getKind() == IResourceDelta.ADDED) {
                    //System.out.println("Resource added.");
                    checkMarkerDeltas(delta);
                } else if (delta.getKind() == IResourceDelta.REPLACED) {
                    //System.out.println("Resource replaced.");
                    checkMarkerDeltas(delta);
                } else if (delta.getKind() == IResourceDelta.REMOVED) {
                    //System.out.println("Resource removed.");
                    checkMarkerDeltas(delta);
                }
            } // end if FILE
            return true; // keep going
        }

        private void checkMarkerDeltas(IResourceDelta delta)
        {
            IMarkerDelta[] md1 = delta.getMarkerDeltas();
            int len = md1.length;
            //System.out.println("       ... found " + len + " markerDeltas.");
        }

        /**
         * Show info about the marker in the marker delta. This is just tracing the info available until we do something
         * with it. For now, we're just doing a (big) viewer.refresh() to refresh all the markers. When we get more
         * intelligent about just updating the ones that changed, we can remove that. Shouldn't make much different for
         * small sets of markers, but for many markers, this could be a significant performance improvement.
         * 
         * @param delta3
         */
        private void showMarkerDeltaKind(IMarkerDelta delta3)
        {

            String mid = "", ml = "", mlpi = "";
            try {
                // note: we're getting marker deltas on ALL markers,
                // not just artifact markers, which can throw us off.
                // in particular, temp markers used by actions?

                //mid = m.getAttribute(uniqueID_).toString();
                //ml = m.getAttribute(IMarker.LINE_NUMBER).toString();
                //mlpi = m.getAttribute(IDs.LINE).toString();
            } catch (Exception e1) {
                // ignore errors; only tracing for now.
                System.out.println("ATV.UpdateVisitor error getting marker info ");
                e1.printStackTrace();
            }
            if (traceOn) System.out.println("    markerID_=" + mid + "  lineNo(mkr-mpiA)=" + ml + "-" + mlpi);
        }

    } // end class UpdateVisitor
    
    
    // Action for dblclick and go to line
    public static class GotoLineAction extends Action
    {
        protected   ViewPart   viewPart_ = null;
        protected   Viewer     viewer_   = null;
        
        public GotoLineAction(ViewPart viewPart, Viewer viewer)
        {
            viewPart_ = viewPart;
            viewer_   = viewer;
        }
        
        public void run()
        {
            ISelection selection = viewer_.getSelection();
            Object obj = ((IStructuredSelection)selection).getFirstElement();
            IMarker marker = (IMarker) obj;
            try {
                OpenMPError error = (OpenMPError)marker.getAttribute(ProblemMarkerAttrIds.PROBLEMOBJECT);
                if (error==null) return;
                String filename = error.getPath()+"/"+error.getFilename(); 
                
                IResource r = ParserUtil.getResourceForFilename(filename);
                IFile     f = (r instanceof IFile ? (IFile)r : null);
                if (f==null)  return;
                // IFile fakefile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fqstr));
                
                IWorkbenchPage wbp = viewPart_.getSite().getPage();
                IEditorPart editor = IDE.openEditor(wbp, f);
                String      markerID = "org.eclipse.core.resource.textmarker";
                IMarker     marker1 = f.createMarker(markerID);
                marker1.setAttribute(IMarker.LINE_NUMBER, error.getLineno());
                marker1.setAttribute(IMarker.LOCATION, filename);
                  
                IDE.gotoMarker(editor, marker1);
                marker1.delete();
            }
            catch(Exception e) {e.printStackTrace();}
         }
    }

}