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

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAoctet;
import hla.rti1516e.encoding.HLAvariantRecord;

/**
 * Spatial Variant Decoder Adapted from
 * https://github.com/openlvc/portico/pull/178
 * 
 * @author QinetiQ
 */
public class SpatialVariantDecoder
{
    private final EncoderFactory encoderFactory;
    private final HLAvariantRecord<HLAoctet> spatialVariantStruct;
    
    public SpatialVariantDecoder(final EncoderFactory encoderFactory)
    {
        this.encoderFactory = encoderFactory;
        
        // WorldLocationStruct
        final HLAfixedRecord worldLocationStruct = encoderFactory.createHLAfixedRecord();
        final HLAfloat64BE x = encoderFactory.createHLAfloat64BE();
        final HLAfloat64BE y = encoderFactory.createHLAfloat64BE();
        final HLAfloat64BE z = encoderFactory.createHLAfloat64BE();
        worldLocationStruct.add(x);
        worldLocationStruct.add(y);
        worldLocationStruct.add(z);
        
        // isFrozen
        final HLAoctet isFrozen = encoderFactory.createHLAoctet();
        
        // OrientationStruct
        final HLAfixedRecord orientationStruct = encoderFactory.createHLAfixedRecord();
        final HLAfloat32BE psi = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE theta = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE phi = encoderFactory.createHLAfloat32BE();
        orientationStruct.add(psi);
        orientationStruct.add(theta);
        orientationStruct.add(phi);
        
        // VelocityVectorStruct
        final HLAfixedRecord yelocityVectorStruct = encoderFactory.createHLAfixedRecord();
        final HLAfloat32BE xVelocity = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE yVelocity = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE zVelocity = encoderFactory.createHLAfloat32BE();
        yelocityVectorStruct.add(xVelocity);
        yelocityVectorStruct.add(yVelocity);
        yelocityVectorStruct.add(zVelocity);
        
        // AngularVelocityVectorStruct
        final HLAfixedRecord angularVelocityVectorStruct = encoderFactory.createHLAfixedRecord();
        final HLAfloat32BE xAngularVelocity = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE yAngularVelocity = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE zAngularVelocity = encoderFactory.createHLAfloat32BE();
        angularVelocityVectorStruct.add(xAngularVelocity);
        angularVelocityVectorStruct.add(yAngularVelocity);
        angularVelocityVectorStruct.add(zAngularVelocity);
        
        // AccelerationVectorStruct
        final HLAfixedRecord accelerationVectorStruct = encoderFactory.createHLAfixedRecord();
        final HLAfloat32BE xAcceleration = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE yAcceleration = encoderFactory.createHLAfloat32BE();
        final HLAfloat32BE zAcceleration = encoderFactory.createHLAfloat32BE();
        accelerationVectorStruct.add(xAcceleration);
        accelerationVectorStruct.add(yAcceleration);
        accelerationVectorStruct.add(zAcceleration);
        
        // SpatialOtherStruct DRM#0: None
        final HLAfixedRecord spatialOtherStruct = encoderFactory.createHLAfixedRecord();
        spatialOtherStruct.add(worldLocationStruct);
        
        // SpatialStaticStruct DRM#1: Static
        final HLAfixedRecord spatialStaticStruct = encoderFactory.createHLAfixedRecord();
        spatialStaticStruct.add(worldLocationStruct);
        spatialStaticStruct.add(isFrozen);
        spatialStaticStruct.add(orientationStruct);
        
        // SpatialFPStruct DRM#2: DRM_FPW
        final HLAfixedRecord spatialFPStruct = encoderFactory.createHLAfixedRecord();
        spatialFPStruct.add(worldLocationStruct);
        spatialFPStruct.add(isFrozen);
        spatialFPStruct.add(orientationStruct);
        spatialFPStruct.add(yelocityVectorStruct);
        
        // SpatialRPStruct DRM#3: DRM_RPW
        final HLAfixedRecord spatialRPStruct = encoderFactory.createHLAfixedRecord();
        spatialRPStruct.add(worldLocationStruct);
        spatialRPStruct.add(isFrozen);
        spatialRPStruct.add(orientationStruct);
        spatialRPStruct.add(yelocityVectorStruct);
        spatialRPStruct.add(angularVelocityVectorStruct);
        
        // SpatialRVStruct DRM#4: DRM_RVW
        final HLAfixedRecord spatialRVStruct = encoderFactory.createHLAfixedRecord();
        spatialRVStruct.add(worldLocationStruct);
        spatialRVStruct.add(isFrozen);
        spatialRVStruct.add(orientationStruct);
        spatialRVStruct.add(yelocityVectorStruct);
        spatialRVStruct.add(accelerationVectorStruct);
        spatialRVStruct.add(angularVelocityVectorStruct);
        
        // SpatialFVStruct DRM#5: DRM_FVW
        final HLAfixedRecord spatialFVStruct = encoderFactory.createHLAfixedRecord();
        spatialFVStruct.add(worldLocationStruct);
        spatialFVStruct.add(isFrozen);
        spatialFVStruct.add(orientationStruct);
        spatialFVStruct.add(yelocityVectorStruct);
        spatialFVStruct.add(accelerationVectorStruct);
        
        // DRM#6: DRM_FPB, same as SpatialFPStruct
        // DRM#7: DRM_RPB, same as SpatialRPStruct
        // DRM#8: DRM_RVB, same as SpatialRVStruct
        // DRM#9: DRM_FVB, same as SpatialFVStruct
        // SpatialVariantStruct Discriminators
        final HLAoctet[] discriminatorArray = new HLAoctet[10];
        for (int i = 0; i < discriminatorArray.length; i++)
        {
            discriminatorArray[i] = encoderFactory.createHLAoctet();
            discriminatorArray[i].setValue((byte) i);
        }
        
        // SpatialVariantStruct
        final HLAoctet discriminator = encoderFactory.createHLAoctet();
        spatialVariantStruct = encoderFactory.createHLAvariantRecord(discriminator);
        spatialVariantStruct.setVariant(discriminatorArray[0], spatialOtherStruct);
        spatialVariantStruct.setVariant(discriminatorArray[1], spatialStaticStruct);
        spatialVariantStruct.setVariant(discriminatorArray[2], spatialFPStruct);
        spatialVariantStruct.setVariant(discriminatorArray[3], spatialRPStruct);
        spatialVariantStruct.setVariant(discriminatorArray[4], spatialRVStruct);
        spatialVariantStruct.setVariant(discriminatorArray[5], spatialFVStruct);
        spatialVariantStruct.setVariant(discriminatorArray[6], spatialFPStruct);
        spatialVariantStruct.setVariant(discriminatorArray[7], spatialRPStruct);
        spatialVariantStruct.setVariant(discriminatorArray[8], spatialRVStruct);
        spatialVariantStruct.setVariant(discriminatorArray[9], spatialFVStruct);
    }
    
    public SpatialRecord decode(final byte[] bytes) throws DecoderException
    {
        spatialVariantStruct.decode(bytes);
        HLAoctet discriminant = spatialVariantStruct.getDiscriminant();
        HLAfixedRecord spatialFPStruct = copyFixedRecord((HLAfixedRecord) spatialVariantStruct.getValue());
        return new SpatialRecord(discriminant, spatialFPStruct);
    }
    
    /**
     * Provide a straight copy of the HLAfixedRecord
     * 
     * @param record
     *            The HLAfixedRecord to copy
     * @return The copy of the HLAfixedRecord
     */
    private HLAfixedRecord copyFixedRecord(final HLAfixedRecord record)
    {
        HLAfixedRecord copy = encoderFactory.createHLAfixedRecord();
        record.forEach((d) -> copy.add(d));
        return copy;
    }
    
}
