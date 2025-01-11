package bg.sofia.uni.fmi.mjt.poll.command;

import bg.sofia.uni.fmi.mjt.poll.server.repository.PollRepository;

public abstract class Command {
    PollRepository pollRepository;

    Command(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
    }

    public abstract String executeCommand(String... args);
}
