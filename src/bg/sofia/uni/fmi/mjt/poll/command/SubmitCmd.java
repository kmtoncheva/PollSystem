package bg.sofia.uni.fmi.mjt.poll.command;

import bg.sofia.uni.fmi.mjt.poll.server.model.Poll;
import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;

import java.util.Map;

public class SubmitCmd extends Command {
    private static final int MIN_COUNT_ARGS = 2;
    private static final int DEFAULT_COUNT = 0;
    private static final int ANS_INDEX = 1;

    private static final String INVALID_COMMAND_ARGS_MSG =
        "{\"status\":\"ERROR\",\"message\":\"Usage: submit-vote <poll-id> <option>\"}";
    private static final String INVALID_ID_MSG =
        "{\"status\":\"ERROR\",\"message\":\"Poll with ID %d does not exist.\"}";
    private static final String INVALID_VOTE_MSG =
        "{\"status\":\"ERROR\",\"message\":\"Invalid option. Option %s does not exist.\"}";
    private static final String VOTED_SUCCESSFULLY_MSG =
        "{\"status\":\"OK\",\"message\":\"Vote submitted successfully for option: %s \"}";

    SubmitCmd(PollRepository pollRepository) {
        super(pollRepository);
    }

    @Override
    public String executeCommand(String... args) {
        if (args.length != MIN_COUNT_ARGS) {
            return INVALID_COMMAND_ARGS_MSG;
        }
        int id;
        try {
            id = Integer.parseInt(args[DEFAULT_COUNT]);
        } catch (NumberFormatException e) {
            return INVALID_ID_MSG;
        }

        Poll poll = pollRepository.getPoll(id);

        if (poll == null) {
            return String.format(INVALID_ID_MSG, id);
        }

        String vote = args[ANS_INDEX];
        Map<String, Integer> options = poll.options();
        if (!options.containsKey(vote)) {
            return String.format(INVALID_VOTE_MSG, vote);
        }
        options.put(vote, options.get(vote) + ANS_INDEX);

        return String.format(VOTED_SUCCESSFULLY_MSG, vote);
    }
}