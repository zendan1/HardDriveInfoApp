package com.example.harddriveinfoapp.models;

public class TierEntry {
    private String documentId;  // ID документа в Firestore
    private String driveId;     // ID диска (может не понадобиться)
    private String name;        // название диска (Seagate BarraCuda, SSD и т.д.)
    private String type;        // например "hdd", "ssd sata", "ssd m2"
    private String imageUrl;    // ссылка на изображение
    private String tier;        // буква Tier (A, B, C, S и т.д.)
    private double price;       // цена (если нужно)

    // Пустой конструктор обязателен для Firestore
    public TierEntry() { }

    // Геттеры и сеттеры
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDriveId() {
        return driveId;
    }
    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTier() {
        return tier;
    }
    public void setTier(String tier) {
        this.tier = tier;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
}
