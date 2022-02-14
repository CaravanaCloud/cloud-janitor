package rooster;

import io.quarkus.runtime.QuarkusApplication;

public class Rooster implements QuarkusApplication{

    @Override
    public int run(String... args) throws Exception {
        System.out.println("🐓");
        return 0;
    }

}