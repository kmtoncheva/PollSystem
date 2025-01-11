package bg.sofia.uni.fmi.mjt.poll.command;

import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;

public class DisconnectCmd extends Command {
    private static final String MSG = "Disconnected";

    DisconnectCmd(PollRepository pollRepository) {
        super(pollRepository);
    }

    @Override
    public String executeCommand(String... args) {
        return MSG;
    }
}