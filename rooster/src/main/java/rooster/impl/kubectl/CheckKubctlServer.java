package rooster.impl.kubectl;

import rooster.impl.ProcessFlow;
public class CheckKubctlServer extends ProcessFlow {
    public CheckKubctlServer(){
        super(x -> x.startsWith("Server Version:"),
                "kubectl version --server");
    }
}
