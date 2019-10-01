package org.patriques.output.technicalindicators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.patriques.input.technicalindicators.Interval;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.JsonParser;
import org.patriques.output.technicalindicators.data.MACDData;
import org.patriques.output.technicalindicators.data.RSIData;

/**
 * Representation of the relative strength index (RSI) response from api.
 *
 * @see TechnicalIndicatorResponse
 */
public class RSI extends TechnicalIndicatorResponse<RSIData> {

	private RSI(final Map<String, String> metaData,
			final List<RSIData> indicatorData) {
		super(metaData, indicatorData);
	}

	/**
	 * Creates {@code RSI} instance from json.
	 *
	 * @param interval
	 *            specifies how to interpret the date key to the data json
	 *            object
	 * @param json
	 *            string to parse
	 * @return RSI instance
	 */
	public static RSI from(Interval interval, String json) {
		Parser parser = new Parser(interval);
		return parser.parseJson(json);
	}

	/**
	 * Helper class for parsing json to {@code RSI}.
	 *
	 * @see TechnicalIndicatorParser
	 * @see JsonParser
	 */
	private static class Parser extends TechnicalIndicatorParser<RSI> {

		public Parser(Interval interval) {
			super(interval);
		}

		@Override
		String getIndicatorKey() {
			return "Technical Analysis: RSI";
		}

		@Override
		RSI resolve(Map<String, String> metaData,
				Map<String, Map<String, String>> indicatorData)
				throws AlphaVantageException {
			
			if (indicatorData == null ){
				System.out.println("Should not be here");
			}
			//List<RSIData> indicators = new ArrayList<>();
			indicatorData.forEach((key, values) -> {
//				System.out.println(key + Double.parseDouble(values.get("RSI")));
//				if(key != null && values != null){
//				indicators.add(new RSIData(key, Double.parseDouble(values.get("RSI"))));
//				}else{
//					System.out.println("InsideeeeeeeeeIndicatorData ***************************");
//				}
			});
			

			List<RSIData> indicators = new ArrayList<>();
		      indicatorData.forEach((key, values) -> indicators.add(new RSIData(
		              resolveDate(key),
		              Double.parseDouble(values.get("RSI"))
		      )));
		      return new RSI(metaData, indicators);
		}
	}
}
