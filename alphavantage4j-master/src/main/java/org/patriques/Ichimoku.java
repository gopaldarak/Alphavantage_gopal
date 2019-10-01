package org.patriques;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.patriques.input.technicalindicators.Interval;
import org.patriques.input.technicalindicators.SeriesType;
import org.patriques.input.technicalindicators.TimePeriod;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.model.IchimokuData;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.database.DBUtils;
import org.patriques.output.technicalindicators.RSI;
import org.patriques.output.technicalindicators.data.RSIData;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.data.StockData;

public class Ichimoku {
	private static final int SLEEP_INTERVAL = 35000;
	private static int count = 0;
	static Connection conn = null;
	static ArrayList<String> ListToRefreshPriceTable =null;
	private static IchimokuData ichimoku = new IchimokuData();

	public static void main(String[] args) throws Exception {
		//String apiKey = "J716XINOZ7UCVCTG";
		String apiKey = "1GIM7BDTR3JWBXFH";
		// String apiKeycontactgopaldarak = "1GIM7BDTR3JWBXFH";
		int timeout = 15000;
		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey,
				timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(
				apiConnector);
		TimeSeries timeSeries = new TimeSeries(apiConnector);
		conn = DBUtils.getConnection();

		ArrayList<String> selectStocks = DBUtils.selectStocks(conn);
		
		//work on todays_price_data table preparation
		removeDataFromTodaysPriceTableList(selectStocks);
		
		for (String stock : selectStocks) {
			String myStock = stock.trim();
			// System.out.println("processing " + myStock);
			try {

				List<StockData> dailyStockData = dailyStockData(timeSeries,
						myStock);
				if (dailyStockData.size() >= 100)
					ichimoku(myStock, dailyStockData, technicalIndicators);

			} catch (AlphaVantageException e) {
				System.out.println("something went wrong for " + myStock
						+ e.getMessage());
			} catch (NullPointerException e) {
				System.out.println("NPE occured for " + myStock
						+ e.getMessage());
				e.printStackTrace();
			}
		}

		for (String stock : ListToRefreshPriceTable) {
			String myStock = stock.trim();
			try {
				//GET DATA
				List<StockData> dailyStockData = dailyStockData(timeSeries,
						myStock);
				//GET TODAY'S DATA
				StockData stockData = dailyStockData.stream().findFirst().get();
				//INSERT INTO DATABASE
				InsertTodaysPriceTable(conn, stockData, myStock);

			} catch (AlphaVantageException e) {
				System.out.println("something went wrong for " + myStock
						+ e.getMessage());
			} catch (NullPointerException e) {
				System.out.println("NPE occured for " + myStock
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private static void removeDataFromTodaysPriceTableList(
			ArrayList<String> selectStocks) throws Exception, SQLException {
		
		DBUtils.deleteTodayStockPrice(conn);
		DBUtils.getPstmt().executeBatch();
		Ichimoku.ListToRefreshPriceTable = DBUtils.selectDistinctStocks(conn);
		System.out.println("Before:Size of ListToRefreshPriceTable "+ ListToRefreshPriceTable.size());
		ListToRefreshPriceTable.removeAll(selectStocks);
		System.out.println("After :Size of ListToRefreshPriceTable "+ ListToRefreshPriceTable.size());
	}

	/**
	 * @param timeSeries
	 * @param myStock
	 * @return
	 * @throws InterruptedException
	 */
	private static List<StockData> dailyStockData(TimeSeries timeSeries,
			String myStock) throws InterruptedException {

		count++;
		if (count % 2 == 0)
			Thread.sleep(SLEEP_INTERVAL);

		Daily response = timeSeries.daily(myStock, OutputSize.COMPACT);

		return response.getStockData().stream().limit(100)
				.collect(Collectors.toList());
	}

	public static void ichimoku(String myStock, List<StockData> dailyStockData,
			TechnicalIndicators technicalIndicators) throws Exception {
		Comparator<StockData> highestHigh = (StockData s1, StockData s2) -> {
			return (s1.getHigh() > (s2.getHigh()) ? 1 : (s1.getHigh() < (s2
					.getHigh()) ? -1 : 0));
		};
		Comparator<StockData> lowestLow = (StockData s1, StockData s2) -> {
			return (s1.getHigh() > (s2.getHigh()) ? 1 : (s1.getHigh() < (s2
					.getHigh()) ? -1 : 0));
		};

		for (int i = 1; i < 20; i++) {
			double tline = getTandKLines(dailyStockData, highestHigh,
					lowestLow, 9, i);
			double kline = getTandKLines(dailyStockData, highestHigh,
					lowestLow, 26, i);
			if (tline > kline) {
				continue;
			} else {
				double todayTLine = getTandKLines(dailyStockData, highestHigh,
						lowestLow, 9, 0);
				double todayKLine = getTandKLines(dailyStockData, highestHigh,
						lowestLow, 26, 0);
				StockData stockdata = dailyStockData.stream().findFirst().get();
				double currentPrice = stockdata.getClose();
				double laggingSpanPrice = dailyStockData.stream().skip(26)
						.findFirst().get().getClose();
				double crossOverDayPrice = dailyStockData.stream().skip(i)
						.findFirst().get().getClose();
				LocalDateTime priceDateTime = dailyStockData.stream()
						.findFirst().get().getDateTime();
				double todaySenkouSpanBToday = getTandKLines(dailyStockData,
						highestHigh, lowestLow, 52, 0);
				double SenkouSpanBCrossOverDay = getTandKLines(dailyStockData,
						highestHigh, lowestLow, 52, 26 + (i + 1));
				// System.out.println(" Today T:" + todayTLine + " Today K:" +
				// todayKLine + " Current price "+currentPrice +
				// " Lower Cloud "+todaySenkouSpanB);

				double laggingSpanPriceReturn = ((currentPrice - laggingSpanPrice) / laggingSpanPrice) * 100;

				double crossOverTLine = getTandKLines(dailyStockData,
						highestHigh, lowestLow, 9, 26 + (i + 1));
				double crossOverKLine = getTandKLines(dailyStockData,
						highestHigh, lowestLow, 26, 26 + (i + 1));
				// System.out.println("cross over day span B was:" +
				// SenkouSpanBCrossOverDay);
				double SenkouSpanACrossOverDay = (crossOverTLine + crossOverKLine) / 2;
				// System.out.println("cross over day span A was:" +
				// SenkouSpanACrossOverDay);
				// System.out.println("cross over T line        :" + tline);

				if (todayTLine > todayKLine
				// && currentPrice >= todayTLine
				// && todayKLine >= todaySenkouSpanB
						&& stockdata.getVolume() > 100000) {
					List<RSIData> rsiResult = rsiResult(technicalIndicators,
							myStock);
					InsertTodaysPriceTable(conn,stockdata,myStock);
					prepareInsert(
							myStock,
							dailyStockData,
							i,
							currentPrice,
							priceDateTime,
							Math.round(todaySenkouSpanBToday * 100.0) / 100.0,
							Math.round(rsiResult.get(0).getValue() * 100.0) / 100.0,
							(i + 1),
							Math.round(crossOverDayPrice * 100.0) / 100.0,
							crossOverZone(tline, SenkouSpanBCrossOverDay,
									SenkouSpanACrossOverDay),
							Math.round(laggingSpanPriceReturn * 100.0) / 100.0);

					System.out
							.println(myStock
									+ ":"
									+ getVolumeMovement(dailyStockData, (i + 1))
									+ ":"
									+ Math.round(currentPrice * 100.0)
									/ 100.0
									+ ":cross over price:"
									+ Math.round(crossOverDayPrice * 100.0)
									/ 100.0
									+ ":span B today:"
									+ Math.round(todaySenkouSpanBToday * 100.0)
									/ 100.0
									+ ":cross over happenned before :"
									+ (i + 1)
									+ " :days :"
									+ crossOverZone(tline,
											SenkouSpanBCrossOverDay,
											SenkouSpanACrossOverDay)
									+ ":RSI:"
									+ rsiResult.get(0)
									+ (laggingSpanPriceReturn > 10 ? ":lagging span on rise:"
											+ laggingSpanPriceReturn
											: ":lagging span not on rise:"
													+ laggingSpanPriceReturn));
					Ichimoku.DBInsert(conn, ichimoku);
				}
				break;
			}
		}

	}

	private static String crossOverZone(double tline,
			double SenkouSpanBCrossOverDay, double SenkouSpanACrossOverDay) {
		if (SenkouSpanACrossOverDay > SenkouSpanBCrossOverDay) {
			if (tline > SenkouSpanACrossOverDay)
				return "bullish";
			else if ((tline < SenkouSpanACrossOverDay)
					&& (tline > SenkouSpanBCrossOverDay))
				return "turbulent";
			else if ((tline < SenkouSpanACrossOverDay)
					&& (tline < SenkouSpanBCrossOverDay))
				return "bearish";
		} else {
			if (tline > SenkouSpanBCrossOverDay)
				return "bullish";
			else if ((tline < SenkouSpanBCrossOverDay)
					&& (tline > SenkouSpanACrossOverDay))
				return "turbulent";
			else if ((tline < SenkouSpanBCrossOverDay)
					&& (tline < SenkouSpanACrossOverDay))
				return "bearish";
		}
		return "";
	}

	private static void prepareInsert(String myStock,
			List<StockData> dailyStockData, int i, double currentPrice,
			LocalDateTime priceDateTime, double todaySenkouSpanBToday,
			double rsi, double crossoverdays, double crossoverprice,
			String crossOverZone, double laggingSpanPriceReturn) {
		ichimoku.setPrice(currentPrice);
		ichimoku.setPriceDateTime(Timestamp.valueOf(priceDateTime));
		ichimoku.setRunDateTime(Timestamp.valueOf(LocalDateTime.now()));
		ichimoku.setStock(myStock);
		ichimoku.setVolume(getVolumeMovement(dailyStockData, (i + 1)));
		ichimoku.setRank(9);
		ichimoku.setSpanB(todaySenkouSpanBToday);
		ichimoku.setRsi(rsi);
		ichimoku.setCrossoverdays(crossoverdays);
		ichimoku.setCrossoverprice(crossoverprice);
		ichimoku.setCrossOverZone(crossOverZone);
		ichimoku.setLaggingSpanPriceReturn(laggingSpanPriceReturn);
	}

	private static double getTandKLines(List<StockData> dailyStockData,
			Comparator<StockData> highestHigh, Comparator<StockData> lowestLow,
			int limit, int skip) {
		double max = dailyStockData.stream().skip(skip).limit(limit)
				.max(highestHigh).get().getHigh();
		double min = dailyStockData.stream().skip(skip).limit(limit)
				.min(lowestLow).get().getLow();
		double tline = (max + min) / 2.0;
		return tline;
	}

	private static String getVolumeMovement(List<StockData> dailyStockData,
			int count) {
		Double averageLast100Days = dailyStockData.stream().limit(100)
				.mapToLong(x -> x.getVolume()).average().getAsDouble();
		Double averageLast10Days = dailyStockData.stream().limit(count)
				.mapToLong(x -> x.getVolume()).average().getAsDouble();

		return averageLast10Days > averageLast100Days ? "YES" : "NO";
	}

	private static void DBInsert(Connection conn, IchimokuData ichimoku)
			throws Exception {
		DBUtils.insertStocks(conn, ichimoku);
		DBUtils.getPstmt().executeBatch();
	}

	private static List<RSIData> rsiResult(
			TechnicalIndicators technicalIndicators, String myStock)
			throws InterruptedException {
		Thread.sleep(SLEEP_INTERVAL);
		RSI response = technicalIndicators.rsi(myStock, Interval.DAILY,
				TimePeriod.of(15), SeriesType.CLOSE);
		List<RSIData> rsiData = response.getData().stream().limit(1)
				.collect(Collectors.toList());
		return rsiData;
	}

	private static void InsertTodaysPriceTable(Connection conn,
			StockData stockData, String stock) throws Exception {
		DBUtils.insertTodayStockPrice(conn, stockData, stock);
		DBUtils.getPstmt().executeBatch();
		DBUtils.getPstmt().close();
		
	}
}
