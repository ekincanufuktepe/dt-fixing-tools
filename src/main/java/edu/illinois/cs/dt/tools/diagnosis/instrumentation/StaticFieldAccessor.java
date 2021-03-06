package edu.illinois.cs.dt.tools.diagnosis.instrumentation;

import com.reedoei.testrunner.configuration.Configuration;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StaticFieldAccessor implements FieldAccessor {
    private static URLClassLoader loader;

    public static URLClassLoader loader() {
        if (loader == null) {
            final String str = Configuration.config().getProperty("testrunner.classpath", "");

            final URL[] urls =
                    Arrays.stream(str.split(File.pathSeparator))
                            .flatMap(s -> {
                                try {
                                    return Stream.of(new File(s).toURI().toURL());
                                } catch (MalformedURLException ignored) {}

                                return Stream.empty();
                            })
                            .toArray(URL[]::new);
            loader = new URLClassLoader(urls);
        }

        return loader;
    }

    public static Optional<StaticFieldAccessor> forField(final String fieldName) {
        try {
            final int i = fieldName.lastIndexOf(".");
            final String className = fieldName.substring(0, i);

            final Class<?> fieldClz = loader().loadClass(className);
            final Field field = fieldClz.getDeclaredField(fieldName.substring(i + 1));

            return Optional.of(new StaticFieldAccessor(field));
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    private final Field field;

    private StaticFieldAccessor(final Field field) {
        this.field = field;
    }

    @Override
    public void set(final Object o) {
        try {
            field.setAccessible(true);
            field.set(null, o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object get() {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
