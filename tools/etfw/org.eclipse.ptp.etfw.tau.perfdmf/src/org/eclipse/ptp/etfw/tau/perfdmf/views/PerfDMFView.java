/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alan Morris - initial API and implementation
 *    Wyatt Spear - various modifications
 ****************************************************************************/
package org.eclipse.ptp.etfw.tau.perfdmf.views;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ptp.etfw.tau.perfdmf.PerfDMFUIPlugin;
import org.eclipse.ptp.etfw.tau.perfdmf.views.ParaProfController.Level;
import org.eclipse.ptp.etfw.tau.perfdmf.views.ParaProfController.TreeTuple;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Defines a perfdmf database browser view and associated operations
 * @author wspear
 *
 */
public class PerfDMFView extends ViewPart {
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	//private Action action1; //TODO: Add an 'upload external performance data' option
	private Action refreshAction;
	private Action doubleClickAction;

	private Action paraprofAction;
	private Action launchparaprofAction;

	private Action switchDatabaseAction;

	ParaProfController ppc;

	List<TreeTuple> databases;
	private TreeTuple database=null;

	class TreeNode implements IAdaptable {
		private TreeTuple tt;
		private TreeNode parent;

		private ArrayList<TreeNode> children = new ArrayList<TreeNode>();

		public TreeNode(TreeTuple tt){
			this.tt=tt;
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		public int getID(){
			return tt.id;
		}

		public String getName() {
			if(tt==null){
				return ""; //$NON-NLS-1$
			}
			return tt.name;
		}

		public void setParent(TreeNode parent) {
			this.parent = parent;
		}

		public TreeNode getParent() {
			return parent;
		}

		public String toString() {
			return getName();
		}

		public void addChild(TreeNode child) {
			children.add(child);
			child.setParent(this);
		}

		public void removeChild(TreeNode child) {
			children.remove(child);
			child.setParent(null);
		}

		public TreeNode[] getChildren() {
			return children.toArray(new TreeNode[children.size()]);
		}

		public boolean hasChildren() {
			return children.size() > 0;
		}

	}

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	IFile getFile(String filename, IResource[] resources) {
		try {
			for (int j = 0; j < resources.length; j++) {
				//System.out.println("  considering resource '" + resources[j] + "'");
				if (resources[j] instanceof IFile) {
					IFile f = (IFile) resources[j];
					// System.out.println("filename = " + f.getName());
					if (f.getName().equals(filename)) {
						return f;
					}
				} else if (resources[j] instanceof IFolder) {
					//System.out.println("recurse on Folder");
					IFile f = getFile(filename, ((IFolder) resources[j]).members());
					if (f != null) {
						return f;
					}
				} else if (resources[j] instanceof IProject) {
					//System.out.println("recurse on Project");
					IFile f = getFile(filename, ((IProject) resources[j]).members());
					if (f != null) {
						return f;
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	/**
	 * Returns a list of all found databases in the form NAME - CONNECTION-STRING
	 * @return
	 */
	public String[] getDatabaseNames()
	{	
		String[] names = new String[databases.size()];

		for(int i=0;i<names.length;i++){
			names[i]=databases.get(i).name;
		}

		return names;
	}

	/**
	 * Returns just the configuration name of the full database identification string provided.
	 * @param A database name "name - connection string" as provided by getDatabaseNames()
	 * @return
	 */
	public static String extractDatabaseName(String name)
	{

		//   	 List dbs = Database.getDatabases();
		//	 
		//	 if(dbs.size()==0)
		//		 return null;
		//	 
		//	 //String[] names = new String[dbs.size()];
		//	 
		//	 Iterator dit = dbs.iterator();
		//
		//     while(dit.hasNext())
		//     {
		//    	 Database dtest=(Database)dit.next();
		//    	 if(name.endsWith(dtest.getConfig().getName()+" - "+dtest.getConfig().getConnectionString()))
		//    			 return dtest.getConfig().getName();
		//     }

		//return null;
		//TODO: Until we implement otherwise, database names and ID strings are the same, so this function is redundant
		return name;
	}

	/**
	 * Given the name of a database configuration, returns the associated database and sets the 
	 * current databaseName and displayed database name to that database
	 * @param name
	 * @return
	 */
	public TreeTuple getDatabase(String name)
	{
		if (databases==null||databases.size() < 1) {
			database=null;
			return null;
		}
		if(name==null){
			name=Messages.PerfDMFView_Default;
		}
		Iterator<TreeTuple> dit = databases.iterator();
		int defdex=0;
		int i=0;
		while(dit.hasNext())
		{
			TreeTuple dtest=dit.next();
			if(dtest.name.equals(name))
			{
				database=dtest;

				if(switchDatabaseAction!=null)
					switchDatabaseAction.setText(Messages.PerfDMFView_UsingDatabase+ database.name);

				return(dtest);
			}
			if(dtest.name.equals(Messages.PerfDMFView_Default)){
				defdex=i;
			}
			i++;
		}

		database=databases.get(defdex);
		if(switchDatabaseAction!=null)
			switchDatabaseAction.setText(Messages.PerfDMFView_UsingDatabase+ database.name);

		return database;//(Database) dbs.get(0);
	}

	private void openSource(String projectName, String filename, int startLine, int endLine){//final SourceRegion sourceLink) {

		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			//IProject[] projects = workspace.getRoot().getProjects();
			IWorkspaceRoot root = workspace.getRoot();

			IFile file = getFile(filename, root.members());//sourceLink.getFilename()

			if (file == null) {
				return;
			}
			IEditorInput iEditorInput = new FileEditorInput(file);

			IWorkbenchPage p = getActivePage();
			String editorid="org.eclipse.cdt.ui.editor.CEditor"; //$NON-NLS-1$
			if(file.getContentDescription().toString().indexOf("org.eclipse.photran.core.freeFormFortranSource")>=0||file.getContentDescription().toString().indexOf("org.eclipse.photran.core.fortranSource")>=0) //$NON-NLS-1$ //$NON-NLS-2$
				editorid="org.eclipse.photran.ui.FreeFormFortranEditor"; //$NON-NLS-1$
			else
				if(file.getContentDescription().toString().indexOf("org.eclipse.photran.core.fixedFormFortranSource")>=0) //$NON-NLS-1$
					editorid="org.eclipse.photran.ui.FixedFormFortranEditor"; //$NON-NLS-1$

			IEditorPart part = null;
			if (p != null) {
				part = p.openEditor(iEditorInput, editorid, true);
			}


			//IEditorPart part = EditorUtility.openInEditor(file);

			TextEditor textEditor = (TextEditor) part;

			final int start = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).getLineOffset(
					startLine-1);//sourceLink.getStartLine() - 1
			final int end = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).getLineOffset(
					endLine);//sourceLink.getEndLine()

			textEditor.setHighlightRange(start, end - start, true);

			AbstractTextEditor abstractTextEditor = textEditor;

			ISourceViewer viewer = null;

			final Field fields[] = AbstractTextEditor.class.getDeclaredFields();
			for (int i = 0; i < fields.length; ++i) {
				if ("fSourceViewer".equals(fields[i].getName())) { //$NON-NLS-1$
					Field f = fields[i];
					f.setAccessible(true);
					viewer = (ISourceViewer) f.get(abstractTextEditor);
					break;
				}
			}

			if (viewer != null) {
				viewer.revealRange(start, end - start);
				viewer.setSelectedRange(start, end - start);
			}

		} catch (Throwable t) {
			// t.printStackTrace();
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		private TreeNode invisibleRoot;

		public void refresh(Viewer v) {
			invisibleRoot = null;
			v.refresh();
		}

		public Object getRoot() {
			return invisibleRoot;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
				{
					if(!initialize())
					{
						return null;
					}
				}
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			return ((TreeNode) child).getParent();
		}

		public Object[] getChildren(Object parent) {
			return ((TreeNode) parent).getChildren();
		}

		public boolean hasChildren(Object parent) {
			return ((TreeNode) parent).hasChildren();
		}


		/*
		 * We will set up a dummy model to initialize tree heararchy.
		 * In a real code, you will connect to a real model and
		 * expose its hierarchy.
		 */
		 private boolean initialize() {

			class SourceWatcher implements Runnable{
				public void run(){
					BlockingQueue<String> q = ppc.getPullQueue();
					if(q!=null)
					while(true){
						String s = null;
						try {
							s = q.take();
							if(s==null||s.equals("DONE")) //$NON-NLS-1$
								break;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						String[] split = s.split(" "); //$NON-NLS-1$
						final String source = split[2];
						final int start=Integer.parseInt(split[3]);
						final int finish=Integer.parseInt(split[5]);
						Display.getDefault().asyncExec(new Runnable() {

							public void run() {
								openSource(null,source,start,finish);
							}

						});
					}

				}
			}

				new Thread(new SourceWatcher()).start();

				invisibleRoot = new TreeNode(null);

				//TreeTuple database=getDatabase("Default");
				if(databases==null)
				{   
					invisibleRoot.addChild(new TreeNode(null));//new TreeNode("none",-1,0,null));
					return true;
				}

				if(database==null){
					getDatabase(Messages.PerfDMFView_Default);
					if(database==null){
						database=ParaProfController.EMPTY;
						return true;
					}
				}

				for (Iterator<TreeTuple> it = ppc.getApplications(database.id).iterator(); it.hasNext();) {
					TreeTuple app = it.next();

					TreeNode root = new TreeNode(app);
					for (Iterator<TreeTuple> it2 = ppc.getExperiments(database.id, app.id).iterator(); it2.hasNext();) {
						TreeTuple exp = it2.next();

						TreeNode tp = new TreeNode(exp);

						for (Iterator<TreeTuple> it3 = ppc.getTrials(database.id, exp.id).iterator(); it3.hasNext();) {
							TreeTuple trial = it3.next();
							TreeNode to = new TreeNode(trial);
							tp.addChild(to);
						}
						root.addChild(tp);
					}
					invisibleRoot.addChild(root);

				}
			return true;
		 }
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_FOLDER;

			if (((TreeNode) obj).tt.level==Level.TRIAL){//.getUserObject() instanceof Trial) {
				imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	class NameSorter extends ViewerSorter {
	}


	/**
	 * The constructor.
	 */
	public PerfDMFView() {
		ppc=new ParaProfController();

		PerfDMFUIPlugin.registerPerfDMFView(this);

		databases=ppc.getDatabases();

	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				PerfDMFView.this.fillContextMenu(manager);
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
		//manager.add(action1);  //TODO: Add an 'upload external performance data' option
		manager.add(new Separator());
		manager.add(refreshAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(paraprofAction);
		manager.add(refreshAction);
		// manager.add(action1);// //TODO: Add an 'upload external performance data' option
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(switchDatabaseAction);
		manager.add(launchparaprofAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private boolean openInParaProf(TreeTuple trial) {

		if(trial.level==Level.TRIAL)
			ppc.openTrial(trial.dbid, trial.id);
		return true;
	}

	private void makeActions() {
		/*action1 = new Action() {
            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("sampleview.views.SampleView");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                //addProfile("This Project", "/tmp/profiles");

            }
        };
        action1.setText("Do something cool!");
        action1.setToolTipText("Action 1 tooltip");
        action1.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/refresh.gif"));*/   //TODO: Add an 'upload external performance data' option

		refreshAction = new Action() {
			public void run() {
				((ViewContentProvider) viewer.getContentProvider()).refresh(viewer);
			}
		};
		refreshAction.setText(Messages.PerfDMFView_Refresh);
		refreshAction.setToolTipText(Messages.PerfDMFView_RefreshData);
		refreshAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/refresh.gif")); //$NON-NLS-1$ //$NON-NLS-2$

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				TreeNode to = (TreeNode) obj;

				if (to.tt != null) {
					openInParaProf(to.tt);
				}
				//showMessage("Double-click detected on " + obj.toString());
			}
		};

		switchDatabaseAction=new Action(){


			public void run(){
				ListDialog dblist = new ListDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
				ArrayContentProvider dbs=new ArrayContentProvider();
				LabelProvider dl=new LabelProvider();

				String[] names=getDatabaseNames();

				dblist.setHelpAvailable(false);
				dblist.setContentProvider(dbs);
				dblist.setLabelProvider(dl);
				dblist.setTitle(Messages.PerfDMFView_SelectDatabase);
				dblist.setInput(names);
				dblist.open();

				Object[] result = dblist.getResult();

				if(result!=null&&result.length>=1)
				{
					//databaseName=extractDatabaseName(result[0].toString());
					database=getDatabase(result[0].toString());
					((ViewContentProvider) viewer.getContentProvider()).refresh(viewer);
				}
			}

		};
		switchDatabaseAction.setText(Messages.PerfDMFView_UsingDatabase+database.name);
		switchDatabaseAction.setToolTipText(Messages.PerfDMFView_SelectOtherDatabase);

		launchparaprofAction = new Action() {
			public void run() {
				ppc.openManager();
			}
		};
		launchparaprofAction.setText(Messages.PerfDMFView_LaunchParaProf);
		launchparaprofAction.setToolTipText(Messages.PerfDMFView_LaunchParaProf);
		launchparaprofAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/pp.gif")); //$NON-NLS-1$ //$NON-NLS-2$

		paraprofAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				TreeNode node = (TreeNode) obj;
				//Trial trial = (Trial) node.getUserObject();
				openInParaProf(node.tt);
			}
		};
		paraprofAction.setText(Messages.PerfDMFView_OpenInParaProf);
		paraprofAction.setToolTipText(Messages.PerfDMFView_OpenInParaProf);
		paraprofAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("PDMA", "icons/pp.gif")); //$NON-NLS-1$ //$NON-NLS-2$

	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	/*
    private void showMessage(String message) {
        MessageDialog.openInformation(viewer.getControl().getShell(), "Performance Data View", message);
    }*/

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}


	public boolean showProfile(String project, String projectType, String trialName){//,String dbname

		ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();

		// reloads the tree
		vcp.refresh(viewer);

		Object[] objs;
		objs = vcp.getChildren(vcp.getRoot());

		for (int i = 0; i < objs.length; i++) {
			TreeNode node = (TreeNode) objs[i];
			if (node.tt.name.equals(project)) {
				viewer.setExpandedState(node, true);
				Object[] expObjs = node.getChildren();
				for (int j = 0; j < expObjs.length; j++) {
					TreeNode expNode = (TreeNode) expObjs[j];
					if ( expNode.getName().equals(projectType)) {
						viewer.setExpandedState(expNode, true);

						Object[] trialObjs = expNode.getChildren();
						for (int k = 0; k < trialObjs.length; k++) {
							TreeNode trialNode = (TreeNode) trialObjs[k];
							if ( trialNode.getName().equals(trialName)) {
								StructuredSelection selection = new StructuredSelection(trialNode);
								viewer.setSelection(selection);
							}
						}
					}
				}
			}
		}

		return true;
	}

	public boolean addProfile(String project, String projectType, String trialName, String directory, String dbname) {
		TreeTuple database = getDatabase(dbname);
		if(database==null)
			return false;

		ppc.uploadTrial(directory, database.id, project, projectType, trialName);

		showProfile(project, projectType, trialName);

		return true;
	}
}