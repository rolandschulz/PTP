#!/bin/sh
#
# This script is used to build proxy plugins in the workspace
#
RMS="orte mpich2 ibm.pe ibm.ll"

KNOWN_PLUGINS="\
	org.eclipse.ptp.utils*\
	org.eclipse.ptp.proxy*\
	org.eclipse.ptp.debug.sdm*"
		
PARENT=`dirname $PWD`
PLUGIN=`basename $PWD`
PREFIX=$PARENT/$PLUGIN

if [ ! -d bin ]; then
	mkdir bin
fi

for rms in $RMS
do
	KNOWN_PLUGINS="$KNOWN_PLUGINS org.eclipse.ptp.rm.$rms.proxy*"
done

cd $PARENT

for plugin in `echo $KNOWN_PLUGINS`
do
	if [ -e $plugin/configure ]; then
		echo "Building $plugin..."
		(cd $plugin; \
		sh configure --prefix=$PREFIX && \
		chmod +x install-sh && \
		make install)
		echo; echo
	fi
done

exit 0

