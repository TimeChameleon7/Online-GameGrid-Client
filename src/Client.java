import javax.swing.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
    private boolean connected = true;
    private final Socket socketTCP;
    private final DatagramSocket socketUDP;
    private final JFrame frame;
    private JLabel label;
    private int bufferSize;
    private Grid grid;
    Client(String address, int port) throws IOException {//todo create method that starts all threads and remove them from constructor, name all threads, and setDaemon where appropriate
        socketTCP = new Socket(address,port);
        socketUDP = new DatagramSocket(socketTCP.getLocalPort());
        new Thread(new ReceiveTCP()).start();
        new Thread(new SendTCP()).start();
        bufferSize = 4096;
        frame = new JFrame("Grid Game");

        ControlHandler handler = new ControlHandler(1);
        Controls.load(handler,this);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addKeyListener(handler);

        label = new JLabel();
        frame.getContentPane().add(label);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        new Thread(new ReceiveUDP()).start();
    }

    public JFrame getFrame() {
        return frame;
    }

    private void disconnect(){
        connected = false;
        frame.dispose();
        System.exit(1);
    }

    class SendTCP implements Runnable{
        @Override
        public void run() {
            Scanner in = new Scanner(System.in);
            while (connected){
                try {
                    send(in.nextLine());
                }catch (IOException ignored){}
            }
        }

    }

    void send(String message) throws IOException {
        new DataOutputStream(socketTCP.getOutputStream()).writeUTF(message);
    }

    class ReceiveTCP implements Runnable{

        private final DataInputStream inTCP;
        ReceiveTCP() throws IOException {
            inTCP = new DataInputStream(socketTCP.getInputStream());
        }

        @Override
        public void run() {
            while(connected){
                try {
                    String received = receive();
                    if (received.toCharArray()[0] == '<')
                        System.out.println(received);
                    else {
                        bufferSize = 4096 * Integer.parseInt(received);
                        System.out.println("Changed buffer size to: "+bufferSize);
                    }
                } catch (SocketException ignored){
                    System.out.println(String.format("Disconnected from %s",socketTCP.getRemoteSocketAddress()));
                    disconnect();
                } catch (IOException e) {
                    disconnect();
                    e.printStackTrace();
                }
            }
        }

        String receive() throws IOException {
            return inTCP.readUTF();
        }
    }

    void sendUDP(Movement movement){
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream)){
            objectOutput.writeObject(movement);
            byte[] data = byteArrayOutputStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(
                    data,
                    0,
                    data.length,
                    socketTCP.getRemoteSocketAddress()
            );
            socketUDP.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Event receive() throws IOException{
        byte[] buf = new byte[bufferSize];
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
        socketUDP.receive(packet);
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)){
            return (Event) objectInputStream.readObject();
        }catch (ClassNotFoundException ignored){}
        return null;
    }

    class ReceiveUDP implements Runnable{

        @Override
        public void run() {
            boolean packed = false;
            while(connected){
                try {
                    Event event = receive();
                    if (event instanceof GridUpdateEvent) {
                        grid = ((GridUpdateEvent) event).grid;
                    }
                    if(grid != null) {
                        event.grid = grid;
                        event.function();
                        label.setIcon(new ImageIcon(grid.getGrid()));
                        if (!packed) {
                            frame.pack();
                            packed = true;
                        }
                    }else {
                        send("/requestgrid");
                    }
                } catch (EOFException ignored){
                    System.out.println("EOF");
                    try {
                        send("/requestgrid");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
