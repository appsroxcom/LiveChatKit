package live.chatkit.android;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import live.chatkit.android.crypto.CryptoUtil;
import live.chatkit.android.model.UserVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.sample.common.data.model.User;

import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    protected ImageLoader imageLoader;
    private Picasso picasso;
    private User mCurrentUser;
    private String publicKey, privateKey;

    protected abstract void onInit(User currentUser);

    protected User getCurrentUser() {
        return mCurrentUser;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Picasso.Builder picassoBuilder = new Picasso.Builder(getApplicationContext());
        picassoBuilder.addRequestHandler(new AvatarRequestHandler(getApplicationContext()));
        picasso = picassoBuilder.build();

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                if (!TextUtils.isEmpty(url)) picasso.load(url)/*.placeholder(R.drawable.placeholder)*/.into(imageView);
            }
        };
    }

    protected void login(String... args) {
        String userId = AuthRepository.getInstance().getCurrentUserId();
        if (userId != null) {
            if (ChatUtil.getPrivateKey(getApplicationContext()) == null) {
                exit();
                return;
            }

            fetchUser(userId, new CallbackListener() {
                @Override
                public void onResult(Object result) {
                    mCurrentUser = UserVO.toUser((UserVO) result);
                    onInit(mCurrentUser);
                }
            });

        } else {
            AuthRepository.getInstance().login(new ResultListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BaseActivity.this, R.string.err_auth, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onSuccess(Object result) {
                    init((String) result);
                }
            }, args);
        }
    }

    protected void logout() {
        AuthRepository.getInstance().logout();
    }

    private void exit() {
        Toast.makeText(this, R.string.err_retry, Toast.LENGTH_LONG).show();
        logout();
        finish();
    }

    private void init(final String userId) {
        mCurrentUser = new User(userId, getString(R.string.guest), null, false);

        //init keys
        try {
            Map keyPair = CryptoUtil.generateKeyPair();
            publicKey = (String) keyPair.get("publicKey");
            privateKey = (String) keyPair.get("privateKey");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(publicKey) || TextUtils.isEmpty(privateKey)) {
            exit();
            return;
        }

        UserVO user = new UserVO(mCurrentUser);
        user.publicKey = publicKey;
        UserRepository.getInstance().addUser(user, new ResultListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exit();
            }

            @Override
            public void onSuccess(Object result) {
                ChatUtil.setPrivateKey(getApplicationContext(), privateKey);

                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();
                                UserRepository.getInstance().updateFcmToken(userId, token, null);
                            }
                        });

                onInit(mCurrentUser);
            }
        });
    }

    protected void fetchUser(final String userId, final CallbackListener listener) {
        UserRepository.getInstance().fetchUser(true, userId, new ResultListener() {
            @Override
            public void onSuccess(Object result) {
                if (result == null) {
                    UserRepository.getInstance().fetchUser(false, userId, new ResultListener() {
                        @Override
                        public void onSuccess(Object result) {
                            if (result != null) {
                                if (listener != null) listener.onResult(result);
                            }
                        }
                    });
                } else {
                    if (listener != null) listener.onResult(result);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                UserRepository.getInstance().fetchUser(false, userId, new ResultListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result != null) {
                            if (listener != null) listener.onResult(result);
                        }
                    }
                });
            }
        });
    }

    protected void fetchChatKey(final String chatId, final String userId, final CallbackListener listener) {
        ChatRepository.getInstance().fetchKey(true, chatId, userId, new ResultListener() {
            @Override
            public void onSuccess(Object result) {
                if (result == null) {
                    ChatRepository.getInstance().fetchKey(false, chatId, userId, new ResultListener() {
                        @Override
                        public void onSuccess(Object result) {
                            if (result != null) {
                                if (listener != null) listener.onResult(result);
                            }
                        }
                    });
                } else {
                    if (listener != null) listener.onResult(result);
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                ChatRepository.getInstance().fetchKey(false, chatId, userId, new ResultListener() {
                    @Override
                    public void onSuccess(Object result) {
                        if (result != null) {
                            if (listener != null) listener.onResult(result);
                        }
                    }
                });
            }
        });
    }
}
