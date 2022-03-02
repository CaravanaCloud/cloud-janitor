package tasktree.app;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import java.util.Random;

@Dependent
public class TaskSelector extends Composite<Div> {

    public TaskSelector() {
    }

    @PostConstruct
    private void init() {
        var hello = new Label(" - Hello ");
        var world = new Label(" -   World ");
        var random = new Label("" + new Random().nextInt());
        getContent().add(hello, world, random);

    }

}
