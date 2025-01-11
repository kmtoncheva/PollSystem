package bg.sofia.uni.fmi.mjt.poll.server.repository;

import bg.sofia.uni.fmi.mjt.poll.server.model.Poll;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPollRepository implements PollRepository {
    private final Map<Integer, Poll> allPolls = new ConcurrentHashMap<>();

    @Override
    public int addPoll(Poll poll) {
        int id = allPolls.size();
        allPolls.put(id, poll);

        return id;
    }

    @Override
    public Poll getPoll(int pollId) {
        if (!allPolls.containsKey(pollId)) {
            return null;
        }

        return allPolls.get(pollId);
    }

    @Override
    public Map<Integer, Poll> getAllPolls() {
        return Map.copyOf(allPolls);
    }

    @Override
    public void clearAllPolls() {
        allPolls.clear();
    }
}