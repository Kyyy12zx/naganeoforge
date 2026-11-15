package com.leon.saintsdragons.neoforge.platform;

import com.leon.saintsdragons.platform.ConfigHelper;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

/** NeoForge-backed implementation of the shared config builder. */
public final class NeoForgeConfigHelper implements ConfigHelper {
    private final ModContainer container;

    public NeoForgeConfigHelper(ModContainer container) {
        this.container = container;
    }

    @Override
    public ConfigBuilder commonBuilder(String fileName) {
        return new Builder(container, fileName);
    }

    private static final class Builder implements ConfigBuilder {
        private final ModContainer container;
        private final String fileName;
        private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        private Builder(ModContainer container, String fileName) {
            this.container = container;
            this.fileName = fileName;
        }

        @Override public void push(String category) { builder.push(category); }
        @Override public void pop() { builder.pop(); }
        @Override public void comment(String comment) { builder.comment(comment); }

        @Override
        public IntValue defineInt(String key, int defaultValue, int min, int max) {
            return new IntValueAdapter(builder.defineInRange(key, defaultValue, min, max));
        }

        @Override
        public DoubleValue defineDouble(String key, double defaultValue, double min, double max) {
            return new DoubleValueAdapter(builder.defineInRange(key, defaultValue, min, max));
        }

        @Override
        public BooleanValue defineBoolean(String key, boolean defaultValue) {
            return new BooleanValueAdapter(builder.define(key, defaultValue));
        }

        @Override
        public ListValue defineList(String key, List<String> defaultValue) {
            return new ListValueAdapter(builder.defineListAllowEmpty(key, defaultValue, value -> value instanceof String));
        }

        @Override
        public void build() {
            ModConfigSpec spec = builder.build();
            container.registerConfig(ModConfig.Type.COMMON, spec, fileName);
        }
    }

    private abstract static class ValueAdapter<T> {
        final ModConfigSpec.ConfigValue<T> value;
        ValueAdapter(ModConfigSpec.ConfigValue<T> value) { this.value = value; }
        void save() { value.save(); }
    }

    private static final class IntValueAdapter extends ValueAdapter<Integer> implements IntValue {
        IntValueAdapter(ModConfigSpec.IntValue value) { super(value); }
        @Override public int get() { return value.get(); }
        @Override public void set(int next) { value.set(next); }
        @Override public void save() { super.save(); }
    }

    private static final class DoubleValueAdapter extends ValueAdapter<Double> implements DoubleValue {
        DoubleValueAdapter(ModConfigSpec.DoubleValue value) { super(value); }
        @Override public double get() { return value.get(); }
        @Override public void set(double next) { value.set(next); }
        @Override public void save() { super.save(); }
    }

    private static final class BooleanValueAdapter extends ValueAdapter<Boolean> implements BooleanValue {
        BooleanValueAdapter(ModConfigSpec.BooleanValue value) { super(value); }
        @Override public boolean get() { return value.get(); }
        @Override public void set(boolean next) { value.set(next); }
        @Override public void save() { super.save(); }
    }

    private static final class ListValueAdapter extends ValueAdapter<List<? extends String>> implements ListValue {
        ListValueAdapter(ModConfigSpec.ConfigValue<List<? extends String>> value) { super(value); }
        @Override public List<String> get() { return new ArrayList<>(value.get()); }
        @Override public void set(List<String> next) { value.set(next); }
        @Override public void save() { super.save(); }
    }
}
