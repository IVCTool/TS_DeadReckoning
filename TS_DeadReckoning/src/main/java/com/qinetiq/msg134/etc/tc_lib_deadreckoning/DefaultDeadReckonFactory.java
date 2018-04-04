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
 * A factory to instantiate the concrete classes to implement the specified dead
 * reckoning algorithms
 * 
 * @author QinetiQ
 */
public class DefaultDeadReckonFactory implements DeadReckonFactory
{
    
    /**
     * Create the concrete dead reckoning algorithm class
     * 
     * @param algorithm
     *            The algorithm number in the range 2 to 9 inclusive
     * @param logger
     *            The logger to use
     * @return The object implementing the specified dead reckoner algorithm
     * @throws IllegalArgumentException
     *             for algorithms not supported.
     */
    @Override
    public DeadReckoner createDeadReckoner(final int algorithm, final Logger logger) throws IllegalArgumentException
    {
        final DeadReckoner deadReckon;
        
        switch (algorithm)
        {
            case 1:
                logger.info("Algorithm 1 is static and does not dead reckon.");
                deadReckon = null;
                break;
            case 2:
                logger.debug("Creating DeadReckon2 instance.");
                deadReckon = new DeadReckon2(logger);
                break;
            case 3:
                logger.debug("Creating DeadReckon3 instance.");
                deadReckon = new DeadReckon3(logger);
                break;
            case 4:
                logger.debug("Creating DeadReckon4 instance.");
                deadReckon = new DeadReckon4(logger);
                break;
            case 5:
                logger.debug("Creating DeadReckon5 instance.");
                deadReckon = new DeadReckon5(logger);
                break;
            case 6:
                logger.debug("Creating DeadReckon6 instance.");
                deadReckon = new DeadReckon6(logger);
                break;
            case 7:
                logger.debug("Creating DeadReckon7 instance.");
                deadReckon = new DeadReckon7(logger);
                break;
            case 8:
                logger.debug("Creating DeadReckon8 instance.");
                deadReckon = new DeadReckon8(logger);
                break;
            case 9:
                logger.debug("Creating DeadReckon9 instance.");
                deadReckon = new DeadReckon9(logger);
                break;
            default:
                String msg = "Incorrect algorithm provided: " + algorithm;
                logger.error(msg);
                throw new IllegalArgumentException(msg);
        }
        
        return deadReckon;
    }
}