package rooster.impl.kubectl;

import rooster.impl.ProcessFlow;

public class CheckKubctlClient extends ProcessFlow {
    public CheckKubctlClient(){
        super(x -> x.startsWith("Client Version:"),
                "kubectl version --client");
    }
}