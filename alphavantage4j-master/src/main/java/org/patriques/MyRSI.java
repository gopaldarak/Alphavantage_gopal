package org.patriques;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.patriques.input.technicalindicators.FastKPeriod;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.SlowDMaType;
import org.patriques.input.technicalindicators.SlowDPeriod;
import org.patriques.input.technicalindicators.SlowKMaType;
import org.patriques.input.technicalindicators.SlowKPeriod;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.model.RSIPrice;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.database.DBUtils;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.STOCH;
import org.patriques.output.technicalindicators.data.RSIData;
import org.patriques.output.technicalindicators.data.STOCHDataSlow;
import org.patriques.output.timeseries.DailyAdjusted;
import org.patriques.output.timeseries.data.StockData;

public class MyRSI {
	private static final String START_DATE = "2016-08-04T00:00:30";
	private static final int SLEEP_INTERVAL = 20000;
	private static final int DENOMINATOR = 1;
	private static final int timeout = 50000;
	private static final String apiKey = "J716XINOZ7UCVCTG";
	private static AlphaVantageConnector apiConnector = new AlphaVantageConnector(
			apiKey, timeout);
	private static TechnicalIndicators technicalIndicators = new TechnicalIndicators(
			apiConnector);
	private static TimeSeries timeSeries = new TimeSeries(apiConnector);
	private static List<RSIData> spxInfo;

	public static void main(String[] args) throws Exception {

		spxInfo = getSPXInfo();
		Connection conn = DBUtils.getConnection();
		List<String> selectStocks = DBUtils.selectStocks(conn).stream().skip(50).collect(Collectors.toList());
		// ArrayList<String> selectStocks = new ArrayList<>();
		// selectStocks.add("CCL");

		System.out.println("# of stocks selected :" + selectStocks.size());

		for (String stock : selectStocks) {
			String myStock = stock.trim();
			try {
				List<RSIPrice> rsiPriceList = new ArrayList<RSIPrice>();
				// try {
				List<StockData> priceList = collectPriceData(myStock);

				double averageVolume = priceList.stream()
						.mapToLong(x -> x.getVolume()).summaryStatistics()
						.getAverage();
				if (averageVolume > 500000) {
					List<RSIData> rsiList = collectRsiData(myStock);
					List<STOCHDataSlow> stochSlowList = collectSTOCHData(myStock);

					int size = priceList.size() > rsiList.size() ? rsiList
							.size() : priceList.size();
					for (int i = 0; i < size; i++) {
						StockData price = priceList.get(i);
						RSIData rsi = rsiList.get(i);
						STOCHDataSlow stochDataSlow = stochSlowList.get(i);

						rsiPriceList.add(new RSIPrice(myStock, price
								.getDateTime(), rsi.getValue(), stochDataSlow
								.getSlowK(), stochDataSlow.getSlowD(), price
								.getClose()));
					}
					runAnalysis(myStock, rsiPriceList);
				}
				else{
					System.out.println("Average volume of "+myStock+" is less than 500000,actual volume is "+averageVolume);
				}
				// } catch (Exception e) {
				// System.out.println("Exception occured for " + myStock);
				// }
				Thread.sleep(SLEEP_INTERVAL);

			} catch (AlphaVantageException e) {
				e.printStackTrace();
				System.out.println("something went wrong" + e.getMessage() + e.getCause().toString());
			} catch (NullPointerException e) {
				System.out.println("NPE occured for " + myStock);
				e.printStackTrace();
			}
			catch (Exception e) {
				System.out.println("Exception occured for " + myStock + " " + e.getCause() + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	// Todo-get oldest data for +/- 5 days to analyze k,d and rsi values
	private static void runAnalysis(String myStock, List<RSIPrice> rsiPriceList) {

//		System.out.println("Run analysis is called for " + myStock
//				+ " with size of list" + rsiPriceList.size());
		RSIPrice latest = rsiPriceList.stream().findFirst().get();
		rsiPriceList
				.stream()
				.filter(data -> findDateinSPX(data))
				// .filter(x -> x.getRsi() >= 35 && x.getRsi() <=45
				// && ((x.getSlowK() >=15 && x.getSlowK() <=40)
				// || (x.getSlowD() >=15 && x.getSlowD() <=40)
				// )
				// )
				.map(y -> gain.apply(latest, y))
				.forEach(x -> System.out.print(""));
	}

	private static Boolean findDateinSPX(RSIPrice data) {
		return spxInfo
				.stream()
				.filter(x -> x
						.getDateTime()
						.toString()
						.substring(0, 10)
						.equals(data.getPriceDateTime().toString()
								.substring(0, 10))).count() > 0;
	}

	private static List<RSIData> collectRsiData(String myStock)
			throws Exception {
		return rsiResult(technicalIndicators, myStock).stream().collect(
				Collectors.toList());
	}

	private static List<StockData> collectPriceData(String myStock)
			throws Exception {
		return dailyStockData(timeSeries, myStock).stream().collect(
				Collectors.toList());
	}

	private static List<STOCHDataSlow> collectSTOCHData(String myStock)
			throws Exception {
		return stochResult(technicalIndicators, myStock).stream().collect(
				Collectors.toList());
	}

	private static List<RSIData> rsiResult(
			TechnicalIndicators technicalIndicators, String myStock)
			throws InterruptedException {
		Thread.sleep(SLEEP_INTERVAL / DENOMINATOR);
		RSI response = technicalIndicators.rsi(myStock, Interval.DAILY,
				TimePeriod.of(15), SeriesType.CLOSE);
		return response
				.getData()
				.stream()
				.filter(x -> x.getDateTime().isAfter(
						LocalDateTime.parse(MyRSI.START_DATE))
						&& x.getDateTime().isBefore(LocalDateTime.now()))
				.collect(Collectors.toList());

	}

	private static List<StockData> dailyStockData(TimeSeries timeSeries,
			String myStock) throws InterruptedException {
		Thread.sleep(SLEEP_INTERVAL / DENOMINATOR);
		DailyAdjusted dailyAdjusted = timeSeries.dailyAdjusted(myStock,
				OutputSize.FULL);
		return dailyAdjusted
				.getStockData()
				.stream()
				.filter(x -> x.getDateTime().isAfter(
						LocalDateTime.parse(MyRSI.START_DATE))
						&& x.getDateTime().isBefore(LocalDateTime.now()))
				.collect(Collectors.toList());
	}

	private static List<STOCHDataSlow> stochResult(
			TechnicalIndicators technicalIndicators, String myStock)
			throws InterruptedException {
		Thread.sleep(SLEEP_INTERVAL / DENOMINATOR);
		STOCH stockDataSlowResponse = technicalIndicators.stoch(myStock.trim(),
				Interval.DAILY, FastKPeriod.of(14), SlowKPeriod.of(3),
				SlowDPeriod.of(3), SlowKMaType.SMA, SlowDMaType.SMA);
		return stockDataSlowResponse
				.getData()
				.stream()
				.filter(x -> x.getDateTime().isAfter(
						LocalDateTime.parse(MyRSI.START_DATE))
						&& x.getDateTime().isBefore(LocalDateTime.now()))
				.collect(Collectors.toList());

	}

	private static BiFunction<RSIPrice, RSIPrice, Double> gain = (current,
			oldest) -> {
		double roi = ((current.getPrice() - oldest.getPrice()) / oldest
				.getPrice()) * 100;
		System.out.println(current.getStock() + ":" + roi + "%. Current RSI:"
				+ current.getRsi() + ":date:"
				+ oldest.getPriceDateTime().toString().substring(0, 10)
				+ ":Older rsi:" + oldest.getRsi() + ":Older k:"
				+ oldest.getSlowK() + ":Older d:" + oldest.getSlowD());
		return roi;
	};

	private static List<RSIData> getSPXInfo() throws Exception {
		return collectRsiData("SPX").stream().filter(x -> x.getValue() < 38)
				.collect(Collectors.toList());

	}

}
