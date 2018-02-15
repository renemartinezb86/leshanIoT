package client.SmartObjects;

//import com.ericsson.appiot.demo.lwm2m.client.tools.Utils;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static client.tools.Utils.stringEquals;

public class ParticleConcentration extends BaseInstanceEnabler {

    private static final Logger LOG = LoggerFactory.getLogger(ParticleConcentration.class);

    private static final int SENSOR_VALUE = 5700;
    private static final int SENSOR_UNITS = 5701;
    private static final int COUNTER_RESET = 5505;
    private static final int APPLICATION_TYPE = 5750;

    private int counter = 12345;
    private String applicationType = "CO2";
    private int units = 432;

    public ParticleConcentration(int value, int units, String type) {
        this.setCounter(value);
        this.setUnits(units);
        this.setApplicationType(type);
    }

    @Override
    public synchronized ReadResponse read(int resourceId) {
        switch (resourceId) {
            case SENSOR_VALUE:
                return ReadResponse.success(resourceId, getCounter());
            case SENSOR_UNITS:
                return ReadResponse.success(resourceId, getUnits());
            case APPLICATION_TYPE:
                return ReadResponse.success(resourceId, getApplicationType());
            default:
                return super.read(resourceId);
        }
    }

    @Override
    public synchronized ExecuteResponse execute(int resourceId, String params) {
        switch (resourceId) {
            case COUNTER_RESET:

                if (getCounter() != 0) {
                    setCounter(0);
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

                if (stringEquals(strValue, getApplicationType())) {
                    LOG.info("Application Type changed from " + getApplicationType() + " to " + strValue);
                    setApplicationType(strValue);
                    fireResourcesChange(APPLICATION_TYPE);
                }

            default:
                return super.write(resourceId, value);
        }
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }
}
