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
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.base.CommonUtils;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.Logical;
import org.guanzon.appdriver.iface.GEntity;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Client_Mobile  implements GEntity{
    final String XML = "Model_Client_Mobile.xml";
    Connection poConn;          //connection
    CachedRowSet poEntity;      //rowset
    String psMessage;           //warning, success or error message
    GRider poGRider;
    int pnEditMode;
    
    public JSONObject poJSON;
    
    private final String psDefaultDate = "1900-01-01";
    
    public Model_Client_Mobile(GRider poValue){
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
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());
            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            poEntity.updateString("sMobileID", MiscUtil.getNextCode(getTable(), "sMobileID", true, poConn, poGRider.getBranchCode()));
            poEntity.updateString("cMobileTp", Logical.NO);
            poEntity.updateString("cOwnerxxx", Logical.NO);
            poEntity.updateString("cPrimaryx", Logical.NO);
            poEntity.updateString("cRecdStat", Logical.YES);
            
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
        return "client_mobile";
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
    public JSONObject setValue(int lnColumn, Object foValue) {
        poJSON = new JSONObject();
        try {
            poEntity.updateObject(lnColumn, foValue);
            poEntity.updateRow();
            poJSON.put("result", "success");
            poJSON.put("value", getValue(lnColumn));
            return poJSON;
        } catch (SQLException e) {
            e.printStackTrace();
            psMessage = e.getMessage();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
            return poJSON;
        }
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
        return poJSON;
    }

    @Override
    public JSONObject openRecord(String fsValue) {
        //System.out.println("----------------------LOAD CLIENT MOBILE---------------------------");
        poJSON = new JSONObject();

        String lsSQL = MiscUtil.addCondition(getSQL(), "sMobileID = " + SQLUtil.toSQL(fsValue));
        //System.out.println(lsSQL);
        
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
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }

    @Override
    public JSONObject saveRecord() {
        poJSON = new JSONObject();
        
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
            if(String.valueOf(getValue("cMobileTp")).equals("0")){
                setSubscrbr(CommonUtils.classifyNetwork(getMobileNo()));
            } else {
                setSubscrbr("");
            }
            String lsSQL;
            if (pnEditMode == EditMode.ADDNEW){
                //replace with the primary key column info
                setMobileID(MiscUtil.getNextCode(getTable(), "sMobileID", true, poGRider.getConnection(), poGRider.getBranchCode()));
                setdLastVeri(SQLUtil.toDate(psDefaultDate, SQLUtil.FORMAT_SHORT_DATE));
                setdInvalid(SQLUtil.toDate(psDefaultDate, SQLUtil.FORMAT_SHORT_DATE));
                setModifiedBy(poGRider.getUserID());
                setModifiedDte(poGRider.getServerDate());
                setEntryBy( poGRider.getUserID());
                setEntryDte(poGRider.getServerDate());
                
                //setSubscrbr(CommonUtils.classifyNetwork(getMobileNo()));
                lsSQL = MiscUtil.makeSQL(this);
                
                if (!lsSQL.isEmpty()){
                    if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                        poJSON.put("result", "success");
                        poJSON.put("sMobileID", getMobileID());
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
                Model_Client_Mobile loOldEntity = new Model_Client_Mobile(poGRider);
                
                //replace with the primary key column info
                JSONObject loJSON = loOldEntity.openRecord(this.getMobileID());
                setModifiedDte(poGRider.getServerDate());
                setModifiedBy(poGRider.getUserID());
                
                if ("success".equals((String) loJSON.get("result"))){
                    //replace the condition based on the primary key column of the record
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, "sMobileID = " + SQLUtil.toSQL(this.getMobileID()));
                    
                    if (!lsSQL.isEmpty()){
                        if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                            poJSON.put("result", "success");
                            poJSON.put("sMobileID", getMobileID());
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
        
        System.out.println("List of public methods for class " + this.getClass().getName() + ":");
        for (Method method : methods) {
            System.out.println(method.getName());
        }
    }
    
    public String getSQL(){
        return    "  SELECT     "         
                + "  sMobileID  "//1      
                + ", sClientID  "//2      
                + ", sMobileNo  "//3      
                + ", cMobileTp  "//4      
                + ", cOwnerxxx  "//5      
                + ", cIncdMktg  "//6      
                + ", cVerified  "//7      
                + ", dLastVeri  "//8      
                + ", cInvalidx  "//9      
                + ", dInvalidx  "//10     
                + ", cPrimaryx  "//11     
                + ", cSubscrbr  "//12     
                + ", sRemarksx  "//13     
                + ", cRecdStat  "//14     
                + ", sEntryByx  "//15     
                + ", dEntryDte  "//16     
                + ", sModified  "//17     
                + ", dModified  "//18     
                + "FROM client_mobile  " ;                 

    }    
    
     /**
     * Sets the ID of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setMobileID(String fsValue){
        return setValue("sMobileID", fsValue);
    }
    
    /** 
     * @return The ID of this record. 
     */
    public String getMobileID(){
        return (String) getValue("sMobileID");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setClientID(String fsValue){
        return setValue("sClientID", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getClientID(){
        return (String) getValue("sClientID");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setMobileNo(String fsValue){
        return setValue("sMobileNo", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getMobileNo(){
        String lsValue = "";
        if(getValue("sMobileNo") != null){
            lsValue = String.valueOf(getValue("sMobileNo"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setMobileTp(String fsValue){
        return setValue("cMobileTp", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getMobileTp(){
        String lsValue = "";
        if(getValue("cMobileTp") != null){
            lsValue = String.valueOf(getValue("cMobileTp"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setOwner(String fsValue){
        return setValue("cOwnerxxx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getOwner(){
        String lsValue = "";
        if(getValue("cOwnerxxx") != null){
            lsValue = String.valueOf(getValue("cOwnerxxx"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setIncdMktg(String fsValue){
        return setValue("cIncdMktg", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getIncdMktg(){
        String lsValue = "";
        if(getValue("cIncdMktg") != null){
            lsValue = String.valueOf(getValue("cIncdMktg"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setVerified(String fsValue){
        return setValue("cVerified", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getVerified(){
        String lsValue = "";
        if(getValue("cVerified") != null){
            lsValue = String.valueOf(getValue("cVerified"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setdLastVeri(Date fdValue){
        return setValue("dLastVeri", fdValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public Date getdLastVeri(){
        return (Date) getValue("dLastVeri");
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setInvalid(String fsValue){
        return setValue("cInvalidx", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getInvalid(){
        String lsValue = "";
        if(getValue("cInvalidx") != null){
            lsValue = String.valueOf(getValue("cInvalidx"));
        }
        return lsValue;
    }
    
    /**
     * Sets the Value of this record.
     * 
     * @param fdValue 
     * @return  True if the record assignment is successful.
     */
    public JSONObject setdInvalid(Date fdValue){
        return setValue("dInvalidx", fdValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public Date getdInvalid(){
        return (Date) getValue("dInvalidx");
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
    public JSONObject setSubscrbr(String fsValue){
        return setValue("cSubscrbr", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getSubscrbr(){
        String lsValue = "";
        if(getValue("cSubscrbr") != null){
            lsValue = String.valueOf(getValue("cSubscrbr"));
        }
        return lsValue;
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
    public JSONObject setRecdStat(String fsValue){
        return setValue("cRecdStat", fsValue);
    }
    
    /** 
     * @return The Value of this record. 
     */
    public String getRecdStat(){
        String lsValue = "";
        if(getValue("cRecdStat") != null){
            lsValue = String.valueOf(getValue("cRecdStat"));
        }
        return lsValue;
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
        String lsValue = "";
        if(getValue("sEntryByx") != null){
            lsValue = String.valueOf(getValue("sEntryByx"));
        }
        return lsValue;
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
     * @param fsValue 
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
}
