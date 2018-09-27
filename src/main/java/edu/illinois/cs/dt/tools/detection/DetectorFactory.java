package edu.illinois.cs.dt.tools.detection;

import com.reedoei.testrunner.configuration.Configuration;
import com.reedoei.testrunner.mavenplugin.TestPluginPlugin;
import com.reedoei.testrunner.runner.Runner;

import java.util.List;

public class DetectorFactory {
    public static final int ROUNDS = Configuration.config().getProperty("dt.randomize.rounds", 20);

    public static Detector makeDetector(final Runner runner, final List<String> tests) {
        return new RandomDetector(runner, ROUNDS, tests);
    }
}
