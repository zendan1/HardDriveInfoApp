package com.example.harddriveinfoapp.models;

import java.util.Map;

/**
 * Модель «диска» (HDD/SSD SATA/SSD M.2).
 * Соответствует документам в коллекциях “hddDrives”, “ssdSataDrives” и “ssdM2Drives” Firestore.
 *
 * Структура каждого документа в Firestore:
 *   name       : String
 *   price      : Number
 *   imageUrl   : String
 *   productUrl : String
 *   specs      : Map<String,Object>
 *   type       : String   (например, “hdd sata” или “ssd sata” и т.д. – но в AddDriveDialogFragment мы его перезапишем)
 *
 * В дополнение к этому после чтения мы будем явно записывать d.setType("hdd") / ("ssd sata") / ("ssd m2").
 */
public class Drive {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
    private String productUrl;
    private Map<String, Object> specs;
    private String type;

    // Пустой конструктор нужен Firestore (doc.toObject(Drive.class))
    public Drive() { }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductUrl() {
        return productUrl;
    }
    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public Map<String, Object> getSpecs() {
        return specs;
    }
    public void setSpecs(Map<String, Object> specs) {
        this.specs = specs;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
