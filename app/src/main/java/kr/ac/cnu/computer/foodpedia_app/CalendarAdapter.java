//package kr.ac.cnu.computer.foodpedia_app;
//
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.PopupWindow;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.cardview.widget.CardView;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.kakao.kakaolink.v2.KakaoLinkResponse;
//import com.kakao.kakaolink.v2.KakaoLinkService;
//import com.kakao.message.template.ButtonObject;
//import com.kakao.message.template.ContentObject;
//import com.kakao.message.template.FeedTemplate;
//import com.kakao.message.template.LinkObject;
//import com.kakao.message.template.SocialObject;
//import com.kakao.network.ErrorResult;
//import com.kakao.network.callback.ResponseCallback;
//import com.kakao.util.helper.log.Logger;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
//
//    Context mContext;
////    List<RecyclerItem> resultsList;
//    int itemLayout;
//
//    String kakaoid;
//    String url;
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        public TextView date, time, result, count;
//        public CardView cardview;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            date = itemView.findViewById(R.id.text_date);
//            time = itemView.findViewById(R.id.text_time);
//            result = itemView.findViewById(R.id.text_result);
//            count = itemView.findViewById(R.id.text_count);
//            cardview = itemView.findViewById(R.id.record_view);
//        }
//    }
//
//    public CalendarAdapter(Context mcontext, int itemLayout, String kakaoid) {
//        this.mContext = mcontext;
////        this.resultsList = resultsList;
//        this.itemLayout = itemLayout;
//        this.kakaoid = kakaoid;
//    }
//
//    public void showPopup(View view, String date, String time, String result) {
//        final View popupView = LayoutInflater.from(view.getContext()).inflate(R.layout.activity_dialog, null);
//        final PopupWindow mPopupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//
//        ImageView imageView = popupView.findViewById(R.id.DialogImage);
//
//        downLoadImageFromStorage(imageView, mContext, date, time, result); // 파이어베이스에서 그래프 불러오기
//
//        mPopupWindow.setFocusable(true); // 외부 영역 선택시 PopUp 종료
//        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
//
//        Button cancel = popupView.findViewById(R.id.Cancel);
//        cancel.setOnClickListener(v -> mPopupWindow.dismiss());
//
//        Button ok = popupView.findViewById(R.id.Ok);
//        ok.setOnClickListener(v -> shareResult(result));
//    }
//
//    private void downLoadImageFromStorage(ImageView imageView, Context mContext, String date, String time, String result) { // 달력관련접근 고치기
//        String fileName = kakaoid + "/" + date + "/" + time + "_" + result;
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageReference = storage.getReference().child(fileName);
//        // https://firebasestorage.googleapis.com/v0/b/[projectID].appspot.com/o/[folderName]%2F[파일 이름]
//        String s1 = "https://firebasestorage.googleapis.com/v0/b/keepwalking-5a03f.appspot.com/o/";
//        String s2 = kakaoid + "%2F" + date + "%2F" + time + "_" + result + "?alt=media";
//        url = s1 + s2;
//
//        storageReference.getDownloadUrl().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Glide.with(mContext.getApplicationContext())
//                        .load(task.getResult())
//                        .override(1024, 980)
//                        .into(imageView);
//                Toast.makeText(mContext.getApplicationContext(), "그래프가 정상적으로 로드되었습니다.", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(mContext.getApplicationContext(), "그래프가 정상적으로 로드되지 않았습니다.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void shareResult(String result) {
//        String userEmail = GlobalApplication.getGlobalApplicationContext().getBasicEmail();
//        String userName = GlobalApplication.getGlobalApplicationContext().getBasicName();
//        Log.e("dddd : ", userName);
//        int tempIdx = userEmail.lastIndexOf("@");
//        String tempStr1 = userEmail.substring(0, tempIdx) + ".";
//        String tempstr2 = userEmail.substring(tempIdx + 1);
//        userEmail = tempStr1 + tempstr2;
//        Log.e("이메일 : ", userEmail);
//        Intent email = new Intent(Intent.ACTION_SEND);
//        email.setType("plain/text");
//
//        // email setting 배열로 해놔서 복수 발송 가능
//        String[] address = {userEmail};
//        email.putExtra(Intent.EXTRA_EMAIL, address);
//        email.putExtra(Intent.EXTRA_SUBJECT, "[킵워킹] " + userName + "님의 측정결과입니다.");
//        email.putExtra(Intent.EXTRA_TEXT, "측정 결과를 확인해보세요.\n" + url);
//        mContext.startActivity(new Intent(email)
//                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        );
//
//        FeedTemplate params = FeedTemplate
//                .newBuilder(ContentObject.newBuilder("딥러닝을 통한 보행 건강 관리, 킵워킹", url,
//                        LinkObject.newBuilder().setWebUrl("https://developers.kakao.com")
//                                .setMobileWebUrl("https://developers.kakao.com").build())
//                        .setDescrption("나의 측정결과: " + result)
//                        .build())
//                .setSocial(SocialObject.newBuilder().setLikeCount(10).setCommentCount(20)
//                        .setSharedCount(30).setViewCount(40).build())
//                .addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder().setWebUrl("https://developers.kakao.com").setMobileWebUrl("https://developers.kakao.com").build()))
//                .build();
//
//        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
//        serverCallbackArgs.put("user_id", "${current_user_id}");
//        serverCallbackArgs.put("product_id", "${shared_product_id}");
//
//        KakaoLinkService.getInstance().sendDefault(mContext, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
//            @Override
//            public void onFailure(ErrorResult errorResult) {
//                Logger.e(errorResult.toString());
//            }
//
//            @Override
//            public void onSuccess(KakaoLinkResponse result) {
//            }
//        });
//    }
//}