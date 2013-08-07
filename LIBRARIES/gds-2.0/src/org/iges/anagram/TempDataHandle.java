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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/TempDataHandle.java,v $
*/
package org.iges.anagram;

import java.io.*;
import java.util.*;

/** A handle used by the catalog module to manage temporary data. */
public interface TempDataHandle
    extends Serializable {

    /** @return Returns handles for accessing the data. Multiple handles
     *          may be returned if the data can be accessed by multiple
     *          names (for instance, via an analysis expression or using
     *          a short name), or if a single operation generated multiple
     *          data objects
     */
    public DataHandle[] getDataHandles();

    /** @return The number of bytes of storage being used by this data.
     *          This is dependent on the storage format of the data and
     *          thus does not directly indicate the number of data values
     *          being stored. 
     */
    public long getStorageSize();

    /** @return The time at which the data was initially stored
     */
    public long getCreateTime();

    /** A set of data handle names upon which were used to generate this
     *  temporary data. The catalog module uses this to check if a 
     *  result is made out-of-date by the modification of another
     *  dataset.
     */
    public Set getDependencies();

    /** Deletes the temporary data. 
     */
    public void deleteStorage();

}
