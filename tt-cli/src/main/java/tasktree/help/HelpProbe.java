package tasktree.help;

import tasktree.BaseProbe;
import tasktree.spi.Sample;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("help")
@ApplicationScoped
public class HelpProbe extends BaseProbe {
    @Override
    public Sample call() {
        return Sample.withOutput("help","Take it easy, it's just a help probe.");
    }
}
