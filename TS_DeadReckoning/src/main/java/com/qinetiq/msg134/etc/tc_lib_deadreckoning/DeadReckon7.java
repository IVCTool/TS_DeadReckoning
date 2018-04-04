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
 * Implementation of dead reckoning algorithm 7
 *
 * @author rjjones3
 * @since 10/08/2016
 */
public class DeadReckon7 extends AbstractDeadReckon
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
    public DeadReckon7(final Logger logger)
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
        if (DeadReckonCommon.checkSize(pZero, vZero, aZero, eulerAngles, bodyAxisVelocity))
        {
            SimpleMatrix pzeroMatrix = DeadReckonCommon.vectorToSimpleMatrix(pZero);
            SimpleMatrix vzeroMatrix = DeadReckonCommon.vectorToSimpleMatrix(vZero);
            SimpleMatrix eulerAnglesMatrix = DeadReckonCommon.vectorToSimpleMatrix(eulerAngles);
            SimpleMatrix bodyAxisVelocityMatrix = DeadReckonCommon.vectorToSimpleMatrix(bodyAxisVelocity);
            
            SimpleMatrix Rzero = DeadReckonCommon.computeInitialOrientation(eulerAnglesMatrix);
            SimpleMatrix RzeroTranspose = Rzero.transpose();
            SimpleMatrix R1 = DeadReckonCommon.computeR1(bodyAxisVelocityMatrix, deltaT);
            SimpleMatrix PzeroT = pzeroMatrix.transpose();
            SimpleMatrix VzeroT = vzeroMatrix.transpose();
            
            /**
             * Positional dead reckoning
             */
            R1 = R1.mult(VzeroT);
            SimpleMatrix R1TimesRzeroTranspose = RzeroTranspose.mult(R1);
            
            SimpleMatrix newPosition = R1TimesRzeroTranspose.plus(PzeroT);
            pOne = newPosition.transpose();
            
            /**
             * Orientation dead reckoning
             */
            SimpleMatrix DR = DeadReckonCommon.computeDRMatrix(bodyAxisVelocityMatrix, deltaT);
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
        final double[] orientation;
        if (newEulerAngles == null)
        {
            logger.error("New Orientation not calculated, call DeadReckon() first.");
            orientation = null;
        }
        else
        {
            if (newEulerAngles.getNumElements() != DeadReckonCommon.VECTOR_LENGTH)
            {
                logger.error("Incorrect matrix size, returning null.");
                orientation = null;
            }
            else
            {
                orientation = new double[]
                {
                        newEulerAngles.get(0), newEulerAngles.get(1), newEulerAngles.get(2)
                };
            }
        }
        
        return orientation;
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
