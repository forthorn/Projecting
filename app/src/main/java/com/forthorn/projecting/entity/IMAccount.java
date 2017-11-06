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
     * data : {"equipment_id":35,"equipment_code":"adfdf","equipment_im_account":"7fdfd7d0b61918587ee7ad51b492cef99d5ac33d","equipment_im_password":"c62a06cd720d077d343c8ba8a13d56a5ae91a6e1","longitude":"1","dimension":"1","address":"金海路1255号","province":"上海市","city":""}
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
         * equipment_id : 35
         * equipment_code : adfdf
         * equipment_im_account : 7fdfd7d0b61918587ee7ad51b492cef99d5ac33d
         * equipment_im_password : c62a06cd720d077d343c8ba8a13d56a5ae91a6e1
         * longitude : 1
         * dimension : 1
         * address : 金海路1255号
         * province : 上海市
         * city :
         */

        private int equipment_id;
        private String equipment_code;
        private String equipment_im_account;
        private String equipment_im_password;
        private String equipment_name;
        private String longitude;
        private String dimension;
        private String address;
        private String province;
        private String city;
        private String type;


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

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getEquipment_name() {
            return equipment_name;
        }

        public void setEquipment_name(String equipment_name) {
            this.equipment_name = equipment_name;
        }

        @Override
        public String toString() {
            return "Account{" +
                    "equipment_id=" + equipment_id +
                    ", equipment_code='" + equipment_code + '\'' +
                    ", equipment_im_account='" + equipment_im_account + '\'' +
                    ", equipment_im_password='" + equipment_im_password + '\'' +
                    ", equipment_name='" + equipment_name + '\'' +
                    ", longitude='" + longitude + '\'' +
                    ", dimension='" + dimension + '\'' +
                    ", address='" + address + '\'' +
                    ", province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "IMAccount{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
