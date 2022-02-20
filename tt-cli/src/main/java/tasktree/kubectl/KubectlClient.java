package tasktree.kubectl;

import tasktree.ProcessProbe;

public class KubectlClient extends ProcessProbe {
    public KubectlClient(){
        super(x -> x.startsWith("Client Version:"),
                "kubectl version --client");
    }
}