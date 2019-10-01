package org.patriques.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class RSIPrice {
	String stock;
	LocalDateTime priceDateTime;
	Double rsi;	
	Double slowK;
	Double slowD;
	Double price;
	public String getStock() {
		return stock;
	}
	public void setStock(String stock) {
		this.stock = stock;
	}
	public LocalDateTime getPriceDateTime() {
		return priceDateTime;
	}
	public void setPriceDateTime(LocalDateTime priceDateTime) {
		this.priceDateTime = priceDateTime;
	}
	public Double getRsi() {
		return rsi;
	}
	public void setRsi(Double rsi) {
		this.rsi = rsi;
	}
	public Double getSlowK() {
		return slowK;
	}
	public void setSlowK(Double slowK) {
		this.slowK = slowK;
	}
	public Double getSlowD() {
		return slowD;
	}
	public void setSlowD(Double slowD) {
		this.slowD = slowD;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "RSIPrice [stock=" + stock + ", priceDateTime=" + priceDateTime
				+ ", rsi=" + rsi + ", slowK=" + slowK + ", slowD=" + slowD
				+ ", price=" + price + "]";
	}
	public RSIPrice(String stock, LocalDateTime priceDateTime, Double rsi,
			Double slowK, Double slowD, Double price) {
		super();
		this.stock = stock;
		this.priceDateTime = priceDateTime;
		this.rsi = rsi;
		this.slowK = slowK;
		this.slowD = slowD;
		this.price = price;
	}


}
