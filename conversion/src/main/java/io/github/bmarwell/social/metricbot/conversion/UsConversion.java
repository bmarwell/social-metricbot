/*
 * Copyright 2019-2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.conversion;

import static java.util.stream.Collectors.joining;

import io.github.bmarwell.social.metricbot.conversion.converters.UsUnitConverter;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Dependent
public class UsConversion {

    private Instance<UsUnitConverter> converters;

    public UsConversion() {
        // injection
    }

    @Inject
    public UsConversion(final @Any Instance<UsUnitConverter> converters) {
        this.converters = converters;
    }

    public String returnConverted(final String input) {
        return returnConverted(input, ", ");
    }

    public String returnConverted(final String input, final String separator) {
        final LinkedHashSet<io.github.bmarwell.social.metricbot.conversion.UnitConversion> outputUnits =
                this.converters.stream()
                        .filter(converter -> converter.matches(input))
                        .map(converter -> converter.getConvertedUnits(input))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        final List<String> collect = outputUnits.stream()
                .map(conv -> String.format(
                        "%s%s=%s%s",
                        conv.getInputAmount(), conv.getInputUnit(), conv.getMetricAmount(), conv.getMetricUnit()))
                .distinct()
                .collect(Collectors.toList());

        return String.join(separator, collect);
    }

    public boolean containsUsUnits(final String text) {
        return this.converters.stream().anyMatch(converters -> converters.matches(text));
    }

    public String getSerchTerms() {
        return this.converters.stream().map(this::toOrSeparatedQuotedTerms).collect(joining(" OR "));
    }

    private String toOrSeparatedQuotedTerms(final UsUnitConverter usUnitConverter) {
        final String orJoined = usUnitConverter.getSearchTerms().stream()
                .map(term -> String.format("\"%s\"", term))
                .collect(joining(" OR "));

        return String.format("(%s)", orJoined);
    }

    public Instance<UsUnitConverter> getConverters() {
        return this.converters;
    }

    public void setConverters(final Instance<UsUnitConverter> converters) {
        this.converters = converters;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UsConversion.class.getSimpleName() + "[", "]")
                .add("converters=" + this.converters)
                .toString();
    }
}
