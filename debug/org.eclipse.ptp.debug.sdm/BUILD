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

CMD=configure
if [ $# -gt 0 ]; then
	CMD=$1
fi


cd `dirname $0`

if [ "$(basename `dirname $PWD`)" = "debug" ] ; then # building in GIT with subdirectories

    KNOWN_PLUGINS="\
	core/org.eclipse.ptp.utils$VERSION\
	core/org.eclipse.ptp.proxy$VERSION\
	debug/org.eclipse.ptp.debug.sdm$VERSION"
    PARENT=$(dirname `dirname $PWD`)

else

    KNOWN_PLUGINS="\
	org.eclipse.ptp.utils$VERSION\
	org.eclipse.ptp.proxy$VERSION\
	org.eclipse.ptp.debug.sdm$VERSION"
    PARENT=`dirname $PWD`

fi

	
if [ "$CMD" = "configure" ]; then
	UTILS_CONFIGURE_OPTIONS=""
	PROXY_CONFIGURE_OPTIONS=""
	SDM_CONFIGURE_OPTIONS=""

	set "$UTILS_CONFIGURE_OPTIONS"\
	    "$PROXY_CONFIGURE_OPTIONS"\
	    "$SDM_CONFIGURE_OPTIONS"
fi

PREFIX=$PWD

if [ ! -d bin ]; then
	mkdir bin
fi

cd $PARENT

for plugin in `echo $KNOWN_PLUGINS`
do
	case "$CMD" in
	configure)
		if [ -e $plugin/configure ]; then
			echo "Building $plugin..."
			(cd $plugin; \
			sh configure --prefix=$PREFIX $1 && \
			chmod +x install-sh && \
			make install)
			echo; echo
		fi
		shift
		;;
	build)
		if [ -e $plugin/Makefile ]; then
			(cd $plugin; make install)
		fi
		;;
	clean)
		if [ -e $plugin/Makefile ]; then
			(cd $plugin; make clean)
		fi
		;;
	esac
done

exit 0

