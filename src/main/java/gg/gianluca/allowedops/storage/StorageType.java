package gg.gianluca.allowedops.storage;

public enum StorageType {
    FLATFILE,
    SQL;

    public static StorageType from(final String value) {
        if (value == null) {
            return FLATFILE;
        }
        return switch (value.trim().toLowerCase()) {
            case "sql", "database", "mysql", "mariadb", "postgresql", "postgres", "sqlite" -> SQL;
            default -> FLATFILE;
        };
    }
}
