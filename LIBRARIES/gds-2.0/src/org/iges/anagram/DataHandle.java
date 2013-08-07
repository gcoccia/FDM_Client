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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/DataHandle.java,v $
*/
package org.iges.anagram;

import java.io.*;

/** An entry in the catalog representing a data object being served
 *  by the server  */
public class DataHandle 
    extends Handle {

    /** Creates a new DataHandle
     * @param completeName The complete name of this entry, starting 
     *      with "/"
     * @param description A concise description of this data object, to be
     *      used in directory listings and catalogs
     * @param toolInfo An object containing information needed 
     *  by the Tool module to access this data. The class of this 
     *  object up to the writer of the particular Anagram implementation. 
     *  However, it should implement the Serializable
     *  interface if possible. A non-Serializable toolInfo object will
     *  prevent the catalog from saving its entries to disk, thus
     *  forcing the server to re-import all data objects each time it
     *  is started.
     * @param createTime The official creation time for this dataset.
     */
    public DataHandle(String completeName, 
		      String description,
		      Object toolInfo,
		      long createTime) 
	throws AnagramException {

	super(completeName);
	this.description = description;
	this.toolInfo = toolInfo;
	this.createTime = createTime;
	this.available = true;
    }
	
	
    /** @return True if the data is currently in a usable state.
     */
    public boolean isAvailable() {
	return available;
    }
    
    /** @return A concise description of this data object */
    public String getDescription() {
	return description;
    }

    /** Changes the description of this data object. <i>This
     *  method should not be called without first obtaining
     *  an exclusive lock on this data handle.</i>
     */
    public void setDescription(String description) {
	this.description = description;
	this.createTime = System.currentTimeMillis();
    }

    /** @return The object containing information needed 
     *  by the Tool module to access this data.
     *  The class of this Object is determined by the Tool.
     */
    public Object getToolInfo() {
	return toolInfo;
    }

    /** Changes the tool info object. <i>This
     *  method should not be called without first obtaining
     *  an exclusive lock on this data handle.</i>
     */
    public void setToolInfo(Object toolInfo) {
	this.toolInfo = toolInfo;
	this.createTime = System.currentTimeMillis();
    }

    /** @return The time that this dataset was last modified.
     * This time will be updated every time the setDescription() or
     * setToolInfo() methods are called.
     */
    public long getCreateTime() {
	return createTime;
    }

    /** Set the availability of this dataset. The server will
     *  only attempt to access datasets that are flagged as
     *  available. <i>This
     *  method should not be called without first obtaining
     *  an exclusive lock on this data handle.</i>
     */
    public void setAvailable(boolean available) {
	this.available = available;
    }


    protected boolean available;
    protected Object toolInfo;
    protected String description;
    protected long createTime;
}
