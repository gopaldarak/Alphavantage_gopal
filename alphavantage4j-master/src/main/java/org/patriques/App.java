package org.patriques;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.patriques.input.technicalindicators.FastKPeriod;
import org.patriques.input.technicalindicators.FastPeriod;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.SignalPeriod;
import org.patriques.input.technicalindicators.SlowDMaType;
import org.patriques.input.technicalindicators.SlowDPeriod;
import org.patriques.input.technicalindicators.SlowKMaType;
import org.patriques.input.technicalindicators.SlowKPeriod;
import org.patriques.input.technicalindicators.SlowPeriod;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.technicalindicators.MACD;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.STOCH;
import org.patriques.output.technicalindicators.data.MACDData;
import org.patriques.output.technicalindicators.data.RSIData;
import org.patriques.output.technicalindicators.data.STOCHDataSlow;

public class App {
	public static Map<String, RSIData> ups = new HashMap<String, RSIData>();
	public static Map<String, RSIData> downs = new HashMap<String, RSIData>();

	public static Map<String, RSIData> dailyups = new HashMap<String, RSIData>();
	public static Map<String, RSIData> dailydowns = new HashMap<String, RSIData>();

	public static void main(String[] args) throws Exception {
		String apiKey = "J716XINOZ7UCVCTG";
		int timeout = 30000;
		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey,
				timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(
				apiConnector);
//		Connection conn = DBUtils.getConnection();
//
//		ArrayList<String> selectStocks = DBUtils.selectStocks(conn);
		ArrayList<String> selectStocks = new ArrayList<String>();
		selectStocks.add("CULP");

		for (String stock : selectStocks) {
			try {
				Thread.sleep(5500);
				RSI response = technicalIndicators.rsi(stock.trim(),
						Interval.DAILY, TimePeriod.of(15), SeriesType.CLOSE);
				List<RSIData> rsiData = response.getData().stream().limit(1)
						.collect(Collectors.toList());
				calculateBigMovement(response.getData().stream().limit(5)
						.collect(Collectors.toList()), stock.trim());
				rsiData.forEach(data -> {
					System.out.println("RSI :" + stock.trim() + ":"
							+ data.getDateTime().toString().substring(0, 10)
							+ ":" + data.getValue());
				});
				
				Thread.sleep(5500);
				
				MACD macdResponse = technicalIndicators.macd(stock.trim(), Interval.DAILY, TimePeriod.of(15), SeriesType.CLOSE, FastPeriod.of(12), SlowPeriod.of(26), SignalPeriod.of(9));
				
				List<MACDData> macdData = macdResponse.getData().stream().limit(1).collect(Collectors.toList());
				
				macdData.forEach(data -> {
					System.out.println("MACD:"+stock.trim() + ":"
							+ data.getDateTime().toString().substring(0, 10)
							+ ":" + data.getMacd() + ":" + data.getSignal() + ":" + data.getHist());
				});
				
				Thread.sleep(5500);
				
				STOCH stockDataSlowResponse = technicalIndicators.stoch(stock.trim(),Interval.DAILY , FastKPeriod.of(14), SlowKPeriod.of(3), SlowDPeriod.of(3), SlowKMaType.SMA, SlowDMaType.SMA);
				
				List<STOCHDataSlow> stochDataSlow = stockDataSlowResponse.getData().stream().limit(1).collect(Collectors.toList());
				
				stochDataSlow.forEach(data -> {
					System.out.println("STOCH:"+stock.trim() + ":"
							+ data.getDateTime().toString().substring(0, 10)
							+ ":" + data.getSlowK() + ":" + data.getSlowD());
				});
				

			} catch (AlphaVantageException e) {
				e.printStackTrace();
				System.out.println("something went wrong");
			} catch (NullPointerException e) {
				System.out.println("NPE occured for " + stock.trim());
				e.printStackTrace();
			}
		}

		printUpsDowns();
	}

	private static void calculateBigMovement(List<RSIData> collect, String stock) {
		Double current = collect.get(0).getValue();
		Double previous1 = collect.get(1).getValue();
		Double previous2 = collect.get(2).getValue();
		Double previous3 = collect.get(3).getValue();
		Double previous4 = collect.get(4).getValue();

		double delta1 = ((current - previous1) / previous1) * 100;
		double delta2 = ((previous1 - previous2) / previous2) * 100;
		double delta3 = ((previous2 - previous3) / previous3) * 100;
		double delta4 = ((previous3 - previous4) / previous4) * 100;

		double delta = delta1 + delta2 + delta3 + delta4;

		if (delta > 20) {
			ups.put(stock.concat(":").concat(Double.toString(delta)),
					collect.get(0));
		}
		if (delta < -20) {
			downs.put(stock.concat(":").concat(Double.toString(delta)),
					collect.get(0));
		}

		if ((current-previous1) > 12 || current > 80) {
			dailyups.put(stock.concat(":").concat(Double.toString(delta1)),
					collect.get(0));
		}
		if ((current-previous1) < -12 || current <30.0) {
			dailydowns.put(stock.concat(":").concat(Double.toString(delta1)),
					collect.get(0));
		}

	}

	public static void printUpsDowns() {
		System.out.println("******************UP LIST***************");

		ups.forEach((key, value) -> System.out.println(key + "-->" + value));

		System.out.println("******************DOWN LIST***************");
		downs.forEach((key, value) -> System.out.println(key + "-->" + value));

		System.out.println("******************Daily UP LIST***************");

		dailyups.forEach((key, value) -> System.out
				.println(key + "-->" + value));

		System.out.println("******************Daily DOWN LIST***************");
		dailydowns.forEach((key, value) -> System.out.println(key + "-->"
				+ value));

	}
}
