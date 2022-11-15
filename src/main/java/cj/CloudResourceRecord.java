package cj;

import java.util.Map;

public class CloudResourceRecord {
    CloudResourceType type;
    String placement;
    String name;
    Map<String, String> properties;
    Boolean matchesFilter;
}
