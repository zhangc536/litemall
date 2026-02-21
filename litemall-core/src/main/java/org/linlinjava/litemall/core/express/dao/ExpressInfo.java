/**
 * Copyright 2018 bejson.com
 */
package org.linlinjava.litemall.core.express.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Auto-generated: 2018-07-19 22:27:22
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class ExpressInfo {

    @JsonProperty("LogisticCode")
    private String LogisticCode;
    @JsonProperty("ShipperCode")
    private String ShipperCode;
    @JsonProperty("Traces")
    private List<Traces> Traces;
    @JsonProperty("State")
    private String State;
    @JsonProperty("StateEx")
    private String StateEx;
    @JsonProperty("Location")
    private String Location;
    @JsonProperty("EBusinessID")
    private String EBusinessID;
    @JsonProperty("Success")
    private boolean Success;
    @JsonProperty("Reason")
    private String Reason;
    @JsonProperty("OrderCode")
    private String OrderCode;
    @JsonProperty("Callback")
    private String Callback;
    @JsonProperty("Station")
    private String Station;
    @JsonProperty("StationTel")
    private String StationTel;
    @JsonProperty("StationAdd")
    private String StationAdd;
    @JsonProperty("DeliveryMan")
    private String DeliveryMan;
    @JsonProperty("DeliveryManTel")
    private String DeliveryManTel;
    @JsonProperty("NextCity")
    private String NextCity;

    private String ShipperName;

    public String getLogisticCode() {
        return LogisticCode;
    }

    public void setLogisticCode(String LogisticCode) {
        this.LogisticCode = LogisticCode;
    }

    public String getShipperCode() {
        return ShipperCode;
    }

    public void setShipperCode(String ShipperCode) {
        this.ShipperCode = ShipperCode;
    }

    public List<Traces> getTraces() {
        return Traces;
    }

    public void setTraces(List<Traces> Traces) {
        this.Traces = Traces;
    }

    public String getState() {
        return State;
    }

    public void setState(String State) {
        this.State = State;
    }

    public String getStateEx() {
        return StateEx;
    }

    public void setStateEx(String StateEx) {
        this.StateEx = StateEx;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public String getEBusinessID() {
        return EBusinessID;
    }

    public void setEBusinessID(String EBusinessID) {
        this.EBusinessID = EBusinessID;
    }

    public boolean getSuccess() {
        return Success;
    }

    public void setSuccess(boolean Success) {
        this.Success = Success;
    }

    public String getReason() {
        return Reason;
    }

    public void setReason(String Reason) {
        this.Reason = Reason;
    }

    public String getOrderCode() {
        return OrderCode;
    }

    public void setOrderCode(String OrderCode) {
        this.OrderCode = OrderCode;
    }

    public String getCallback() {
        return Callback;
    }

    public void setCallback(String Callback) {
        this.Callback = Callback;
    }

    public String getStation() {
        return Station;
    }

    public void setStation(String Station) {
        this.Station = Station;
    }

    public String getStationTel() {
        return StationTel;
    }

    public void setStationTel(String StationTel) {
        this.StationTel = StationTel;
    }

    public String getStationAdd() {
        return StationAdd;
    }

    public void setStationAdd(String StationAdd) {
        this.StationAdd = StationAdd;
    }

    public String getDeliveryMan() {
        return DeliveryMan;
    }

    public void setDeliveryMan(String DeliveryMan) {
        this.DeliveryMan = DeliveryMan;
    }

    public String getDeliveryManTel() {
        return DeliveryManTel;
    }

    public void setDeliveryManTel(String DeliveryManTel) {
        this.DeliveryManTel = DeliveryManTel;
    }

    public String getNextCity() {
        return NextCity;
    }

    public void setNextCity(String NextCity) {
        this.NextCity = NextCity;
    }

    public String getShipperName() {
        return ShipperName;
    }

    public void setShipperName(String shipperName) {
        ShipperName = shipperName;
    }

    @Override
    public String toString() {
        return "ExpressInfo{" +
                "LogisticCode='" + LogisticCode + '\'' +
                ", ShipperCode='" + ShipperCode + '\'' +
                ", Traces=" + Traces +
                ", State='" + State + '\'' +
                ", StateEx='" + StateEx + '\'' +
                ", Location='" + Location + '\'' +
                ", EBusinessID='" + EBusinessID + '\'' +
                ", Success=" + Success +
                ", Reason=" + Reason +
                ", OrderCode='" + OrderCode + '\'' +
                ", Callback='" + Callback + '\'' +
                ", Station='" + Station + '\'' +
                ", StationTel='" + StationTel + '\'' +
                ", StationAdd='" + StationAdd + '\'' +
                ", DeliveryMan='" + DeliveryMan + '\'' +
                ", DeliveryManTel='" + DeliveryManTel + '\'' +
                ", NextCity='" + NextCity + '\'' +
                ", ShipperName='" + ShipperName + '\'' +
                '}';
    }
}
