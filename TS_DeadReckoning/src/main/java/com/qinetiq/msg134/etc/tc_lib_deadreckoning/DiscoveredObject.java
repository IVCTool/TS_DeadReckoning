/**
 * Copyright 2018, QinetiQ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qinetiq.msg134.etc.tc_lib_deadreckoning;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;

/**
 * Encapsulates the details of a discovered HLA object that has been received
 * from the RTI.
 * 
 * @author QinetiQ
 */
public class DiscoveredObject
{
    /**
     * The object class handle, as provided by the HLA RTI.
     */
    private final ObjectClassHandle theObjectClass;
    
    /**
     * The object instance handle, as provided by the HLA RTI.
     */
    private final ObjectInstanceHandle theObject;
    
    /**
     * The object name, as provided by the HLA RTI.
     */
    private final String objectName;
    
    /**
     * Creates an instance of this class given the following immutable values
     * @param theObject
     *            The object instance handle, as provided by the HLA RTI.
     *            
     * @param theObjectClass
     *            The object class handle, as provided by the HLA RTI.
     * @param objectName
     *            The object name, as provided by the HLA RTI.
     */
    public DiscoveredObject(final ObjectInstanceHandle theObject, final ObjectClassHandle theObjectClass,
            final String objectName)
    {
        super();
        this.theObject = theObject;
        this.theObjectClass = theObjectClass;
        this.objectName = objectName;
    }
    
    /**
     * @return The object class handle
     */
    public ObjectClassHandle getTheObjectClass()
    {
        return theObjectClass;
    }
    
    /**
     * @return The object instance handle
     */
    public ObjectInstanceHandle getTheObject()
    {
        return theObject;
    }
    
    /**
     * @return The object name
     */
    public String getObjectName()
    {
        return objectName;
    }
    
    @Override
    public String toString()
    {
        return String.join(" ", "Object:", objectName, "Handle:", theObject.toString(), "Class:",
                theObjectClass.toString());
    }
    
}
