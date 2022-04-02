package tasktree.visitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasktree.Result;
import tasktree.ResultType;
import tasktree.spi.Task;

import java.util.*;

public abstract class TaskVisitor implements Visitor {
    protected static final Logger log = LoggerFactory.getLogger(TaskVisitor.class);

    Map<ResultType, List<Result>> resultsMap = new HashMap<>();

    protected void collect(Result result){
        if (result == null) return;
        var resultType = result.getType();
        var results = resultsMap.getOrDefault(
                resultType,
                new ArrayList<>());
        results.add(result);
        resultsMap.put(resultType, results);
    }

    protected List<Result> getResults(ResultType type){
        return resultsMap.get(type);
    }

    public List<Result> getResults(){
        var results = resultsMap.values().stream()
                .flatMap(Collection::stream)
                .toList();
        return new ArrayList<Result>(results);
    }
}
