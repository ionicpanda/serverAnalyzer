package com.commercebank.serveranalyzer.BackEnd.Config;

import java.util.List;

public class ConfigCategory {
    private String category;
    private List<ConfigFlag> flags;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<ConfigFlag> getFlags() {
        return flags;
    }

    public void setFlags(List<ConfigFlag> flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("  - category: ");
        result.append(category);
        result.append('\n');
        result.append("    flags:\n");
        for(ConfigFlag flag : flags) {
            result.append(flag.toString());
            result.append('\n');
        }
        return result.toString();
    }
}
