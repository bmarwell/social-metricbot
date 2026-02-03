/*
 * Copyright 2020-2023 The social-metricbot contributors
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
package io.github.bmarwell.social.metricbot.conversion.converters;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import io.github.bmarwell.social.metricbot.conversion.DecimalFormats;
import io.github.bmarwell.social.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.social.metricbot.conversion.UnitConversion;
import jakarta.enterprise.context.Dependent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class HorsePowerConverter implements io.github.bmarwell.social.metricbot.conversion.converters.UsUnitConverter {

    private static final long serialVersionUID = -4122166957202486050L;

    private static final Logger LOG = LoggerFactory.getLogger(HorsePowerConverter.class);

    private static final Pattern PATTERN_HP = Pattern.compile(
            "\\b((?:[0-9],)*(?:[0-9]*[.]?)?[0-9]+)[^A-Za-z0-9]?(?:\\s|\\b)?hp(?:s)?\\b",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    private static final double KW_PER_HP = 0.7457d;

    private static final NumberFormat FORMAT_HP = DecimalFormats.noFractionDigits();
    private static final NumberFormat FORMAT_KW = DecimalFormats.atMostOneFractionDigits();

    @Override
    public List<String> getSearchTerms() {
        return emptyList();
    }

    @Override
    public boolean matches(final String text) {
        if (null == text || text.isEmpty()) {
            return false;
        }

        return PATTERN_HP.matcher(text).find();
    }

    @Override
    public Collection<UnitConversion> getConvertedUnits(final String text) {
        if (null == text || text.isEmpty()) {
            return emptyList();
        }

        final List<UnitConversion> conversions = new ArrayList<>();
        final Matcher matcher = PATTERN_HP.matcher(text);
        while (matcher.find()) {
            try {
                final String hpNum = matcher.group(1).replaceAll(",", "");
                final double hpDecimal = Double.parseDouble(hpNum);
                final double kwDecimal = hpDecimal * KW_PER_HP;

                final UnitConversion unitConversion = ImmutableUnitConversion.builder()
                        .inputAmount(FORMAT_HP.format(hpDecimal))
                        .inputUnit("hp")
                        .metricAmount(FORMAT_KW.format(kwDecimal))
                        .metricUnit("kW")
                        .build();

                conversions.add(unitConversion);
            } catch (final RuntimeException rtEx) {
                LOG.error("Unable to convert found text: [{}].", text, rtEx);
            }
        }

        return unmodifiableList(conversions);
    }
}
