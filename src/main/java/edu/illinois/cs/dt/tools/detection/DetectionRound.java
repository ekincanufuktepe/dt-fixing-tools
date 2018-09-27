package edu.illinois.cs.dt.tools.detection;

import com.google.gson.Gson;
import edu.illinois.cs.dt.tools.runner.data.DependentTest;
import edu.illinois.cs.dt.tools.runner.data.DependentTestList;

import java.util.List;

/**
 * Simple wrapper around a list of tests and a time, to represent each round run by the Executing Detector.
 * Exists mostly so that GSON can serialize just one objects
 */
public class DetectionRound {
    private final DependentTestList unfilteredTests;
    private final DependentTestList filteredTests;
    private final double roundTime;

    public DetectionRound(final List<DependentTest> unfiltered, final List<DependentTest> filtered, final double roundTime) {
        this.unfilteredTests = new DependentTestList(unfiltered);
        this.filteredTests = new DependentTestList(filtered);
        this.roundTime = roundTime;
    }

    public double roundTime() {
        return roundTime;
    }

    public DependentTestList unfilteredTests() {
        return unfilteredTests;
    }

    public DependentTestList filteredTests() {
        return filteredTests;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
