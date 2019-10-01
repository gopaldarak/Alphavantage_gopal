package org.patriques.output.technicalindicators.data;

import java.time.LocalDateTime;

/**
 * Representation of MAMA indicator json objects, i.e: "2017-12-01 16:00":
 * "MACD_Signal": "-0.0265", "MACD_Hist": "-0.0074", "MACD": "-0.0339" }
 */
public class RSIData {
	private final LocalDateTime datetime;
	private final double value;

	public RSIData(LocalDateTime datetime, double value) {
		this.datetime = datetime;
		this.value = value;
	}

	public LocalDateTime getDateTime() {
		return datetime;
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return  datetime.toString().substring(0, 10)
				.concat(",RSI=,")
				.concat(String.format("%.2f", value));
	}

}
