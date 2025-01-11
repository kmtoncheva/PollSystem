package bg.sofia.uni.fmi.mjt.poll.command;

import bg.sofia.uni.fmi.mjt.poll.server.model.Poll;
import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;

import java.util.Map;

public class ListCmd extends Command {
    public static final String NO_ACTIVE_POLL_MSG = "{\"status\":\"ERROR\",\"message\":\"No active polls available.\"}";
    public static final String OK_STATUS_MSG = "{\"status\":\"OK\",\"polls\":";

    ListCmd(PollRepository pollRepository) {
        super(pollRepository);
    }

    @Override
    public String executeCommand(String... args) {
        Map<Integer, Poll> polls = pollRepository.getAllPolls();

        if (pollRepository.getAllPolls().isEmpty()) {
            return NO_ACTIVE_POLL_MSG;
        }

        return OK_STATUS_MSG + polls + "}";
    }
}