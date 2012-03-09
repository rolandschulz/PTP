=====================================================
ENVIRONMENT MANAGEMENT SYSTEM SUPPORT FOR ECLIPSE PTP
=====================================================

The org.eclipse.ptp.ems.core and org.eclipse.ptp.ems.ui plug-ins provide infrastructure which allows the user to interact with an
environment management system, such as Modules or SoftEnv, on a remote machine.

The org.eclipse.ptp.ems.core plug-in provides the following API:

    1. An interface, org.eclipse.ptp.ems.core.IEnvManager, which allows clients to interact with a remote machine's environment
       management system.

    2. A class, EnvManagerRegistry, which is used to acquire an IEnvManager object for a particular machine.  The EnvManagerRegistry
       is responsible for auto-detecting the remote machine's environment management system.

    3. An extension point, org.eclipse.ptp.ems.core.envmanager, whose extensions determine what environment management systems are
       supported by PTP.  Extensions must implement the IEnvManager interface.

    4. An interface, IEnvManagerConfig, and two implementations, EnvMangerProjectProperties and EnvManagerConfigString, which
       allow a particular configuration of an environment management system to be persisted.

The org.eclipse.ptp.ems.ui plug-in provides the following API:

    1. A custom control, org.eclipse.ptp.ems.ui.EnvManagerConfigControl, which can be embedded into SWT/JFace dialogs in order to
       provide environment configuration.

    2. A more compact custom control, EnvManagerConfigButton, which consists of a "Configure..." button that, when clicked,
       displays an EnvManagerConfigControl in a dialog box.

--Jeff Overbey (February, 2012)