package cj.hello;

import cj.Configuration;
import cj.Input;
import cj.LocalInput;
import cj.StartupObserver;

import static cj.LocalInput.message;

public class HelloStartup extends StartupObserver {
    @Override
    protected void onStart() {
        describeInput(message,
                "Input message to display",
                "cj.hello.message",
                cfg -> cfg.hello().message(),
                () -> "Hello World!!!",
                "That same great message!!!",
                new String[]{"Any string is fine"},
                false);
    }
}
