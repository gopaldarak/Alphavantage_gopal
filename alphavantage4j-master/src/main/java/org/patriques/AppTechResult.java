package org.patriques;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.patriques.input.technicalindicators.FastKPeriod;
import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.SlowDMaType;
import org.patriques.input.technicalindicators.SlowDPeriod;
import org.patriques.input.technicalindicators.SlowKMaType;
import org.patriques.input.technicalindicators.SlowKPeriod;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.database.DBUtils;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.STOCH;
import org.patriques.output.technicalindicators.data.RSIData;
import org.patriques.output.technicalindicators.data.STOCHDataSlow;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.data.StockData;

public class AppTechResult {
	private static final int SLEEP_INTERVAL = 35000;
	public static Map<String, RSIData> ups = new HashMap<String, RSIData>();
	public static Map<String, RSIData> downs = new HashMap<String, RSIData>();

	public static Map<String, RSIData> dailyups = new HashMap<String, RSIData>();
	public static Map<String, RSIData> dailydowns = new HashMap<String, RSIData>();

	public static Map<String, Map<RSIData, STOCHDataSlow>> dailyAnotherdowns = new HashMap<String, Map<RSIData, STOCHDataSlow>>();

	public static void main(String[] args) throws Exception {
		String apiKey = "J716XINOZ7UCVCTG";
		int timeout = 15000;
		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey,
				timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(
				apiConnector);

		TimeSeries timeSeries = new TimeSeries(apiConnector);
		Connection conn = DBUtils.getConnection();

//		 ArrayList<String> selectStocks = new ArrayList<String>();
//		 selectStocks.add("CTRL");
		ArrayList<String> selectStocks = DBUtils.selectStocks(conn);

		 
		for (String stock : selectStocks) {
			String myStock = stock.trim();
			try {
//				List<RSIData> rsiResult = rsiResult(technicalIndicators,
//						myStock);

				// List<MACDData> macdResult = macdResult(technicalIndicators,
				// myStock);

//				List<STOCHDataSlow> stochResult = stochResult(
//						technicalIndicators, myStock);
//				calculateAnotherDailyBigMovement(rsiResult, stochResult,
//						myStock);
				 List<StockData> dailyStockData = dailyStockData(timeSeries,
				 myStock);
//				StockData stockData = new StockData(LocalDateTime.now(), 0, 0,
//						0, 0, 0);
//				List<StockData> dailyStockData = new ArrayList<StockData>();
//				dailyStockData.add(stockData);

//				AppTechResultOutput appTechResultOutput = new AppTechResultOutput(
//						myStock, rsiResult, stochResult, dailyStockData);
				// System.out.println(appTechResultOutput);
				 
//				 AppTechResult.ichimoku(myStock,dailyStockData);
				 AppTechResult.temp(myStock,dailyStockData);
//				appTechResultOutput.printIt();
//				appTechResultOutput.ichimoku(dailyStockData);

			} catch (AlphaVantageException e) {
				e.printStackTrace();
				System.out.println("something went wrong");
			} catch (NullPointerException e) {
				System.out.println("NPE occured for " + myStock);
				e.printStackTrace();
			}
		}

		printUpsDowns();
	}

	/**
	 * @param timeSeries
	 * @param myStock
	 * @return
	 * @throws InterruptedException
	 */
	private static List<StockData> dailyStockData(TimeSeries timeSeries,
			String myStock) throws InterruptedException {
		Thread.sleep(SLEEP_INTERVAL);

		Daily response = timeSeries.daily(myStock, OutputSize.COMPACT);

		return response.getStockData().stream().limit(50)
				.collect(Collectors.toList());
	}

	/**
	 * @param technicalIndicators
	 * @param myStock
	 * @throws InterruptedException
	 */
	private static List<STOCHDataSlow> stochResult(
			TechnicalIndicators technicalIndicators, String myStock)
			throws InterruptedException {
		Thread.sleep(SLEEP_INTERVAL);

		STOCH stockDataSlowResponse = technicalIndicators.stoch(myStock,
				Interval.DAILY, FastKPeriod.of(14), SlowKPeriod.of(3),
				SlowDPeriod.of(3), SlowKMaType.SMA, SlowDMaType.SMA);

		List<STOCHDataSlow> stochDataSlow = stockDataSlowResponse.getData()
				.stream().limit(26).collect(Collectors.toList());

		return stochDataSlow;
	}

	/**
	 * @param technicalIndicators
	 * @param myStock
	 * @return
	 * @throws InterruptedException
	 */
	// private static List<MACDData> macdResult(TechnicalIndicators
	// technicalIndicators,
	// String myStock) throws InterruptedException {
	// Thread.sleep(SLEEP_INTERVAL);
	//
	// MACD macdResponse = technicalIndicators.macd(myStock, Interval.DAILY,
	// TimePeriod.of(15), SeriesType.CLOSE, FastPeriod.of(12),
	// SlowPeriod.of(26), SignalPeriod.of(9));
	//
	// List<MACDData> macdData =
	// macdResponse.getData().stream().limit(26).collect(Collectors.toList());
	//
	// return macdData;
	// }

	/**
	 * @param technicalIndicators
	 * @param myStock
	 * @return
	 * @throws InterruptedException
	 */
	private static List<RSIData> rsiResult(
			TechnicalIndicators technicalIndicators, String myStock)
			throws InterruptedException {
		Thread.sleep(SLEEP_INTERVAL);
		RSI response = technicalIndicators.rsi(myStock, Interval.DAILY,
				TimePeriod.of(15), SeriesType.CLOSE);
		List<RSIData> rsiData = response.getData().stream().limit(26)
				.collect(Collectors.toList());
		calculateBigMovement(rsiData, myStock);
		return rsiData;
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

		if ((current - previous1) > 12 || current > 80) {
			dailyups.put(stock.concat(":").concat(Double.toString(delta1)),
					collect.get(0));
		}
		if ((current - previous1) < -12 || current < 30.0) {
			dailydowns.put(stock.concat(":").concat(Double.toString(delta1)),
					collect.get(0));
		}

	}

	private static void calculateAnotherDailyBigMovement(List<RSIData> rsi,
			List<STOCHDataSlow> stochastic, String stock) {
		Double currentRsi = rsi.get(0).getValue();
		Double slowD = stochastic.get(0).getSlowD();
		Double slowK = stochastic.get(0).getSlowK();

		if (currentRsi <= 45 &&(slowD <= 20 || slowK <= 20)) {
			Map<RSIData, STOCHDataSlow> value = new HashMap<RSIData, STOCHDataSlow>();
			value.put(rsi.get(0), stochastic.get(0));
			dailyAnotherdowns.put(stock.concat(":"), value);
			System.out.println("***Alert***"+stock + "-->" + value);
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

		System.out
				.println("******************New Daily DOWN LIST***************");
		dailyAnotherdowns.forEach((key, value) -> System.out.println(key
				+ "-->" + value));

	}
	
	public static void ichimoku(String myStock, List<StockData> dailyStockData) {
		Comparator<StockData> closePrice = (StockData s1, StockData s2) -> {
		      return (s1.getClose() >(s2.getClose()) ? 1 : (s1.getClose() <(s2.getClose())? -1 : 0));
		    };
		    double max;
		    double min;
		    double tline = getTandKLines(dailyStockData, closePrice);
		    
		    max = dailyStockData.stream().limit(26).max(closePrice).get().getClose();
		    min = dailyStockData.stream().limit(26).min(closePrice).get().getClose();
		    double kline = (max + min) / 2.0;
		    double currentPrice = dailyStockData.stream().findFirst().get().getClose();
//		    System.out.println("max is " + max + " and min is " + min + "K line value is "+ kline + "current Price is " + currentPrice);

		    
		    if (tline > kline &&  currentPrice >= tline){
		    	System.out.println(" Alert T line is crossing K line for "+ myStock);
		    	
		    }
		    
	}
	
	public static void temp(String myStock, List<StockData> dailyStockData) {
		Comparator<StockData> closePrice = (StockData s1, StockData s2) -> {
		      return (s1.getClose() >(s2.getClose()) ? 1 : (s1.getClose() <(s2.getClose())? -1 : 0));
		    };
	
		    Map<Double,Double> map = new LinkedHashMap<Double, Double>();
		    double tLine=0;
		    double kLine=0;
		    double currentPrice=0;

		    for (int i=0;i<=10;i++) {
		    	if (i ==0){
		    		tLine = getTandKLines(dailyStockData, closePrice, 9,i);
		    		kLine = getTandKLines(dailyStockData, closePrice, 26,i);
		    		currentPrice = dailyStockData.stream().findFirst().get().getClose();
		    	}
		    	if (getTandKLines(dailyStockData, closePrice, 9,i) > getTandKLines(dailyStockData, closePrice, 26,i)){
		    		continue;
		    	}
		    	else{
		    		if (tLine > kLine && currentPrice >= tLine){
				    	System.out.println(" Alert T line is crossing K line for "+ myStock);
				    	break;
		    		}
		    	}
			}


	}

	private static double getTandKLines(List<StockData> dailyStockData,
			Comparator<StockData> closePrice, int limit ,int skip) {
		double max = dailyStockData.stream().skip(skip).limit(limit).max(closePrice).get().getClose();
		double min = dailyStockData.stream().skip(skip).limit(limit).min(closePrice).get().getClose();
		double tline = (max + min) / 2.0;
		return tline;
	}
}
