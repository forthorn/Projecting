package com.forthorn.projecting.entity;

/**
 * Created by: Forthorn
 * Date: 10/31/2017.
 * Description:
 */

public class IMAccount {


    /**
     * code : 200
     * msg : 获取数据成功
     * data : {"equipment_id":"4","equipment_code":"1234","equipment_im_account":"4324525","equipment_im_password":"dsafdsa "}
     */

    private String code;
    private String msg;
    private Account data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Account getData() {
        return data;
    }

    public void setData(Account data) {
        this.data = data;
    }

    public static class Account {
        /**
         * equipment_id : 4
         * equipment_code : 1234
         * equipment_im_account : 4324525
         * equipment_im_password : dsafdsa
         */

        private int equipment_id;
        private String equipment_code;
        private String equipment_im_account;
        private String equipment_im_password;

        public int getEquipment_id() {
            return equipment_id;
        }

        public void setEquipment_id(int equipment_id) {
            this.equipment_id = equipment_id;
        }

        public String getEquipment_code() {
            return equipment_code;
        }

        public void setEquipment_code(String equipment_code) {
            this.equipment_code = equipment_code;
        }

        public String getEquipment_im_account() {
            return equipment_im_account;
        }

        public void setEquipment_im_account(String equipment_im_account) {
            this.equipment_im_account = equipment_im_account;
        }

        public String getEquipment_im_password() {
            return equipment_im_password;
        }

        public void setEquipment_im_password(String equipment_im_password) {
            this.equipment_im_password = equipment_im_password;
        }
    }
}
