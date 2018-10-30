/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.performance.results;

import org.gradle.performance.measure.Amount;
import org.gradle.performance.measure.DataSeries;
import org.gradle.performance.measure.Duration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FormatSupport {
    public static String executionTimestamp() {
        return timestamp(new Date());
    }

    public static String timestamp(Date date) {
        return format(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String date(Date date) {
        return format(date, "yyyy-MM-dd");
    }

    private static String format(Date date, String format) {
        DateFormat timeStampFormat = new SimpleDateFormat(format);
        timeStampFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return timeStampFormat.format(date);
    }

    public static Number getTotalTimeSeconds(MeasuredOperationList baseline, MeasuredOperationList current) {
        return baseline.getTotalTime().getMedian().toUnits(Duration.SECONDS).getValue();
    }

    public static Number getConfidence(MeasuredOperationList baseline, MeasuredOperationList current) {
        return DataSeries.confidenceInDifference(baseline.getTotalTime(), current.getTotalTime());
    }

    public static Number getDifference(MeasuredOperationList baseline, MeasuredOperationList current) {
        return getDifference(baseline.getTotalTime(), current.getTotalTime());
    }

    public static Number getDifference(DataSeries<Duration> baselineVersion, DataSeries<Duration> currentVersion) {
        double base = baselineVersion.getMedian().getValue().doubleValue();
        double current = currentVersion.getMedian().getValue().doubleValue();
        return (current - base) / base;
    }

    public static String formatDifference(DataSeries<Duration> baselineVersion, DataSeries<Duration> currentVersion) {
        Amount<Duration> base = baselineVersion.getMedian();
        Amount<Duration> current = currentVersion.getMedian();
        Amount<Duration> diff = current.minus(base);

        return String.format("%s (%s)", diff.format(), 100.0 * FormatSupport.getDifference(baselineVersion, currentVersion).doubleValue());
    }

    public static String formatConfidence(DataSeries<Duration> baselineVersion, DataSeries<Duration> currentVersion) {
        return String.format("%.1f%%", 100.0 * DataSeries.confidenceInDifference(baselineVersion, currentVersion));
    }
}
