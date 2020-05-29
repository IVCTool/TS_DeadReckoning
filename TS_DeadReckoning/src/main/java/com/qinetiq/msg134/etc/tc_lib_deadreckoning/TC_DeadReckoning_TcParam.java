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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import de.fraunhofer.iosb.tc_lib.IVCT_TcParam;
import de.fraunhofer.iosb.tc_lib.TcInconclusive;

/**
 * Provides test case configuration from a JSON file in line with the IVCT
 * guidelines
 * 
 * @author QinetiQ
 */
public class TC_DeadReckoning_TcParam implements IVCT_TcParam
{
    /**
     * The host of the HLA RTI
     */
    protected String rtiHost = "localhost";
    
    /**
     * The IVCT Federation name
     */
    protected String federationName = "IVCT";
    
    
    /**
     * The federate name of the test case
     */
    protected String tcFederateName = "TC_0001";
    
    /**
     * The federate name of the SuT
     */
    protected String sutFederateName;
    
    /**
     * The federate type of the SuT
     */
    protected String sutFederateType;
    
    /**
     * The maximum time to wait for the SuT federate to join
     */
    protected double sutFederateJoinTimeout = 15.0;
    

    /**
     * The test timeout in seconds
     */
    protected double testTimeout = 30.0;
    
    /**
     * The maximum position threshold in metres
     */
    protected double positionThresholdMax = 1.0;
    
    /**
     * The minimum position threshold in metres
     */
    protected double positionThresholdMin = 0.1;
    
    /**
     * The maximum orientation threshold in radians
     */
    protected double orientationThresholdMax = 1.0;
    
    /**
     * The minimum orientation threshold in radians
     */
    protected double orientationThresholdMin = 0.1;
    
    /**
     * Whether or not to accept non time-stamped updates
     */
    protected boolean timestampRequired;
    
    /**
     * Whether both position and orientation tests need to pass
     */
    protected boolean positionAndOrientationRequired;
    
    /**
     * The urls of the FOM files to be used
     */
    protected URL[] urls;
    
    /**
     * Create a new instance of this class based on the following parameters.
     * 
     * @param tcParamJson
     *            The JSON file from which to read the values
     * @param logger
     *            The logger
     * @throws TcInconclusive
     *             If for any reason the initialisation fails
     */
    public TC_DeadReckoning_TcParam(final String tcParamJson, final Logger logger) throws TcInconclusive
    {
        boolean errors = false;
        
        JSONParser jsonParser = new JSONParser();
        
        JSONObject  jsonObject;
        try
        {
            jsonObject = (JSONObject) jsonParser.parse(tcParamJson);
        }
        catch (ParseException e)
        {
            throw new TcInconclusive("Unable to parse TcParam JSON string");
        }
        
        rtiHost = (String) jsonObject.get("rtiHost");
        federationName = (String) jsonObject.get("federationName");
        tcFederateName = (String) jsonObject.get("tcFederateName");
        sutFederateName = (String) jsonObject.get("sutFederateName");
        sutFederateType = (String) jsonObject.get("sutFederateType");
        sutFederateJoinTimeout  = (Double) jsonObject.get("sutFederateJoinTimeout");
        testTimeout = (Double) jsonObject.get("testTimeout");
        positionThresholdMax = (Double) jsonObject.get("positionThresholdMax");
        positionThresholdMin = (Double) jsonObject.get("positionThresholdMin");
        orientationThresholdMax = (Double) jsonObject.get("orientationThresholdMax");
        orientationThresholdMin = (Double) jsonObject.get("orientationThresholdMin");
        timestampRequired = (Boolean) jsonObject.get("timestampRequired");
        positionAndOrientationRequired  = (Boolean) jsonObject.get("positionAndOrientationRequired");
        
        // FOMS
        JSONArray fomsJSONArray = (JSONArray) jsonObject.get("urls");
        
        if (fomsJSONArray == null || fomsJSONArray.isEmpty())
        {
            urls = new URL[0];
        }
        else
        {
            int size = fomsJSONArray.size();
            
            urls = new URL[size];
            for (int index = 0; index < size; index++)
            {
                String urlName = (String) fomsJSONArray.get(index);
                
                // is it a file?
                File file = new File(urlName);
                
                if (!file.exists())
                {
                    file = new File("", urlName);
                    
                    if (!file.exists())
                    {
                        file = new File("/", urlName);
                        
                        if (!file.exists())
                        {
                            file = new File("/resources", urlName);
                        }
                    }
                }
                
                if (file.exists())
                {
                    logger.info(file.getAbsolutePath() + " was specified in the TcParam file");
                    try
                    {
                        urls[index] = file.toURI().toURL();
                    }
                    catch (MalformedURLException e)
                    {
                        logger.error(
                                file.getAbsolutePath() + " was specified in the TcParam file but is not a valid URL");
                        errors = true;
                    }
                }
                else
                {
                    logger.error(String.join(" ", "URL", urlName,
                            "was specified in the TcParam file but cannot be resolved"));
                    errors = true;
                }
            }
            
        }
        
        if (errors)
        {
            String msg = "Unable to continue due to errors encountered during TcParam configuration. Refer to log file";
            logger.error(msg);
            throw new TcInconclusive(msg);
        }
    }
    /**
     * @return the federation name
     */
    public String getFederationName()
    {
        return this.federationName;
    }
    
    /**
     * @return the RTI host value
     */
    public String getRtiHost()
    {
        return this.rtiHost;
    }
    
    /**
     * @return the settings designator
     */
    public String getSettingsDesignator()
    {
        return "crcAddress=" + rtiHost;
    }
    
    @Override
    public URL[] getUrls()
    {
        return Arrays.copyOf(urls, urls.length);
    }
    

    /**
     * @return Return the test timeout in seconds
     */
    public double getTestTimeout()
    {
        return testTimeout;
    }
    
    /**
     * @return The federate name of the test case
     */
    public String getTcFederateName()
    {
        return tcFederateName;
    }

    /**
     * @return The maximum time to wait for the SuT federate to join
     */
    public double getSuTFederateJoinTimeout()
    {
        return sutFederateJoinTimeout;
    }

    /**
     * @return The SuT federate name
     */
    public String getSutFederateName()
    {
        return sutFederateName;
    }
    
    /**
     * @return The SuT federate type
     */
    public String getSutFederateType()
    {
        return sutFederateType;
    }

    /**
     * @return True if a time stamp is required from the SuT for a spatial update, otherwise false
     */
    public boolean isTimestampRequired()
    {
        return timestampRequired;
    }

    /**
     * @return The maximum position threshold in metres
     */
    public double getPositionThresholdMax()
    {
        return positionThresholdMax;
    }

    /**
     * @return The minimum position threshold in metres
     */
    public double getPositionThresholdMin()
    {
        return positionThresholdMin;
    }

    /**
     * @return The maximum orientation threshold in radians
     */
    public double getOrientationThresholdMax()
    {
        return orientationThresholdMax;
    }

    /**
     * @return The minimum orientation threshold in radians
     */
    public double getOrientationThresholdMin()
    {
        return orientationThresholdMin;
    }

    /**
     * @return True if both position and orientation are required for a test to
     *         pass, otherwise false
     */
    public boolean isPositionAndOrientationRequired()
    {
        return positionAndOrientationRequired;
    }
    
    
    
}
