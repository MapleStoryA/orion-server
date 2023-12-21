package server.base.config;

public enum Environment {
    LOCAL,
    PROD,
    ;

    public static Environment resolve() {
        var env = System.getenv("ENV");
        if (env != null) {
            return Environment.valueOf(env.toUpperCase());
        }
        return LOCAL;
    }
}
