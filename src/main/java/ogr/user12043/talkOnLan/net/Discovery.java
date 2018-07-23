package ogr.user12043.talkOnLan.net;

import ogr.user12043.talkOnLan.util.Constants;

import java.io.IOException;
import java.net.*;

/**
 * Created by user12043 on 23.07.2018 - 16:32
 * part of project: talk-onLan
 */
public class Discovery extends Thread implements Runnable {
    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;

    public Discovery() throws UnknownHostException, SocketException {
        receiveSocket = new DatagramSocket(Constants.receivePort, InetAddress.getLocalHost());
        receiveSocket.setBroadcast(true);
        sendSocket = new DatagramSocket(Constants.sendPort, InetAddress.getLocalHost());
        sendSocket.setBroadcast(true);
    }

    public void receive() throws IOException {
        byte[] buffer = Constants.discoveryPacketRequestData.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        receiveSocket.receive(packet);
        System.out.println("Discovery package received! -> " + packet.getAddress() + " : " + packet.getPort());
        String data = new String(packet.getData()).trim();
        if (data.equals(Constants.discoveryPacketRequestData)) {
            byte[] response = Constants.discoveryPacketResponseData.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
            receiveSocket.send(responsePacket);
            System.out.println("Response sent to: " + packet.getAddress() + " : " + packet.getPort());
        } else {
            System.err.println("Error, not valid package!" + packet.getAddress() + " : " + packet.getPort());
        }
    }

    public void send() throws IOException {
        DatagramSocket socket = new DatagramSocket(Constants.sendPort, InetAddress.getLocalHost());
        socket.setBroadcast(true);
        byte[] data = Constants.discoveryPacketRequestData.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), Constants.receivePort);
        socket.send(packet);
        System.out.println("Discovery package sent!" + packet.getAddress() + " : " + packet.getPort());
        byte[] response = Constants.discoveryPacketResponseData.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(response, response.length);
        socket.receive(responsePacket);
        System.out.println("Discovery response received!" + responsePacket.getAddress() + " : " + responsePacket.getPort());
        String responseData = new String(responsePacket.getData()).trim();
        if (responseData.equals(Constants.discoveryPacketResponseData)) {
            System.out.println("Found buddy!" + responsePacket.getAddress() + " : " + responsePacket.getPort());
        }
    }

    @Override
    public void run() {
        super.run();
    }
}
