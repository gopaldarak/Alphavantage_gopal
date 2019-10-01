package org.patriques.model;

import java.sql.Timestamp;

public class IchimokuData {
	String stock;
	String volume;
	Double price;
	Timestamp priceDateTime;
	Timestamp runDateTime;
	Integer rank;
	String signal;
	String comments;
	Double spanB;
	Double rsi;
	Double crossoverdays;
	Double crossoverprice;
	String crossOverZone;
	Double laggingSpanPriceReturn;

	public String getStock() {
		return stock;
	}

	public void setStock(String stock) {
		this.stock = stock;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Timestamp getPriceDateTime() {
		return priceDateTime;
	}

	public void setPriceDateTime(Timestamp priceDateTime) {
		this.priceDateTime = priceDateTime;
	}

	public Timestamp getRunDateTime() {
		return runDateTime;
	}

	public void setRunDateTime(Timestamp runDateTime) {
		this.runDateTime = runDateTime;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public String getSignal() {
		return signal;
	}

	public void setSignal(String signal) {
		this.signal = signal;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	

	public Double getSpanB() {
		return spanB;
	}

	public void setSpanB(Double spanB) {
		this.spanB = spanB;
	}
	
	
	public Double getRsi() {
		return rsi;
	}

	public void setRsi(Double rsi) {
		this.rsi = rsi;
	}

	
	public Double getCrossoverdays() {
		return crossoverdays;
	}

	public void setCrossoverdays(Double crossoverdays) {
		this.crossoverdays = crossoverdays;
	}

	public Double getCrossoverprice() {
		return crossoverprice;
	}

	public void setCrossoverprice(Double crossoverprice) {
		this.crossoverprice = crossoverprice;
	}

	public String getCrossOverZone() {
		return crossOverZone;
	}

	public void setCrossOverZone(String crossOverZone) {
		this.crossOverZone = crossOverZone;
	}

	public Double getLaggingSpanPriceReturn() {
		return laggingSpanPriceReturn;
	}

	public void setLaggingSpanPriceReturn(Double laggingSpanPriceReturn) {
		this.laggingSpanPriceReturn = laggingSpanPriceReturn;
	}

	public IchimokuData() {
		super();
	}

	@Override
	public String toString() {
		return "IchimokuData [stock=" + stock + ", volume=" + volume
				+ ", price=" + price + ", priceDateTime=" + priceDateTime
				+ ", runDateTime=" + runDateTime + ", rank=" + rank
				+ ", signal=" + signal + ", comments=" + comments + ", spanB="
				+ spanB + ", rsi=" + rsi + ", crossoverdays=" + crossoverdays
				+ ", crossoverprice=" + crossoverprice + ", crossOverZone="
				+ crossOverZone + ", laggingSpanPriceReturn="
				+ laggingSpanPriceReturn + "]";
	}

	

	

}
