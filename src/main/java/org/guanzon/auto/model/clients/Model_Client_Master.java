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
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.iface.GEntity;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_Client_Master implements GEntity{
    final String XML = "Model_Client_Master.xml";
    Connection poConn;          //connection
    CachedRowSet poEntity;      //rowset
    String psMessage;           //warning, success or error message
    GRider poGRider;
    int pnEditMode;
    
    public JSONObject poJSON;
    
    private final String psDefaultDate = "1900-01-01";
    
    public Model_Client_Master(GRider poValue){
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
            poEntity.updateString("cClientTp", Logical.NO);
            poEntity.updateObject("dBirthDte", SQLUtil.toDate(psDefaultDate, SQLUtil.FORMAT_SHORT_DATE));
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
        return "client_master";
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
        
        //replace with the primary key column info
        setClientID(MiscUtil.getNextCode(getTable(), "sClientID", true, poConn, poGRider.getBranchCode()));
        
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    @Override
    public JSONObject openRecord(String fsValue) {
        System.out.println("----------------------LOAD CLIENT MASTER---------------------------");
        poJSON = new JSONObject();

        String lsSQL = MiscUtil.addCondition(getSQL(), "a.sClientID = " + SQLUtil.toSQL(fsValue));
        System.out.println(lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);

        try {
            if (loRS.next()){
                for (int lnCtr = 1; lnCtr <= loRS.getMetaData().getColumnCount(); lnCtr++){
                    setValue(lnCtr, loRS.getObject(lnCtr));
                    System.out.println(loRS.getMetaData().getColumnLabel(lnCtr) + " = " + loRS.getString(lnCtr));
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
        String lsExclude = "sCntryNme»sTownName»sCustName»sSpouseNm»sAddressx";
        String lsSQL;
        poJSON = new JSONObject();
        
        System.out.println("SPOUSE ID : " + getSpouseID());
        
        setValue("sSpouseID", getSpouseID());
        
        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE){
            if(getClientTp().equals("0")){
                if(getValue("sMiddName") == null){
                    setCompnyNm(getValue("sLastName")  + ", " + getValue("sFrstName"));
                } else {
                    setCompnyNm(getValue("sLastName")  + ", " + getValue("sFrstName") + " " + getValue("sMiddName"));
                }
            } else {
                setBirthDte(SQLUtil.toDate(psDefaultDate, SQLUtil.FORMAT_SHORT_DATE));
            }
            if (pnEditMode == EditMode.ADDNEW){
                //replace with the primary key column info
                setClientID(MiscUtil.getNextCode(getTable(), "sClientID", true, poGRider.getConnection(), poGRider.getBranchCode()));
                setModifiedBy(poGRider.getUserID());
                setModifiedDte(poGRider.getServerDate());
                setEntryBy( poGRider.getUserID());
                setEntryDte(poGRider.getServerDate());
                
                lsSQL = MiscUtil.makeSQL(this, lsExclude);
                
                if (!lsSQL.isEmpty()){
                    if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                        poJSON.put("result", "success");
                        poJSON.put("sClientID", getClientID());
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
                Model_Client_Master loOldEntity = new Model_Client_Master(poGRider);
                
                //replace with the primary key column info
                JSONObject loJSON = loOldEntity.openRecord(this.getClientID());
                
                if ("success".equals((String) loJSON.get("result"))){
                    //replace the condition based on the primary key column of the record
                    setModifiedDte(poGRider.getServerDate());
                    setModifiedBy(poGRider.getUserID());
                    
                    System.out.println(this.getSpouseID());
                    System.out.println(this.getSpouseNm());
                    
                    System.out.println("old " + loOldEntity.getSpouseID());
                    System.out.println("old " + loOldEntity.getSpouseNm());
                    
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, "sClientID = " + SQLUtil.toSQL(this.getClientID()), lsExclude);
                    
                    if (!lsSQL.isEmpty()){
                        if (poGRider.executeQuery(lsSQL, getTable(), poGRider.getBranchCode(), "") > 0){
                            poJSON.put("result", "success");
                            poJSON.put("sClientID", getClientID());
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
    
    public String getMessage(){
        return psMessage;
    }
    
    public String getSQL(){
        return    " SELECT "                                                                                              
                + "  IFNULL(a.sClientID,'') sClientID " //1                                                             
                + ", IFNULL(a.sLastName,'') sLastName " //2                                                             
                + ", IFNULL(a.sFrstName,'') sFrstName " //3                                                             
                + ", IFNULL(a.sMiddName,'') sMiddName " //4                                                             
                + ", IFNULL(a.sMaidenNm,'') sMaidenNm " //5                                                             
                + ", IFNULL(a.sSuffixNm,'') sSuffixNm " //6                                                             
                + ", IFNULL(a.sTitlexxx,'') sTitlexxx " //7                                                             
                + ", IFNULL(a.cGenderCd,'') cGenderCd " //8                                                             
                + ", IFNULL(a.cCvilStat,'') cCvilStat " //9                                                             
                + ", IFNULL(a.sCitizenx,'') sCitizenx " //10                                                            
                + ", a.dBirthDte "                       //11                                                                                  
                + ", IFNULL(a.sBirthPlc,'') sBirthPlc " //12                                                            
                + ", IFNULL(a.sTaxIDNox,'') sTaxIDNox " //13                                                            
                + ", IFNULL(a.sLTOIDxxx,'') sLTOIDxxx " //14                                                            
                + ", IFNULL(a.sAddlInfo,'') sAddlInfo " //15                                                            
                + ", IFNULL(a.sCompnyNm,'') sCompnyNm " //16                                                            
                + ", IFNULL(a.sClientNo,'') sClientNo " //17                                                            
                + ", IFNULL(a.cClientTp,'') cClientTp " //18                                                            
                + ", IFNULL(a.cRecdStat,'') cRecdStat " //19                                                            
                + ", IFNULL(a.sEntryByx,'') sEntryByx " //20                                                            
                + ", a.dEntryDte"                       //21                                                                                  
                + ", IFNULL(a.sModified,'') sModified " //22                                                            
                + ", a.dModified "                       //23                                                                                  
                + ", IFNULL(a.sSpouseID,'') sSpouseID " //24                                                            
                + ", IFNULL(b.sCntryNme, '') sCntryNme "   //25                                                         
                + ", TRIM(CONCAT(c.sTownName, ', ', d.sProvName)) sTownName"   //26                                     
                + ", TRIM(CONCAT(a.sLastName, ', ', a.sFrstName, ' ', a.sSuffixNm, ' ', a.sMiddName)) sCustName"   //27 
                + ", TRIM(CONCAT(e.sLastName, ', ', e.sFrstName)) sSpouseNm"   //28                                     
                + ", IFNULL(CONCAT( IFNULL(CONCAT(g.sHouseNox,' ') , ''), "                                             
                + "    IFNULL(CONCAT(g.sAddressx,' ') , ''), "                                                          
                + "    IFNULL(CONCAT(h.sBrgyName,' '), ''),  "                                                          
                + "    IFNULL(CONCAT(i.sTownName, ', '),''), "                                                          
                + "    IFNULL(CONCAT(j.sProvName),'') )	, '') AS sAddressx   "  //29  
                //+ " CASE WHEN a.cClientTp = '0' THEN 'CLIENT' ELSE 'COMPANY' END AS cClientTp "  //30                                  
                + " FROM " + getTable() + " a"                                                                          
                + " LEFT JOIN Country b ON a.sCitizenx = b.sCntryCde"                                                   
                + " LEFT JOIN TownCity c ON a.sBirthPlc = c.sTownIDxx"                                                  
                + " LEFT JOIN Province d ON c.sProvIDxx = d.sProvIDxx"                                                  
                + " LEFT JOIN client_master e ON e.sClientID = a.sSpouseID"                                             
                + " LEFT JOIN client_address f ON f.sClientID = a.sClientID AND f.cPrimaryx = '1'"                      
                + " LEFT JOIN addresses g ON g.sAddrssID = f.sAddrssID"                                                 
                + " LEFT JOIN barangay h ON h.sBrgyIDxx = g.sBrgyIDxx"                                                  
                + " LEFT JOIN towncity i ON i.sTownIDxx = g.sTownIDxx"                                                  
                + " LEFT JOIN province j ON j.sProvIDxx = i.sProvIDxx" ;                                                
    }
    
    /**
     * Sets the ID of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setClientID(String fsValue){
        setValue("sClientID", fsValue);
        return true;
    }
    
    /** 
     * @return The ID of this record. 
     */
    public String getClientID(){
        return (String) getValue("sClientID");
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
    public boolean setMaidenName(String fsValue){
        setValue("sMaidenNm", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getMaidenName(){
        return (String) getValue("sMaidenNm");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setSuffixName(String fsValue){
        setValue("sSuffixNm", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getSuffixName(){
        return (String) getValue("sSuffixNm");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setTitle(String fsValue){
        setValue("sTitlexxx", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getTitle(){
        return (String) getValue("sTitlexxx");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setGender(String fsValue){
        setValue("cGenderCd", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getGender(){
        return (String) getValue("cGenderCd");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setCvilStat(String fsValue){
        setValue("cCvilStat", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getCvilStat(){
        return (String) getValue("cCvilStat");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setCitizen(String fsValue){
        setValue("sCitizenx", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getCitizen(){
        return (String) getValue("sCitizenx");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setBirthDte(Date fdValue){
        setValue("dBirthDte", fdValue);
        return true;
    }
    
    /**
     * @return The date of birth of the client.
     */
    public Date getBirthDte(){
        System.out.println("dBirthDte" + getValue("dBirthDte"));
        Date date = null;
        if(!getValue("dBirthDte").toString().isEmpty()){
            date = CommonUtils.toDate(getValue("dBirthDte").toString());
        }
        return date;
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setBirthPlc(String fsValue){
        setValue("sBirthPlc", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getBirthPlc(){
        return (String) getValue("sBirthPlc");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setTaxIDNo(String fsValue){
        setValue("sTaxIDNox", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getTaxIDNo(){
        return (String) getValue("sTaxIDNox");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setLTOID(String fsValue){
        setValue("sLTOIDxxx", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getLTOID(){
        return (String) getValue("sLTOIDxxx");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setAddlInfo(String fsValue){
        setValue("sAddlInfo", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getAddlInfo(){
        return (String) getValue("sAddlInfo");
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
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setClientNo(String fsValue){
        setValue("sClientNo", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getClientNo(){
        return (String) getValue("sClientNo");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setClientTp(String fsValue){
        setValue("cClientTp", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getClientTp(){
        return (String) getValue("cClientTp");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setRecdStat(String fsValue){
        setValue("cRecdStat", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getRecdStat(){
        return (String) getValue("cRecdStat");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setEntryBy(String fsValue){
        setValue("sEntryByx", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getEntryBy(){
        return (String) getValue("sEntryByx");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setEntryDte(Date fdValue){
        setValue("dEntryDte", fdValue);
        return true;
    }
    
    /**
     * @return The date and time the record was modified.
     */
    public Date getEntryDte(){
        return (Date) getValue("dEntryDte");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setModifiedBy(String fsValue){
        setValue("sModified", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getModifiedBy(){
        return (String) getValue("sModified");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setModifiedDte(Date fdValue){
        setValue("dModified", fdValue);
        return true;
    }
    
    /**
     * @return The date and time the record was modified.
     */
    public Date getModifiedDte(){
        return (Date) getValue("dModified");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setSpouseID(String fsValue){
        setValue("sSpouseID", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getSpouseID(){
        return (String) getValue("sSpouseID");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setCntryNme(String fsValue){
        setValue("sCntryNme", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getCntryNme(){
        return (String) getValue("sCntryNme");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setTownName(String fsValue){
        setValue("sTownName", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getTownName(){
        return (String) getValue("sTownName");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setCustName(String fsValue){
        setValue("sCustName", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getCustName(){
        return (String) getValue("sCustName");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setSpouseNm(String fsValue){
        setValue("sSpouseNm", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getSpouseNm(){
        return (String) getValue("sSpouseNm");
    }
    
    /**
     * Sets the value of this record.
     * 
     * @param fsValue 
     * @return  True if the record assignment is successful.
     */
    public boolean setAddressx(String fsValue){
        setValue("sAddressx", fsValue);
        return true;
    }
    
    /** 
     * @return The value of this record. 
     */
    public String getAddressx(){
        return (String) getValue("sAddressx");
    }
}
