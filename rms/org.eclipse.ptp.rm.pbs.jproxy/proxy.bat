REM echo off
cd /d %~dp0

REM C:\Users\Dieter\Desktop\workspace.ptp.git\org.eclipse.ptp\rms\org.eclipse.ptp.rm.pbs.jproxy

java -cp ../org.eclipse.ptp.proxy.protocol/bin/;../org.eclipse.ptp.utils.core/bin/;../org.eclipse.ptp.rm.proxy.core/bin/;bin/ org.eclipse.ptp.rm.pbs.jproxy.PBSProxyRuntimeServer %1 %2 %3 %4 %5 %6 %7 %8 %9
REM java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp ../org.eclipse.ptp.proxy.protocol/bin/;bin/ org.eclipse.ptp.rm.pbs.jproxy.PBSProxyRuntimeServer %1 %2 %3 %4 %5 %6 %7 %8 %9
