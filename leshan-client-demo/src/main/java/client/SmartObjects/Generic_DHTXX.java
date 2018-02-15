package client.SmartObjects;

import client.tools.DHTXX;
import client.tools.Utils;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static client.tools.Utils.getTwoDigitValue;


/**
 * Created by eremtas on 2017-09-29.
 */

public abstract class Generic_DHTXX extends BaseInstanceEnabler implements TimeTriggeredResource{

    private static final Logger LOG = LoggerFactory.getLogger(Generic_DHTXX.class);

    private static final int SENSOR_VALUE = 5700;
    private static final int UNITS = 5701;
    private static final int MAX_MEASURED_VALUE = 5602;
    private static final int MIN_MEASURED_VALUE = 5601;
    private static final int RESET_MIN_MAX_MEASURED_VALUES = 5605;
    private double sensorValue = 20d;
    private double minMeasuredValue = sensorValue;
    private double maxMeasuredValue = sensorValue;

    final DHTXX dhtxxSensor;

    public Generic_DHTXX(DHTXX dhtxx) {

        this.dhtxxSensor = dhtxx;
    }


    public abstract String getUnit();
    public abstract float getValue();

    @Override
    public synchronized ReadResponse read(int resourceId) {
        switch (resourceId) {
            case MIN_MEASURED_VALUE:
                return ReadResponse.success(resourceId, getTwoDigitValue(minMeasuredValue));
            case MAX_MEASURED_VALUE:
                return ReadResponse.success(resourceId, getTwoDigitValue(maxMeasuredValue));
            case SENSOR_VALUE:
                return ReadResponse.success(resourceId, getTwoDigitValue(sensorValue));
            case UNITS:
                return ReadResponse.success(resourceId, getUnit());
            default:
                return super.read(resourceId);
        }
    }

    @Override
    public synchronized ExecuteResponse execute(int resourceId, String params) {
        switch (resourceId) {
            case RESET_MIN_MAX_MEASURED_VALUES:
                resetMinMaxMeasuredValues();
                return ExecuteResponse.success();
            default:
                return super.execute(resourceId, params);
        }
    }


    @Override
    public synchronized void timeOn() {

        double value = getValue();

        if( Utils.doubleEqual(value, sensorValue, 1e-2))
            return;

        LOG.trace("Sensor value changed from " + sensorValue + " to " + value);

        sensorValue = value;

        Integer changedResource = adjustMinMaxMeasuredValue(sensorValue);
        if (changedResource != null) {
            fireResourcesChange(SENSOR_VALUE, changedResource);
        } else {
            fireResourcesChange(SENSOR_VALUE);
        }
    }


    private Integer adjustMinMaxMeasuredValue(double newTemperature) {

        if (newTemperature > maxMeasuredValue) {
            maxMeasuredValue = newTemperature;
            return MAX_MEASURED_VALUE;
        } else if (newTemperature < minMeasuredValue) {
            minMeasuredValue = newTemperature;
            return MIN_MEASURED_VALUE;
        } else {
            return null;
        }
    }

    private void resetMinMaxMeasuredValues() {
        minMeasuredValue = sensorValue;
        maxMeasuredValue = sensorValue;
    }


}
