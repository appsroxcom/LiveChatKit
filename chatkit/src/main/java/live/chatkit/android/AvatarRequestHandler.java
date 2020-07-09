package live.chatkit.android;

import android.content.Context;
import android.graphics.Color;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

import get.avatar.android.AvatarUtil;

import static get.avatar.android.AvatarUtil.AVATAR_SCHEME;

public class AvatarRequestHandler extends RequestHandler {

    private Context mContext;

    public AvatarRequestHandler(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        return AVATAR_SCHEME.equals(data.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        return new Result(AvatarUtil.getAvatarBitmap(mContext, request.uri.toString(), Color.WHITE), Picasso.LoadedFrom.DISK);
    }

}
