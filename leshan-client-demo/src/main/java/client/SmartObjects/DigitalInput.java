package client.SmartObjects;

//import com.ericsson.appiot.demo.lwm2m.client.tools.Utils;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static client.tools.Utils.stringEquals;

public class DigitalInput extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(DigitalInput.class);

    private static final int DIGITAL_INPUT_STATE = 5500;
    private static final int DIGITAL_INPUT_COUNTER = 5501;
    private static final int COUNTER_RESET = 5505;
    private static final int APPLICATION_TYPE = 5750;
    private static final int SENSOR_TYPE = 5751;
    private boolean currentState = false;
    private int counter = 0;
    private String applicationType = "Versatile";
    private final String sensorType = "Button/Switch";

    //private final GpioPinDigitalInput btn;

    public DigitalInput(int value) {
        counter = value;
    }

    public DigitalInput(Pin pin) {

        /*this.btn = GpioFactory.getInstance().provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);

        this.btn.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {

                handleStateChangeEvent(gpioPinDigitalStateChangeEvent);

            }
        });*/

    }

    @Override
    public synchronized ReadResponse read(int resourceId) {
        switch (resourceId) {
            case DIGITAL_INPUT_STATE:
                return ReadResponse.success(resourceId, currentState);
            case DIGITAL_INPUT_COUNTER:
                return ReadResponse.success(resourceId, counter);
            case APPLICATION_TYPE:
                return ReadResponse.success(resourceId, applicationType);
            case SENSOR_TYPE:
                return ReadResponse.success(resourceId, sensorType);
            default:
                return super.read(resourceId);
        }
    }

    @Override
    public synchronized ExecuteResponse execute(int resourceId, String params) {
        switch (resourceId) {
            case COUNTER_RESET:

                if (counter != 0) {
                    counter = 0;
                    fireResourcesChange(COUNTER_RESET);
                    LOG.info("Resetting counter");
                }

                return ExecuteResponse.success();
            default:
                return super.execute(resourceId, params);
        }
    }

    @Override
    public WriteResponse write(int resourceId, LwM2mResource value) {

        switch (resourceId) {

            case APPLICATION_TYPE:

                String strValue = (String) value.getValue();

                if (stringEquals(strValue, applicationType)) {
                    LOG.info("Application Type changed from " + applicationType + " to " + strValue);
                    applicationType = strValue;
                    fireResourcesChange(APPLICATION_TYPE);
                }

            default:
                return super.write(resourceId, value);
        }

    }


    private void handleStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {

        boolean value = gpioPinDigitalStateChangeEvent.getState().isHigh();

        if (currentState != value) {

            LOG.info("Digital input changed from " + currentState + " to " + value);

            currentState = value;

            if (value) {
                counter++;
                fireResourcesChange(DIGITAL_INPUT_STATE, DIGITAL_INPUT_COUNTER);
            } else {
                fireResourcesChange(DIGITAL_INPUT_STATE);
            }

        }
    }

}
