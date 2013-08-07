* Script to unpack a UDF-encoded dataset
*
* Copyright (C) 2000-2004 Institute for Global Environment and Society /
*                        Center for Ocean-Land-Atmosphere Studies
* Author: Joe Wielgosz <joew@cola.iges.org>
* 
* This file is part of the GrADS Data Server.
* 
* The GrADS Data Server is free software; you can redistribute it 
* and/or modify it under the terms of the GNU General Public License 
* as published by the Free Software Foundation; either version 2, or (at your 
* option) any later version.
* 
* The GrADS Data Server is distributed in the hope that it will be useful, but 
* WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General 
* Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with the GrADS Data Server; see the file COPYRIGHT.  If not, write to 
* the Free Software Foundation, Inc., 59 Temple Place - Suite 330, 
* Boston, MA 02111-1307, USA.  
* 
* You can contact IGES/COLA at 4041 Powder Mill Rd Ste 302, Calverton MD 20705.
* 
*
* Last modified: $Date: 2008/04/16 16:57:29 $ 
* Revision for this file: $Revision: 1.5 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/scripts/udf.gs,v $

function read (args)

ctl_out = subwrd(args, 1)
bin_out = subwrd(args, 2)
udf_in = subwrd(args, 3)

* TBD open udf using new grads mods
* ??? new function call

* write CTL file
'q ctlinfo'

if (rc!=0)
  say "error: dummy CTL feature not supported by GrADS executable"
  'quit'
endif

rc = write(ctl_out, result)


* TBD write contents to binary file
* ??? something using fwrite -be


'quit'

