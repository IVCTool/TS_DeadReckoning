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

import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAfloat32LE;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAfloat64LE;
import hla.rti1516e.encoding.HLAoctet;

/**
 * Encapsulates a decoded spatial record and the discriminant received from a
 * particular object.
 * 
 * @author QinetiQ
 */
public class SpatialRecord
{
    /**
     * The discriminant
     */
    private final HLAoctet discriminant;
    
    /**
     * The spatial data
     */
    private final HLAfixedRecord spatial;
    
    /**
     * The constructor to create this record based upon the discriminant and spatial
     * record provided as the formal parameters
     * 
     * @param discriminant
     *            The discriminant
     * @param spatial
     *            The decoded spatial data
     */
    public SpatialRecord(final HLAoctet discriminant, final HLAfixedRecord spatial)
    {
        super();
        this.discriminant = discriminant;
        this.spatial = spatial;
    }
    
    /**
     * @return The discriminant
     */
    public int getDiscriminant()
    {
        return discriminant.getValue();
    }
    
    /**
     * @return The spatial data
     */
    public HLAfixedRecord getSpatial()
    {
        return spatial;
    }
    
    /**
     * Extracts and returns the x y z world position from the spatial data
     * 
     * @return The x y z world position
     * @throws DecoderException
     *             If the data could not be decoded
     */
    public double[] getPosition() throws DecoderException
    {
        return getXYZVector(0);
    }
    
    /**
     * Determines whether or not this object is frozen depending upon the relevant
     * flag in the spatial data
     * 
     * @return True if the object is marked as frozen, otherwise false
     */
    public boolean isFrozen()
    {
        return ((HLAoctet) spatial.get(1)).getValue() == 1;
    }
    
    /**
     * Extracts and returns the Phi Theta PSI orientation from the spatial data
     * 
     * @return The Phi Theta Psi orientation
     * @throws DecoderException
     *             If there was a problem decoding the data
     */
    public double[] getOrientation() throws DecoderException
    {
        final HLAfixedRecord orientation = (HLAfixedRecord) spatial.get(2);
        
        // Orientation
        // Reverse psi theta phi order to work with DR algorithms
        // to clarify - FOM has this order Psi, Theta, Phi we're working with PHI,
        // THETA, PSI
        // Values are RADIANS
        return new double[]
        {
                getDoubleValue(orientation.get(2)), // phi
                getDoubleValue(orientation.get(1)), // theta
                getDoubleValue(orientation.get(0))  // psi
        };
    }
    
    /**
     * Extracts and returns the x y z velocity vector from the spatial data
     * 
     * @return The x y z velocity vector
     * @throws DecoderException
     *             If there was a problem decoding the data
     */
    public double[] getVelocity() throws DecoderException
    {
        return getXYZVector(3);
    }
    
    /**
     * Extracts and returns the x y z acceleration vector from the spatial data
     * 
     * @return The x y z acceleration vector
     * @throws DecoderException
     *             If there was a problem decoding the data
     */
    public double[] getAcceleration() throws DecoderException
    {
        double[] acceleration;
        
        switch (getDiscriminant())
        {
            case 4:
            case 5:
            case 8:
            case 9:
                acceleration = getXYZVector(4);
                break;
            
            default:
                acceleration = DeadReckonCommon.nullVector();
        }
        
        return acceleration;
    }
    
    /**
     * Extracts and returns the x y z angular velocity vector from the spatial data
     * 
     * @return The x y z angular velocity vector
     * @throws DecoderException
     *             If there was a problem decoding the data
     */
    public double[] getAngularVelocity() throws DecoderException
    {
        double[] angularVelocity;
        
        switch (getDiscriminant())
        {
            case 3:
            case 7:
                angularVelocity = getXYZVector(4);
                break;
            
            case 4:
            case 8:
                angularVelocity = getXYZVector(5);
                break;
            
            default:
                angularVelocity = DeadReckonCommon.nullVector();
        }
        
        return angularVelocity;
    }
    
    /**
     * Extracts the x y z data from the fixed record at the specified index
     * 
     * @param index
     *            The index of the fixed record from which to extract the data
     * @return x y z data from specified the fixed record
     * @throws DecoderException
     *             If there was a problem decoding the data
     */
    private double[] getXYZVector(final int index) throws DecoderException
    {
        final HLAfixedRecord vector = (HLAfixedRecord) spatial.get(index);
        
        // Vector
        return new double[]
        {
                getDoubleValue(vector.get(0)), // x component
                getDoubleValue(vector.get(1)), // y component
                getDoubleValue(vector.get(2))  // z component
        };
    }
    
    /**
     * Extracts a double value from the specified data element
     * 
     * @param dataElement
     *            The data element from which to extract the double value
     * @return The double value extracted
     * @throws DecoderException
     *             If there was a problem extracting the data
     */
    private double getDoubleValue(final DataElement dataElement) throws DecoderException
    {
        double val;
        if (dataElement instanceof HLAfloat32BE)
        {
            val = ((HLAfloat32BE) dataElement).getValue();
        }
        else if (dataElement instanceof HLAfloat64BE)
        {
            val = ((HLAfloat64BE) dataElement).getValue();
        }
        else if (dataElement instanceof HLAfloat32LE)
        {
            val = ((HLAfloat32LE) dataElement).getValue();
        }
        else if (dataElement instanceof HLAfloat64LE)
        {
            val = ((HLAfloat64LE) dataElement).getValue();
        }
        else
        {
            throw new DecoderException("Error decoding SpacialRecord");
        }
        return val;
    }
    
}
