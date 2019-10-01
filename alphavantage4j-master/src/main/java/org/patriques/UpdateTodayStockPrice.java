package org.patriques;

import java.sql.BatchUpdateException;
import java.sql.Connection;
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

public class UpdateTodayStockPrice {
	private static final int SLEEP_INTERVAL = 30000;
	private static int count = 0;
	static Connection conn = null;
	private static IchimokuData ichimoku = new IchimokuData();

	public static void main(String[] args) throws Exception {
		// SETUP
		String apiKey = "J716XINOZ7UCVCTG";
		int timeout = 15000;
		AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey,
				timeout);
		TechnicalIndicators technicalIndicators = new TechnicalIndicators(
				apiConnector);
		TimeSeries timeSeries = new TimeSeries(apiConnector);
		// GET CONNECTION
		conn = DBUtils.getConnection();
		// DROP ALL ROWS AND COMMIT
		DBUtils.deleteTodayStockPrice(conn);
		DBUtils.getPstmt().executeBatch();
		// GET ALL DISTINCT STOCKS
		ArrayList<String> selectStocks = DBUtils.selectDistinctStocks(conn);

		for (String stock : selectStocks) {
			String myStock = stock.trim();
			try {
				//GET DATA
				List<StockData> dailyStockData = dailyStockData(timeSeries,
						myStock);
				//GET TODAY'S DATA
				StockData stockData = dailyStockData.stream().findFirst().get();
				//INSERT INTO DATABASE
				UpdateTodayStockPrice.DBInsert(conn, stockData, stock.trim());

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

	private static void DBInsert(Connection conn, StockData stockData,
			String stock) throws Exception {
		DBUtils.insertTodayStockPrice(conn, stockData, stock);
		DBUtils.getPstmt().executeBatch();
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

}
