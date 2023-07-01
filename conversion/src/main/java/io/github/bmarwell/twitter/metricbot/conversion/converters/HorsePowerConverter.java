package io.github.bmarwell.twitter.metricbot.conversion.converters;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import io.github.bmarwell.twitter.metricbot.conversion.DecimalFormats;
import io.github.bmarwell.twitter.metricbot.conversion.ImmutableUnitConversion;
import io.github.bmarwell.twitter.metricbot.conversion.UnitConversion;
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
public class HorsePowerConverter implements UsUnitConverter {

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
