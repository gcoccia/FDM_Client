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
* Last modified: $Date: 2008/07/22 17:22:26 $ 
* Revision for this file: $Revision: 1.4 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/ConfigException.java,v $
*/
package org.iges.anagram;

/** Thrown when a module is unable to compensate for 
 *  invalid or missing settings during configuration.
 *  Throwing this method from Module.configure() will cause
 *  server startup or reconfiguration to fail. 
 */
public class ConfigException 
    extends ModuleException {

    /** Creates a ConfigException associated with the given Module,
     *  with the given message. */
    public ConfigException(Module module, String message) {
	super(module, message);
    }

    /** Creates a ConfigException associated with the given Module
     *  and Setting, with the given message. */
    public ConfigException(Module module, String message, Setting setting) {
	super(module, message);
	this.setting = setting;
    }

    public String getMessage() {
	if (setting == null) {
	    return super.getMessage();
	} else {
	    return super.getMessage() + "\ntag: " + setting;
	}
    }

    protected Setting setting;

}
