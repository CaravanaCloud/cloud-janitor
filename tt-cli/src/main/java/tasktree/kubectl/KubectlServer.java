package tasktree.kubectl;

import tasktree.ProcessProbe;
public class KubectlServer extends ProcessProbe {
    public KubectlServer(){
        super(x -> x.startsWith("Server Version:"),
                "kubectl version --server");
    }
}
