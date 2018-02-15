package org.eclipse.leshan.client.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import client.socket.Server;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.util.NamedThreadFactory;

import static org.apache.http.protocol.HTTP.USER_AGENT;


public class RandomTemperatureSensor extends BaseInstanceEnabler {

    private static int tankValue = 0;
    private static int value = 0;

    private static final String UNIT_CELSIUS = "cel";
    private HttpClient client = HttpClientBuilder.create().build();
    private static final int SENSOR_VALUE = 5700;
    private static final int UNITS = 5701;
    private static final int MAX_MEASURED_VALUE = 5602;
    private static final int MIN_MEASURED_VALUE = 5601;
    private static final int RESET_MIN_MAX_MEASURED_VALUES = 5605;
    //  private final ScheduledExecutorService scheduler3;
    private final ScheduledExecutorService scheduler2;
    private final ScheduledExecutorService scheduler;
    private final Random rng = new Random();
    private double currentTemp = 20d;
    private double minMeasuredValue = currentTemp;
    private double maxMeasuredValue = currentTemp;
    private String remoteValue = "0";

    public RandomTemperatureSensor() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Temperature Sensor"));
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                adjustTemperature();
            }
        }, 2, 2, TimeUnit.SECONDS);

//        this.scheduler2 = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Temperature Sensor"));
//        scheduler2.scheduleAtFixedRate(new Runnable() {
//
//            @Override
//            public void run() {
//                RandomTemperatureSensor.tankValue= readSensorValue1();
//            }
//        }, 2, 2, TimeUnit.SECONDS);


        this.scheduler2 = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Temperature Sensor"));
        scheduler2.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                System.out.println("getting http data sensor");
                RandomTemperatureSensor.value = getHttpSensorData();
            }
        }, 2, 2, TimeUnit.SECONDS);

//        this.scheduler3 = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Temperature Sensor"));
//        scheduler3.scheduleAtFixedRate(new Runnable() {
//
//            @Override
//            public void run() {
//               Server server = new Server();
//               server.run();
//            }
//        }, 2, 11000000, TimeUnit.SECONDS);


    }

    @Override
    public synchronized ReadResponse read(int resourceId) {
        switch (resourceId) {
            case MIN_MEASURED_VALUE:
                return ReadResponse.success(resourceId, getTwoDigitValue(minMeasuredValue));
            case MAX_MEASURED_VALUE:
                return ReadResponse.success(resourceId, getTwoDigitValue(maxMeasuredValue));
            case SENSOR_VALUE:
                return ReadResponse.success(resourceId, getSensorValue());
            case UNITS:
                return ReadResponse.success(resourceId, UNIT_CELSIUS);
            default:
                return super.read(resourceId);
        }
    }

    private Float getSensorValue() {
       // System.out.println(Server.message);
        System.out.println(value);
//        int value = Integer.parseInt(Server.message);

        if (value == 0) tankValue = tankValue + new Random().nextInt(60) + 30;
        else tankValue = tankValue - (new Random().nextInt(60) + 30);
        if (tankValue > 100) tankValue = 100;
        if (tankValue < 9) tankValue = 9;


//                    new Float(100);
        System.out.println("returning tank " + tankValue);
        //return tankValue;
        return new Float(tankValue);
    }

    private int readSensorValue1() {
        try {
            // if (true) return new Float(10);
            String cmd = "python readtank.py ";
            System.out.println("reading tank");
            Process p;
            BufferedReader input = null;
            p = Runtime.getRuntime().exec(cmd);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = input.readLine();
            Integer value = Integer.parseInt(s);
            //    Integer value = 1;
            //    Integer value =  new Random().nextInt(2);

            if (value == 0) tankValue = tankValue + new Random().nextInt(60) + 30;
            else tankValue = tankValue - (new Random().nextInt(60) + 30);
            if (tankValue > 100) tankValue = 100;
            if (tankValue < 9) tankValue = 9;


//                    new Float(100);
            System.out.println("returning tank " + tankValue);
            return tankValue;

            //return RANDOM.nextInt(7);
        } catch (IOException e) {
            e.printStackTrace();
            return 10;
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

    private double getTwoDigitValue(double value) {
        BigDecimal toBeTruncated = BigDecimal.valueOf(value);
        return toBeTruncated.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private synchronized void adjustTemperature() {
        float delta = (rng.nextInt(20) - 10) / 10f;
        currentTemp += delta;
        Integer changedResource = adjustMinMaxMeasuredValue(currentTemp);
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
        minMeasuredValue = currentTemp;
        maxMeasuredValue = currentTemp;
    }

    private int getHttpSensorData() {
        /*try {
            String url = "http://192.168.1.2:8080/sensor/readdata.jsp";


            HttpGet request = new HttpGet(url);

            request.addHeader("User-Agent", USER_AGENT);
            HttpResponse response = null;

            response = client.execute(request);


            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            String data = result.substring(result.indexOf("<body>")+7, result.indexOf("</body>")).trim();
            System.out.println("http data = "+data);
            return Integer.parseInt(data);


        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return 0;
    }
}
