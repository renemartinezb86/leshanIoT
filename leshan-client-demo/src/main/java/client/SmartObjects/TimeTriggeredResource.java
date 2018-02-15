package client.SmartObjects;

/**
 * Created by eremtas on 2017-09-06.
 */

/**
 * A resource that gets called back on a periodic basis
 */
public interface TimeTriggeredResource {

    /**
     * Callback when time is up
     */
    public void timeOn();
}
