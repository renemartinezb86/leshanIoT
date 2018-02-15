package client.SmartObjects;

//import com.ericsson.appiot.demo.lwm2m.client.tools.Utils;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static client.tools.Utils.stringEquals;

public class StopWatch extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(StopWatch.class);

    // This resource represents a light, which can be controlled
    private static final int ON_OFF_STATE = 5850;
    // The time in seconds that the light has been on.
    // Writing a value of 0 resets the counter.
    private static final int CUMULATIVE_TIME = 5544;
    // The power factor of the light.
//    private static final int POWER_FACTOR = 5820;
//    // A string representing a value in some color space
//    private static final int SENSOR_COLOUR = 5706;
//    // If present, the type of sensor defined as the UCUM Unit
//    // Definition e.g. “Cel” for Temperature in Celcius.
//    private static final int SENSOR_UNIT = 5701;
    private static final int DIGITAL_INPUT_COUNTER = 5501;
    private static final int APPLICATION_TYPE = 5750;


    private boolean currentState;
    private String applicationType = "StopWatch application";
    private int digitalInputCounter = 0;

    private final GpioPinDigitalOutput relay;

    private long lastUpTimestamp = -1;
    private long cumulativeTime = 0;
    private float powerFactor = 100f;
    // private String sensorColour = "RGB(255,0,0)";
    private String sensorUnit = "0=Off/1=On";

    private final ScheduledExecutorService scheduler;

    public StopWatch(Pin pin) {

        relay = GpioFactory.getInstance().provisionDigitalOutputPin(pin, PinState.LOW);
        currentState = false;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Digital Input Sensor"));
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                updateOnTimer();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public synchronized ReadResponse read(int resourceId) {
        switch (resourceId) {
        case ON_OFF_STATE:
            return ReadResponse.success(resourceId, currentState);
        case CUMULATIVE_TIME:
            return ReadResponse.success(resourceId, cumulativeTime);
        case DIGITAL_INPUT_COUNTER:
            return ReadResponse.success(resourceId, digitalInputCounter);
        case APPLICATION_TYPE:
            return ReadResponse.success(resourceId, applicationType);
        default:
            return super.read(resourceId);
        }
    }

    @Override
    public synchronized ExecuteResponse execute(int resourceId, String params) {
        return super.execute(resourceId, params);
    }

    @Override
    public WriteResponse write(int resourceId, LwM2mResource value) {

        switch (resourceId) {

            case ON_OFF_STATE:

                boolean state = (Boolean) value.getValue();

                if( currentState == state ) {
                    return WriteResponse.success();
                }

                LOG.info("Stopwatch state changed from " + currentState + " to " + state);

                if (state) {
                    relay.high();
                    lastUpTimestamp = System.currentTimeMillis();
                    digitalInputCounter++;
                }
                else {
                    relay.low();
                    updateOnTimer();
                }

                currentState = state;
                fireResourcesChange(ON_OFF_STATE);
                return WriteResponse.success();

            case CUMULATIVE_TIME:

                long newOnTime = (Long)value.getValue();

                if( newOnTime != cumulativeTime) {

                    LOG.info("On Time has changed from " + cumulativeTime + " to " + newOnTime);
                    cumulativeTime = newOnTime;

                    fireResourcesChange(CUMULATIVE_TIME);
                }

                return WriteResponse.success();

            case APPLICATION_TYPE:

                String strValue = (String)value.getValue();
                if( !stringEquals(strValue, applicationType) ) {

                    LOG.info("Application Type changed from " + applicationType + " to " + strValue);

                    applicationType = strValue;

                    fireResourcesChange(APPLICATION_TYPE);
                }

                return WriteResponse.success();

        }

        return WriteResponse.notFound();
    }

    public void updateOnTimer(){

        if(currentState){

            long currentTimestamp = System.currentTimeMillis();
            cumulativeTime += (currentTimestamp - lastUpTimestamp)/1000;
            fireResourcesChange(CUMULATIVE_TIME);
            lastUpTimestamp = currentTimestamp;

        }

    }

}
