package org.patriques;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

import org.patriques.output.technicalindicators.data.MACDData;
import org.patriques.output.technicalindicators.data.RSIData;
import org.patriques.output.technicalindicators.data.STOCHDataSlow;
import org.patriques.output.timeseries.data.StockData;

public class AppTechResultOutput {

	private String stock;
	List<RSIData> rsiResult;
	List<STOCHDataSlow> stochResult;
	List<StockData> dailyStockData;

	public String getStock() {
		return stock;
	}

	public void setStock(String stock) {
		this.stock = stock;
	}

	public List<RSIData> getRsiResult() {
		return rsiResult;
	}

	public void setRsiResult(List<RSIData> rsiResult) {
		this.rsiResult = rsiResult;
	}



	public List<STOCHDataSlow> getStochResult() {
		return stochResult;
	}

	public void setStochResult(List<STOCHDataSlow> stochResult) {
		this.stochResult = stochResult;
	}
	
	public List<StockData> getDailyStockData() {
		return dailyStockData;
	}

	public void setDailyStockData(List<StockData> dailyStockData) {
		this.dailyStockData = dailyStockData;
	}


	public AppTechResultOutput(String stock, List<RSIData> rsiResult,
			List<STOCHDataSlow> stochResult, List<StockData> dailyStockData) {
		super();
		this.stock = stock;
		this.rsiResult = rsiResult;
		this.stochResult = stochResult;
		this.dailyStockData = dailyStockData;
	}

	@Override
	public String toString() {
		return "stock," + stock + "," + rsiResult.get(0)
				+ "," + stochResult.get(0)+ "," + dailyStockData.get(0);
	}
	
	
	public void printIt() {
		for(int i = 0;i <15;i++){
		System.out.println("stock," + stock + "," + rsiResult.get(i)
				+ "," + stochResult.get(i)
				+ "," + dailyStockData.get(i)
				+ "," + dailyStockData.get(0).getClose());
		}
	}

	public void ichimoku(List<StockData> dailyStockData) {
		Comparator<StockData> closePrice = (StockData s1, StockData s2) -> {
		      return (s1.getClose() >(s2.getClose()) ? 1 : (s1.getClose() <(s2.getClose())? -1 : 0));
		    };
		    double max = dailyStockData.stream().limit(9).max(closePrice).get().getClose();
		    double min = dailyStockData.stream().limit(9).min(closePrice).get().getClose();
		    double tline = (max + min) / 2.0;
		    System.out.println("max is " + max + " and min is " + min + "T line value is "+ tline);
		    
		    max = dailyStockData.stream().limit(26).max(closePrice).get().getClose();
		    min = dailyStockData.stream().limit(26).min(closePrice).get().getClose();
		    double kline = (max + min) / 2.0;
		    System.out.println("max is " + max + " and min is " + min + "K line value is "+ kline);
		    
		    if (tline > kline ){
		    	System.out.println(" Alert T line is crossing K line");
		    }
	}

}
