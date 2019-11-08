package com.bc.wechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.bc.wechat.R;
import com.bc.wechat.cons.Constant;
import com.bc.wechat.entity.Friend;
import com.bc.wechat.entity.User;
import com.bc.wechat.utils.CommonUtil;
import com.bc.wechat.utils.PreferencesUtil;
import com.bc.wechat.utils.VolleyUtil;
import com.bc.wechat.widget.LoadingDialog;

import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class LoginActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    public static int sequence = 1;

    private VolleyUtil volleyUtil;

    EditText mPhoneEt;
    EditText mPasswordEt;
    Button mLoginBtn;
    TextView mRegisterTv;
    LoadingDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PreferencesUtil.getInstance().init(this);
        volleyUtil = VolleyUtil.getInstance(this);
        dialog = new LoadingDialog(LoginActivity.this);
        initView();
    }

    private void initView() {
        mPhoneEt = findViewById(R.id.et_user_phone);
        mPasswordEt = findViewById(R.id.et_password);
        mLoginBtn = findViewById(R.id.btn_login);
        mRegisterTv = findViewById(R.id.tv_register);

        mPhoneEt.addTextChangedListener(new TextChange());
        mPasswordEt.addTextChangedListener(new TextChange());
        mLoginBtn.setOnClickListener(this);
        mRegisterTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                dialog.setMessage(getString(R.string.logging_in));
                dialog.show();
                final String phone = mPhoneEt.getText().toString().trim();
                final String password = mPasswordEt.getText().toString().trim();
                login(phone, password);
                break;
            case R.id.tv_register:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
        }
    }

    class TextChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean phoneEtHasText = mPhoneEt.getText().length() > 0;
            boolean passwordEtHasText = mPasswordEt.getText().length() > 0;
            if (phoneEtHasText && passwordEtHasText) {
                mLoginBtn.setTextColor(0xFFFFFFFF);
                mLoginBtn.setEnabled(true);
            } else {
                mLoginBtn.setTextColor(0xFFD0EFC6);
                mLoginBtn.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private void login(String phone, String password) {
        String url = Constant.BASE_URL + "users/login?phone=" + phone + "&password=" + password;
        volleyUtil.httpGetRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "server response: " + response);
                User user = JSON.parseObject(response, User.class);
                Log.d(TAG, "userId:" + user.getUserId());
                // 登录成功后设置user和isLogin至sharedpreferences中
                PreferencesUtil.getInstance().setUser(user);
                PreferencesUtil.getInstance().setLogin(true);
                // 注册jpush
                JPushInterface.setAlias(LoginActivity.this, sequence, user.getUserId());
                // 注册jim
                JMessageClient.login(user.getUserId(), "123456", new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                    }
                });

                List<User> friendList = user.getFriendList();
                for (User userFriend : friendList) {
                    if (null != userFriend) {
                        List<Friend> checkList = Friend.find(Friend.class, "user_id = ?", userFriend.getUserId());
                        if (null != checkList && checkList.size() > 0) {
                            // 好友已存在，更新基本信息
                            Friend friend = checkList.get(0);
                            friend.setUserNickName(userFriend.getUserNickName());
                            friend.setUserWxId(userFriend.getUserWxId());
                            friend.setUserAvatar(userFriend.getUserAvatar());
                            friend.setUserHeader(CommonUtil.setUserHeader(userFriend.getUserNickName()));
                            friend.setUserSex(userFriend.getUserSex());
                            friend.setUserLastestCirclePhotos(userFriend.getUserLastestCirclePhotos());
                            Friend.save(friend);
                        } else {
                            // 不存在,插入sqlite
                            Friend friend = new Friend();
                            friend.setUserId(userFriend.getUserId());
                            friend.setUserNickName(userFriend.getUserNickName());
                            friend.setUserWxId(userFriend.getUserWxId());
                            friend.setUserAvatar(userFriend.getUserAvatar());
                            friend.setUserHeader(CommonUtil.setUserHeader(userFriend.getUserNickName()));
                            friend.setUserSex(userFriend.getUserSex());
                            friend.setUserLastestCirclePhotos(userFriend.getUserLastestCirclePhotos());
                            Friend.save(friend);
                        }
                    }
                }

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.dismiss();

                if (volleyError instanceof NetworkError) {
                    Toast.makeText(LoginActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                } else if (volleyError instanceof TimeoutError) {
                    Toast.makeText(LoginActivity.this, R.string.network_time_out, Toast.LENGTH_SHORT).show();
                    return;
                }

                int errorCode = volleyError.networkResponse.statusCode;
                switch (errorCode) {
                    case 400:
                        Toast.makeText(LoginActivity.this,
                                R.string.username_or_password_error, Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
            }
        });
    }
}
