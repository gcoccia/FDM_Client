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
* Revision for this file: $Revision: 1.4 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/ModuleException.java,v $
*/
package org.iges.anagram;

/** Thrown when a module cannot complete the task it is attempting. */
public class ModuleException 
    extends AnagramException {
    
    /** Creates a ModuleException associated with the given module, 
     *  with the given message.
     */
    public ModuleException(Module module, String message) {
	super(message);
	this.module = module;
    }

    /** Creates a ModuleException associated with the given module, 
     *  with the given message and cause.
     */
    public ModuleException(Module module, String message, Throwable cause) {
	super(message, cause);
	this.module = module;
    }

    /** Creates a ModuleException associated with the given module, 
     *  with the given message, plus a different message that should
     *  be returned to the client. This can be used to avoid
     *  revealing sensitive information to the client, or to provide
     *  a detailed message for the client while including a shorter
     *  one in the log file.
     */
    public ModuleException(Module module, String clientMessage, 
			   String message) {
	super(message);
	this.clientMessage = clientMessage;
	this.module = module;
    }

    /** Returns the module that generated this exception */
    public Module getModule() {
	return module;
    }

    /** Returns the client message, if any. */
    public String getClientMessage() {
	return clientMessage;
    }

    protected String clientMessage;
    protected Module module;

}
