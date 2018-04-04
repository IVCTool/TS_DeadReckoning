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
 * Implementation of dead reckoning algorithm 4
 *
 * @author rjjones3
 * @since 10/08/2016
 */
public class DeadReckon4 extends AbstractDeadReckon
{
    
    /**
     * The matrix holding the calculated dead reckoned orientation vectors at Time 1
     */
    private SimpleMatrix newEulerAngles = null;
    
    /**
     * The constructor into which is passed a reference to the logger
     * 
     * @param logger
     *            The logger to use
     */
    public DeadReckon4(final Logger logger)
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
     * @param bodyAxisVelocity
     *            The x y z rotational velocity vector at Time 0
     * @param deltaT
     *            Delta time in seconds to be applied to the dead reckoning
     *            algorithm
     */
    @Override
    public void deadReckon(final double[] pZero, final double[] vZero, final double[] aZero, final double[] eulerAngles,
            final double[] bodyAxisVelocity, final double deltaT)
    {
        /**
         * Check vector sizes
         */
        if (DeadReckonCommon.checkSize(pZero, vZero, aZero, eulerAngles, bodyAxisVelocity))
        {
            SimpleMatrix pzeroMatrix = DeadReckonCommon.vectorToSimpleMatrix(pZero);
            SimpleMatrix vzeroMatrix = DeadReckonCommon.vectorToSimpleMatrix(vZero);
            SimpleMatrix azeroMatrix = DeadReckonCommon.vectorToSimpleMatrix(aZero);
            SimpleMatrix eulerAnglesMatrix = DeadReckonCommon.vectorToSimpleMatrix(eulerAngles);
            SimpleMatrix bodyAxisVelocityMatrix = DeadReckonCommon.vectorToSimpleMatrix(bodyAxisVelocity);
            
            /**
             * Position
             */
            
            // add velocity * time to pzero
            pzeroMatrix = pzeroMatrix.plus(vzeroMatrix.scale(deltaT));
            
            // add 1/2 acceleration * time to pzero
            pzeroMatrix = pzeroMatrix.plus(azeroMatrix.scale(deltaT * deltaT * (0.5)));
            
            pOne = pzeroMatrix.copy();
            
            /**
             * Orientation
             */
            // calc DR
            SimpleMatrix DR = DeadReckonCommon.computeDRMatrix(bodyAxisVelocityMatrix, deltaT);
            // cal Rzero
            SimpleMatrix Rzero = DeadReckonCommon.computeInitialOrientation(eulerAnglesMatrix);
            // DR matrix mX with Rzero
            SimpleMatrix Rone = DR.mult(Rzero);
            // recover new euler angles from Rone
            SimpleMatrix RoneEulerAngles = DeadReckonCommon.recoverEulerAngles(Rone);
            
            newEulerAngles = RoneEulerAngles.copy();
        }
        else
        {
            logger.error("Incorrect input vector length passed into dead reckoning algorithm");
        }
    }
    
    /**
     * @return The Phi Theta Psi values of the dead reckoned orientation
     */
    @Override
    public double[] getOrientation()
    {
        if (newEulerAngles == null)
        {
            logger.error("New Orientation not calculated, call DeadReckon() first.");
            return null;
        }
        else
        {
            if (newEulerAngles.getNumElements() != DeadReckonCommon.VECTOR_LENGTH)
            {
                logger.error("Incorrect matrix size, returning null.");
                return null;
            }
            else
            {
                return new double[]
                {
                        newEulerAngles.get(0), newEulerAngles.get(1), newEulerAngles.get(2)
                };
            }
        }
    }
    
    /**
     * @return True, indicating that this dead reckoning algorithm does compute
     *         orientation
     */
    @Override
    public boolean isOrientationCalculated()
    {
        return true;
    }
}