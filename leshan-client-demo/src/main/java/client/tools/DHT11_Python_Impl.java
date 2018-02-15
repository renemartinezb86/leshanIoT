package client.tools;


import client.SmartObjects.TimeTriggeredResource;
import org.eclipse.leshan.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eremtas on 2017-09-29.
 *
 * Needed installed https://iotbykarthik.wordpress.com/2016/02/07/dht11-spi-and-raspberry-pi/
 */
public class DHT11_Python_Impl implements DHTXX{

    private static final Logger LOG = LoggerFactory.getLogger(DHT11_Python_Impl.class);

    private final int pin;
    private final ScheduledExecutorService scheduler;
    private final String cmd;
    Set<TimeTriggeredResource> triggeredResources = new HashSet<>();

    private float temperature;
    private float humidity;

    //Temp=Temp=12.1* Humidity=-12.1%
    Pattern pattern = Pattern.compile("\\w+=([-]?[0-9]*\\.?[0-9]+)\\*\\s+\\w+=([-]?[0-9]*\\.?[0-9]+)%");

    public DHT11_Python_Impl (int pin) {

        this.pin = pin;

        cmd = "python /home/pi/Adafruit_Python_DHT/examples/AdafruitDHT.py 11 " + pin;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(this.getClass().getName()));

        this.scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                read_python();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public float getTemperature(){
        return this.temperature;
    }

    public float getHumidity(){
        return this.humidity;
    }

    private void read_python(){

        BufferedReader input = null;

        try{
            Process p = Runtime.getRuntime().exec(cmd);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = input.readLine();

            LOG.trace(line);

            Matcher m = pattern.matcher(line);

            if(m.find()){
                this.temperature = Float.parseFloat(m.group(1));
                this.humidity = Float.parseFloat(m.group(2));

                LOG.trace("Temperature: " + this.temperature);
                LOG.trace("Humidity: " + this.humidity);
            }


        }catch (IOException e){
            LOG.error("Error reading pin: " + e);
        }finally {
            if(input != null){
                try{
                    input.close();
                }catch (IOException e){
                    LOG.error("Error closing buffer: " + e);
                }
            }
        }

        wakeCallbacks();

    }

    public void addTimeTriggeredResource( TimeTriggeredResource resource){
        triggeredResources.add(resource);
    }

    public void removeTimeTriggeredResource( TimeTriggeredResource resource){
        triggeredResources.remove(resource);
    }

    public void stopService() {

        this.scheduler.shutdownNow();
    }

    private void wakeCallbacks() {

        Iterator<TimeTriggeredResource> it =  triggeredResources.iterator();

        while(it.hasNext()){
            it.next().timeOn();
        }

    }

}
