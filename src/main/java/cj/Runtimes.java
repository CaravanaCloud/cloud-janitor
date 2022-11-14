package cj;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class Runtimes {
    @Produces
    public Runtime getRuntime(){
        return Runtime.getRuntime();
    }
}
