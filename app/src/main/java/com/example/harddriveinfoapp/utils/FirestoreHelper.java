package com.example.harddriveinfoapp.utils;

import androidx.annotation.NonNull;
import com.example.harddriveinfoapp.models.Drive;
import com.example.harddriveinfoapp.models.TierEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference drivesCollection =
            db.collection(Constants.COLLECTION_DRIVES);
    private static final CollectionReference tierCollection =
            db.collection(Constants.COLLECTION_TIER_LIST);

    // 1. Получить список всех дисков по типу ("HDD", "SSD_SATA" или "SSD_M2")
    public static Task<QuerySnapshot> getDrivesByType(String type) {
        return drivesCollection
                .whereEqualTo(Constants.FIELD_TYPE, type)
                .get();
    }

    // 2. Получить конкретный диск по его ID
    public static Task<DocumentSnapshot> getDriveById(String driveId) {
        return drivesCollection.document(driveId).get();
    }

    // 3. Добавить новый диск (принимает поля как Map)
    public static Task<DocumentReference> addDrive(String name,
                                                   String type,
                                                   Map<String, Object> specs,
                                                   double price,
                                                   String imageUrl,
                                                   String productUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.FIELD_NAME, name);
        data.put(Constants.FIELD_TYPE, type);
        data.put(Constants.FIELD_SPECS, specs);
        data.put(Constants.FIELD_PRICE, price);
        data.put(Constants.FIELD_IMAGE_URL, imageUrl);
        data.put(Constants.FIELD_PRODUCT_URL, productUrl);
        return drivesCollection.add(data);
    }

    // 4. Обновить существующий диск (по ID)
    public static Task<Void> updateDrive(String driveId, Map<String, Object> updatedFields) {
        return drivesCollection.document(driveId).update(updatedFields);
    }

    // 5. Удалить диск
    public static Task<Void> deleteDrive(String driveId) {
        return drivesCollection.document(driveId).delete();
    }

    // -------- Tier-List --------

    // 6. Получить весь tier-лист (всех пользователей или текущего)
    public static Task<QuerySnapshot> getAllTierEntries() {
        return tierCollection.get();
    }

    // Если хотим получить только tier-лист текущего пользователя:
    public static Task<QuerySnapshot> getTierEntriesForUser(String userId) {
        return tierCollection
                .whereEqualTo(Constants.FIELD_USER_ID, userId)
                .get();
    }

    // 7. Добавить запись в tier-лист (driveId, tier, userId, timestamp)
    public static Task<DocumentReference> addTierEntry(String driveId, String tier, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.FIELD_DRIVE_ID, driveId);
        data.put(Constants.FIELD_TIER, tier);
        data.put(Constants.FIELD_USER_ID, userId);
        data.put("timestamp", Timestamp.now());
        return tierCollection.add(data);
    }

    // 8. Обновить tier (по documentId)
    public static Task<Void> updateTierEntry(String entryId, String newTier) {
        return tierCollection.document(entryId).update(Constants.FIELD_TIER, newTier);
    }

    // 9. Удалить запись tier-листа
    public static Task<Void> deleteTierEntry(String entryId) {
        return tierCollection.document(entryId).delete();
    }

    // 10. Получить все доступные типы дисков (например, distinct значения поля "type")
    //     Firestore не поддерживает distinct напрямую, но вы можете хранить отдельную коллекцию
    //     или массив «types» вручную. Для простоты: будем фильтровать на клиенте или
    //     заранее знать 3 возможных типа ("HDD", "SSD_SATA", "SSD_M2").
}
