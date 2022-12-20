package cj;

public enum Arch {
    ia64, amd64, x86, arm, arm7;

    public static Arch of(){
        var arch = System.getProperty("os.arch");
        return Arch.valueOf(arch);
    }
}
