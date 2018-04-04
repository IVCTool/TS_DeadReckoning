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

import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;

/**
 * Library class for DeadReckon classes. Provides static methods.
 *
 * @author rjjones3
 * @since 16/08/2016
 */
public final class DeadReckonCommon
{
    /**
     * Constant value defining the number of elements in a vector
     */
    public static final int VECTOR_LENGTH = 3;
    
    /**
     * Private default constructor ensuring this class cannot be instantiated.
     */
    private DeadReckonCommon()
    {
        throw new RuntimeException();
    }
    
    /**
     * Convert the given vector to a SimpleMatrix. This input parameter must be a
     * single dimensional array of 3 elements in length, otherwise null is returned
     * 
     * @param vector
     *            A single dimensional array of double which must contain exactly 3
     *            elements.
     * @return The value as a matrix
     */
    public static SimpleMatrix vectorToSimpleMatrix(final double[] vector)
    {
        SimpleMatrix matrix;
        
        if (vector.length == VECTOR_LENGTH)
        {
            matrix = new SimpleMatrix(new double[][]
            {
                    {
                            vector[0], vector[1], vector[2]
                    }
            });
            
        }
        else
        {
            matrix = null;
        }
        
        return matrix;
    }
    
    /**
     * Create a skew matrix for the given body angular velocities.
     *
     * PRESUMES BODY AXIS VELOCITIES. Use eulerToBodyAxis() if the matrix is in the
     * incorrect form.
     * 
     * @param angularVelocity
     *            The angular velocity
     * @return The matrix skewed
     */
    public static SimpleMatrix computeSkewMatrix(final SimpleMatrix angularVelocity)
    {
        final double oX = angularVelocity.get(0);
        final double oY = angularVelocity.get(1);
        final double oZ = angularVelocity.get(2);
        
        SimpleMatrix skewMatrix = new SimpleMatrix(new double[][]
        {
                {
                        0.0, -oZ, oY
                }, // row1 0, -omegaZ, omegaY
                {
                        oZ, 0.0, -oX
                }, // row2 omegaZ, 0, -omegaX
                {
                        -oY, oX, 0.0
                }, // row3 -omegaY, omegaX, 0
        });
        return skewMatrix;
    }
    
    /**
     * Compute the omega T transposed matrix from the given body axis velocities.
     *
     * PRESUMES BODY AXIS VELOCITIES. Use eulerToBodyAxis() if the matrix is in the
     * incorrect form.
     * 
     * @param angularVelocity
     *            The angular velocity
     * @return The transposed matrix
     */
    public static SimpleMatrix computeOmegaOmegaTranspose(final SimpleMatrix angularVelocity)
    {
        final double oX = angularVelocity.get(0);
        final double oY = angularVelocity.get(1);
        final double oZ = angularVelocity.get(2);
        
        SimpleMatrix omegaomegaT = new SimpleMatrix(new double[][]
        {
                {
                        oX * oX, oX * oY, oX * oZ
                }, // row1
                {
                        oY * oX, oY * oY, oY * oZ
                }, // row2
                {
                        oZ * oX, oZ * oY, oZ * oZ
                } // row3
        });
        return omegaomegaT;
    }
    
    /**
     * Compute the magnitude of the given matrix. This matrix must contain a single
     * row having exactly 3 elements, otherwise NaN is returned
     * 
     * @param matrix
     *            A matrix which must contain a single row of exactly 3 elements.
     * @return The magnitude, or NaN if the input was not as required
     */
    public static double computeMagnitude(final SimpleMatrix matrix)
    {
        double magnitude;
        
        if (matrix.getNumElements() == VECTOR_LENGTH)
        {
            double a = matrix.get(0);
            double b = matrix.get(1);
            double c = matrix.get(2);
            magnitude = Math.sqrt((Math.pow(a, 2) + Math.pow(b, 2) + Math.pow(c, 2)));
        }
        else
        {
            magnitude = Double.NaN;
        }
        
        return magnitude;
    }
    
    /**
     * Compute the DR matrix pertaining to the body axis velocities matrix and delta
     * time passed in as the formal parameters
     *
     * PRESUMES BODY AXIS VELOCITIES. Use eulerToBodyAxis() if the matrix is in the
     * incorrect form.
     * 
     * @param angularVelocity
     *            The angular velocity
     * @param deltaT
     *            The delta time value
     * @return The DR matrix calculated from the angularVelocity and deltaT value
     */
    public static SimpleMatrix computeDRMatrix(final SimpleMatrix angularVelocity, final double deltaT)
    {
        /**
         * Determinant of the angular velocity vector, and a 3x3 identity matrix
         */
        final double magnitudeAngularVelocity = computeMagnitude(angularVelocity);
        final SimpleMatrix identity = SimpleMatrix.identity(3);
        
        /**
         * the Skew matrix
         */
        final SimpleMatrix skewMatrix = computeSkewMatrix(angularVelocity);
        
        /**
         * omega omegaT matrix
         */
        final SimpleMatrix omegaomegaT = computeOmegaOmegaTranspose(angularVelocity);
        
        /**
         * Calculate DR
         */
        double omegaomegaTscaleFactor = (1 - Math.cos(magnitudeAngularVelocity * deltaT))
                / (magnitudeAngularVelocity * magnitudeAngularVelocity);
        
        if (Double.isNaN(omegaomegaTscaleFactor))
        {
            omegaomegaTscaleFactor = 0.0;
        }
        
        SimpleMatrix scaleM1 = omegaomegaT.scale(omegaomegaTscaleFactor);
        SimpleMatrix scaleM2 = identity.scale(Math.cos(magnitudeAngularVelocity * deltaT));
        
        double skewMatrixScaleFactor = (Math.sin(magnitudeAngularVelocity) * deltaT) / magnitudeAngularVelocity;
        
        if (Double.isNaN(skewMatrixScaleFactor))
        {
            skewMatrixScaleFactor = 0.0;
        }
        
        final SimpleMatrix scaleM3 = skewMatrix.scale(skewMatrixScaleFactor);
        
        final SimpleMatrix DRMatrix = scaleM1.plus(scaleM2).minus(scaleM3);
        
        return DRMatrix;
    }
    
    /**
     * Compute the initial orientation based on the euler angles passed in as the
     * formal parameter
     * 
     * PRESUMES EULER ANGLES. Use bodyAxisToEuler() if the matrix is in the
     * incorrect form.
     * 
     * @param eulerAngles
     *            The euler angles
     * @return The initial orientation
     */
    public static SimpleMatrix computeInitialOrientation(final SimpleMatrix eulerAngles)
    {
        final double phi = eulerAngles.get(0);
        final double theta = eulerAngles.get(1);
        final double psi = eulerAngles.get(2);
        
        final SimpleMatrix initO = new SimpleMatrix(new double[][]
        {
                {
                        Math.cos(theta) * Math.cos(psi), Math.cos(theta) * Math.sin(psi), -Math.sin(theta)
                },
                
                {
                        (Math.sin(phi) * Math.sin(theta) * Math.cos(psi)) - (Math.cos(phi) * Math.sin(psi)),
                        (Math.sin(phi) * Math.sin(theta) * Math.sin(psi)) + (Math.cos(phi) * Math.cos(psi)),
                        Math.sin(phi) * Math.cos(theta)
                },
                
                {
                        (Math.cos(phi) * Math.sin(theta) * Math.cos(psi)) + (Math.sin(phi) * Math.sin(psi)),
                        (Math.cos(phi) * Math.sin(theta) * Math.sin(psi)) - (Math.sin(phi) * Math.cos(psi)),
                        Math.cos(phi) * Math.cos(theta)
                }
        });
        return initO;
    }
    
    /**
     * Recover the euler angles for the given final orientation a Time = 1
     * 
     * @param finalOrientation
     *            The finalOrientation matrix
     * @return The euler angles computed from the finalOrientation matrix
     */
    public static SimpleMatrix recoverEulerAngles(final SimpleMatrix finalOrientation)
    {
        /**
         * For an array of A11 through A33 ...
         */
        // top row
        final double A11 = finalOrientation.get(0, 0);
        final double A12 = finalOrientation.get(0, 1);
        final double A13 = finalOrientation.get(0, 2);
        
        // middle row
        final double A23 = finalOrientation.get(1, 2);
        
        // bottom row
        final double A33 = finalOrientation.get(2, 2);
        
        /**
         * Theta from ...
         */
        final double theta = Math.asin(-A13);
        /**
         * Psi from ...
         */
        double psi = Math.acos(A11 / Math.cos(theta)) * Math.signum(A12);
        
        /**
         * Phi from ...
         */
        double phi = Math.acos(A33 / Math.cos(theta)) * Math.signum(A23);
        
        // If theta is NOT in the range pi/2 > theta > -pi/2 and is equal to either
        // negative or positive pi/2 then zero phi and psi
        
        if (Double.isNaN(phi) || Double.isNaN(psi))
        {
            phi = 0;
            psi = 0;
        }
        
        SimpleMatrix euler = new SimpleMatrix(new double[][]
        {
                {
                        phi, theta, psi
                }
        });
        
        return euler;
    }
    
    /**
     * Computes the derivative v zero for the initial acceleration and velocity
     * values and the skew matrix passed in tas the formal parameters
     * 
     * @param aZero
     *            The initial acceleration vector
     * @param skewMatrix
     *            The skew matrix
     * @param vZero
     *            The initial velocity vector
     * @return The computed zero velocity
     */
    public static SimpleMatrix computeDerivativeVzero(final SimpleMatrix aZero, final SimpleMatrix skewMatrix,
            final SimpleMatrix vZero)
    {
        final SimpleMatrix vVector = vZero.transpose();
        final SimpleMatrix aVector = aZero.transpose();
        final SimpleMatrix ab = aVector.minus((skewMatrix.mult(vVector)));
        return ab;
    }
    
    /**
     * Computes the R1 value for the angular velocity and delta time values passed
     * in as the formal parameters
     * 
     * @param angularVelocity
     *            The angular velocity
     * @param deltaT
     *            The delta time in seconds
     * @return The R1 value
     */
    public static SimpleMatrix computeR1(final SimpleMatrix angularVelocity, final double deltaT)
    {
        double magAV = computeMagnitude(angularVelocity);
        SimpleMatrix omegaOmegaT = computeOmegaOmegaTranspose(angularVelocity);
        SimpleMatrix identity = SimpleMatrix.identity(3);
        SimpleMatrix skewMatrix = computeSkewMatrix(angularVelocity);
        
        double omegaOmegaTScale = ((magAV * deltaT) - Math.sin(magAV * deltaT)) / Math.pow(magAV, 3);
        if (Double.isNaN(omegaOmegaTScale))
        {
            omegaOmegaTScale = 0.0;
        }
        
        double identityScale = Math.sin(magAV * deltaT) / magAV;
        if (Double.isNaN(identityScale))
        {
            identityScale = 0.0;
        }
        
        double skewScale = (1 - Math.cos(magAV * deltaT)) / Math.pow(magAV, 2);
        if (Double.isNaN(skewScale))
        {
            skewScale = 0.0;
        }
        
        SimpleMatrix R1 = omegaOmegaT.scale(omegaOmegaTScale)
                .plus(identity.scale(identityScale).plus(skewMatrix.scale(skewScale)));
        return R1;
    }
    
    /**
     * Computes the R2 value for the angular velocity and delta time values passed
     * in as the formal parameters
     * 
     * @param angularVelocity
     *            The angular velocity
     * @param deltaT
     *            The delta time in seconds
     * @return The R2 value
     */
    public static SimpleMatrix computeR2(final SimpleMatrix angularVelocity, final double deltaT)
    {
        double magAV = computeMagnitude(angularVelocity);
        SimpleMatrix omegaOmegaT = computeOmegaOmegaTranspose(angularVelocity);
        SimpleMatrix identity = SimpleMatrix.identity(3);
        SimpleMatrix skewMatrix = computeSkewMatrix(angularVelocity);
        
        double omegaOmegaTScale = ((0.5 * Math.pow(magAV, 2) * Math.pow(deltaT, 2)) - Math.cos(magAV * deltaT)
                - (magAV * deltaT * Math.sin(magAV * deltaT)) + 1.0) / (Math.pow(magAV, 4));
        
        if (Double.isNaN(omegaOmegaTScale))
        {
            omegaOmegaTScale = 0.0;
        }
        
        double identityScale = ((Math.cos(magAV * deltaT) + (magAV * deltaT * Math.sin(magAV * deltaT))) - 1.0)
                / (Math.pow(magAV, 2));
        
        if (Double.isNaN(identityScale))
        {
            identityScale = 0.0;
        }
        
        double skewScale = (Math.sin(magAV * deltaT) - (magAV * deltaT * Math.cos(magAV * deltaT)))
                / (Math.pow(magAV, 3));
        
        if (Double.isNaN(skewScale))
        {
            skewScale = 0.0;
        }
        
        SimpleMatrix R2 = omegaOmegaT.scale(omegaOmegaTScale)
                .plus(identity.scale(identityScale).plus(skewMatrix.scale(skewScale)));
        return R2;
    }
    
    /**
     * Convenience method to create a vector of VECTOR_LENGTH elements containing
     * zeros
     * 
     * @return A vector of VECTOR_LENGTH elements containing zeros
     */
    public static final double[] nullVector()
    {
        return new double[VECTOR_LENGTH];
    }
    
    /**
     * Checks that the all arrays passed in as the formal parameter are
     * VECTOR_LENGTH in length
     * 
     * @param vectors
     *            The arrays to check for length
     * @return True of all arrays are VECTOR_LENGTH in length, otherwise false
     */
    public static final boolean checkSize(final double[]... vectors)
    {
        return vectors.length > 0 && Arrays.asList(vectors).stream().allMatch((v) -> v.length == VECTOR_LENGTH);
    }
}
