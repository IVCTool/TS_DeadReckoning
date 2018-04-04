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

import java.time.Instant;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * Stores the  spatial records received from a particular object.
 * 
 * @author QinetiQ
 */
public class SpatialObject
{
    /**
     * The spatial records, keyed by the timestamp
     */
    private final SortedMap<Instant, SpatialRecord> spacialRecords = new ConcurrentSkipListMap<>();
    
    /**
     * Add a new spatial record
     * @param timestamp The time stamp pertaining to the spatial record
     * @param spacialRecord The spatial record
     */
    public void add(final Instant timestamp, SpatialRecord spacialRecord)
    {
       spacialRecords.put(timestamp, spacialRecord);
    }
    
    /**
     * @return The map of spatial records sorted in order of the time stamp
     */
    public SortedMap<Instant, SpatialRecord> getSortedSpacialRecords()
    {
        return new TreeMap<>(spacialRecords);
    }
    
    /**
     * @return The time stamp of the first spatial record received
     */
    public Instant getStartTime()
    {
        return spacialRecords.isEmpty() ? null : spacialRecords.firstKey();
    }
    
    /**
     * @return The time stamp of the last spatial record received
     */
    public Instant getLastTime()
    {
        return spacialRecords.isEmpty() ? null : spacialRecords.lastKey();
    }
    
    /**
     * Determines whether or not any spatial records have been stored
     * @return True if spatial records have been stored, otherwise false
     */
    public boolean isEmpty()
    {
        return spacialRecords.isEmpty();
    }
    
    /**
     * @return The number of spatial records stored
     */
    public int getUpdatesCount()
    {
        return spacialRecords.size();
    }
    
}
