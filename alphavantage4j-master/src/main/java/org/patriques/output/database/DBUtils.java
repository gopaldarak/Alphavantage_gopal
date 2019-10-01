package org.patriques.output.database;

import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;

import org.patriques.model.IchimokuData;
import org.patriques.output.timeseries.data.StockData;

public class DBUtils {
	static PreparedStatement pstmt = null;

	public static PreparedStatement getPstmt() {
		return pstmt;
	}

	public static Connection getConnection() throws Exception {
		String driver = "oracle.jdbc.driver.OracleDriver";
		String url = "jdbc:oracle:thin:@localhost:1521:XE";
		String username = "system";
		String password = "manager";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, username, password);
		return conn;
	}

	public static void insertStocks(Connection conn, IchimokuData ichimoku)
			throws Exception {

		try {
			// System.out.println("stock"+stock);
			// System.out.println("volume:"+volume);
			// System.out.println("price:"+price);
			// System.out.println("DATE:"+localDate.toString());
			String query = "insert into TEST.ICHIMOKU values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, ichimoku.getStock());
			pstmt.setString(2, ichimoku.getVolume());
			pstmt.setDouble(3, ichimoku.getPrice());
			pstmt.setTimestamp(4, ichimoku.getPriceDateTime());
			pstmt.setTimestamp(5, ichimoku.getRunDateTime());
			pstmt.setLong(6, ichimoku.getRank());
			pstmt.setString(7, "");
			pstmt.setString(8, "");
			pstmt.setDouble(9, ichimoku.getSpanB());
			pstmt.setDouble(10, ichimoku.getRsi());
			pstmt.setDouble(11, ichimoku.getCrossoverdays());
			pstmt.setDouble(12, ichimoku.getCrossoverprice());
			pstmt.setString(13, ichimoku.getCrossOverZone());
			pstmt.setDouble(14, ichimoku.getLaggingSpanPriceReturn());
			pstmt.addBatch();
			pstmt.clearParameters();
		} catch (BatchUpdateException e) {
			if (e.getMessage().contains("unique constraint")) {
				System.out.println("Bypass insert for " + ichimoku.getStock());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<String> selectStocks(Connection conn)
			throws Exception {
		ArrayList<String> al = new ArrayList();
		try {

			// String query =
			// "select distinct(stock) from system.screener where " +
			// "stock in (select distinct(stock) from system.screener where screen_name not like '%Buffettology-esque%' group by stock having count(*) >= 2 ) "
			// +
			// "and stock not in (select stock from system.blacklist)order by stock";
			String query = "select distinct(stock) from system.screener where "
					+ " screen_name not in ('%Buffett%')   "
					+ " and stock not in (select stock from system.blacklist)order by stock ";
			// String query =
			// "select distinct(stock) from system.screener group by stock having count(*) >= 2   order by stock";
			pstmt = conn.prepareStatement(query);

			ResultSet result = pstmt.executeQuery();
			while (result.next()) {
				al.add(result.getString("stock"));
			}
			return al;

		} catch (BatchUpdateException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static ArrayList<String> selectDistinctStocks(Connection conn)
			throws Exception {
		ArrayList<String> al = new ArrayList();
		try {

			String query = "select distinct(stock) from TEST.ICHIMOKU where "
					+ " stock not in (select stock from system.blacklist) order by stock ";
			pstmt = conn.prepareStatement(query);

			ResultSet result = pstmt.executeQuery();
			while (result.next()) {
				al.add(result.getString("stock"));
			}
			return al;

		} catch (BatchUpdateException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}

	}

	public static void insertTodayStockPrice(Connection conn,
			StockData stockData, String stock) throws Exception {
		try {

			String query = "insert into TEST.TODAY_STOCK_PRICE values(?, ?, ?, ?, ?, ?, ?)";
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, stock);
			pstmt.setTimestamp(2, Timestamp.valueOf(stockData.getDateTime()));
			pstmt.setDouble(3, stockData.getClose());
			pstmt.setDouble(4, stockData.getHigh());
			pstmt.setDouble(5, stockData.getLow());
			pstmt.setDouble(6, stockData.getOpen());
			pstmt.setDouble(7, stockData.getVolume());
			pstmt.addBatch();
			pstmt.clearParameters();
		} catch (BatchUpdateException e) {
				System.out.println("Bypass insert for " + stock);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void deleteTodayStockPrice(Connection conn) throws Exception {
		try {

			String query = "delete from TEST.TODAY_STOCK_PRICE";
			pstmt = conn.prepareStatement(query);
			pstmt.addBatch();
			pstmt.clearParameters();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if (conn!=null)
			System.out.println("All rows from TODAY_STOCK_PRICE table are droppped");
		}

	}

}
