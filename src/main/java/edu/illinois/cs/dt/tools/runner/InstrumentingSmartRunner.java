package edu.illinois.cs.dt.tools.runner;

import com.reedoei.eunomia.io.capture.CaptureOutStream;
import com.reedoei.testrunner.data.framework.TestFramework;
import com.reedoei.testrunner.data.results.TestRunResult;
import com.reedoei.testrunner.runner.Runner;
import com.reedoei.testrunner.runner.SmartRunner;
import com.reedoei.testrunner.runner.TestInfoStore;
import com.reedoei.testrunner.util.ExecutionInfo;
import com.reedoei.testrunner.util.ExecutionInfoBuilder;
import edu.illinois.cs.dt.tools.diagnosis.instrumentation.JavaAgent;
import edu.illinois.cs.dt.tools.diagnosis.instrumentation.StaticTracer;
import edu.illinois.cs.dt.tools.diagnosis.instrumentation.TracerMode;
import org.apache.maven.project.MavenProject;
import scala.Option;
import scala.collection.immutable.Stream;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

public class InstrumentingSmartRunner extends SmartRunner {
    private final String prefixes;
    private final String javaAgent;

    public static InstrumentingSmartRunner fromRunner(final Runner runner) {
        if (runner instanceof SmartRunner) {
            return new InstrumentingSmartRunner(runner.project(), runner.framework(), ((SmartRunner) runner).info());
        } else {
            return new InstrumentingSmartRunner(runner.project(), runner.framework(), new TestInfoStore());
        }
    }

    private InstrumentingSmartRunner(final MavenProject mavenProject, final TestFramework testFramework, final TestInfoStore infoStore) {
        super(mavenProject, testFramework, infoStore);

        final URL url = JavaAgent.class.getProtectionDomain().getCodeSource().getLocation();
        this.javaAgent = url.getFile();

        this.prefixes = getPrefixes();
    }

    private String getPrefixes() {
        return getPrefixes(Paths.get(project().getBuild().getOutputDirectory())) + "," +
               getPrefixes(Paths.get(project().getBuild().getTestOutputDirectory()));
    }

    private String getPrefixes(final Path dir) {
        final Path longest = dir.relativize(getLongest(dir).toAbsolutePath());

        if (longest.getNameCount() < 3) {
            try {
                final List<Path> paths =
                        Files.walk(dir.resolve(longest), 3)
                        .filter(Files::isDirectory)
                        .map(dir::relativize)
                        .collect(Collectors.toList());

                final OptionalInt pathCount = paths.stream().mapToInt(Path::getNameCount).max();

                if (pathCount.isPresent()) {
                    return paths.stream()
                            .filter(p -> p.getNameCount() == pathCount.getAsInt())
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return longest.toString();
    }

    private Path getLongest(final Path path) {
        try {
            final List<Path> list =
                    Files.list(path)
                            .filter(Files::isDirectory)
                            .filter(p -> p.getFileName().toString().toLowerCase().equals(p.getFileName().toString()))
                            .collect(Collectors.toList());

            if (list.size() == 1) {
                return getLongest(list.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }

    @Override
    public ExecutionInfo execution(final Stream<String> testOrder, final ExecutionInfoBuilder executionInfoBuilder) {
        // NOTE: If you're trying to do something inside the executor and it's not printing, the calls to inheritIO
        // are probably the cause. You probably want to use true instead.
        if (!StaticTracer.mode().equals(TracerMode.NONE) && javaAgent != null) {
            return super.execution(testOrder,
                    executionInfoBuilder
                            .inheritIO(false)
                            .addProperty("dtfixingtools.transformer.class_prefix", prefixes)
                            .javaAgent(Paths.get(javaAgent)));
        } else {
            return super.execution(testOrder, executionInfoBuilder
                    .inheritIO(false));
//            );
        }
    }

    @Override
    public Option<TestRunResult> runWithCp(final String cp, final Stream<String> testOrder) {
        return new CaptureOutStream<>(() -> super.runWithCp(cp, testOrder)).run().valueRequired();
    }
}