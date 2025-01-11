package bg.sofia.uni.fmi.mjt.poll.client;

import bg.sofia.uni.fmi.mjt.poll.command.CommandType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class PollClient {
    private final String host;
    private final int port;
    private final ByteBuffer byteBuffer;

    private static final int DEFAULT_CLOSE = 0;
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;

    private static final String LOCAL_HOST = "localhost";
    private static final String CONNECTED_MSG = "Connected to server successfully.";
    private static final String DISCONNECTED_MSG = "Disconnected from server successfully.";
    private static final String ENTER_COMMAND = "Enter command... ";
    private static final String UNABLE_TO_PROCESS_RSP_ERROR_MSG = "Error occurred while processing server response: ";
    private static final String CLOSED_CONNECTION = "The connection was closed by the server.";

    private boolean clientOnDemand;

    public PollClient(String host, int port) {
        this.host = host;
        this.port = port;
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        clientOnDemand = true;
    }

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open(); Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(host, port));
            System.out.println(CONNECTED_MSG);

            while (clientOnDemand) {
                System.out.print(ENTER_COMMAND);
                String clientInput = scanner.nextLine();

                if (CommandType.DISCONNECT.equalsIgnoreCase(clientInput.trim())) {
                    sendCommandToServer(socketChannel, byteBuffer, clientInput);
                    System.out.println(DISCONNECTED_MSG);
                    clientOnDemand = false;
                    continue;
                }
                sendCommandToServer(socketChannel, byteBuffer, clientInput);

                System.out.println(getServerResponse(socketChannel, byteBuffer));
            }
        } catch (IOException e) {
            System.out.println(UNABLE_TO_PROCESS_RSP_ERROR_MSG + e.getMessage());
        }
    }

    private void sendCommandToServer(SocketChannel socketChannel, ByteBuffer buffer, String clientInput)
        throws IOException {
        buffer.clear();
        buffer.put(clientInput.getBytes());
        buffer.flip();

        socketChannel.write(buffer);
    }

    private String getServerResponse(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead < DEFAULT_CLOSE) {
            socketChannel.close();

            return CLOSED_CONNECTION;
        }
        buffer.flip();
        byte[] serverRsp = new byte[byteBuffer.remaining()];
        byteBuffer.get(serverRsp);

        return new String(serverRsp, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        PollClient client = new PollClient(LOCAL_HOST, PORT);
        client.start();
    }
}