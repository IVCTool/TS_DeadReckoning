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

import org.ejml.simple.SimpleMatrix;
import org.slf4j.Logger;

/**
 * Abstract dead reckoning class to be extended by the concrete classes
 * providing the specific dead reckoning algorithms.
 *
 * @author QinetiQ
 */
public abstract class AbstractDeadReckon implements DeadReckoner
{
    /**
     * The logger
     */
    protected final Logger logger;
    
    /**
     * The matrix holding the calculated dead reckoned position vectors at Time 1
     */
    protected SimpleMatrix pOne;
    
    /**
     * The constructor into which is passed a reference to the logger
     * 
     * @param logger
     *            The logger to use
     */
    public AbstractDeadReckon(final Logger logger)
    {
        this.logger = logger;
    }
    
    /**
     * @return The x y z coordinates of the dead reckoned position
     */
    @Override
    public double[] getPosition()
    {
        final double[] position;
        
        if (pOne == null)
        {
            logger.error("Pone not calculated, call DeadReckon() first.");
            position = null;
        }
        else
        {
            if (pOne.getNumElements() == DeadReckonCommon.VECTOR_LENGTH)
            {
                position = new double[]
                {
                        pOne.get(0), // x
                        pOne.get(1), // y
                        pOne.get(2) // z
                };
                
            }
            else
            {
                logger.error("Incorrect matrix size, returning null.");
                position = null;
            }
        }
        
        return position;
    }

    /**
     * @return The simple class name of the dead reckoning algorithm implementation
     */
    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }
    
}
