package bg.sofia.uni.fmi.mjt.poll.command;

import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;

public class CommandHandler {
    private final PollRepository pollRepository;

    private static final String UNKNOWN_CMD = "{\"status\":\"ERROR\",\"message\":\"Unknown command.\"}";
    private static final int CMD_INDEX = 0;
    private static final int LEN = 1;
    private static final int LIMIT = 2;

    public CommandHandler(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    public String executeCmd(String clientInput) {
        String sanitizedInput = clientInput.trim();

        String[] tokens = sanitizedInput.split(" ", LIMIT);
        String[] args = tokens.length > LEN ? tokens[LEN].split(" ") : new String[CMD_INDEX];

        Command cmd = createCmd(tokens[CMD_INDEX]);
        if (cmd == null) {
            return UNKNOWN_CMD;
        }

        return cmd.executeCommand(args);
    }

    private Command createCmd(String cmdName) {

        return switch (cmdName) {
            case CommandType.CREATE_POLL -> new CreatePollCmd(pollRepository);
            case CommandType.LIST_POLLS -> new ListCmd(pollRepository);
            case CommandType.SUBMIT_VOTE -> new SubmitCmd(pollRepository);
            case CommandType.DISCONNECT -> new DisconnectCmd(pollRepository);
            default -> null;
        };
    }
}