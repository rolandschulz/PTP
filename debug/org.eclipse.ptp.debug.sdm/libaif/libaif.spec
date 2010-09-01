#
# Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

Summary: libaif: Architecture Independent Format Library
Name: libaif
Version: 1.0.5
Release: 1
Copyright: EPL
Group: System Environment/Libraries
Source: libaif-%{version}.tar.gz
Buildroot: /var/tmp/libaif

Packager: Greg Watson <greg@guardsoft.net>

%description
libaif is a libarary of architecture independent format routines. It 
is used primarily for transferring and manipulating data to/from 
different architectures.

This package contains a library and include files.

#--------------------------------------------------------------------------
%prep
%setup -q

%build
rm -rf $RPM_BUILD_ROOT
./configure --prefix=$RPM_BUILD_ROOT/usr
make

%install
make install

%clean
rm -rf $RPM_BUILD_ROOT

%post

%files
%defattr(-,root,root)
/usr/lib/libaif.a
/usr/include/aif.h
/usr/include/fds.h

%changelog
* Fri Nov 08 2002 Greg Watson <greg@guardsoft.net>
- Initial version
