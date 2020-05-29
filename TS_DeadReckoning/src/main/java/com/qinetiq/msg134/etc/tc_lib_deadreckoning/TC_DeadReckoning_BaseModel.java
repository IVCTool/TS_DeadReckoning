/**
 * Copyright 2018, QinetiQ
 * Copyright 2020, Thales Training & Simulation
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

import de.fraunhofer.iosb.tc_lib.IVCT_BaseModel;
import de.fraunhofer.iosb.tc_lib.IVCT_RTIambassador;
import de.fraunhofer.iosb.tc_lib.TcFailed;
import de.fraunhofer.iosb.tc_lib.TcInconclusive;
import hla.rti1516e.*;
import hla.rti1516e.encoding.*;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.CouldNotCreateLogicalTimeFactory;
import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.FederateNameAlreadyInUse;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.InvalidLocalSettingsDesignator;
import hla.rti1516e.exceptions.NameNotFound;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;
import hla.rti1516e.exceptions.UnsupportedCallbackModel;

import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Extension to TcBaseModel to overwrite default on object, attribute etc.
 * behaviour
 *
 * @author rjjones3
 * @author srhunt
 * @since 01/09/2016
 */
public class TC_DeadReckoning_BaseModel extends IVCT_BaseModel
{
    /**
     * The IVCT RTI ambassador
     */
    private final IVCT_RTIambassador ivct_rti;
    
    /**
     * The encoder factory
     */
    private final EncoderFactory encoderFactory;
    
    /**
     * The dead reckoning factory
     */
    private final DeadReckonFactory deadReckonFactory;
    
    /**
     * Text formatter for printing floating point numbers tot he log file
     */
    private static final DecimalFormat format = new DecimalFormat("#.###");
    
    /**
     * Discovered objects whereby the key is the object instance handle and the
     * value is the discovered object
     */
    private final Map<ObjectInstanceHandle, DiscoveredObject> discoveredObjects = new HashMap<>();
    
    /**
     * Spatial objects whereby the key is the object instance handle and the value
     * is the spatial object
     */
    private final Map<ObjectInstanceHandle, SpatialObject> spatialObjects = new HashMap<>();
    
    /**
     * The position threshold minimum value
     */
    final double positionThresholdMin;
    
    /**
     * The position threshold maximum value
     */
    final double positionThresholdMax;
    
    /**
     * The orientation threshold minimum value
     */
    final double orientationThresholdMax;
    
    /**
     * The orientation threshold maximum value
     */
    final double orientationThresholdMin;
    
    /**
     * Defines whether or not a time stamp is required from the SuT indicating the
     * instant at which the spatial data was valid. If a time stamp is required but
     * not received, the test will be inconclusive
     */
    final boolean timestampRequired;
    
    /**
     * Defines whether or not both the position and orientation (where appropriate)
     * are required for a test to pass. If true, then both position and orientation
     * must be within their respective threshold ranges for the test to pass,
     * otherwise one or the other will suffice
     */
    final boolean positionAndOrientationRequired;
    
    /**
     * BaseEntity Object Class Handle
     */
    private ObjectClassHandle baseEntityObjectClassHandle;
    
    /**
     * BaseEntity Attribute Set
     */
    private AttributeHandleSet baseEntityAttributeSet;
    
    /**
     * Spatial Attribute Handle
     */
    private AttributeHandle spatialAttributeHandle;
    
    /**
     * EntityID Attribute Handle
     */
    private AttributeHandle entityIDAttributeHandle;
    
    /**
     * A list of object IDs for which time stamp information was not received
     */
    private Set<String> objectsSendingNonTimestampedUpdates = new HashSet<>();
    
    /**
     * The logger. This is repeated here because there logger in the superclass
     * cannot be referenced herein because is is private. Although this therefore
     * hides the superclass's logger, it references the same instance.
     */
    private Logger logger;
    
    /**
     * @param logger
     *            reference to the logger
     * @param ivct_rti
     *            ivct rti
     * @param ivct_TcParam
     *            IVCT Test Case Parameters.
     * @param factory
     *            The dead reckoning factory
     */
    public TC_DeadReckoning_BaseModel(final Logger logger, final IVCT_RTIambassador ivct_rti,
            final TC_DeadReckoning_TcParam ivct_TcParam, final DeadReckonFactory factory)
    {
        super(ivct_rti, logger, ivct_TcParam);
        this.ivct_rti = ivct_rti;
        this.logger = logger;
        this.deadReckonFactory = factory;
        this.encoderFactory = ivct_rti.getEncoderFactory();
        
        this.positionThresholdMax = ivct_TcParam.getPositionThresholdMax();
        this.positionThresholdMin = ivct_TcParam.getPositionThresholdMin();
        this.orientationThresholdMax = ivct_TcParam.getOrientationThresholdMax();
        this.orientationThresholdMin = ivct_TcParam.getOrientationThresholdMin();
        
        this.timestampRequired = ivct_TcParam.isTimestampRequired();
        this.positionAndOrientationRequired = ivct_TcParam.isPositionAndOrientationRequired();
    }
    
    /**
     * Initialises the base model
     * 
     * @throws TcInconclusive
     *             If it fails to connect
     */
    public void init() throws TcInconclusive
    {
        objectsSendingNonTimestampedUpdates.clear();
        subscribe();
    }
    
    /**
     * Set up the necessary subscriptions
     * 
     * @throws TcInconclusive
     *             If there was a problem subscribing
     */
    public void subscribe() throws TcInconclusive
    {
        try
        {
            baseEntityObjectClassHandle = ivct_rti.getObjectClassHandle("BaseEntity");
            spatialAttributeHandle = ivct_rti.getAttributeHandle(baseEntityObjectClassHandle, "Spatial");
            entityIDAttributeHandle = ivct_rti.getAttributeHandle(baseEntityObjectClassHandle, "EntityIdentifier");
            baseEntityAttributeSet = ivct_rti.getAttributeHandleSetFactory().create();
            baseEntityAttributeSet.add(spatialAttributeHandle);
            baseEntityAttributeSet.add(entityIDAttributeHandle);
            
            // Subscribe to the required entity class and attributes
            ivct_rti.subscribeObjectClassAttributes(baseEntityObjectClassHandle, baseEntityAttributeSet);
            
        }
        catch (Exception e)
        {
            final String msg = "Exception occurred whilst subscribing";
            logger.error(msg, e);
            throw new TcInconclusive(msg, e);
        }
    }
    
    /**
     * Removes the subscriptions
     * 
     * @throws TcInconclusive
     *             If there was a problem unsubscribing
     */
    public void unsubscribe() throws TcInconclusive
    {
        try
        {
            ivct_rti.unsubscribeObjectClass(baseEntityObjectClassHandle);
        }
        catch (Exception e)
        {
            final String msg = "Exception occurred whilst unsubscribing";
            logger.error(msg, e);
            throw new TcInconclusive(msg, e);
        }
    }
    
    /**
     * Evaluate the spatial records against the specified dead reckoning algorithm.
     * This method takes a consecutive pair of spatial records received from a
     * particular object. The first record provides the initial position, velocity,
     * acceleration, orientation and rotational velocity spatial components, and the
     * dead reckoning algorithm to be used. This data, along with delta time between
     * the records, is fed into the specified dead reckoning algorithm to calculate
     * the new dead reckoned position and orientation (if the particular algorithm
     * calculates orientation). These calculated values are then compared against
     * the actual values provided by record1. If the calculated values fall within a
     * set of thresholds, the test is deemed to have passed.
     * 
     * @param objectId
     *            The object to which the spatial updates pertain
     * @param time0
     *            The time stamp of the initial spatial object of the pair
     * @param record0
     *            The initial spatial object of the pair
     * @param time1
     *            The time stamp of the second spatial object of the pair
     * @param record1
     *            The second spatial object of the pair
     * @return True if the position and/or orientation were within the specified
     *         thresholds
     */
    private boolean evaluateSpatialRecords(final String objectId, final Instant time0, final SpatialRecord record0,
            final Instant time1, final SpatialRecord record1) throws TcFailed
    {
        final boolean success;
        
        // The algorithm to use
        final int algo = record0.getDiscriminant();
        
        if (algo == 1)
        {
            logger.info(String.join(" ", "Skipping DR test for", objectId, "because it is using algorithm 1"));
            success = true;
        }
        else if (algo > 9)
        {
            logger.error(String.join(" ", "Invalid algorithm for", objectId, ".Algorithm=", String.valueOf(algo)));
            success = false;
        }
        else if (record1.isFrozen())
        {
            // If this spatial record indicates that the state is frozen, do not dead reckon
            logger.info(String.join(" ", "Skipping DR test for", objectId,
                    "because its spatial record is indicating that it is frozen"));
            success = true;
        }
        else
        {
            try
            {
                // Prepare the inputs to dead reckoning algorithm
                
                // World coordinates defining the initial position
                final double[] pZero = record0.getPosition();
                
                // Initial orientation
                final double[] oZero = record0.getOrientation();
                
                // initial velocity
                final double[] vZero = record0.getVelocity();
                
                // Initial acceleration
                final double[] aZero = record0.getAcceleration();
                
                // Initial angular velocity
                final double[] angularVzero = record0.getAngularVelocity();
                
                // Prepare new positions and orientations actualPone and actualOone
                
                // World
                final double[] actualPone = record1.getPosition();
                
                // Orientation
                final double[] actualOone = record1.getOrientation();
                
                logger.info(String.join(" ", "Calculating Dead Reckoned values for object", objectId, "using algorithm",
                        String.valueOf(algo)));
                
                // Log the DR inputs
                logger.info(String.join(" ", "Dead Reckoning inputs from spatial update at time=", time1.toString(),
                        "..."));
                logSpatialValues(pZero, oZero, vZero, aZero, angularVzero);
                
                // Select the relevant dead reckoning algorithm
                final DeadReckoner dr = deadReckonFactory.createDeadReckoner(algo, logger);
                
                // Perform dead reckoning
                Duration delta = Duration.between(time0, time1);
                double deltaSeconds = delta.getSeconds() + (delta.getNano() / 1_000_000_000.0);
                dr.deadReckon(pZero, vZero, aZero, oZero, angularVzero, deltaSeconds);
                
                // Extract the calculated position
                final double[] drPosition = dr.getPosition();
                
                // Extract the calculated orientation
                final double[] drOrientation = dr.getOrientation();
                
                // Log the DR outputs
                logger.info(
                        String.join("", "Dead Reckoned outputs for delta time=", format.format(deltaSeconds), "s..."));
                logger.info(String.join(" ", "Dead Reckoned Position:", formatPosition(drPosition)));
                logger.info(String.join(" ", "Dead Reckoned Orientation:", formatOrientation(drOrientation)));
                
                // Log the actuals
                logger.info(String.join(" ", "Actual spatial values from update at time=", time1.toString(), "..."));
                logSpatialValues(actualPone, actualOone, record1.getVelocity(), record1.getAcceleration(),
                        record1.getAngularVelocity());
                
                logDistance(deltaSeconds, pZero, actualPone);
                
                // Compare the calculated position against the values received
                logger.info(String.join(" ", "Evaluating Dead Reckoned position for object", objectId,
                        "using algorithm", String.valueOf(algo)));
                final boolean positionOk = isPositionWithinTolerance(drPosition, actualPone);
                
                // Compare the calculated orientation against the values received
                final boolean orientationOk;
                
                // If this algorithm does not calculate the orientation, then the orientation
                // test can be skipped
                if (dr.isOrientationCalculated())
                {
                    logger.info(String.join(" ", "Evaluating Dead Reckoned orientation for object", objectId,
                            "using algorithm", String.valueOf(algo)));
                    
                    orientationOk = isOrientationWithinTolerance(drOrientation, actualOone);
                }
                else
                {
                    logger.info(String.join(" ", "Skipping evaluation of Dead Reckoned orientation for object",
                            objectId, "because algorithm", String.valueOf(algo), "does not calculate orientation"));
                    
                    orientationOk = true;
                }
                
                // Does this test case is configured to require both position and orientation to
                // be within tolerance in order to pass?
                if (positionAndOrientationRequired)
                {
                    // Test passes only if both position and orientation passed
                    success = positionOk && orientationOk;
                }
                else
                {
                    // Test passes if position was within tolerance, or the orientation if
                    // calculation was necessary
                    success = positionOk || (dr.isOrientationCalculated() && orientationOk);
                }
                
            }
            catch (DecoderException e)
            {
                throw new TcFailed(e.getMessage());
            }
            
        }
        
        return success;
    }
    
    /**
     * Convenience method to print the distance between two positions to the logger
     * 
     * @param time
     *            The time between the respective updates
     * @param position1
     *            The initial position
     * @param position2
     *            The second position
     */
    private void logDistance(final double time, final double[] position1, final double[] position2)
    {
        final double dx = position2[0] - position1[0];
        final double dy = position2[1] - position1[1];
        final double dz = position2[2] - position1[2];
        
        // Magnitude of the distance
        final double distTravelled = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        logger.info(String.join(" ", "Distance travelled in", format.format(time), "seconds = ",
                format.format(distTravelled), "metres"));
    }
    
    /**
     * Convenience method to print the spatial components to the logger
     * 
     * @param position
     *            The position component
     * @param orientation
     *            The orientation component
     * @param velocity
     *            The orientation component
     * @param acceleration
     *            The acceleration component
     * @param angularVelocity
     *            The angular velocity component
     */
    private void logSpatialValues(final double[] position, final double[] orientation, final double[] velocity,
            final double[] acceleration, final double[] angularVelocity)
    {
        logger.info(String.join(" ", "Position:", formatPosition(position)));
        logger.info(String.join(" ", "Orientation:", formatOrientation(orientation)));
        logger.info(String.join(" ", "Velocity:", formatPosition(velocity)));
        logger.info(String.join(" ", "Acceleration:", formatPosition(acceleration)));
        logger.info(String.join(" ", "AngularVelocity:", formatPosition(angularVelocity)));
    }
    
    /**
     * Formats the position component for printing to the logger
     * 
     * @param position
     *            The x y z position to format
     * @return The formatted position
     */
    private String formatPosition(final double[] position)
    {
        return String.join("", "X=", format.format(position[0]), ",Y=", format.format(position[1]), ",Z=",
                format.format(position[2]));
    }
    
    /**
     * Formats the orientation component for printing to the logger
     * 
     * @param position
     *            The Phi Theta Psi orientation to format
     * @return The formatted orientation
     */
    private String formatOrientation(final double[] orientation)
    {
        return String.join("", "Phi=", format.format(orientation[0]), ",Theta=", format.format(orientation[1]), ",Psi=",
                format.format(orientation[2]));
    }
    
    /**
     * Determines whether or not sufficient data has been received. At least 2
     * spatial updates must have been received from at least 1 object.
     * 
     * @return True if sufficient data has been received, otherwise false.
     */
    public boolean isSuffientDataReceived()
    {
        return spatialObjects.values().stream().anyMatch(so -> so.getUpdatesCount() > 2);
    }
    
    /**
     * Performs the dead reckoning comparison between the received data and the
     * calculated data. If any pair of spatial updates were received that were
     * outside the specified tolerances, it is deemed a failure and a TcFailed
     * exception is thrown. If this returns without throwing the exception, all
     * tests are deemed to have passed.
     * 
     * @throws TcFailed
     *             If a failure occurs
     */
    public void deadReckonCompare() throws TcFailed
    {
        int successesTotal = 0;
        int failuresTotal = 0;
        
        for (Entry<ObjectInstanceHandle, SpatialObject> currObject : spatialObjects.entrySet())
        {
            final ObjectInstanceHandle objectHandle = currObject.getKey();
            final SpatialObject spacialObject = currObject.getValue();
            final String objectId = discoveredObjects.get(objectHandle).getObjectName();
            
            logger.info(String.join(" ", "Processing updates received from object", objectId));
            
            int successes = 0;
            int failures = 0;
            
            Entry<Instant, SpatialRecord> prevUpdate = null;
            for (Entry<Instant, SpatialRecord> currUpdate : spacialObject.getSortedSpacialRecords().entrySet())
            {
                if (prevUpdate != null)
                {
                    final Instant prevTime = prevUpdate.getKey();
                    final SpatialRecord prevSpatial = prevUpdate.getValue();
                    final Instant currTime = currUpdate.getKey();
                    final SpatialRecord currSpatial = currUpdate.getValue();
                    
                    boolean ok;
                    try
                    {
                        ok = evaluateSpatialRecords(objectId, prevTime, prevSpatial, currTime, currSpatial);
                    }
                    catch (TcFailed e)
                    {
                        // Test has failed, but continue anyway to accumulate logging data
                        ok = false;
                        logger.error(String.join(" ", "Exception occurred during Dead Reckoning calculation for object",
                                objectId));
                    }
                    
                    if (ok)
                    {
                        logger.info(String.join(" ", "Successful Dead Reckoning calculation for object", objectId));
                        successes++;
                    }
                    else
                    {
                        logger.error(String.join(" ", "Unsuccessful Dead Reckoning calculation for object", objectId));
                        failures++;
                    }
                }
                prevUpdate = currUpdate;
            }
            
            logger.info(String.join(" ", "Finished processing updates received from object", objectId));
            logSuccessRate(objectId, successes, failures);
            
            // Add to total
            successesTotal += successes;
            failuresTotal += failures;
        }
        
        logSuccessRate("Total", successesTotal, failuresTotal);
        
        if (failuresTotal > 0)
        {
            final String msg = String.join(" ", String.valueOf(failuresTotal), "Dead Reckoning calculations failed.");
            logger.error(msg);
            throw new TcFailed(msg);
        }
    }
    
    /**
     * Convenience method to print the success rate to the logger
     * 
     * @param id
     *            The id of the object whose success rate is to be logged
     * @param successes
     *            The number of successes
     * @param failures
     *            The number of failures
     */
    private void logSuccessRate(final String id, final int successes, final int failures)
    {
        logger.info(
                String.join("", id, " Successes=", String.valueOf(successes), " Failures=", String.valueOf(failures),
                        " Success rate=", format.format((double) successes / (successes + failures))));
    }
    
    /**
     * Determines whether the deviation between dead reckoned position and the
     * actual position are within the specified tolerance
     * 
     * @param drOne
     *            Dead reckoned position 'one'
     * @param rxOne
     *            Received position 'one'
     * 
     * 
     * @return True if the calculation was within tolerance, otherwise false
     */
    private boolean isPositionWithinTolerance(final double[] drOne, final double[] rxOne)
    {
        // The x y z deltas
        final double dx = drOne[0] - rxOne[0];
        final double dy = drOne[1] - rxOne[1];
        final double dz = drOne[2] - rxOne[2];
        
        // Get total magnitude of difference
        final double mag = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // Check whether or not the magnitude of difference is within the threshold
        final boolean success = mag >= positionThresholdMin && mag <= positionThresholdMax;
        
        logger.info(String.join(" ", "Position Dead Reckoning", success ? "passed" : "failed", "dx=", format.format(dx),
                "dy=", format.format(dy), "dz=", format.format(dz), "Deviation=", format.format(mag), "Min=",
                format.format(positionThresholdMin), "Max=", format.format(positionThresholdMax)));
        
        return success;
    }
    
    /**
     * Determines whether the deviation between dead reckoned orientation and the
     * actual orientation are within the specified tolerance
     * 
     * @param drOne
     *            Dead reckoned orientation 'one'
     * @param rxOne
     *            Received orientation 'one'
     * 
     * 
     * @return True if the calculation was within tolerance, otherwise false
     */
    private boolean isOrientationWithinTolerance(final double[] drOne, final double[] rxOne)
    {
        double dPhi = Math.abs(Math.atan2(Math.sin(drOne[0] - rxOne[0]), Math.cos(drOne[0] - rxOne[0])));
        double dTheta = Math.abs(Math.atan2(Math.sin(drOne[1] - rxOne[1]), Math.cos(drOne[1] - rxOne[1])));
        double dPsi = Math.abs(Math.atan2(Math.sin(drOne[2] - rxOne[2]), Math.cos(drOne[2] - rxOne[2])));
        
        // Get total magnitude of difference
        final double mag = Math.sqrt(dPhi * dPhi + dTheta * dTheta + dPsi * dPsi);
        
        final boolean success = mag >= orientationThresholdMin && mag <= orientationThresholdMax;
        
        logger.info(String.join(" ", "Orientation Dead Reckoning", success ? "passed" : "failed", "dPhi=",
                format.format(dPhi), "dTheta=", format.format(dTheta), "dPsi=", format.format(dPsi), "dMAG=",
                format.format(mag), "Min=", format.format(orientationThresholdMin), "Max=",
                format.format(orientationThresholdMax)));
        
        return success;
    }
    
    /**
     * Initiates the RTI and joins as a federate
     * 
     * @param tcParam
     *            Reference to the parameters object
     * @param federateReference
     *            Reference to the federate ambassador
     * @return The federate handle
     */
    public FederateHandle initiateRti(final TC_DeadReckoning_TcParam tcParam,
            final FederateAmbassador federateReference)
    {
        
        // Connect to rti
        try
        {
            ivct_rti.connect(federateReference, CallbackModel.HLA_IMMEDIATE, tcParam.getSettingsDesignator());
        }
        catch (AlreadyConnected e)
        {
            this.logger.warn("initiateRti: AlreadyConnected (ignored)");
        }
        catch (ConnectionFailed | InvalidLocalSettingsDesignator | UnsupportedCallbackModel
                | CallNotAllowedFromWithinCallback | RTIinternalError e)
        {
            this.logger.error("initiateRti: ", e);
            return null;
        }
        
        // Create federation execution using tc_param foms
        try
        {
            ivct_rti.createFederationExecution(tcParam.getFederationName(), tcParam.getUrls(), "HLAfloat64Time");
        }
        catch (CouldNotCreateLogicalTimeFactory | InconsistentFDD | ErrorReadingFDD | CouldNotOpenFDD | NotConnected
                | RTIinternalError e)
        {
            this.logger.error("initiateRti: ", e);
            return null;
        }
        
        // Join federation execution
        try
        {
            return ivct_rti.joinFederationExecution(tcParam.getTcFederateName(), tcParam.getSutFederateType(),
                    tcParam.getFederationName());
        }
        catch (CouldNotCreateLogicalTimeFactory | FederationExecutionDoesNotExist | SaveInProgress | RestoreInProgress
                | FederateAlreadyExecutionMember | NotConnected | CallNotAllowedFromWithinCallback | RTIinternalError
                | FederateNameAlreadyInUse e)
        {
            this.logger.error("initiateRti: ", e);
            return null;
        }
    }
    
    /**
     * Determines whether or not a federate with the name defined by sutFederateName
     * is connected to the federation
     * 
     * @param sutFederateName
     *            The name of the federate whose connection status is to be
     *            determined
     * 
     * @return True if the federate of the given name is connected, otherwise false
     * 
     * @throws TcInconclusive
     *             If something went wrong during the test
     */
    public boolean isFederateConnected(final String sutFederateName) throws TcInconclusive
    {
        boolean sutConnected;
        
        // Check if the SuT is already connected
        try
        {
            // If this does not throw an exception, it means that the SuT is present in the
            // federation
            ivct_rti.getFederateHandle(sutFederateName);
            sutConnected = true;
        }
        catch (NameNotFound e)
        {
            // If a NameNotFound or NotConnected exception is thrown it might just mean that
            // the SuT has not yet joined the federation
            logger.info("Unknown federate exception ignored");
            sutConnected = false;
        }
        catch (FederateNotExecutionMember | RTIinternalError | NotConnected e2)
        {
            // If any of these exceptions are thrown, there has been an error, so render the
            // test inconclusive
            String msg = String.join(" ", "Error occurred whilst trying to ascertain if SuT federate", sutFederateName,
                    "is connected to federation");
            logger.error(msg);
            throw new TcInconclusive(msg, e2);
        }
        return sutConnected;
    }
    
    /**
     * @return A list of any objects in the SuT that provided non-time stamped
     *         spatial updates
     */
    public Set<String> getObjectsSendingNonTimestampedUpdates()
    {
        return new HashSet<>(objectsSendingNonTimestampedUpdates);
    }
    
    /**
     * Determines the spatial attribute validity time stamp from the User Supplied
     * Tag TODO: IMPORTANT: THIS METHOD HAS NOT BEEN TESTED. THIS FACT MUST BE DULY
     * CAVEATED IN THE DESIGN CERTIFICATE AND / OR RELEASE NOTE
     * 
     * @param userSuppliedTag
     *            The user supplied tag to decode
     * @return The time stamp in microseconds
     */
    private long userSuppliedTagToMicros(byte[] userSuppliedTag)
    {
        // From 11S-SIW-049 Time Representation and Interpretation in Simulation
        // Interoperability
        long micros;
        
        try
        {
            // Sect 6.2 "Time in DIS"
            userSuppliedTag = Arrays.copyOf(userSuppliedTag, 4);
            micros = (long) ((ByteBuffer.wrap(userSuppliedTag).getInt() / 2) * 1.676);
        }
        catch (Exception e)
        {
            // Sect 6.3 "Time in GRIM-RPR"
            userSuppliedTag = Arrays.copyOf(userSuppliedTag, 8);
            String hexstring = new String(userSuppliedTag, 0, 8, StandardCharsets.US_ASCII);
            micros = Long.parseLong(hexstring, 16);
        }
        
        return micros;
    }
    
    @Override
    public void reflectAttributeValues(final ObjectInstanceHandle theObject,
            final AttributeHandleValueMap theAttributes, final byte[] userSuppliedTag, final OrderType sentOrdering,
            final TransportationTypeHandle theTransport, final FederateAmbassador.SupplementalReflectInfo reflectInfo)
    {
        // Grab the time now, i.e. as early as possible
        final Instant now = Instant.now();
        
        if (discoveredObjects.containsKey(theObject))
        {
            final DiscoveredObject discoveredObject = discoveredObjects.get(theObject);
            
            if (theAttributes.containsKey(spatialAttributeHandle))
            {
                SpatialObject spatialObject = spatialObjects.get(theObject);
                
                // If there's no existing entry for this object, add one
                if (spatialObject == null)
                {
                    spatialObject = new SpatialObject();
                    spatialObjects.put(theObject, spatialObject);
                }
                
                try
                {
                    // IMPORTANT: THE FUNCTIONALITY WITHIN THIS BLOCK HAS NOT BEN TESTED
                    // THIS MUST BE DULY CAVEATED WITHIN THE DESIGN CERTIFICATE AND /OR RELEASE NOTE
                    final SpatialVariantDecoder decoder = new SpatialVariantDecoder(encoderFactory);
                    final SpatialRecord spacialRecord = decoder.decode(theAttributes.get(spatialAttributeHandle));
                    
                    try
                    {
                        // Microseconds since the last hour
                        final long microsPastHour = userSuppliedTagToMicros(userSuppliedTag);
                        
                        // If this is the first update for this spatial object, assign the hour from now
                        if (spatialObject.isEmpty())
                        {
                            Instant time = now.with(LocalTime.now().withMinute(0).withSecond(0).withNano(0));
                            time = time.plusNanos(microsPastHour * 1000);
                            spatialObject.add(time, spacialRecord);
                        }
                        else
                        {
                            final Instant prevTime = spatialObject.getLastTime();
                            final long prevMicrosPastHour = TimeUnit.MINUTES
                                    .toMicros(prevTime.get(ChronoField.MINUTE_OF_HOUR))
                                    + TimeUnit.SECONDS.toMicros(prevTime.get(ChronoField.SECOND_OF_MINUTE))
                                    + prevTime.get(ChronoField.MICRO_OF_SECOND);
                            
                            Instant time = prevTime.minus(prevMicrosPastHour, ChronoUnit.MICROS).plus(microsPastHour,
                                    ChronoUnit.MICROS);
                            
                            // If the new microsPastHour is more than the previous one, add 1 to the hour
                            if (microsPastHour < prevMicrosPastHour)
                            {
                                time = time.plus(1, ChronoUnit.HOURS);
                            }
                            
                            spatialObject.add(time, spacialRecord);
                        }
                        
                    }
                    catch (Exception e)
                    {
                        if (timestampRequired)
                        {
                            objectsSendingNonTimestampedUpdates.add(discoveredObject.getObjectName());
                        }
                        
                        // Just use the receive time stamp
                        spatialObject.add(now, spacialRecord);
                        
                    }
                    
                }
                catch (final DecoderException e)
                {
                    logger.error("Failed to decode incoming attribute for", discoveredObject.getObjectName());
                }
                
            }
            else
            {
                logger.info(String.join(" ", "Received attribute values for ", discoveredObject.toString(),
                        "but no spatial attributes were present. Ignoring"));
            }
        }
        else
        {
            logger.info(String.join(" ", "Received attribute values for object instance handle", theObject.toString(),
                    "but object has not been discovered. Ignoring"));
        }
        
    }
    
    @Override
    public void discoverObjectInstance(final ObjectInstanceHandle theObject, final ObjectClassHandle theObjectClass,
            final String objectName)
    {
        
        logger.info(String.join(" ", "Discovered object", objectName, ", instance handle:", theObject.toString(),
                "class handle:", theObjectClass.toString()));
        
        if (discoveredObjects.containsKey(theObject))
        {
            logger.warn(String.join(" ", "Object", objectName, "has already been discovered"));
        }
        
        discoveredObjects.put(theObject, new DiscoveredObject(theObject, theObjectClass, objectName));
    }
    
    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass,
            String objectName, FederateHandle producingFederate) throws FederateInternalError
    {
        discoverObjectInstance(theObject, theObjectClass, objectName);
    }
    
    @Override
    public void removeObjectInstance(final ObjectInstanceHandle theObject, final byte[] userSuppliedTag,
            final OrderType sentOrdering, final FederateAmbassador.SupplementalRemoveInfo removeInfo)
    {
        final DiscoveredObject removedObject = discoveredObjects.remove(theObject);
        
        if (removedObject == null)
        {
            logger.warn(String.join(" ", "Object instance handle", theObject.toString(),
                    "was removed by remote federate but never actually discovered by this federate"));
        }
        else
        {
            logger.info(String.join(" ", "Object", removedObject.toString(), "removed"));
        }
    }
    
    @Override
    public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering,
            @SuppressWarnings("rawtypes")
            LogicalTime theTime, OrderType receivedOrdering, SupplementalRemoveInfo removeInfo)
            throws FederateInternalError
    {
        removeObjectInstance(theObject, userSuppliedTag, sentOrdering, removeInfo);
    }
    
    @Override
    public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering,
            @SuppressWarnings("rawtypes")
            LogicalTime theTime, OrderType receivedOrdering, MessageRetractionHandle retractionHandle,
            SupplementalRemoveInfo removeInfo) throws FederateInternalError
    {
        removeObjectInstance(theObject, userSuppliedTag, sentOrdering, removeInfo);
    }
    
    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
            byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport,
            @SuppressWarnings("rawtypes")
            LogicalTime theTime, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo)
            throws FederateInternalError
    {
        reflectAttributeValues(theObject, theAttributes, userSuppliedTag, sentOrdering, theTransport, reflectInfo);
    }
    
    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
            byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport,
            @SuppressWarnings("rawtypes")
            LogicalTime theTime, OrderType receivedOrdering, MessageRetractionHandle retractionHandle,
            SupplementalReflectInfo reflectInfo) throws FederateInternalError
    {
        reflectAttributeValues(theObject, theAttributes, userSuppliedTag, sentOrdering, theTransport, reflectInfo);
    }
    
}
