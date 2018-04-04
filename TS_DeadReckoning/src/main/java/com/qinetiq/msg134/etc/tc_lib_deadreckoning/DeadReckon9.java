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
 * Implementation of dead reckoning algorithm 9
 *
 * @author rjjones3
 * @since 10/08/2016
 */
public class DeadReckon9 extends AbstractDeadReckon
{
    
    /**
     * The constructor into which is passed a reference to the logger
     * 
     * @param logger
     *            The logger to use
     */
    public DeadReckon9(final Logger logger)
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
            SimpleMatrix azeroMatrix = DeadReckonCommon.vectorToSimpleMatrix(aZero);
            SimpleMatrix eulerAnglesMatrix = DeadReckonCommon.vectorToSimpleMatrix(eulerAngles);
            SimpleMatrix bodyAxisVelocityMatrix = DeadReckonCommon.vectorToSimpleMatrix(bodyAxisVelocity);
            
            SimpleMatrix skewMatrix = DeadReckonCommon.computeSkewMatrix(bodyAxisVelocityMatrix);
            SimpleMatrix Ab = DeadReckonCommon.computeDerivativeVzero(azeroMatrix, skewMatrix, vzeroMatrix);
            SimpleMatrix Rzero = DeadReckonCommon.computeInitialOrientation(eulerAnglesMatrix);
            SimpleMatrix RzeroTranspose = Rzero.transpose();
            SimpleMatrix R1 = DeadReckonCommon.computeR1(bodyAxisVelocityMatrix, deltaT);
            SimpleMatrix R2 = DeadReckonCommon.computeR2(bodyAxisVelocityMatrix, deltaT);
            SimpleMatrix PzeroT = pzeroMatrix.transpose();
            SimpleMatrix VzeroT = vzeroMatrix.transpose();
            
            /**
             * Positional dead reckoning
             */
            R1 = R1.mult(VzeroT);
            R2 = R2.mult(Ab);
            SimpleMatrix SumR = R1.plus(R2);
            SimpleMatrix SumRTimesRzeroTranspose = RzeroTranspose.mult(SumR);
            
            SimpleMatrix newPosition = SumRTimesRzeroTranspose.plus(PzeroT);
            pOne = newPosition.transpose();
        }
        else
        {
            logger.error("Incorrect input vector length passed into dead reckoning algorithm");
        }
    }
    
    /**
     * @return The Phi Theta Psi coordinates of the dead reckoned orientation, which
     *         are always [0,0,0] because this dead reckoning algorithm does not
     *         compute orientation
     */
    @Override
    public double[] getOrientation()
    {
        logger.warn("Algorithm 9 does not provide Orientation calculation.");
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