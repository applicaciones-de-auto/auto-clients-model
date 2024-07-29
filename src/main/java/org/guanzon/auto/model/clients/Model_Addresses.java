/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.model.clients;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.iface.GEntity;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Addresses implements GEntity{
    final String XML = "Model_Addresses.xml";
    Connection poConn;          //connection
    CachedRowSet poEntity;      //rowset
    String psMessage;           //warning, success or error message
    GRider poGRider;
    int pnEditMode;
    
    public JSONObject poJSON;
    
    public Model_Addresses(GRider poValue){
        if (poValue.getConnection() == null){
            System.err.println("Database connection is not set.");
            System.exit(1);
        }
        pnEditMode = EditMode.UNKNOWN;
        poGRider = poValue;
        poConn = poGRider.getConnection();
        
        initialize();
    }
    
    private void initialize(){
        
        try {
            //System.out.println("path = " + System.getProperty("sys.default.path.metadata") + XML);
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());
            
            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);  
            poEntity.updateDouble("nLatitude", 0.0);
            poEntity.updateDouble("nLongitud", 0.0);
            
            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    } 

    
    @Override
    public String getColumn(int fnCol) {
        try {
            return poEntity.getMetaData().getColumnLabel(fnCol); 
        } catch (SQLException e) {
        }
        return "";
    }

    @Override
    public int getColumn(String fsCol) {
        try {
            return MiscUtil.getColumnIndex(poEntity, fsCol);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getColumnCount() {
        try {
            return poEntity.getMetaData().getColumnCount(); 
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }

    @Override
    public int getEditMode() {
        return pnEditMode;
    }

    @Override
    public String getTable() {
        return "addresses";
    }

    @Override
    public Object getValue(int fnColumn) {
        try {
            return poEntity.getObject(fnColumn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getValue(String fsColumn) {
        try {
            return poEntity.getObject(fsColumn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JSONObject setValue(int fnColumn, Object foValue) {
        try {              
            poJSON = MiscUtil.validateColumnValue(System.getProperty("sys.default.path.metadata") + XML, MiscUtil.getColumnLabel(poEntity, fnColumn), foValue);
            if ("error".equals((String) poJSON.get("result"))) return poJSON;
            
            poEntity.updateObject(fnColumn, foValue);
            poEntity.updateRow();
            
            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("value", getValue(fnColumn));
            //System.out.println("poJSON = " + poJSON);
            
        } catch (SQLException e) {
            e.printStackTrace();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        
        return poJSON;
    }
    @Override
    public JSONObject setValue(String string, Object foValue) {
        try {
            return setValue(MiscUtil.getColumnIndex(poEntity, string), foValue);
        } catch (SQLException ex) {
            
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", ex.getMessage());
            return poJSON;
        }
    }

    @Override
    public JSONObject newRecord() {
        pnEditMode = EditMode.ADDNEW;
        
        //replace with the primary key column info
        setAddressID(MiscUtil.getNextCode(getTable(), "sAddrssID", true, poGRider.getConnection(), poGRider.getBranchCode()));
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "New record success.");
        return poJSON;
    }

    @Override
    public JSONObject openRecord(String fsValue) {
        poJSON = new JSONObject();
        
        String lsSQL = MiscUtil.addCondition(getSQL(), "a.sAddrssID = " + SQLUtil.toSQL(fsValue));
        //System.out.println(lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()){
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++){
                    setValue(lnCtr, loRS.getObject(lnCtr));
                }

                pnEditMode = EditMode.UPDATE;

                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "No record to load.");
            }
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }

    @Override
    public JSONObject saveRecord() {
        String lsExclude = "trimAddress»sBrgyName»sTownName»sProvName»sProvIDxx";
        String lsSQL;
        poJSON = new JSONObject();
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
            if (pnEditMode == EditMode.ADDNEW){
                //replace with the primary key column info
                setAddressID(MiscUtil.getNextCode(getTable(), "sAddrssID", true, poGRider.getConnection(), poGRider.getBranchCode()));
                setModifiedDte(poGRider.getServerDate());
                setModifiedBy(poGRider.getUserID());
                
                lsSQL = MiscUtil.makeSQL(this, lsExclude);
                
                if (!lsSQL.isEmpty()){
                    if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                        poJSON.put("result", "success");
                        poJSON.put("sAddrssID", getAddressID());
                        poJSON.put("message", "Record saved successfully.");
                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("message", poGRider.getErrMsg());
                    }
                } else {
                    poJSON.put("result", "error");
                    poJSON.put("message", "No record to save.");
                }
            } else {
                Model_Addresses loOldEntity = new Model_Addresses(poGRider);
                
                setModifiedDte(poGRider.getServerDate());
                setModifiedBy(poGRider.getUserID());
                //replace with the primary key column info
                JSONObject loJSON = loOldEntity.openRecord(this.getAddressID());
                
                if ("success".equals((String) loJSON.get("result"))){
                    //replace the condition based on the primary key column of the record
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, "sAddrssID = " + SQLUtil.toSQL(this.getAddressID()), lsExclude);
                    
                    if (!lsSQL.isEmpty()){
                        if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                            poJSON.put("result", "success");
                            poJSON.put("sAddrssID", getAddressID());
                            poJSON.put("message", "Record saved successfully.");
                        } else {
                            poJSON.put("result", "error");
                            poJSON.put("message", poGRider.getErrMsg());
                        }
                    } else {
                        poJSON.put("result", "error");
                        poJSON.put("continue", true);
                        poJSON.put("message", "No updates has been made.");
                    }
                } else {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Record discrepancy. Unable to save record.");
                }
            }
        } else {
            poJSON.put("result", "error");
            poJSON.put("message", "Invalid update mode. Unable to save record.");
            return poJSON;
        }
        
        return poJSON;

    }

    @Override
    public void list() { 
        Method[] methods = this.getClass().getMethods();
        
        System.out.println("--------------------------------------------------------------------");
        System.out.println("LIST OF PUBLIC METHODS FOR " + this.getClass().getName() + ":");
        System.out.println("--------------------------------------------------------------------");
        for (Method method : methods) {
            System.out.println(method.getName());
        }
        
        try {
            int lnRow = poEntity.getMetaData().getColumnCount();
        
            System.out.println("--------------------------------------------------------------------");
            System.out.println("ENTITY COLUMN INFO");
            System.out.println("--------------------------------------------------------------------");
            System.out.println("Total number of columns: " + lnRow);
            System.out.println("--------------------------------------------------------------------");

            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++){
                System.out.println("Column index: " + (lnCtr) + " --> Label: " + poEntity.getMetaData().getColumnLabel(lnCtr));
                if (poEntity.getMetaData().getColumnType(lnCtr) == Types.CHAR ||
                    poEntity.getMetaData().getColumnType(lnCtr) == Types.VARCHAR){

                    System.out.println("Column index: " + (lnCtr) + " --> Size: " + poEntity.getMetaData().getColumnDisplaySize(lnCtr));
                }
            }
        } catch (SQLException e) {
        }
        
    }
    
    public String getSQL() {
        return    "  SELECT      "                                                                       
                + "  a.sAddrssID " //1                                                                   
                + ", a.sHouseNox " //2                                                                   
                + ", a.sAddressx " //3                                                                   
                + ", a.sTownIDxx " //4                                                                   
                + ", a.sZippCode " //5                                                                   
                + ", a.sBrgyIDxx " //6                                                                   
                + ", a.nLatitude " //7                                                                   
                + ", a.nLongitud " //8                                                                   
                + ", a.sRemarksx " //9                                                                   
                + ", a.sModified " //10                                                                  
                + ", a.dModified " //11                                                                  
                + ", IFNULL(d.sProvName, '') sProvName " //12                                            
                + ", IFNULL(c.sBrgyName, '') sBrgyName " //13                                            
                + ", IFNULL(b.sTownName, '') sTownName " //14                                            
                + ", IFNULL(d.sProvIDxx, '') sProvIDxx " //15                                            
                + ", REPLACE(CONCAT(IFNULL(a.sHouseNox,''),         "                                    
                + "  IFNULL(a.sAddressx,''),IFNULL(c.sBrgyName,''), "                                    
                + "  IFNULL(b.sTownName,''), IFNULL(d.sProvName,'')), ' ', '') AS trimAddress   " //16   
                + "FROM addresses a   "                                                                  
                + "LEFT JOIN TownCity b ON b.sTownIDxx = a.sTownIDxx "                                   
                + "LEFT JOIN Barangay c ON c.sBrgyIDxx = a.sBrgyIDxx "                                   
                + "LEFT JOIN Province d ON d.sProvIDxx = b.sProvIDxx " ;                          
    }

    /**
     * Sets the ID of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setAddressID(String fsValue){
        return setValue("sAddrssID", fsValue);
    }
    
    /** 
     * @return The ID of this record. 
     */
    public String getAddressID(){
        return (String) getValue("sAddrssID");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setHouseNo(String fsValue){
        return setValue("sHouseNox", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getHouseNo(){
        String lsValue = "";
        if(getValue("sHouseNox") != null){
            lsValue = String.valueOf(getValue("sHouseNox"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setAddress(String fsValue){
        return setValue("sAddressx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getAddress(){
        return (String) getValue("sAddressx");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setProvID(String fsValue){
        return setValue("sProvIDxx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getProvID(){
        
        String lsValue = "";
        if(getValue("sProvIDxx") != null){
            lsValue = String.valueOf(getValue("sProvIDxx"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setTownID(String fsValue){
        return setValue("sTownIDxx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getTownID(){
        
        String lsValue = "";
        if(getValue("sTownIDxx") != null){
            lsValue = String.valueOf(getValue("sTownIDxx"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setZippCode(String fsValue){
        return setValue("sZippCode", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getZippCode(){
        
        String lsValue = "";
        if(getValue("sZippCode") != null){
            lsValue = String.valueOf(getValue("sZippCode"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setBrgyID(String fsValue){
        return setValue("sBrgyIDxx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getBrgyID(){
        
        
        String lsValue = "";
        if(getValue("sBrgyIDxx") != null){
            lsValue = String.valueOf(getValue("sBrgyIDxx"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setLatitude(Double fdValue){
        return setValue("nLatitude", fdValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public Double getLatitude(){
        return (Double) getValue("nLatitude");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setLongitud(Double fdValue){
        return setValue("nLongitud", fdValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public Double getLongitud(){
        return (Double) getValue("nLongitud");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setRemarks(String fsValue){
        return setValue("sRemarksx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getRemarks(){
        return (String) getValue("sRemarksx");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setModifiedBy(String fsValue){
        return setValue("sModified", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getModifiedBy(){
        return (String) getValue("sModified");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fdValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setModifiedDte(Date fdValue){
        return setValue("dModified", fdValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getModifiedDte(){
        return (String) getValue("dModified");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setBrgyName(String fsValue){
        return setValue("sBrgyName", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getBrgyName(){
        return (String) getValue("sBrgyName");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setTownName(String fsValue){
        return setValue("sTownName", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getTownName(){
        return (String) getValue("sTownName");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setProvName(String fsValue){
        return setValue("sProvName", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getProvName(){
        return (String) getValue("sProvName");
    }

    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setTrimAddress(String fsValue){
        return setValue("trimAddress", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getTrimAddress(){
        return (String) getValue("trimAddress");
    }
}
