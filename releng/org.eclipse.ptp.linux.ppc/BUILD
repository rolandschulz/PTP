#!/bin/sh
#
# This script is used to build proxy plugins in the workspace
#

# This should get replaced by build script with the actual
# PTP build version number. The script can also be run out of CVS,
# in which case there is no version.
VERSION="_@buildVersion@"
if [ `expr "$VERSION" : "_@[^@]*@"` -gt 0 ]; then
	VERSION=
fi

ACTIONS="configure install"
if [ $# -gt 0 ]; then
	ACTIONS="$*"
fi

KNOWN_PLUGINS="\
	org.eclipse.ptp.utils$VERSION\
	org.eclipse.ptp.proxy$VERSION\
	org.eclipse.ptp.debug.sdm$VERSION\
	org.eclipse.ptp.rm.ibm.ll.proxy$VERSION\
	org.eclipse.ptp.rm.ibm.pe.proxy$VERSION\
	org.eclipse.ptp.rm.pbs.proxy$VERSION\
	org.eclipse.ptp.rm.slurm.proxy$VERSION"
	
UTILS_CONFIGURE_OPTIONS=""
PROXY_CONFIGURE_OPTIONS=""
SDM_CONFIGURE_OPTIONS=""
IBM_LL_CONFIGURE_OPTIONS=""
IBM_PE_CONFIGURE_OPTIONS=""
PBS_CONFIGURE_OPTIONS="--with-pbs=/usr/local/torque"
SLURM_CONFIGURE_OPTIONS="--with-slurm=/usr/local/slurm"

set "$UTILS_CONFIGURE_OPTIONS"\
    "$PROXY_CONFIGURE_OPTIONS"\
    "$SDM_CONFIGURE_OPTIONS"\
    "$IBM_LL_CONFIGURE_OPTIONS"\
    "$IBM_PE_CONFIGURE_OPTIONS"\
    "$PBS_CONFIGURE_OPTIONS"\
    "$SLURM_CONFIGURE_OPTIONS"
		
PARENT=`dirname $PWD`
PLUGIN=`basename $PWD`
PREFIX=$PARENT/$PLUGIN

if [ ! -d bin ]; then
	mkdir bin
fi

cd $PARENT

# Unzip plugins first
for plugin in `echo $KNOWN_PLUGINS`
do
	if [ ! -d $plugin -a -e $plugin.jar ]; then
		echo "--- Creating $plugin..."
		mkdir $plugin
		(cd $plugin && unzip ../$plugin.jar)
		echo
	fi
done

# Now perform the actions
for action in $ACTIONS
do
	for plugin in `echo $KNOWN_PLUGINS`
	do
		case "$action" in
		configure)
			if [ -e $plugin/configure ]; then
				echo "--- Configuring $plugin..."
				(cd $plugin; \
				sh configure --prefix=$PREFIX $1 && \
				chmod +x install-sh && \
				make)
				echo
			fi
			shift
			;;
		install)
			if [ -e $plugin/Makefile ]; then
				echo "--- Installing $plugin..."
				(cd $plugin; make install)
				echo
			fi
			shift
			;;
		build)
			if [ -e $plugin/Makefile ]; then
				echo "--- Building $plugin..."
				(cd $plugin; make)
				echo
			fi
			;;
		clean)
			if [ -e $plugin/Makefile ]; then
				echo "--- Cleaning $plugin..."
				(cd $plugin; make clean)
				echo
			fi
			;;
		esac
	done
done

exit 0

