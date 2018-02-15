package client.SmartObjects;


import client.tools.DHTXX;

/**
 * Created by eremtas on 2017-09-29.
 */

public class Humidity_DHTXX extends Generic_DHTXX{

    private static final String UNIT_HUMIDITY = "%";

    public Humidity_DHTXX(DHTXX dhtxx) {

        super(dhtxx);
    }


    @Override
    public float getValue() {

        return dhtxxSensor.getHumidity();
    }

    @Override
    public String getUnit(){
        return UNIT_HUMIDITY;
    }

}
