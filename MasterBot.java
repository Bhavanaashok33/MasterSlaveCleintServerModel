import java.awt.List;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Date;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class MasterBot extends Thread {
    ArrayList < String > list = new ArrayList < > ();
    ArrayList < String > list2 = new ArrayList < > ();


    public static MasterBot Master;
    ArrayList < SlaveInfo > slaves = null;

    boolean FindsocketbynameorIP(String hostname) {
        return true;
    }
    void Printlist2(ArrayList < String > i2) {
        list2 = i2;
        System.out.println(list2);
    }
    static Socket server = null;

    String command = null;
    //@SuppressWarnings("resource")
    public static void main(String[] args) {
        Master = new MasterBot();
        int masterport = 1123;
        ServerSocket serversocket = null;
        if (args[0].equals("-p")) {
            masterport = Integer.parseInt(args[1]);
        } else {
            System.out.println("Port number invalid");
            System.exit(0);
        }
        Thread th = new Thread(Master.new CommandLineInterfaceThread());
        th.start();
        try {
            serversocket = new ServerSocket(masterport);
        } catch (Exception e) {
            System.out.println(" Exception occcured while creating a serversocket" + e);
        }
        Master.slaves = new ArrayList < SlaveInfo > ();
        do {
            try {
                server = serversocket.accept();
                System.out.println(" The socket created with IP addr" + " " + server.getInetAddress().getHostName() + " and" + " " + "the port number" + " " + server.getPort());
                SlaveInfo slaveinfo = Master.new SlaveInfo(server);
                Master.slaves.add(slaveinfo);
            } catch (Exception e) {
                System.out.println("Exception  occcured while creating a socket ");
            }
        } while (true);








    }

    boolean send_command(String sHostNameOrInfo, String command) {
        Iterator < SlaveInfo > it = slaves.iterator();
        while (it.hasNext()) {
            SlaveInfo slaveInfo = it.next();
            if (sHostNameOrInfo.equals("all") ||
                slaveInfo.server.getInetAddress().getHostName().compareTo(sHostNameOrInfo) == 0 ||
                slaveInfo.server.getInetAddress().getHostAddress().compareTo(sHostNameOrInfo) == 0) {
                try {
                    System.out.println("sending command to identified slave");
                    PrintWriter p = new PrintWriter(slaveInfo.server.getOutputStream());
                    p.println(command);
                    p.flush(); //flush the op stream
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }





    class Runnable1 implements Runnable {
        public void run() {
            Iterator < SlaveInfo > it = MasterBot.Master.slaves.iterator();
            System.out.println("The list of slves are ");
            while (it.hasNext()) {
                SlaveInfo slaveinfo = it.next();
                slaveinfo.print();
            }
        }
    }

    class Runnable2 implements Runnable {
        public void run() {
            String[] string = command.split("\\s+");
            boolean status = MasterBot.Master.send_command(string[1], command);
            System.out.println("The scan is running. Other commands can be used until the results are shown...");
            if (status == false) {
                System.out.println("Sorry, the slavedetail does not match any entry");
            }
            BufferedReader br;
            String cmd = null;
            try {
                br = new BufferedReader(new InputStreamReader(server.getInputStream()));
                System.out.println("The lists are given by ");
                while (br != null) {
                    cmd = br.readLine();
                    if (command.startsWith("geoipscan")) {

                        //String[] str=cmd.split(" +");
                        System.out.println(cmd);
                    }

                    if (command.startsWith("ipscan")) {
                        //System.out.println("The list of ip addresses which responded are ");
                        System.out.println(cmd);
                    }
                    if (command.startsWith("tcpportscan")) {
                        //System.out.println("The list of ports which responded are ");
                        System.out.println(cmd);
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }



        }
    }
    class CommandLineInterfaceThread implements Runnable {

        public void run() {
            Console c = System.console();

            while (true) {
                //read the command line and parse te commands
                System.out.printf(">");
                command = c.readLine();
                if (command.equals("list") || command.startsWith("connect") || command.startsWith("disconnect") || command.startsWith("ipscan") || command.startsWith("tcpportscan") || command.startsWith("geoipscan")) {
                    String operation = command;
                    if (operation.equals("list")) {
                        Runnable r1 = new Runnable1();
                        Thread t1 = new Thread(r1);
                        t1.start();
                    } else if (command.startsWith("connect") || command.startsWith("disconnect")) {

                        String[] string = command.split("\\s+");
                        boolean status = MasterBot.Master.send_command(string[1], command);
                        if (status == false) {
                            System.out.println("Sorry, the slave details does not match any entry");
                        }

                    } else if (command.startsWith("ipscan") || command.startsWith("tcpportscan") || command.startsWith("geoipscan")) {
                        Runnable r2 = new Runnable2();
                        Thread t2 = new Thread(r2);
                        t2.start();
                    }
                }
            }
        }

    }

    class SlaveInfo {
        Socket server;
        Date date;
        String modifiedDate;


        public SlaveInfo(Socket sock) {
            server = sock;
            date = Calendar.getInstance().getTime();
            modifiedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

        }
        void print() {
            System.out.println(" SlaveHostName " + server.getInetAddress().getHostName() + " " + server.getInetAddress().getHostAddress() + " " +
                server.getPort() + " " + modifiedDate.toString());
        }
    }

}