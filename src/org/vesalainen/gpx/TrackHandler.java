/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.gpx;

import java.util.Collection;

/**
 * @author Timo Vesalainen
 */
public interface TrackHandler
{
    /**
     * Handle track. If implementation returns false the track is skipped
     * @param extensions
     * @return 
     */
    boolean startTrack(String name, Collection<Object> extensions);
    void endTrack();
    void startTrackSeq();
    void endTrackSeq();
    void trackPoint(double latitude, double longitude, long time);
}
