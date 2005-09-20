#
# Copyright (c) 1996-2002 by Guardsoft Pty Ltd.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330,
# Boston, MA 02111-1307, USA.
#

Summary: libaif: Architecture Independent Format Library
Name: libaif
Version: 1.0.5
Release: 1
Copyright: GPL
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
