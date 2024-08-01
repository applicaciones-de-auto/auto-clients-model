/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.model.clients;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import javax.sql.rowset.CachedRowSet;
import org.guanzon.appdriver.base.GRider;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.iface.GEntity;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Sales_Agent implements GEntity {
    final String XML = "Model_Sales_Agent.xml";

    GRider poGRider;                //application driver
    CachedRowSet poEntity;          //rowset
    JSONObject poJSON;              //json container
    int pnEditMode;                 //edit mode
    /**
     * Entity constructor
     *
     * @param foValue - GhostRider Application Driver
     */
    public Model_Sales_Agent(GRider foValue) {
        if (foValue == null) {
            System.err.println("Application Driver is not set.");
            System.exit(1);
        }

        poGRider = foValue;

        initialize();
    }
    
    private void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);
            poEntity.updateString("cRecdStat", "0");

            poEntity.insertRow();
            poEntity.moveToCurrentRow();

            poEntity.absolute(1);

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Gets the column index name.
     *
     * @param fnValue - column index number
     * @return column index name
     */
    @Override
    public String getColumn(int fnValue) {
        try {
            return poEntity.getMetaData().getColumnLabel(fnValue);
        } catch (SQLException e) {
        }
        return "";
    }

    /**
     * Gets the column index number.
     *
     * @param fsValue - column index name
     * @return column index number
     */
    @Override
    public int getColumn(String fsValue) {
        try {
            return MiscUtil.getColumnIndex(poEntity, fsValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Gets the total number of column.
     *
     * @return total number of column
     */
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
        return "sales_agent";
    }

    /**
     * Gets the value of a column index number.
     *
     * @param fnColumn - column index number
     * @return object value
     */
    @Override
    public Object getValue(int fnColumn) {
        try {
            return poEntity.getObject(fnColumn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the value of a column index name.
     *
     * @param fsColumn - column index name
     * @return object value
     */
    @Override
    public Object getValue(String fsColumn) {
        try {
            return poEntity.getObject(MiscUtil.getColumnIndex(poEntity, fsColumn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sets column value.
     *
     * @param fnColumn - column index number
     * @param foValue - value
     * @return result as success/failed
     */
    @Override
    public JSONObject setValue(int fnColumn, Object foValue) {
        try {
            poJSON = MiscUtil.validateColumnValue(System.getProperty("sys.default.path.metadata") + XML, MiscUtil.getColumnLabel(poEntity, fnColumn), foValue);
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }

            poEntity.updateObject(fnColumn, foValue);
            poEntity.updateRow();

            poJSON = new JSONObject();
            poJSON.put("result", "success");
            poJSON.put("value", getValue(fnColumn));
        } catch (SQLException e) {
            e.printStackTrace();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }

        return poJSON;
    }

    /**
     * Sets column value.
     *
     * @param fsColumn - column index name
     * @param foValue - value
     * @return result as success/failed
     */
    @Override
    public JSONObject setValue(String fsColumn, Object foValue) {
        poJSON = new JSONObject();

        try {
            return setValue(MiscUtil.getColumnIndex(poEntity, fsColumn), foValue);
        } catch (SQLException e) {
            e.printStackTrace();
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        }
        return poJSON;
    }

    /**
     * Set the edit mode of the entity to new.
     *
     * @return result as success/failed
     */
    @Override
    public JSONObject newRecord() {
        pnEditMode = EditMode.ADDNEW;

        setRecdStat( "0");
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Opens a record.
     *
     * @param fsValue - filter values
     * @return result as success/failed
     */
    @Override
    public JSONObject openRecord(String fsValue) {
        poJSON = new JSONObject();

        String lsSQL = getSQL();

        //replace the condition based on the primary key column of the record
        lsSQL = MiscUtil.addCondition(lsSQL, " a.sClientID = " + SQLUtil.toSQL(fsValue));

        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()) {
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++) {
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

    /**
     * Save the entity.
     *
     * @return result as success/failed
     */
    @Override
    public JSONObject saveRecord() {
        String lsExclude = "sCompnyNm»sAddressx»sLastName»sFrstName»sMiddName»sMobileNo»sAccountx»sEmailAdd»cClientTp";
        poJSON = new JSONObject();

        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            String lsSQL;
            if (pnEditMode == EditMode.ADDNEW) {
                setModified(poGRider.getUserID());
                setModifiedDte(poGRider.getServerDate());
                
                lsSQL = MiscUtil.makeSQL(this, lsExclude);
                if (!lsSQL.isEmpty()) {
                    if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0) {
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
                Model_Sales_Agent loOldEntity = new Model_Sales_Agent(poGRider);

                //replace with the primary key column info
                JSONObject loJSON = loOldEntity.openRecord(this.getClientID());

                if ("success".equals((String) loJSON.get("result"))) {
                setModified(poGRider.getUserID());
                setModifiedDte(poGRider.getServerDate());
                    //replace the condition based on the primary key column of the record
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, "sClientID = " + SQLUtil.toSQL(this.getClientID()),lsExclude);

                    if (!lsSQL.isEmpty()) {
                        if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0) {
                            poJSON.put("result", "success");
                            poJSON.put("message", "Record saved successfully.");
                        } else {
                            poJSON.put("result", "error");
                            poJSON.put("message", poGRider.getErrMsg());
                        }
                    } else {
                        poJSON.put("result", "success");
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

    /**
     * Prints all the public methods used<br>
     * and prints the column names of this entity.
     */
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

            for (int lnCtr = 1; lnCtr <= lnRow; lnCtr++) {
                System.out.println("Column index: " + (lnCtr) + " --> Label: " + poEntity.getMetaData().getColumnLabel(lnCtr));
                if (poEntity.getMetaData().getColumnType(lnCtr) == Types.CHAR
                        || poEntity.getMetaData().getColumnType(lnCtr) == Types.VARCHAR) {

                    System.out.println("Column index: " + (lnCtr) + " --> Size: " + poEntity.getMetaData().getColumnDisplaySize(lnCtr));
                }
            }
        } catch (SQLException e) {
        }

    }
    
    /**
     * Gets the SQL statement for this entity.
     *
     * @return SQL Statement
     */
    public String makeSQL() {
        return MiscUtil.makeSQL(this, "");
    }
    
    /**
     * Gets the SQL Select statement for this entity.
     *
     * @return SQL Select Statement
     */
    public String makeSelectSQL() {
        return MiscUtil.makeSelect(this);
    }
    
    public String getSQL(){
        return    "   SELECT "                                                                                               
                + "   a.sClientID "                                                                                          
                + " , a.sAgentTyp "                                                                                          
                + " , a.cRecdStat "                                                                                          
                + " , a.sModified "                                                                                          
                + " , a.dModified "                                                                                          
                + " , b.cClientTp "                                                                                          
                + " , b.sLastName "                                                                                          
                + " , b.sFrstName "                                                                                          
                + " , b.sMiddName "                                                                                          
                + " , b.sCompnyNm "                                                                                          
//                + " , c.sMobileNo "                                                                                          
//                + " , d.sAccountx "                                                                                          
//                + " , e.sEmailAdd "                                                                                          
                + " , IFNULL(CONCAT( IFNULL(CONCAT(g.sHouseNox,' ') , ''), "                                                 
                + " IFNULL(CONCAT(g.sAddressx,' ') , ''),  "                                                                 
                + " IFNULL(CONCAT(i.sBrgyName,' '), ''),   "                                                                 
                + " IFNULL(CONCAT(h.sTownName, ', '),''),  "                                                                 
                + " IFNULL(CONCAT(j.sProvName),'') )	, '') AS sAddressx  "                                                  
                + " FROM sales_agent a   "                                                                                   
                + " LEFT JOIN client_master b ON b.sClientID = a.sClientID "                                                 
//                + " LEFT JOIN client_mobile c ON c.sClientID = a.sClientID AND c.cPrimaryx = 1 AND c.cRecdStat = 1  "        
//                + " LEFT JOIN client_social_media d ON  d.sClientID = a.sClientID AND d.cRecdStat = 1   "                    
//                + " LEFT JOIN client_email_address e ON  e.sClientID = a.sClientID AND e.cPrimaryx = 1 AND e.cRecdStat = 1 " 
                + " LEFT JOIN client_address f ON f.sClientID = a.sClientID AND f.cPrimaryx = 1 "                            
                + " LEFT JOIN addresses g ON g.sAddrssID = f.sAddrssID "                                                     
                + " LEFT JOIN TownCity h ON h.sTownIDxx = g.sTownIDxx  "                                                     
                + " LEFT JOIN barangay i ON i.sBrgyIDxx = g.sBrgyIDxx AND i.sTownIDxx = g.sTownIDxx  "                       
                + " LEFT JOIN Province j ON j.sProvIDxx = h.sProvIDxx  " ;                                                                  
    }
    
    /**
     * Description: Sets the ID of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setClientID(String fsValue) {
        return setValue("sClientID", fsValue);
    }

    /**
     * @return The ID of this record.
     */
    public String getClientID() {
        return (String) getValue("sClientID");
    }
    
    /**
     * Description: Sets the ID of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setAgentTyp(String fsValue) {
        return setValue("sAgentTyp", fsValue);
    }

    /**
     * @return The ID of this record.
     */
    public String getAgentTyp() {
        return (String) getValue("sAgentTyp");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setRecdStat(String fsValue) {
        return setValue("cRecdStat", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getRecdStat() {
        return (String) getValue("cRecdStat");
    }
    
    /**
     * Sets record as active.
     *
     * @param fbValue
     * @return result as success/failed
     */
    public JSONObject setActive(boolean fbValue) {
        return setValue("cRecdStat", fbValue ? "1" : "2");
    }

    /**
     * @return If record is active.
     */
    public boolean isActive() {
        return ((String) getValue("cRecdStat")).equals("1");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setModified(String fsValue) {
        return setValue("sModified", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getModified() {
        return (String) getValue("sModified");
    }
    
    /**
     * Sets the date and time the record was modified.
     *
     * @param fdValue
     * @return result as success/failed
     */
    public JSONObject setModifiedDte(Date fdValue) {
        return setValue("dModified", fdValue);
    }

    /**
     * @return The date and time the record was modified.
     */
    public Date getModifiedDte() {
        return (Date) getValue("dModified");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setLastName(String fsValue){
        setValue("sLastName", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getLastName(){
        return (String) getValue("sLastName");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setFirstName(String fsValue){
        setValue("sFrstName", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getFirstName(){
        return (String) getValue("sFrstName");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setMiddleName(String fsValue){
        setValue("sMiddName", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getMiddleName(){
        return (String) getValue("sMiddName");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setCompnyNm(String fsValue){
        setValue("sCompnyNm", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getCompnyNm(){
        return (String) getValue("sCompnyNm");
    }
    
//    /**
//     * Sets the value of this record.
//     * 
//     * @param fsValue 
//     * @return  True if the record assignment is successful.
//     */
//    public boolean setMobileNo(String fsValue){
//        setValue("sMobileNo", fsValue);
//        return true;
//    }
//    
//    /** 
//     * @return The value of this record. 
//     */
//    public String getMobileNo(){
//        return (String) getValue("sMobileNo");
//    }
//    
//    /**
//     * Sets the value of this record.
//     * 
//     * @param fsValue 
//     * @return  True if the record assignment is successful.
//     */
//    public boolean setAccount(String fsValue){
//        setValue("sAccountx", fsValue);
//        return true;
//    }
//    
//    /** 
//     * @return The value of this record. 
//     */
//    public String getAccount(){
//        return (String) getValue("sAccountx");
//    }
//    
//    /**
//     * Sets the value of this record.
//     * 
//     * @param fsValue 
//     * @return  True if the record assignment is successful.
//     */
//    public boolean setEmailAdd(String fsValue){
//        setValue("sEmailAdd", fsValue);
//        return true;
//    }
//    
//    /** 
//     * @return The value of this record. 
//     */
//    public String getEmailAdd(){
//        return (String) getValue("sEmailAdd");
//    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setAddress(String fsValue){
        setValue("sAddressx", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getAddress(){
        return (String) getValue("sAddressx");
    }
    
    
    
    
}

