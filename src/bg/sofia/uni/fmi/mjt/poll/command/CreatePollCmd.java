package bg.sofia.uni.fmi.mjt.poll.command;

import bg.sofia.uni.fmi.mjt.poll.server.model.Poll;
import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;

import java.util.HashMap;
import java.util.Map;

public class CreatePollCmd extends Command {
    private static final int MIN_ANSWERS_COUNT = 2;
    private static final int FIRST_INDEX_OF_ANSWERS = 1;
    private static final int DEFAULT_COUNT = 0;
    private static final String INVALID_COMMAND_ARGS_MSG =
        "{\"status\":\"ERROR\",\"message\":\"Usage: create-poll <question> <option-1> <option-2> [... <option-N>]\"}";
    private static final String CREATED_SUCCESSFULLY_MSG =
        "{\"status\":\"OK\",\"message\":\"Poll %d created successfully.\"}";

    CreatePollCmd(PollRepository pollRepository) {
        super(pollRepository);
    }

    @Override
    public String executeCommand(String... args) {
        if (args.length <= MIN_ANSWERS_COUNT) {
            return INVALID_COMMAND_ARGS_MSG;
        }

        Map<String, Integer> answers = new HashMap<>();
        for (int i = FIRST_INDEX_OF_ANSWERS; i < args.length; i++) {
            answers.put(args[i], DEFAULT_COUNT);
        }

        int id = pollRepository.addPoll(new Poll(args[DEFAULT_COUNT], answers));

        return String.format(CREATED_SUCCESSFULLY_MSG, id);
    }
}