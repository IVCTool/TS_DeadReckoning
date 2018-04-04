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
package com.qinetiq.msg134.etc.tc_deadreckoning;

import de.fraunhofer.iosb.tc_lib.AbstractTestCase;
import de.fraunhofer.iosb.tc_lib.IVCT_BaseModel;
import de.fraunhofer.iosb.tc_lib.IVCT_RTI_Factory;
import de.fraunhofer.iosb.tc_lib.IVCT_RTIambassador;
import de.fraunhofer.iosb.tc_lib.TcFailed;
import de.fraunhofer.iosb.tc_lib.TcInconclusive;
import nato.ivct.commander.Factory;

import java.io.File;
import java.util.Collection;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qinetiq.msg134.etc.tc_lib_deadreckoning.DeadReckonFactory;
import com.qinetiq.msg134.etc.tc_lib_deadreckoning.DefaultDeadReckonFactory;
import com.qinetiq.msg134.etc.tc_lib_deadreckoning.TC_DeadReckoning_BaseModel;
import com.qinetiq.msg134.etc.tc_lib_deadreckoning.TC_DeadReckoning_TcParam;

/**
 * Implements the Dead Reckoning test case.
 * 
 * @author QinetiQ
 */
public class TC_0001_DeadReckoning extends AbstractTestCase
{
    /**
     * The configurable test case parameters
     */
    private TC_DeadReckoning_TcParam tcParam;
    
    /**
     * Reference to the IVCT-RTI
     */
    private IVCT_RTIambassador ivct_rti;
    
    /**
     * The Dead Reckoning base model
     */
    private TC_DeadReckoning_BaseModel tcDeadReckoningBaseModel;
    
    /**
     * Entry point to run the test case as a standalone application.
     * 
     * @param args
     *            The full path of the JSON configuration file.
     */
    public static void main(final String[] args)
    {
        Logger logger = LoggerFactory.getLogger(TC_0001_DeadReckoning.class);
        
        if (args.length >= 1)
        {
            File file = new File(args[0]);
            try (Scanner scanner = new Scanner(file))
            {
                scanner.useDelimiter("\\Z");
                String paramJson = scanner.next();
                TC_0001_DeadReckoning deadReckoningTC = new TC_0001_DeadReckoning();
                deadReckoningTC.execute(paramJson, logger);
                Factory.jmsHelper.disconnect();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.append("TC_0001_DeadReckoning. Expecting JSON filename");
        }
    }
    
    /**
     * Called by the IVCT framework to create the base model and establish the
     * interface to the HLA RTI
     * 
     * @param tcParamJson
     *            test case parameters
     * @param logger
     *            The logger to use
     * @return the verdict
     * @throws TcInconclusive
     *             if anything goes wrong
     */
    @Override
    public IVCT_BaseModel getIVCT_BaseModel(final String tcParamJson, final Logger logger) throws TcInconclusive
    {
        tcParam = new TC_DeadReckoning_TcParam(tcParamJson, logger);
        setTcName(tcParam.getTcFederateName());
        ivct_rti = IVCT_RTI_Factory.getIVCT_RTI(logger);
        DeadReckonFactory factory = new DefaultDeadReckonFactory();
        tcDeadReckoningBaseModel = new TC_DeadReckoning_BaseModel(logger, ivct_rti, tcParam, factory);
        return tcDeadReckoningBaseModel;
    }
    
    /**
     * Adds the test purpose to the log.
     * 
     * @param logger
     *            The logger to use
     */
    @Override
    protected void logTestPurpose(final Logger logger)
    {
        logger.info("-------------------------------------------------------------------------------\n");
        logger.info("TEST PURPOSE\n");
        logger.info("Tests that the system under test issues spatial updates for moving objects\n");
        logger.info("correctly in accordance with the dead reckoning algorithm being used.\n");
        logger.info("Subscribes to the BaseEntity Spatial attribute and receives / stores all spatial\n");
        logger.info("updates for all moving entities over a configurable period of time.\n");
        logger.info("Analyses the captured information and compares actual position and orientation\n");
        logger.info("values against the values calculated using the specified dead reckoning algorithm.\n");
        logger.info("A pass is recorded for all values that fall within user-defined thresholds.\n");
        logger.info("-------------------------------------------------------------------------------\n");
    }
    
    /**
     * Initialise the rti and init (subscribe to BaseEntity)
     * 
     * @param logger
     *            The logger to use
     * @throws TcInconclusive
     *             If something went wrong
     */
    @Override
    protected void preambleAction(final Logger logger) throws TcInconclusive
    {
        // Initiate rti
        tcDeadReckoningBaseModel.initiateRti(tcParam, tcDeadReckoningBaseModel);
        // sub to federation
        tcDeadReckoningBaseModel.init();
    }
    
    /**
     * Wait for storage containers to be populated in the base model that are
     * storing positional data. Analyse the stored data, evaluating the actual
     * position and orientation values against the calculated dead reckoned values.
     * Log the results to the log file and declare whether the test passed or failed
     * accordingly.
     * 
     * @param logger
     *            The logger to use
     * @throws TcInconclusive
     *             If something unexpected went wrong
     * @throws TcFailed
     *             If the test failed
     */
    @Override
    protected void performTest(final Logger logger) throws TcInconclusive, TcFailed
    {
        // Check that the SuT is connected and wait if not
        long startTime = System.currentTimeMillis();
        int federateJoinTimeout = (int) (tcParam.getSuTFederateJoinTimeout() * 1000);
        
        // Determine whether or not the system under test federate is connected
        boolean isSuTConnected = false;
        String sutFederateName = tcParam.getSutFederateName();
        
        // Wait for system under test federate to join
        WAIT_FOR_SUT_FEDERATE_LOOP:
        while (federateJoinTimeout < 0 || (System.currentTimeMillis() - startTime) < federateJoinTimeout)
        {
            if (tcDeadReckoningBaseModel.isFederateConnected(sutFederateName))
            {
                isSuTConnected = true;
                break WAIT_FOR_SUT_FEDERATE_LOOP;
            }
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                final String msg = String.join(" ", "Interrupted whilst waiting for SuT federate ", sutFederateName,
                        "to join. Terminating.");
                logger.error(msg, e);
                throw new TcInconclusive(msg);
            }
        }
        
        // If the system under test federate did not join after the specified timeout
        // value, determine the test inconclusive
        if (!isSuTConnected)
        {
            final String msg = String.join(" ", "SuT federate ", sutFederateName, "did not join after",
                    String.valueOf(federateJoinTimeout), "ms. Terminating.");
            logger.error(msg);
            throw new TcInconclusive(msg);
        }
        
        logger.info(String.join(" ", "Running Dead Reckoning test on SuT federate", sutFederateName));
        
        // Wait for the spatial data to be received from the SuT
        try
        {
            final int timeout = (int) (tcParam.getTestTimeout() * 1000);
            logger.info(String.join(" ", "Collecting spatial information for the duration of", String.valueOf(timeout),
                    "ms..."));
            Thread.sleep(timeout);
        }
        catch (InterruptedException e)
        {
            // If anything went wrong, determine the test inconclusive
            final String msg = "Interrupted whilst waiting for spatial information to arrive. Terminating";
            logger.error(msg, e);
            throw new TcInconclusive(msg);
        }
        
        // Unsubscribe so that no more updates are received
        tcDeadReckoningBaseModel.unsubscribe();
        
        // If sufficient data has been received, process it
        if (tcDeadReckoningBaseModel.isSuffientDataReceived())
        {
            logger.info(String.join(" ", "Evaluating received data against positionThresholdMin:",
                    String.valueOf(tcParam.getPositionThresholdMin()), "positionThresholdMax",
                    String.valueOf(tcParam.getPositionThresholdMax()), "orientationThresholdMin",
                    String.valueOf(tcParam.getOrientationThresholdMin()), "orientationThresholdMax",
                    String.valueOf(tcParam.getOrientationThresholdMax())));
            
            // Perform he comparison of the actual spatial update data received against the
            // calculated dead reckoned data.
            // This method will throw a TcFailed exception if failures are encountered
            tcDeadReckoningBaseModel.deadReckonCompare();
        }
        else
        {
            final String msg = "Insufficient data was received.";
            logger.error(msg);
            throw new TcInconclusive(msg);
        }
        
        // If the spatial update time stamp is required from the system under test, and
        // one or more updates have been received without this timestamp, log the
        // offending objects and render the test as having failed.
        if (tcParam.isTimestampRequired())
        {
            Collection<String> objects = tcDeadReckoningBaseModel.getObjectsSendingNonTimestampedUpdates();
            if (!objects.isEmpty())
            {
                final String msg = "Configuration item 'timestampRequired' is set to 'true' but non timestamped spatial information was received";
                logger.error(String.join(" ", msg, "from the following", String.valueOf(objects.size()), "object(s)"));
                objects.forEach(o -> logger.error(o));
                throw new TcFailed(msg);
            }
        }
        
    }
    
    /**
     * Disconnect from the RTI.
     * 
     * @param logger
     *            The logger to use
     * @throws TcInconclusive
     *             If something unexpected went wrong
     */
    @Override
    protected void postambleAction(final Logger logger) throws TcInconclusive
    {
        // Terminate rti
        tcDeadReckoningBaseModel.terminateRti();
    }
}