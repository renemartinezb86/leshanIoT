package client.socket;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Client {
    Socket requestSocket;
    ObjectOutputStream out;
    ObjectInputStream in;
    int tankValue = 0;

    Client() {
    }

    void run()

    {
        while (true) {
            try {
                //1. creating a socket to connect to the server
                requestSocket = new Socket("localhost", 2004);
                System.out.println("Connected to localhost in port 2004");
                //2. get Input and Output streams
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(requestSocket.getInputStream());
                //3: Communicating with the server
                do {
                    // message = (String)in.readObject();
                    // System.out.println("server>" + message);
                    //sendMessage("Hi my server");
                    //  int n = new Random().nextInt(2);
                    int n = 0;// new Random().nextInt(2);//readSensorValue1();
                    System.out.println(n);
                    if (n != tankValue) {
                        tankValue = n;
                        sendMessage(tankValue + "");
                    } else
                        Thread.sleep(1000);
                } while (true);
            } catch (Exception e) {
                System.err.println("You are trying to connect to an unknown host!");
//           } catch (IOException ioException) {
//               ioException.printStackTrace();
//           } catch (InterruptedException e) {
//               e.printStackTrace();
//           } finally {
//               //4: Closing connection
//               try {
//                   in.close();
//                   out.close();
//                   requestSocket.close();
//               } catch (IOException ioException) {
//                   ioException.printStackTrace();
//               }
            }
        }
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

    void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("client>" + msg);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Client client = new Client();
        client.run();
    }
}