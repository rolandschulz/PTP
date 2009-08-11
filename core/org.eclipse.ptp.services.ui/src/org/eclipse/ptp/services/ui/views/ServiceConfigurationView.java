package org.eclipse.ptp.services.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class ServiceConfigurationView extends ViewPart {

	private class ServiceConfigurationLabelProvider extends WorkbenchLabelProvider {
		
		private Font selectedFont;
		private Font unSelectedFont;
		
		public ServiceConfigurationLabelProvider(Font font) {
			unSelectedFont = font;
			FontData fd = font.getFontData()[0];
			FontData selectedFontData = new FontData(fd.getName(), fd.getHeight(), SWT.BOLD);
			selectedFont = (Font)new LocalResourceManager(JFaceResources.getResources()).get(FontDescriptor.createFrom(selectedFontData));
		}

		private IServiceConfiguration getServiceConfiguration(Object parentElement) {
			IServiceConfiguration conf = null;
			if (parentElement instanceof IAdaptable) {
				conf = (IServiceConfiguration) ((IAdaptable) parentElement).getAdapter(IServiceConfiguration.class);
			}
			return conf;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ui.model.WorkbenchLabelProvider#getFont(java.lang.Object)
		 */
		@Override
		public Font getFont(Object element) {
			IServiceConfiguration conf = getServiceConfiguration(element);
			if (conf != null) {// && conf == ServiceModelManager.getInstance().getSelected()) {
				return selectedFont;
			}
			return unSelectedFont;
		}
	}

	private TreeViewer viewer;

//	private RemoveResourceManagersAction removeResourceManagerAction;
//	private AddResourceManagerAction addResourceManagerAction;
//	private EditResourceManagerAction editResourceManagerAction;
//	private SelectDefaultResourceManagerAction selectResourceManagerAction;

	public ServiceConfigurationView() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI);
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new ServiceConfigurationLabelProvider(viewer.getTree().getFont()));

		viewer.setInput(ServiceModelManager.getInstance());

		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		createContextMenu();

		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		// IPropertySource, or support IPropertySource.class as an adapter type
		// in its AdapterFactory.
		// ----------------------------------------------------------------------
		getSite().setSelectionProvider(viewer);

		viewer.getTree().addMouseListener(new MouseAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				ITreeSelection selection = (ITreeSelection)viewer.getSelection();
				if (!selection.isEmpty()) {
					IServiceConfiguration rm = (IServiceConfiguration)selection.getFirstElement();
					//editResourceManagerAction.setResourceManager(rm);
					//editResourceManagerAction.run();
				}
			}

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent e) {
				ISelection selection = viewer.getSelection();
				TreeItem item = viewer.getTree().getItem(new Point(e.x, e.y));
				if (item == null && !selection.isEmpty()) {
					viewer.getTree().deselectAll();
				}
				else if (item != null) {
				}
			}
		});
	}

	public synchronized void dispose() {
		super.dispose();
	}

	public Font getFont() {
		if (viewer == null) {
			return null;
		}
		return viewer.getTree().getFont();
	}

	public void refreshViewer() {
		viewer.getControl().getDisplay().asyncExec(new Runnable(){
			public void run() {
				viewer.refresh();
			}
		});
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private void createContextMenu() {
		final Shell shell = getSite().getShell();
//		addResourceManagerAction = new AddResourceManagerAction(shell);
//		removeResourceManagerAction = new RemoveResourceManagersAction(shell);
//		editResourceManagerAction = new EditResourceManagerAction(shell);
//		selectResourceManagerAction = new SelectDefaultResourceManagerAction(this);

		MenuManager menuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
//		manager.add(addResourceManagerAction);
		Object[] selectedObjects = selection.toArray();
		boolean inContextForRM = selection.size() > 0;
		boolean inContextForEditRM = inContextForRM;
		boolean inContextForRemoveRM = inContextForRM;
		boolean inContextForSelectRM = inContextForRM;
//		for (int i = 0; i < selectedObjects.length; ++i) {
//			if (!(selectedObjects[i] instanceof IResourceManagerMenuContribution)) {
//				// Not all of the selected are RMs
//				inContextForRM = false;
//				inContextForEditRM = false;
//				inContextForRemoveRM = false;
//				inContextForSelectRM = false;
//				break;
//			}
//			else {
//                final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
//			    IResourceManagerControl rm = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
//			    if (rm.getState() != ResourceManagerAttributes.State.STOPPED) {
//			    	inContextForEditRM = false;
//			        inContextForRemoveRM = false;
//			    } else {
//			        inContextForSelectRM = false;
//			    }
//			}
//		}
//		manager.add(removeResourceManagerAction);
//		removeResourceManagerAction.setEnabled(inContextForRemoveRM);
//		if (inContextForRemoveRM) {
//			IResourceManagerControl[] rmManagers = new IResourceManagerControl[selection.size()];
//			for (int i = 0; i < rmManagers.length; ++i) {
//				final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[i];
//				rmManagers[i] = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
//			}
//			removeResourceManagerAction.setResourceManager(rmManagers);
//		}
//		manager.add(editResourceManagerAction);
//		editResourceManagerAction.setEnabled(inContextForEditRM);
//		if (inContextForEditRM) {
//			final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[0];
//			IResourceManagerControl rmManager = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
//			editResourceManagerAction.setResourceManager(rmManager);
//		}
//		manager.add(new Separator());
//		manager.add(selectResourceManagerAction);
//		selectResourceManagerAction.setEnabled(inContextForSelectRM);
//		if (inContextForSelectRM) {
//			final IResourceManagerMenuContribution menuContrib = (IResourceManagerMenuContribution) selectedObjects[0];
//			IResourceManagerControl rmManager = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);
//			selectResourceManagerAction.setResourceManager(rmManager);
//		}
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
	}

}
