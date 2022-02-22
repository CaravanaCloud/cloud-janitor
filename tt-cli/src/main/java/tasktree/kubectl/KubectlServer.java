package tasktree.kubectl;

import tasktree.ProcessTask;
public class KubectlServer extends ProcessTask {
    public KubectlServer(){
        super(x -> x.startsWith("Server Version:"),
                "kubectl version --server");
    }
}
