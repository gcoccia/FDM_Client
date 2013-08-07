/* 
* Copyright (C) 2000-2008 Institute for Global Environment and Society /
*                        Center for Ocean-Land-Atmosphere Studies
* Author: Joe Wielgosz <joew@cola.iges.org>
* 
* This file is part of the Anagram Server Framework.
* 
* The Anagram Server Framework is free software; you can redistribute
* it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; either version
* 2, or (at your option) any later version.
* 
* The Anagram Server Framework is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with the Anagram Server Framework; see the file COPYRIGHT.  If
* not, write to the Free Software Foundation, Inc., 59 Temple Place -
* Suite 330, Boston, MA 02111-1307, USA.
* 
* You can contact IGES/COLA at 4041 Powder Mill Rd Ste 302, Calverton MD 20705.
* 
* Last modified: $Date: 2008/07/22 17:22:27 $ 
* Revision for this file: $Revision: 1.5 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/filter/DispatchFilter.java,v $
*/
package org.iges.anagram.filter;

import java.util.*;

import org.iges.anagram.*;
import org.iges.anagram.service.*;

/** Dispatches a client request to its designated service. This
 *  filter does not pass requests onwards.
 */

public class DispatchFilter 
    extends Filter {

    public String getFilterName() {
	return "dispatch";
    }

    protected void doFilter(ClientRequest clientRequest) 
	throws ModuleException {

	if (clientRequest.getService() == null || 
	    !clientRequest.getService().isEnabled()) {

	    throw new ModuleException(this, 
				      clientRequest.getServiceName() +
				      " is not an available service");
	}
	if (debug()) debug(clientRequest + "dispatching request");

	clientRequest.getService().handle(clientRequest);

	if (debug()) debug(clientRequest + "request handled succesfully");
    }
    
}
