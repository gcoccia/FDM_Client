* Prints  a  listing  of  metadata  attributes  for  a  given  dataset
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
* Last modified: $Date: 2008/04/22 15:24:45 $ 
* Revision for this file: $Revision: 1.6 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/scripts/attributes.gs,v $

function read (args)

* Read parameters
outfile = subwrd(args, 1)
dataset = subwrd(args, 2)

* Open data file
gopen(dataset,1)

* check if Esize > 1
'q file'
sizes = sublin(result,5)
esize = subwrd(sizes,15)
if (esize > 1)
* write out ensemble attributes
  rc = write(outfile, "Ensemble Attributes:")
  'q ens_name'
  line = sublin(result,1)
  rc = write(outfile, line, append)
  'q ens_length'
  line = sublin(result,1)
  rc = write(outfile, line, append)
  'q ens_tinit'
  line = sublin(result,1)
  rc = write(outfile, line, append)
  rc = write(outfile, "", append)
endif

* write out descriptor and native attributes
'q attr'
rc = write(outfile, result, append)

'quit'
* end of script


function gopen(dataset,fnum)
'sdfopen 'dataset
'q file 'fnum; l1=sublin(result,1); w1=subwrd(l1,1)
if (w1="File"); say 'File 'fnum' opened with sdfopen'; return; endif

'xdfopen 'dataset
'q file 'fnum; l1=sublin(result,1); w1=subwrd(l1,1)
if (w1="File"); say 'File 'fnum' opened with xdfopen'; return; endif

'open 'dataset
'q file 'fnum; l1=sublin(result,1); w1=subwrd(l1,1)
if (w1="File"); say 'File 'fnum' opened with open'; return; endif

