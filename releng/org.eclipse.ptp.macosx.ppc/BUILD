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

RMS="ibm.pe ibm.ll"

KNOWN_PLUGINS="\
	org.eclipse.ptp.utils$VERSION\
	org.eclipse.ptp.proxy$VERSION\
	org.eclipse.ptp.debug.sdm$VERSION"
		
PARENT=`dirname $PWD`
PLUGIN=`basename $PWD`
PREFIX=$PARENT/$PLUGIN

if [ ! -d bin ]; then
	mkdir bin
fi

for rms in $RMS
do
	KNOWN_PLUGINS="$KNOWN_PLUGINS org.eclipse.ptp.rm.$rms.proxy$VERSION"
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

