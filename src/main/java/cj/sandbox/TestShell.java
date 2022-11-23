package cj.sandbox;

import cj.BaseTask;

import javax.enterprise.context.Dependent;
import javax.inject.Named;
import java.util.regex.Pattern;

@Dependent
@Named("test-shell")
public class TestShell extends BaseTask {
    public static void main(String[] args) {
        var replacement = "";
        var regex = "level=([\\S]+) msg= [\\s?]";
        var pattern = Pattern.compile(regex);
        var line = "level=debug msg=  Fetching Ironic bootstrap credentials... ";
        var line2 = "Something Elso credentials... ";
        var match = pattern.matcher(line);
        var matches = match.matches();
        var out = "???";
        if (matches){
            out = match.replaceAll(replacement);
        }

        System.out.println(out);
        System.out.println(line.replaceAll(regex,replacement));
        System.out.println(line2.replaceAll(regex,replacement));

        System.out.println("--");
    }

}
