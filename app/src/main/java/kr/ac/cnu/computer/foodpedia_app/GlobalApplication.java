package kr.ac.cnu.computer.foodpedia_app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import java.util.Map;

public class GlobalApplication extends Application {
    private static GlobalApplication instance;

    // 카카오 로그인
    public String KakaoID;
    public String KakaoName;
    public String KakaoProfile;

    // 기본 로그인
    public String BasicEmail;
    public String BasicName;
    public int steps;

    // Firebase
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getKakaoID() {
        return KakaoID;
    }

    public String setKakaoID(String KakaoID) {
        this.KakaoID = KakaoID;
        return KakaoID;
    }

    public String getKakaoName() {
        return KakaoName;
    }

    public String setKakaoName(String KakaoName) {
        this.KakaoName = KakaoName;
        return KakaoName;
    }

    public String getKakaoProfile() {
        return KakaoProfile;
    }

    public String setKakaoProfile(String KakaoProfile) {
        this.KakaoProfile = KakaoProfile;
        return KakaoProfile;
    }

    public String getBasicEmail() {
        return BasicEmail;
    }

    public String setBasicEmail(String BasicEmail) {
        this.BasicEmail = BasicEmail;
        return BasicEmail;
    }

    public String getBasicName() {
        return BasicName;
    }

    public String setBasicName(String BasicName) {
        this.BasicName = BasicName;
        return BasicName;
    }

    public static GlobalApplication getGlobalApplicationContext() {
        if (instance == null) {
            throw new IllegalStateException("This Application does not inherit com.kakao.GlobalApplication");
        }

        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Kakao Sdk 초기화
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    public static class KakaoSDKAdapter extends KakaoAdapter {

        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        // Application이 가지고 있는 정보를 얻기 위한 인터페이스
        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return GlobalApplication.getGlobalApplicationContext();
                }
            };
        }
    }
}
