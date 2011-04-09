package org.eclipse.ptp.rm.jaxb.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTab;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IFireContentsChangedEnabled;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class JAXBControllerLaunchConfigurationTab extends ExtensibleJAXBControllerTab implements IFireContentsChangedEnabled {

	private final RemoteServicesDelegate delegate;
	private final IJAXBResourceManagerConfiguration rmConfig;
	private final LaunchTab launchTabData;
	private final Script script;
	private final ValueUpdateHandler updateHandler;
	private int saveIndex;

	private ScrolledComposite scrolledParent;
	private LCVariableMap lcMap;

	public JAXBControllerLaunchConfigurationTab(IJAXBResourceManager rm, ILaunchConfigurationDialog dialog) throws Throwable {
		super(dialog);
		rmConfig = rm.getJAXBConfiguration();
		rmConfig.setActive();
		script = rmConfig.getResourceManagerData().getControlData().getScript();
		launchTabData = rmConfig.getResourceManagerData().getControlData().getLaunchTab();
		delegate = rm.getControl().getRemoteServicesDelegate();
		updateHandler = new ValueUpdateHandler(this);
		int i = 0;
		if (launchTabData != null) {
			TabController controller = launchTabData.getBasic();
			if (controller != null) {
				addDynamicTab(new JAXBDynamicLaunchConfigurationTab(rm, dialog, controller, this, i++));
			}
			controller = launchTabData.getAdvanced();
			if (controller != null) {
				addDynamicTab(new JAXBDynamicLaunchConfigurationTab(rm, dialog, controller, this, i++));
			}
			String title = launchTabData.getCustomController();
			if (title != null) {
				addDynamicTab(new JAXBImportedScriptLaunchConfigurationTab(rm, dialog, title, this, i));
			}
		}
	}

	@Override
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		if (parent instanceof ScrolledComposite) {
			scrolledParent = (ScrolledComposite) parent;
		}
		super.createControl(parent, rm, queue);
	}

	@Override
	public void fireContentsChanged() {
		super.fireContentsChanged();
	}

	public RemoteServicesDelegate getDelegate() {
		return delegate;
	}

	public LaunchTab getLaunchTabData() {
		return launchTabData;
	}

	public LCVariableMap getLCMap() {
		return lcMap;
	}

	public IJAXBResourceManagerConfiguration getRmConfig() {
		return rmConfig;
	}

	public int getSaveIndex() {
		return saveIndex;
	}

	public Script getScript() {
		return script;
	}

	public ScrolledComposite getScrolledParent() {
		return scrolledParent;
	}

	public ValueUpdateHandler getUpdateHandler() {
		return updateHandler;
	}

	public boolean hasScript() {
		return script != null;
	}

	@Override
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		try {
			((IJAXBResourceManagerConfiguration) rm.getConfiguration()).setActive();
			lcMap = LCVariableMap.createInstance(RMVariableMap.getActiveInstance());
			LCVariableMap.setActiveInstance(lcMap);
			updateHandler.getControlToModelMap().clear();
		} catch (Throwable t) {
			JAXBUIPlugin.log(t);
			return new RMLaunchValidation(false, t.getMessage());
		}
		return super.initializeFrom(control, rm, queue, configuration);
	}

	public void resize(Control control) {
		if (scrolledParent != null) {
			scrolledParent.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
	}

	public void setSaveIndex(int saveIndex) {
		this.saveIndex = saveIndex;
	}
}
