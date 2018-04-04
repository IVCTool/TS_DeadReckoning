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

import org.slf4j.Logger;

/**
 * Implementation of dead reckoning algorithm 2
 *
 * @author rjjones3
 * @since 10/08/2016
 */
public class DeadReckon2 extends DeadReckon4
{
    /**
     * The constructor into which is passed a reference to the logger
     * 
     * @param logger
     *            The logger to use
     */
    public DeadReckon2(final Logger logger)
    {
        super(logger);
    }
    
    /**
     * Performs the dead reckoning computation for this algorithm based upon the
     * following inputs.
     * 
     * @param pZero
     *            The x y z position vector at Time 0
     * @param vZero
     *            The x y z velocity vector at Time 0
     * @param aZero
     *            The x y z acceleration vector at Time 0
     * @param eulerAngles
     *            The Phi Theta Psi euler angles vector at Time 0
     * @param bodyAxisVelocities
     *            The x y z rotational velocity vector at Time 0
     * @param deltaT
     *            Delta time in seconds to be applied to the dead reckoning
     *            algorithm
     */
    @Override
    public void deadReckon(final double[] pZero, final double[] vZero, final double[] aZero, final double[] eulerAngles,
            final double[] bodyAxisVelocities, final double deltaT)
    {
        super.deadReckon(pZero, vZero, DeadReckonCommon.nullVector(), 
                // This algorithm does not use acceleration
                DeadReckonCommon.nullVector(), // This algorithm does not use euler angles
                DeadReckonCommon.nullVector(), // This algorithm does not use body angle velocities
                deltaT);
    }
    
    /**
     * @return The Phi Theta Psi  values of the dead reckoned orientation, which are
     *         always [0,0,0] because this dead reckoning algorithm does not compute
     *         orientation
     */
    @Override
    public double[] getOrientation()
    {
        logger.warn("Algorithm 2 does not provide Orientation calculation.");
        return DeadReckonCommon.nullVector();
    }
    
    /**
     * @return False, indicating that this dead reckoning algorithm does not compute
     *         orientation
     */
    @Override
    public boolean isOrientationCalculated()
    {
        return false;
    }
    
}
