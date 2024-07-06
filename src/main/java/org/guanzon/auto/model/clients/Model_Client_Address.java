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
import java.util.ArrayList;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.agent.ShowDialogFX;
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
public class Model_Client_Address implements GEntity{
    final String XML = "Model_Client_Address.xml";
    Connection poConn;          //connection
    CachedRowSet poEntity;      //rowset
    String psMessage;           //warning, success or error message
    GRider poGRider;
    int pnEditMode;
    
    public JSONObject poJSON;
    
    String psOrigAddressID; 
    
    public Model_Client_Address(GRider poValue){
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
            System.out.println("path = " + System.getProperty("sys.default.path.metadata") + XML);
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());
            
            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);      
            poEntity.updateString("cOfficexx", Logical.NO);
            poEntity.updateString("cProvince", Logical.NO);
            poEntity.updateString("cBillingx", Logical.NO);
            poEntity.updateString("cShipping", Logical.NO);
            poEntity.updateString("cCurrentx", Logical.NO);
            poEntity.updateString("cPrimaryx", Logical.NO);
            poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);
            
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
        return "client_address";
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
            System.out.println("poJSON = " + poJSON);
            
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
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        poJSON.put("message", "New record success.");
        return poJSON;
    }

    @Override
    public JSONObject openRecord(String fsValue) {
        //System.out.println("----------------------LOAD CLIENT ADDRESS---------------------------");
        poJSON = new JSONObject();
        psOrigAddressID = fsValue;

        String lsSQL = MiscUtil.addCondition(getSQL(), "a.sAddrssID = " + SQLUtil.toSQL(fsValue));
        System.out.println(lsSQL);

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()){
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++){
                    //System.out.println("ROW : "+ lnCtr + loRS.getMetaData().getColumnLabel(lnCtr) + " = " + loRS.getObject(lnCtr));
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
        String lsExclude = "sHouseNox»sAddressx»sTownIDxx»sBrgyIDxx»nLatitude»nLongitud»sRemarksx»sBrgyName»sTownName»sProvName»sProvIDxx»sZippCode";
        String lsSQL;
        poJSON = new JSONObject();
        
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
            
            if (pnEditMode == EditMode.ADDNEW){
                
                setModifiedBy(poGRider.getUserID());
                setModifiedDte(poGRider.getServerDate());
                setEntryBy( poGRider.getUserID());
                setEntryDte(poGRider.getServerDate());
                
                lsSQL = MiscUtil.makeSQL(this, lsExclude);
                
                if (!lsSQL.isEmpty()){
                    if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                        poJSON.put("result", "success");
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
                Model_Client_Address loOldEntity = new Model_Client_Address(poGRider);
                
                setModifiedBy(poGRider.getUserID());
                setModifiedDte(poGRider.getServerDate());
                //replace with the primary key column info
                JSONObject loJSON = loOldEntity.openRecord(psOrigAddressID);
                
                if ("success".equals((String) loJSON.get("result"))){
                    //replace the condition based on the primary key column of the record
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, " sClientID = " + SQLUtil.toSQL(this.getClientID()) + "AND sAddrssID = " + SQLUtil.toSQL(psOrigAddressID), lsExclude);
                    
                    if (!lsSQL.isEmpty()){
                        if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                            poJSON.put("result", "success");
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
    
    public String getSQL(){
        return    " SELECT   "                                                                                   
                + "   IFNULL(a.sAddrssID, '')  sAddrssID   " //1                                                
                + " , IFNULL(a.sClientID, '')  sClientID   " //2                                                
                + " , IFNULL(b.sHouseNox, '')  sHouseNox   " //3                                                
                + " , IFNULL(b.sAddressx, '')  sAddressx   " //4                                                
                + " , IFNULL(b.sTownIDxx, '')  sTownIDxx   " //5                                                
                + " , IFNULL(b.sBrgyIDxx, '')  sBrgyIDxx   " //6                                                
                + " , IFNULL(b.sZippCode, '')  sZippCode   " //7                                                
                + " , b.nLatitude     "                      //8                                                
                + " , b.nLongitud     "                      //9                                                
                + " , IFNULL(b.sRemarksx, '')  sRemarksx   " //10                                               
                + " , IFNULL(a.cOfficexx, '')  cOfficexx   " //11                                               
                + " , IFNULL(a.cProvince, '')  cProvince   " //12                                               
                + " , IFNULL(a.cPrimaryx, '')  cPrimaryx   " //13                                               
                + " , IFNULL(a.cBillingx, '')  cBillingx   " //14                                               
                + " , IFNULL(a.cShipping, '')  cShipping   " //15                                               
                + " , IFNULL(a.cCurrentx, '')  cCurrentx   " //16                                               
                + " , IFNULL(a.cRecdStat, '')  cRecdStat   " //17                                               
                + " , IFNULL(a.sEntryByx, '')  sEntryByx   " //18                                               
                + " , a.dEntryDte     "                      //19                                               
                + " , IFNULL(a.sModified, '')  sModified   " //20                                               
                + " , a.dModified     "                      //21                                               
                + " , IFNULL(e.sProvName, '')  sProvName   " //22                                               
                + " , IFNULL(d.sBrgyName, '')  sBrgyName   " //23                                               
                + " , IFNULL(c.sTownName, '')  sTownName   " //24                                               
                + " , IFNULL(e.sProvIDxx, '')  sProvIDxx   " //25                                               
                + "  FROM client_address a                 "                                                    
                + "  INNER JOIN addresses b ON b.sAddrssID = a.sAddrssID "                                     
                + "  LEFT JOIN TownCity c ON c.sTownIDxx = b.sTownIDxx   "                                     
                + "  LEFT JOIN Barangay d ON d.sBrgyIDxx = b.sBrgyIDxx AND d.sTownIDxx = b.sTownIDxx   "       
                + "  LEFT JOIN Province e ON e.sProvIDxx = c.sProvIDxx   " ;                                    
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
     * Sets the ID of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setClientID(String fsValue){
        return setValue("sClientID", fsValue);
    }
    
    /** 
     * @return The ID of this record. 
     */
    public String getClientID(){
        return (String) getValue("sClientID");
    }
    
    public String getFullAddress(){
        String lsFullAddress = getHouseNo() + "" +  getAddress() + "" + getBrgyName() + "" + getTownName() + "" + getProvName() ;
        return lsFullAddress;
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
        return (String) getValue("sHouseNox");
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
    public JSONObject setTownID(String fsValue){
        return setValue("sTownIDxx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getTownID(){
        return (String) getValue("sTownIDxx");
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
        return (String) getValue("sBrgyIDxx");
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
        return (String) getValue("sZippCode");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setLatitude(String fsValue){
        return setValue("nLatitude", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getLatitude(){
        return (String) getValue("nLatitude");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setLongitud(String fsValue){
        return setValue("nLongitud", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getLongitud(){
        return (String) getValue("nLongitud");
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
    public JSONObject setOffice(String fsValue){
        return setValue("cOfficexx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getOffice(){
        return (String) getValue("cOfficexx");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setProvince(String fsValue){
        return setValue("cProvince", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getProvince(){
        return (String) getValue("cProvince");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fnValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setPrimary(int fnValue){
        return setValue("cPrimaryx", fnValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public Integer getPrimary(){
        return (Integer) getValue("cPrimaryx");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setBilling(String fsValue){
        return setValue("cBillingx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getBilling(){
        return (String) getValue("cBillingx");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setShipping(String fsValue){
        return setValue("cShipping", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getShipping(){
        return (String) getValue("cShipping");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setCurrent(String fsValue){
        return setValue("cCurrentx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getCurrent(){
        return (String) getValue("cCurrentx");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setRecdStat(String fsValue){
        return setValue("cRecdStat", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getRecdStat(){
        return (String) getValue("cRecdStat");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setEntryBy(String fsValue){
        return setValue("sEntryByx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getEntryBy(){
        return (String) getValue("sEntryByx");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setEntryDte(Date fdValue){
        return setValue("dEntryDte", fdValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getEntryDte(){
        return (String) getValue("dEntryDte");
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
    public JSONObject setProvID(String fsValue){
        return setValue("sProvIDxx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getProvID(){
        return (String) getValue("sProvIDxx");
    }
    
}
