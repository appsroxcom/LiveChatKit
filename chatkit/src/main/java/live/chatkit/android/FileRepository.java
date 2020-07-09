package live.chatkit.android;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import live.chatkit.android.model.AttachmentVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

/*
 * storage
 * 	{userId}
 */
public class FileRepository extends BaseRepository {

    private static final String TAG = "FileRepository";

    private static class Lazy {
        private static final FileRepository INSTANCE = new FileRepository();
    }
    public static FileRepository getInstance() {
        return Lazy.INSTANCE;
    }

    private FileRepository() {}

    @Override
    public void clear() {
    }

    /**
     * Get attachment in result
     *
     * @param localUri
     * @param folder
     * @param listener
     */
    public void putFile(Uri localUri, String folder, final ResultListener listener) {
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReference(folder)
                        .child(UUID.randomUUID().toString());//message.id

        storageReference.putFile(localUri).addOnCompleteListener(
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            StorageMetadata metadata = task.getResult().getMetadata();
                            final AttachmentVO attachment = new AttachmentVO(metadata.getReference().toString(), metadata.getContentType(), metadata.getName(), metadata.getSizeBytes());

                            metadata.getReference().getDownloadUrl().addOnCompleteListener(
                                    new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                attachment.url = task.getResult().toString();
                                                if (listener != null) listener.onSuccess(attachment);
                                            } else {
                                                Log.w(TAG, "Getting download url was not successful.", task.getException());
                                                if (listener != null) listener.onFailure(task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.w(TAG, "File upload task was not successful.", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Get download url in result
     *
     * @param bitmap
     * @param folder
     * @param listener
     */
    private void putAvatar(Bitmap bitmap, String folder, final ResultListener listener) {
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReference(folder)
                        .child("avatar.png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        storageReference.putBytes(data).addOnCompleteListener(
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            StorageMetadata metadata = task.getResult().getMetadata();
                            metadata.getReference().getDownloadUrl().addOnCompleteListener(
                                    new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                if (listener != null) listener.onSuccess(task.getResult().toString());
                                            } else {
                                                Log.w(TAG, "Getting download url was not successful.", task.getException());
                                                if (listener != null) listener.onFailure(task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.w(TAG, "File upload task was not successful.", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Get url in result
     *
     * @param remoteUri
     * @param listener
     */
    public void getDownloadUrl(String remoteUri, final ResultListener listener) {
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReferenceFromUrl(remoteUri);

        storageReference.getDownloadUrl().addOnCompleteListener(
                new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String downloadUrl = task.getResult().toString();
                            if (listener != null) listener.onSuccess(downloadUrl);
                        } else {
                            Log.w(TAG, "Getting download url was not successful.", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Download file
     *
     * @param remoteUri
     * @param listener
     */
    public void getFile(String remoteUri, Uri destinationUri, final ResultListener listener) {
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReferenceFromUrl(remoteUri);

        storageReference.getFile(destinationUri).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                if (listener != null) listener.onSuccess(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w(TAG, "File download was not successful.", exception);
                if (listener != null) listener.onFailure(exception);
            }
        });
    }

    /**
     * Delete file
     *
     * @param remoteUri
     * @param listener
     */
    public void deleteFile(String remoteUri, final ResultListener listener) {
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReferenceFromUrl(remoteUri);

        storageReference
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting file", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

    /**
     * Delete user data
     *
     * @param folder
     * @param listener
     */
    public void deleteData(final String folder, final ResultListener listener) {
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReference(folder);

        storageReference.list(100)
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference file : listResult.getItems()) {
                            file.delete();
                        }

                        // Recurse onto next page
                        if (listResult.getPageToken() != null) {
                            deleteData(folder, listener);
                        } else {
                            if (listener != null) listener.onSuccess(null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting data", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

}
