/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.guanzon.auto.model.clients;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
public class Model_Vehicle_Serial_Master implements GEntity {

    final String XML = "Model_Vehicle_Serial_Master.xml";

    GRider poGRider;                //application driver
    CachedRowSet poEntity;          //rowset
    JSONObject poJSON;              //json container
    int pnEditMode;                 //edit mode

    /**
     * Entity constructor
     *
     * @param foValue - GhostRider Application Driver
     */
    public Model_Vehicle_Serial_Master(GRider foValue) {
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
            //poEntity.updateString("cRecdStat", RecordStatus.ACTIVE);

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
        return "vehicle_serial";
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

        //replace with the primary key column info
        setSerialID(MiscUtil.getNextCode(getTable(), "sSerialID", true, poGRider.getConnection(), poGRider.getBranchCode()));

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }

    /**
     * Opens a record.
     *
     * @param fsCondition - filter values
     * @return result as success/failed
     */
    @Override
    public JSONObject openRecord(String fsCondition) {
        poJSON = new JSONObject();

        String lsSQL = MiscUtil.makeSelect(this);

        //replace the condition based on the primary key column of the record
        lsSQL = MiscUtil.addCondition(lsSQL, " sSerialID = " + SQLUtil.toSQL(fsCondition));

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
        poJSON = new JSONObject();

        if (pnEditMode == EditMode.ADDNEW || pnEditMode == EditMode.UPDATE) {
            String lsSQL;
            if (pnEditMode == EditMode.ADDNEW) {
                //replace with the primary key column info
                setSerialID(MiscUtil.getNextCode(getTable(), "sSerialID", true, poGRider.getConnection(), poGRider.getBranchCode()));

                lsSQL = makeSQL();

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
                Model_Vehicle_Serial_Master loOldEntity = new Model_Vehicle_Serial_Master(poGRider);

                //replace with the primary key column info
                JSONObject loJSON = loOldEntity.openRecord(this.getSerialID());

                if ("success".equals((String) loJSON.get("result"))) {
                    //replace the condition based on the primary key column of the record
                    lsSQL = MiscUtil.makeSQL(this, loOldEntity, "sSerialID = " + SQLUtil.toSQL(this.getSerialID()));

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
        return            "  SELECT "                                                                                               
                        + "  a.sSerialID " //1                                                                                           
                        + ", a.sBranchCD " //2                                                                                           
                        + ", a.sFrameNox " //3                                                                                           
                        + ", a.sEngineNo " //4                                                                                           
                        + ", a.sVhclIDxx " //5                                                                                           
                        + ", a.sClientID " //6                                                                                           
                        + ", a.sCoCltIDx " //7                                                                                           
                        + ", a.sCSNoxxxx " //8                                                                                           
                        + ", a.sDealerNm " //9                                                                                           
                        + ", a.sCompnyID " //10                                                                                          
                        + ", a.sKeyNoxxx " //11                                                                                          
                        + ", a.cIsDemoxx " //12                                                                                          
                        + ", a.cLocation " //13                                                                                          
                        + ", a.cSoldStat " //14                                                                                          
                        + ", a.cVhclNewx " //15                                                                                          
                        + ", a.sRemarksx " //16                                                                                          
                        + ", a.sEntryByx " //17                                                                                          
                        + ", a.dEntryDte " //18                                                                                          
                        + ", a.sModified " //19                                                                                          
                        + ", a.dModified " //20                                                                                          
                        + ", b.sPlateNox " //21                                                                                          
                        + ", b.dRegister " //22                                                                                          
                        + ", b.sPlaceReg " //23                                                                                          
                        + ", c.sMakeIDxx " //24                                                                                          
                        + ", d.sMakeDesc " //25                                                                                          
                        + ", c.sModelIDx " //26                                                                                          
                        + ", e.sModelDsc " //27                                                                                          
                        + ", c.sTypeIDxx " //28                                                                                          
                        + ", f.sTypeDesc " //29                                                                                          
                        + ", c.sColorIDx " //30                                                                                          
                        + ", g.sColorDsc " //31                                                                                          
                        + ", c.sTransMsn " //32                                                                                          
                        + ", c.nYearModl " //33                                                                                          
                        + ", c.sDescript " //34                                                                                                                                                                                   
                        + ", h.sCompnyNm " //35                                                                                          
                        + ", i.sCompnyNm " //36                                                                                          
                        + ", IFNULL(CONCAT(IFNULL(CONCAT(jj.sHouseNox,' ') , ''), IFNULL(CONCAT(jj.sAddressx,' ') , ''), "              
                        + "	IFNULL(CONCAT(l.sBrgyName,' '), ''), "              
                        + "	IFNULL(CONCAT(k.sTownName, ', '),''), "              
                        + "	IFNULL(CONCAT(m.sProvName),'') ), '') AS sOwnerAdd " //37                     
                        + ", IFNULL(CONCAT(IFNULL(CONCAT(nn.sHouseNox,' ') , ''), IFNULL(CONCAT(nn.sAddressx,' ') , ''), "              
                        + "	IFNULL(CONCAT(p.sBrgyName,' '), ''), "              
                        + "	IFNULL(CONCAT(o.sTownName, ', '),''), "              
                        + "	IFNULL(CONCAT(q.sProvName),''))	, '') AS sCoOwnerA " //38                      
                        + ",CASE "              
                        + "  WHEN a.cSoldStat = '0' THEN 'NON SALES CUSTOMER' "              
                        + "  WHEN a.cSoldStat = '1' THEN 'AVAILABLE FOR SALE' "              
                        + "  WHEN a.cSoldStat = '2' THEN 'VSP' "              
                        + "  WHEN a.cSoldStat = '3' THEN 'SOLD' "              
                        + " ELSE '' "              
                        + " END AS sVhclStat " //39         
                        + " , r.sReferNox " //40         
                        + " , r.dTransact " //41         
                        + " , s.sCompnyNm " //42         
                        + "FROM vehicle_serial a "              
                        + "LEFT JOIN vehicle_serial_registration b ON a.sSerialID = b.sSerialID "              
                        + "LEFT JOIN vehicle_master c ON c.sVhclIDxx = a.sVhclIDxx "              
                        + "LEFT JOIN vehicle_make   d ON d.sMakeIDxx = c.sMakeIDxx "              
                        + "LEFT JOIN vehicle_model  e ON e.sModelIDx = c.sModelIDx "              
                        + "LEFT JOIN vehicle_type   f ON f.sTypeIDxx = c.sTypeIDxx "              
                        + "LEFT JOIN vehicle_color  g ON g.sColorIDx = c.sColorIDx "              
                        + "LEFT JOIN client_master  h ON h.sClientID = a.sClientID "              
                        + "LEFT JOIN client_master  i ON i.sClientID = a.sCoCltIDx "              
                         /* Owner Address */                                                                                             
                        + "LEFT JOIN client_address j ON j.sClientID = a.sClientID AND j.cPrimaryx = '1' "              
                        + "LEFT JOIN addresses     jj ON jj.sAddrssID = j.sAddrssID "              
                        + "LEFT JOIN TownCity       k ON k.sTownIDxx = jj.sTownIDxx "              
                        + "LEFT JOIN barangay       l ON l.sBrgyIDxx = jj.sBrgyIDxx AND l.sTownIDxx = jj.sTownIDxx "              
                        + "LEFT JOIN Province       m ON m.sProvIDxx = k.sProvIDxx "              
                         /* Co Owner Address */                                                                                          
                        + "LEFT JOIN client_address n ON n.sClientID = a.sCoCltIDx AND n.cPrimaryx = '1' "              
                        + "LEFT JOIN addresses     nn ON nn.sAddrssID = n.sAddrssID "              
                        + "LEFT JOIN TownCity       o ON o.sTownIDxx = nn.sTownIDxx "              
                        + "LEFT JOIN barangay       p ON p.sBrgyIDxx = nn.sBrgyIDxx AND p.sTownIDxx = nn.sTownIDxx "              
                        + "LEFT JOIN Province       q ON q.sProvIDxx = o.sProvIDxx "              
                         /* UDR INFO */                                                                                                  
                        + "LEFT JOIN udr_master     r ON r.sSerialID = a.sSerialID AND r.sClientID = a.sClientID AND r.cTranStat = '1' "
                        + "LEFT JOIN client_master  s ON s.sClientID = r.sClientID ";
                
    }                                                                            

    /**
     * Description: Sets the ID of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setSerialID(String fsValue) {
        return setValue("sSerialID", fsValue);
    }

    /**
     * @return The ID of this record.
     */
    public String getSerialID() {
        return (String) getValue("sSerialID");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setBranchCD(String fsValue) {
        return setValue("sBranchCD", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getBranchCD() {
        return (String) getValue("sBranchCD");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setFrameNo(String fsValue) {
        return setValue("sFrameNox", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getFrameNo() {
        return (String) getValue("sFrameNox");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setEngineNo(String fsValue) {
        return setValue("sEngineNo", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getEngineNo() {
        return (String) getValue("sEngineNo");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setVhclID(String fsValue) {
        return setValue("sVhclIDxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getVhclID() {
        return (String) getValue("sVhclIDxx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setClientID(String fsValue) {
        return setValue("sClientID", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getClientID() {
        return (String) getValue("sClientID");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setCoCltID(String fsValue) {
        return setValue("sCoCltIDx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getCoCltID() {
        return (String) getValue("sCoCltIDx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setCSNo(String fsValue) {
        return setValue("sCSNoxxxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getCSNo() {
        return (String) getValue("sCSNoxxxx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setDealerNm(String fsValue) {
        return setValue("sDealerNm", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getDealerNm() {
        return (String) getValue("sDealerNm");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setCompnyID(String fsValue) {
        return setValue("sCompnyID", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getCompnyID() {
        return (String) getValue("sCompnyID");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setKeyNo(String fsValue) {
        return setValue("sKeyNoxxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getKeyNo() {
        return (String) getValue("sKeyNoxxx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setIsDemo(String fsValue) {
        return setValue("cIsDemoxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getIsDemo() {
        return (String) getValue("cIsDemoxx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setLocation(String fsValue) {
        return setValue("cLocation", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getLocation() {
        return (String) getValue("cLocation");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setSoldStat(String fsValue) {
        return setValue("cSoldStat", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getSoldStat() {
        return (String) getValue("cSoldStat");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setVhclNew(String fsValue) {
        return setValue("cVhclNewx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getVhclNew() {
        return (String) getValue("cVhclNewx");
    }
    
//    /**
//     * Description: Sets the Value of this record.
//     *
//     * @param fsValue
//     * @return result as success/failed
//     */
//    public JSONObject setRecdStat(String fsValue) {
//        return setValue("cRecdStat", fsValue);
//    }
//
//    /**
//     * @return The Value of this record.
//     */
//    public String gsetRecdStat() {
//        return (String) getValue("cRecdStat");
//    }
    
//    /**
//     * Sets record as active.
//     *
//     * @param fbValue
//     * @return result as success/failed
//     */
//    public JSONObject setActive(boolean fbValue) {
//        return setValue("cRecdStat", fbValue ? "1" : "0");
//    }
//
//    /**
//     * @return If record is active.
//     */
//    public boolean isActive() {
//        return ((String) getValue("cRecdStat")).equals("1");
//    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setEntryBy(String fsValue) {
        return setValue("sEntryByx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getEntryBy() {
        return (String) getValue("sEntryByx");
    }
    
    /**
     * Sets the date and time the record was modified.
     *
     * @param fdValue
     * @return result as success/failed
     */
    public JSONObject setEntryDte(Date fdValue) {
        return setValue("dEntryDte", fdValue);
    }

    /**
     * @return The date and time the record was modified.
     */
    public Date getEntryDte() {
        return (Date) getValue("dEntryDte");
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
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setPlateNo(String fsValue) {
        return setValue("sPlateNox", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getPlateNo() {
        return (String) getValue("sPlateNox");
    }
    
    /**
     * Sets the date and time the record was modified.
     *
     * @param fdValue
     * @return result as success/failed
     */
    public JSONObject setRegisterDte(Date fdValue) {
        return setValue("dRegister", fdValue);
    }

    /**
     * @return The date and time the record was modified.
     */
    public Date getRegisterDte() {
        return (Date) getValue("dRegister");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setPlaceReg(String fsValue) {
        return setValue("sPlaceReg", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getPlaceReg() {
        return (String) getValue("sPlaceReg");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setMakeID(String fsValue) {
        return setValue("sMakeIDxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getMakeID() {
        return (String) getValue("sMakeIDxx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setMakeDesc(String fsValue) {
        return setValue("sMakeDesc", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getMakeDesc() {
        return (String) getValue("sMakeDesc");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setModelID(String fsValue) {
        return setValue("sModelIDx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getModelID() {
        return (String) getValue("sModelIDx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setModelDsc(String fsValue) {
        return setValue("sModelDsc", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getModelDsc() {
        return (String) getValue("sModelDsc");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setTypeID(String fsValue) {
        return setValue("sTypeIDxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getTypeID() {
        return (String) getValue("sTypeIDxx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setTypeDesc(String fsValue) {
        return setValue("sTypeDesc", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getTypeDesc() {
        return (String) getValue("sTypeDesc");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setColorID(String fsValue) {
        return setValue("sColorIDx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getColorID() {
        return (String) getValue("sColorIDx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setColorDsc(String fsValue) {
        return setValue("sColorDsc", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getColorDsc() {
        return (String) getValue("sColorDsc");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setTransMsn(String fsValue) {
        return setValue("sTransMsn", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getTransMsn() {
        return (String) getValue("sTransMsn");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fnValue
     * @return result as success/failed
     */
    public JSONObject setYearModl(Integer fnValue) {
        return setValue("nYearModl", fnValue);
    }

    /**
     * @return The Value of this record.
     */
    public Integer getYearModl() {
        return (Integer) getValue("nYearModl");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setDescript(String fsValue) {
        return setValue("sDescript", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getDescript() {
        return (String) getValue("sDescript");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setRemarks(String fsValue) {
        return setValue("sRemarksx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setOwnerNam(String fsValue) {
        return setValue("sOwnerNam", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getOwnerNam() {
        return (String) getValue("sOwnerNam");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setCoOwnerN(String fsValue) {
        return setValue("sCoOwnerN", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getCoOwnerN() {
        return (String) getValue("sCoOwnerN");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setOwnerAdd(String fsValue) {
        return setValue("sOwnerAdd", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getOwnerAdd() {
        return (String) getValue("sOwnerAdd");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setCoOwnerA(String fsValue) {
        return setValue("sCoOwnerA", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getCoOwnerA() {
        return (String) getValue("sCoOwnerA");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setVhclStat(String fsValue) {
        return setValue("sVhclStat", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getVhclStat() {
        return (String) getValue("sVhclStat");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setUdrNo(String fsValue) {
        return setValue("sUdrNoxxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getUdrNo() {
        return (String) getValue("sUdrNoxxx");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fdValue
     * @return result as success/failed
     */
    public JSONObject setUdrDate(Date fdValue) {
        return setValue("sUdrDatex", fdValue);
    }

    /**
     * @return The Value of this record.
     */
    public Date getUdrDate() {
        return (Date) getValue("sUdrDatex");
    }
    
    /**
     * Description: Sets the Value of this record.
     *
     * @param fsValue
     * @return result as success/failed
     */
    public JSONObject setSoldTo(String fsValue) {
        return setValue("sSoldToxx", fsValue);
    }

    /**
     * @return The Value of this record.
     */
    public String getSoldTo() {
        return (String) getValue("sSoldToxx");
    }
}
