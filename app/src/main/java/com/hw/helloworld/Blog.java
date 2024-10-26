package com.hw.helloworld;

public class Blog {
    private int id;
    private String name;
    private String slug;
    private String description;
    private String logo_url;
    private String createdAt;
    private String updatedAt;
    private Integer UserId;
    private Integer BlogCategoryId;
    private Integer BlogThemeId;
    private String public_key;

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }
    public String getLogoUrl() {
        return "https://www.brandbucket.com/sites/default/files/logo_uploads/204472/large_bloggrs_0.png";
    }

    public String getSlug() {
        return this.slug;
    }

    public String getPublicKey() {
        return this.public_key;
    }
    // Getters and setters for each field
    // ...
}
