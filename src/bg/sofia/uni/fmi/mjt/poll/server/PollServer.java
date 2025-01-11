package bg.sofia.uni.fmi.mjt.poll.server;

import bg.sofia.uni.fmi.mjt.poll.command.CommandHandler;
import bg.sofia.uni.fmi.mjt.poll.server.repository.InMemoryPollRepository;
import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class PollServer {
    private final int port;
    private final PollRepository pollRepository;

    private ByteBuffer byteBuffer;
    private Selector selector;

    boolean serverOnDemand;
    private final CommandHandler commandHandler;

    private static final int DEFAULT_CLOSE = 0;
    private static final int PORT = 8080;
    private static final int BYTE_BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";
    private static final String UNABLE_TO_START_SERVER_ERROR_MSG = "Failed to start the server.";
    private static final String UNABLE_TO_PROCESS_RQST_ERROR_MSG = "Error occurred while processing client request: ";
    private static final String NEW_CLIENT_CONNECTED = "A new client was connected.";
    private static final String SERVER_READY = "Server is ready.";

    public PollServer(int port, PollRepository pollRepository) {
        this.port = port;
        this.pollRepository = pollRepository;
        commandHandler = new CommandHandler(pollRepository);
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocket(serverSocketChannel, selector);

            this.byteBuffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
            serverOnDemand = true;

            System.out.println(SERVER_READY);

            while (serverOnDemand) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == DEFAULT_CLOSE) {
                        continue;
                    }

                    handleSelector();

                } catch (IOException e) {
                    System.out.println(UNABLE_TO_PROCESS_RQST_ERROR_MSG + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(UNABLE_TO_START_SERVER_ERROR_MSG, e);
        }
    }

    public void handleSelector() throws IOException {
        Iterator<SelectionKey> readyKeys = selector.selectedKeys().iterator();

        while (readyKeys.hasNext()) {
            SelectionKey currentKey = readyKeys.next();

            if (currentKey.isReadable()) {
                SocketChannel clientSocketChannel = (SocketChannel) currentKey.channel();
                String clientInput = readFromClient(clientSocketChannel);

                if (clientInput == null) {
                    clientSocketChannel.close();
                    continue;
                }

                String resultFromCommand = commandHandler.executeCmd(clientInput);
                if (resultFromCommand.equalsIgnoreCase("disconnected")) {
                    clientSocketChannel.close();
                }

                writeToClient(clientSocketChannel, resultFromCommand);
            } else if (currentKey.isAcceptable()) {
                acceptClient(selector, currentKey);

                System.out.println(NEW_CLIENT_CONNECTED);
            }

            readyKeys.remove();
        }
    }

    public void stop() {
        this.serverOnDemand = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocket(ServerSocketChannel serverSocketChannel, Selector selector)
        throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(HOST, this.port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String readFromClient(SocketChannel clientSocketChannel) throws IOException {
        byteBuffer.clear();

        int bytesRead = clientSocketChannel.read(byteBuffer);
        if (bytesRead < DEFAULT_CLOSE) {
            clientSocketChannel.close();
            return null;
        }

        byteBuffer.flip();

        byte[] clientMsg = new byte[byteBuffer.remaining()];
        byteBuffer.get(clientMsg);

        return new String(clientMsg, StandardCharsets.UTF_8);
    }

    private void writeToClient(SocketChannel socketChannel, String response) throws IOException {
        byteBuffer.clear();
        byteBuffer.put(response.getBytes());
        byteBuffer.flip();

        socketChannel.write(byteBuffer);
    }

    private void acceptClient(Selector selector, SelectionKey key) throws IOException {
        SocketChannel clientSocket = ((ServerSocketChannel) key.channel()).accept();

        clientSocket.configureBlocking(false);
        clientSocket.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        PollRepository repository = new InMemoryPollRepository();
        PollServer server = new PollServer(PORT, repository);
        server.start();
    }
}