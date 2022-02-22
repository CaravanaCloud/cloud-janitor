package tasktree.kubectl;

import tasktree.ProcessTask;

public class KubectlClient extends ProcessTask {
    public KubectlClient(){
        super(x -> x.startsWith("Client Version:"),
                "kubectl version --client");
    }
}