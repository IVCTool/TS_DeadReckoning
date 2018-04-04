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

/**
 * Defines the interface for the classes to implement the dead reckoning algorithms
 * @author QinetiQ
 */
public interface DeadReckoner
{
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
    void deadReckon(double[] pZero, double[] vZero, double[] aZero, double[] eulerAngles, double[] bodyAxisVelocity,
            double deltaT);
    
    /**
     * @return The x y z coordinates of the dead reckoned position
     */
    double[] getPosition();
    
    /**
     * @return The Phi Theta Psi values of the dead reckoned orientation
     */
    double[] getOrientation();
    
    /**
     * Determines whether the concrete dead reckon class computes orientation
     * 
     * @return True if orientation is supported, otherwise false
     */
    boolean isOrientationCalculated();

}
