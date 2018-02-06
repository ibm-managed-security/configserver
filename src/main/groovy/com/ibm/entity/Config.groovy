package com.ibm.entity

class Config {

    String name
    String profile
    String label
    ConfigFormat format
    String content

    Config() {

    }

    Config(String name, String profile, String label, ConfigFormat format, String content) {
        this.format = format
        this.name = name
        this.profile = profile
        this.label = label
        this.content = content
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getProfile() {
        return profile
    }

    void setProfile(String profile) {
        this.profile = profile
    }

    String getLabel() {
        return label
    }

    void setLabel(String label) {
        this.label = label
    }

    ConfigFormat getFormat() {
        return format
    }

    void setFormat(ConfigFormat type) {
        this.format = type
    }

    String getContent() {
        return content
    }

    void setContent(String content) {
        this.content = content
    }


    @Override
    public String toString() {
        return "Config{" +
                "name='" + name + '\'' +
                ", profile='" + profile + '\'' +
                ", label='" + label + '\'' +
                ", format=" + format +
                '}';
    }
}
