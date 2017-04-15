/**
 *
 */
package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 货柜货道可见状态对象
 *
 * @author liujia on 2016年6月27日
 */
public class CabinetVisiableStateInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;



    /**
     * 设备DB用货柜号
     */
    private String dbCabinetNo;

    /**
     * 货道是否可见 0：不可见 1：可见
     */
    private Integer visiable;

    public Integer getVisiable() {
        return visiable;
    }

    public void setVisiable(Integer visiable) {
        this.visiable = visiable;
    }

    public String getDbCabinetNo() {
        return dbCabinetNo;
    }

    public void setDbCabinetNo(String dbCabinetNo) {
        this.dbCabinetNo = dbCabinetNo;
    }

}
