package rooster.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Flows {
  static final ServiceLoader<Flow> loader = ServiceLoader.load(Flow.class);

  public static Iterator<Flow> providers() {
    return providers(true);
  }
  public static Iterator<Flow> providers(boolean refresh) {
      if (refresh) {
          loader.reload();
      }
      return loader.iterator();
  }


}
